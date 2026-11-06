package com.zut.campusbite;

import java.sql.*;
import java.util.*;

public class SqliteCampusBiteRepository implements CampusBiteRepository {
    private final String url;

    public SqliteCampusBiteRepository(String databasePath) {
        this.url = "jdbc:sqlite:" + databasePath;
        initialise();
    }

    private Connection connect() throws SQLException { return DriverManager.getConnection(url); }

    private void initialise() {
        String[] sql = {
            "CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT NOT NULL, email TEXT NOT NULL, hostel TEXT NOT NULL, room TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS food_items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, price REAL NOT NULL CHECK(price >= 0), available INTEGER NOT NULL DEFAULT 1, image_data TEXT)",
            "CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, total REAL NOT NULL, location TEXT NOT NULL, notes TEXT, status TEXT NOT NULL, created_at TEXT NOT NULL, FOREIGN KEY(student_id) REFERENCES students(id))",
            "CREATE TABLE IF NOT EXISTS order_lines (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, food_item_id INTEGER, item_name TEXT NOT NULL, quantity INTEGER NOT NULL CHECK(quantity > 0), unit_price REAL NOT NULL, FOREIGN KEY(order_id) REFERENCES orders(id))"
        };
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            for (String statement : sql) s.execute(statement);
            addColumnIfMissing(s, "orders", "preparing_at", "TEXT");
            addColumnIfMissing(s, "orders", "in_transit_at", "TEXT");
            addColumnIfMissing(s, "orders", "delivered_at", "TEXT");
            migrateOrderLinesForFoodDeletion(c, s);
            // Upgrade existing databases created before food images were supported.
            boolean hasImageColumn = false;
            try (ResultSet columns = s.executeQuery("PRAGMA table_info(food_items)")) {
                while (columns.next()) if ("image_data".equalsIgnoreCase(columns.getString("name"))) hasImageColumn = true;
            }
            if (!hasImageColumn) s.execute("ALTER TABLE food_items ADD COLUMN image_data TEXT");
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM food_items")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO food_items(name,price,available) VALUES(?,?,1)")) {
                        String[][] seed = {{"Nshima with chicken","55"},{"Rice and beef","60"},{"Beans with nshima","35"},{"Chips and sausage","40"}};
                        for (String[] row : seed) { p.setString(1,row[0]); p.setDouble(2,Double.parseDouble(row[1])); p.addBatch(); }
                        p.executeBatch();
                    }
                }
            }
        } catch (SQLException e) { throw new IllegalStateException("Could not initialise SQLite database", e); }
    }

    private void addColumnIfMissing(Statement statement, String table, String column, String type) throws SQLException {
        boolean found = false;
        try (ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) {
                if (column.equalsIgnoreCase(columns.getString("name"))) found = true;
            }
        }
        if (!found) statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    private void migrateOrderLinesForFoodDeletion(Connection c, Statement s) throws SQLException {
        boolean foodItemIdRequired = false;
        try (ResultSet columns = s.executeQuery("PRAGMA table_info(order_lines)")) {
            while (columns.next()) {
                if ("food_item_id".equalsIgnoreCase(columns.getString("name"))) {
                    foodItemIdRequired = columns.getInt("notnull") == 1;
                }
            }
        }
        if (!foodItemIdRequired) return;

        c.setAutoCommit(false);
        try {
            s.execute("CREATE TABLE order_lines_new (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, food_item_id INTEGER, item_name TEXT NOT NULL, quantity INTEGER NOT NULL CHECK(quantity > 0), unit_price REAL NOT NULL, FOREIGN KEY(order_id) REFERENCES orders(id))");
            s.execute("INSERT INTO order_lines_new(id,order_id,food_item_id,item_name,quantity,unit_price) SELECT id,order_id,food_item_id,item_name,quantity,unit_price FROM order_lines");
            s.execute("DROP TABLE order_lines");
            s.execute("ALTER TABLE order_lines_new RENAME TO order_lines");
            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        }
    }

    @Override public List<Student> findStudents() {
        List<Student> out = new ArrayList<>();
        try (Connection c=connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT * FROM students ORDER BY id DESC")) {
            while(r.next()) out.add(student(r));
            return out;
        } catch(SQLException e){ throw new IllegalStateException(e); }
    }
    @Override public Optional<Student> findStudent(int id) {
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("SELECT * FROM students WHERE id=?")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()){ return r.next()?Optional.of(student(r)):Optional.empty(); }
        } catch(SQLException e){ throw new IllegalStateException(e); }
    }
    private Student student(ResultSet r) throws SQLException {
        return new Student(r.getInt("id"),r.getString("name"),r.getString("phone"),r.getString("email"),r.getString("hostel"),r.getString("room"));
    }
    @Override public Student saveStudent(Student s) {
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("INSERT INTO students(name,phone,email,hostel,room) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1,s.getName());p.setString(2,s.getPhone());p.setString(3,s.getEmail());p.setString(4,s.getHostelName());p.setString(5,s.getRoomNumber());p.executeUpdate();
            try(ResultSet k=p.getGeneratedKeys()){ if(k.next()) s.setUserId(k.getInt(1)); } return s;
        } catch(SQLException e){ throw new IllegalStateException(e); }
    }
    @Override public List<FoodItem> findFoodItems() {
        List<FoodItem> out=new ArrayList<>();
        try(Connection c=connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM food_items ORDER BY id DESC")){while(r.next())out.add(food(r));return out;}
        catch(SQLException e){throw new IllegalStateException(e);}
    }
    @Override public Optional<FoodItem> findFoodItem(int id) {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM food_items WHERE id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(food(r)):Optional.empty();}}
        catch(SQLException e){throw new IllegalStateException(e);}
    }
    private FoodItem food(ResultSet r)throws SQLException{return new FoodItem(r.getInt("id"),r.getString("name"),r.getDouble("price"),r.getInt("available")==1,r.getString("image_data"));}
    @Override public FoodItem saveFoodItem(FoodItem f) {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO food_items(name,price,available,image_data) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,f.getItemName());p.setDouble(2,f.getPrice());p.setInt(3,f.isAvailable()?1:0);p.setString(4,f.getImageData());p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){if(k.next())f.setItemId(k.getInt(1));}return f;
        }catch(SQLException e){throw new IllegalStateException(e);}
    }
    @Override public boolean deleteFoodItem(int id) {
        try(Connection c=connect()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("UPDATE order_lines SET food_item_id=NULL WHERE food_item_id=?")) {
                    p.setInt(1,id);
                    p.executeUpdate();
                }
                int deleted;
                try(PreparedStatement p=c.prepareStatement("DELETE FROM food_items WHERE id=?")) {
                    p.setInt(1,id);
                    deleted=p.executeUpdate();
                }
                if(deleted==0) {
                    c.rollback();
                    return false;
                }
                c.commit();
                return true;
            } catch(SQLException e) {
                c.rollback();
                throw e;
            }
        } catch(SQLException e) {
            throw new IllegalStateException("Could not delete food item",e);
        }
    }
    @Override public void updateAvailability(int id,boolean available) {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("UPDATE food_items SET available=? WHERE id=?")){p.setInt(1,available?1:0);p.setInt(2,id);if(p.executeUpdate()==0)throw new InvalidOrderException("Food item not found");}
        catch(SQLException e){throw new IllegalStateException(e);}
    }
    @Override public Order saveOrder(Order o) {
        String orderSql="INSERT INTO orders(student_id,total,location,notes,status,created_at) VALUES(?,?,?,?,?,?)";
        try(Connection c=connect()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(orderSql,Statement.RETURN_GENERATED_KEYS)){
                p.setInt(1,o.getStudentId());p.setDouble(2,o.getTotalCost());p.setString(3,o.getDeliveryLocation());p.setString(4,o.getNotes());p.setString(5,o.getStatus().name());p.setString(6,o.getCreatedAt());p.executeUpdate();
                try(ResultSet k=p.getGeneratedKeys()){if(k.next())o.setOrderId(k.getInt(1));}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO order_lines(order_id,food_item_id,item_name,quantity,unit_price) VALUES(?,?,?,?,?)")){
                for(OrderLine line:o.getItems()){p.setInt(1,o.getOrderId());p.setInt(2,line.getFoodItemId());p.setString(3,line.getItemName());p.setInt(4,line.getQuantity());p.setDouble(5,line.getUnitPrice());p.addBatch();}p.executeBatch();
            }
            c.commit();return o;
        }catch(SQLException e){throw new IllegalStateException(e);}
    }
    @Override public List<Order> findOrders() {
        List<Order> out=new ArrayList<>();
        try(Connection c=connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT o.*,s.name AS student_name FROM orders o JOIN students s ON s.id=o.student_id ORDER BY o.id DESC")){
            while(r.next())out.add(loadOrder(r));return out;
        }catch(SQLException e){throw new IllegalStateException(e);}
    }
    @Override public Optional<Order> findOrder(int id) {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT o.*,s.name AS student_name FROM orders o JOIN students s ON s.id=o.student_id WHERE o.id=?")){
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(loadOrder(r)):Optional.empty();}
        }catch(SQLException e){throw new IllegalStateException(e);}
    }
    @Override public boolean deleteDeliveredOrder(int id) {
        try(Connection c=connect()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("DELETE FROM order_lines WHERE order_id=? AND EXISTS (SELECT 1 FROM orders WHERE id=? AND status='DELIVERED')")) {
                    p.setInt(1,id);
                    p.setInt(2,id);
                    p.executeUpdate();
                }
                int deleted;
                try(PreparedStatement p=c.prepareStatement("DELETE FROM orders WHERE id=? AND status='DELIVERED'")) {
                    p.setInt(1,id);
                    deleted=p.executeUpdate();
                }
                if(deleted==0) {
                    c.rollback();
                    return false;
                }
                c.commit();
                return true;
            } catch(SQLException e) {
                c.rollback();
                throw e;
            }
        } catch(SQLException e) {
            throw new IllegalStateException("Could not delete delivered order",e);
        }
    }
    private Order loadOrder(ResultSet r)throws SQLException {
        int id=r.getInt("id");List<OrderLine> lines=new ArrayList<>();
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM order_lines WHERE order_id=?")){p.setInt(1,id);try(ResultSet l=p.executeQuery()){while(l.next())lines.add(new OrderLine(l.getInt("food_item_id"),l.getString("item_name"),l.getInt("quantity"),l.getDouble("unit_price")));}}
        return new Order(id,r.getInt("student_id"),r.getString("student_name"),lines,r.getDouble("total"),r.getString("location"),r.getString("notes"),OrderStatus.valueOf(r.getString("status")),r.getString("created_at"),r.getString("preparing_at"),r.getString("in_transit_at"),r.getString("delivered_at"));
    }
    @Override public void updateOrderStatus(int id,OrderStatus status) {
        try(Connection c=connect()) {
            c.setAutoCommit(false);
            try {
                String createdAt;
                String preparingAt;
                String inTransitAt;
                try(PreparedStatement p=c.prepareStatement("SELECT created_at,preparing_at,in_transit_at FROM orders WHERE id=?")) {
                    p.setInt(1,id);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next()) throw new InvalidOrderException("Order not found");
                        createdAt=r.getString("created_at");
                        preparingAt=r.getString("preparing_at");
                        inTransitAt=r.getString("in_transit_at");
                    }
                }

                String now=java.time.Instant.now().toString();
                if(status==OrderStatus.PENDING) {
                    preparingAt=null;
                    inTransitAt=null;
                } else if(status==OrderStatus.PREPARING) {
                    if(preparingAt==null) preparingAt=now;
                    inTransitAt=null;
                } else {
                    if(preparingAt==null) preparingAt=createdAt;
                    if(inTransitAt==null) inTransitAt=now;
                }
                String deliveredAt=status==OrderStatus.DELIVERED?now:null;
                try(PreparedStatement p=c.prepareStatement("UPDATE orders SET status=?,preparing_at=?,in_transit_at=?,delivered_at=? WHERE id=?")) {
                    p.setString(1,status.name());
                    p.setString(2,preparingAt);
                    p.setString(3,inTransitAt);
                    p.setString(4,deliveredAt);
                    p.setInt(5,id);
                    p.executeUpdate();
                }
                c.commit();
            } catch(SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch(SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
