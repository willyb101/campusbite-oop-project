package com.zut.campusbite;

public class Student extends User {
    private String hostelName;
    private String roomNumber;

    public Student(int userId, String name, String phone, String email, String hostelName, String roomNumber) {
        super(userId, name, phone, email);
        this.hostelName = requireText(hostelName, "hostelName");
        this.roomNumber = requireText(roomNumber, "roomNumber");
    }

    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = requireText(hostelName, "hostelName"); }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = requireText(roomNumber, "roomNumber"); }
}
