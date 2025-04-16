package com.example.exp2;

public class Club {
    private String date;
    private String letter_path;
    private String letter_url;
    private String name;
    private String reason;
    private String roll_no;
    private String status;
    private String subject;

    public Club(String date, String letter_path, String letter_url, String name, String reason, String roll_no, String status, String subject) {
        this.date = date;
        this.letter_path = letter_path;
        this.letter_url = letter_url;
        this.name = name;
        this.reason = reason;
        this.roll_no = roll_no;
        this.status = status;
        this.subject = subject;
    }

    public String getDate() { return date; }
    public String getLetterPath() { return letter_path; }
    public String getLetterUrl() { return letter_url; }
    public String getName() { return name; }
    public String getReason() { return reason; }
    public String getRollNo() { return roll_no; }
    public String getStatus() { return status; }
    public String getSubject() { return subject; }
}