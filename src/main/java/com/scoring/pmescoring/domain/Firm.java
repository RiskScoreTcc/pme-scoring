package com.scoring.pmescoring.domain;

import com.scoring.pmescoring.model.EntityStatus;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "registered_company_name", nullable = false)
    private String registeredCompanyName;

    @Column(name = "average_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal averageRevenue;

    @Column(name = "time_months", nullable = false)
    private Integer timeMonths;

    @Column(name = "number_of_employees", nullable = false)
    private Integer numberOfEmployees;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "creation_date", updatable = false)
    private LocalDate creationDate;

    public Firm() {
    }

    public Firm(User user, String cnpj, String registeredCompanyName, BigDecimal averageRevenue, int timeMonths, int numberOfEmployees) {
        this.user = user;
        this.cnpj = cnpj;
        this.registeredCompanyName = registeredCompanyName;
        this.averageRevenue = averageRevenue;
        this.timeMonths = timeMonths;
        this.numberOfEmployees = numberOfEmployees;
        this.status = EntityStatus.ACTIVE;
    }

    public Firm(String cnpj, String registeredCompanyName, BigDecimal averageRevenue, int timeMonths, int numberOfEmployees) {
        this.cnpj = cnpj;
        this.registeredCompanyName = registeredCompanyName;
        this.averageRevenue = averageRevenue;
        this.timeMonths = timeMonths;
        this.numberOfEmployees = numberOfEmployees;
        this.status = EntityStatus.ACTIVE;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public Integer getTimeMonths() {
        return timeMonths;
    }

    public void setTimeMonths(Integer timeMonths) {
        this.timeMonths = timeMonths;
    }

    public Integer getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(Integer numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void delete() {
        this.status = EntityStatus.DELETED;
    }

    public void update(String cnpj, Integer timeMonths, BigDecimal averageRevenue, Integer numberOfEmployees, String registeredCompanyName) {
        if (cnpj != null && !cnpj.isBlank()) {
            this.cnpj = cnpj;
        }
        if (timeMonths != null) {
            this.timeMonths = timeMonths;
        }
        if (averageRevenue != null) {
            this.averageRevenue = averageRevenue;
        }
        if (numberOfEmployees != null) {
            this.numberOfEmployees = numberOfEmployees;
        }
        if (registeredCompanyName != null && !registeredCompanyName.isBlank()) {
            this.registeredCompanyName = registeredCompanyName;
        }
    }
}
