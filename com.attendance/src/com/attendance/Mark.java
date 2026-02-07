package com.attendance;

/**
 * Represents marks for a student in a specific subject or test.
 */
public class Mark {
    private final int studentRoll;
    private final String subject;
    private final double marks;

    public Mark(int studentRoll, String subject, double marks) {
        this.studentRoll = studentRoll;
        this.subject = subject;
        this.marks = marks;
    }

    public int getStudentRoll() {
        return studentRoll;
    }

    public String getSubject() {
        return subject;
    }

    public double getMarks() {
        return marks;
    }
}
