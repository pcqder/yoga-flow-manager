package com.yoga;

import java.io.Serializable;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String email;
    private String phone;
    private String membershipType; // Monthly, Drop-in, Unlimited
    private int classesAttendedThisMonth;
    private double monthlyFee;
    private boolean paidThisMonth;

    public Student(String name, String email, String phone, String membershipType, double monthlyFee) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.membershipType = membershipType;
        this.monthlyFee = monthlyFee;
        this.classesAttendedThisMonth = 0;
        this.paidThisMonth = false;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getMembershipType() { return membershipType; }
    public int getClassesAttendedThisMonth() { return classesAttendedThisMonth; }
    public double getMonthlyFee() { return monthlyFee; }
    public boolean isPaidThisMonth() { return paidThisMonth; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
    public void setMonthlyFee(double monthlyFee) { this.monthlyFee = monthlyFee; }
    public void setPaidThisMonth(boolean paid) { this.paidThisMonth = paid; }

    public void incrementAttendance() { this.classesAttendedThisMonth++; }
    public void resetMonthly() {
        this.classesAttendedThisMonth = 0;
        this.paidThisMonth = false;
    }

    @Override
    public String toString() {
        return String.format("%-20s | %-25s | %-15s | %-12s | Attended: %2d | Fee: $%.2f | %s",
                name, email, phone, membershipType, classesAttendedThisMonth, monthlyFee,
                paidThisMonth ? "PAID" : "UNPAID");
    }
}
