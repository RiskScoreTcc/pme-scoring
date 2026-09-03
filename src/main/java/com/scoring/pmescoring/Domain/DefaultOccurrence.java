package com.scoring.pmescoring.Domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "default_occurrence")
public class DefaultOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "firm_id", referencedColumnName = "id", nullable = false)
    private Firm firm;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate dateOccurrence;

    @Column(name = "amount_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "status_resolved", nullable = false)
    private Boolean statusResolved = false;

    @Column( name = "description")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "creation_date", updatable = false)
    private LocalDate creationDate;

    public DefaultOccurrence(){}

    public DefaultOccurrence(Firm firm, LocalDate dateOccurrence, BigDecimal amountDue, String description) {
        this.firm = firm;
        this.dateOccurrence = dateOccurrence;
        this.amountDue = amountDue;
        this.statusResolved = false;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Firm getFirm() {
        return firm;
    }

    public void setFirm(Firm firm) {
        this.firm = firm;
    }

    public LocalDate getDateOccurrence() {
        return dateOccurrence;
    }

    public void setDateOccurrence(LocalDate dateOccurrence) {
        this.dateOccurrence = dateOccurrence;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public Boolean getStatusResolved() {
        return statusResolved;
    }

    public void setStatusResolved(Boolean statusResolved) {
        this.statusResolved = statusResolved;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultOccurrence that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
