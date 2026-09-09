package com.scoring.pmescoring.domain;

import com.scoring.pmescoring.model.EntityStatus;
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

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "creation_date", updatable = false)
    private LocalDate creationDate;

    public DefaultOccurrence() {
    }

    public DefaultOccurrence(LocalDate dateOccurrence, BigDecimal amountDue, String description) {
        this.dateOccurrence = dateOccurrence;
        this.amountDue = amountDue;
        this.statusResolved = false;
        this.description = description;
        this.status = EntityStatus.ACTIVE;
    }

    public DefaultOccurrence(Firm firm, LocalDate dateOccurrence, BigDecimal amountDue, String description) {
        this.firm = firm;
        this.dateOccurrence = dateOccurrence;
        this.amountDue = amountDue;
        this.statusResolved = false;
        this.description = description;
        this.status = EntityStatus.ACTIVE;
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

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
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

    public void update(BigDecimal amountDue, LocalDate dateOccurrence, String description) {
        if (amountDue != null) {
            this.amountDue = amountDue;
        }
        if (dateOccurrence != null) {
            this.dateOccurrence = dateOccurrence;
        }
        this.description = description;
    }

    public void delete() {
        this.status = EntityStatus.DELETED;
    }
}
