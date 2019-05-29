package csl.database.model;

import java.sql.Date;

public class LogActivity {

    private Long id;
    private String name;
    private Double calories;
    private Date day;

    public LogActivity() {
    }

    public LogActivity(Long id, String name, Double calories, Date day) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.day = day;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }
}
