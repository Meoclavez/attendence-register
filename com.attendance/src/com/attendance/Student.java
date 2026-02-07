package com.attendance;

/**
 * Represents a student with a roll number and name.
 */
public class Student {
    private final int roll;
    private final String name;

    public Student(int roll, String name) {
        this.roll = roll;
        this.name = name;
    }

    public int getRoll() {
        return roll;
    }

    public String getName() {
        return name;
    }
}
