package csl.database.model;

import java.sql.Date;

public class Weight {

    private Long id;
    private Double weight;
    private Date day;

    public Weight() {
    }

    public Weight(Long id, Double weight, Date day) {
        this.id = id;
        this.day = day;
        this.weight = weight;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }
}
