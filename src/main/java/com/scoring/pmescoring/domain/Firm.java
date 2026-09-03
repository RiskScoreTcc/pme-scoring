package com.scoring.pmescoring.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "companies")
public class Firm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @Column(name = "cnpj", nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "registered_company_name", nullable = false)
    private String registeredCompanyName;

    @Column(name = "average_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal averageRevenue;

    @Column(name = "time_months", nullable = false )
    private Integer timeMonths;

    @Column(name = "number_of_employees", nullable = false)
    private Integer numberOfEmployees;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "creation_date", updatable = false)
    private LocalDate creationDate;

    public Firm(){}

    public Firm(User user, String cnpj, String registeredCompanyName, BigDecimal averageRevenue, int timeMonths, int numberOfEmployees) {
        this.user = user;
        this.cnpj = cnpj;
        this.registeredCompanyName = registeredCompanyName;
        this.averageRevenue = averageRevenue;
        this.timeMonths = timeMonths;
        this.numberOfEmployees = numberOfEmployees;
    }

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Firm firm)) return false;
        return Objects.equals(id, firm.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public void setUserID(User user) {
        this.user = user;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRegisteredCompanyName() {
        return registeredCompanyName;
    }

    public void setRegisteredCompanyName(String registeredCompanyName) {
        this.registeredCompanyName = registeredCompanyName;
    }

    public BigDecimal getAverageRevenue() {
        return averageRevenue;
    }

    public void setAverageRevenue(BigDecimal averageRevenue) {
        this.averageRevenue = averageRevenue;
    }

    public int getTimeMonths() {
        return timeMonths;
    }

    public void setTimeMonths(int timeMonths) {
        this.timeMonths = timeMonths;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public Boolean getActive(){ return this.active; }

    public void setActive(Boolean active){ this.active = active; }

    public LocalDate getCreationDate() {
        return creationDate;
    }
}
