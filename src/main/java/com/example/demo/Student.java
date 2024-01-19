package com.example.demo;

public class Student {
    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private String gender;
    private double gpa;
    private int level;
    public void setId(String id) {
        this.id = id;
    }

    public void setfirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getAddress() {
        return address;
    }
    public String getGender() {
        return gender;
    }
    public double getGpa() {
        return gpa;
    }
    public int getLevel() {
        return level;
    }



    public String display() {
        return "Student{" +
                "id='" + id + '\'' +
                ", firstname='" + firstName + '\'' +
                ", lastname = '"+lastName+'\''+
                ", address = '"+address+'\''+
                ", gender = '"+gender+'\''+
                ", gpa = '"+gpa+'\''+
                ", level = '"+level+'\''+
                '}';
    }
    // Add other fields as needed

    // Generate getters and setters
}
