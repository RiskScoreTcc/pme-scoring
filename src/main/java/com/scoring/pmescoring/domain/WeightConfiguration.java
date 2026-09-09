package com.scoring.pmescoring.domain;

import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.FormulaType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "weight_configuration")
public class WeightConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "formula_type", nullable = false)
    private FormulaType formulaType;

    @Column(name = "revenue_weight", nullable = false, precision = 4, scale = 2)
    private BigDecimal revenueWeight;

    @Column(name = "time_weight", nullable = false, precision = 4, scale = 2)
    private BigDecimal timeWeight;

    @Column(name = "default_weight", nullable = false, precision = 4, scale = 2)
    private BigDecimal defaultWeight;

    @Column(name = "max_revenue_reference", nullable = false)
    private BigDecimal maxRevenueReference;

    @Column(name = "max_time_reference_months", nullable = false)
    private Integer maxTimeReferenceMonths;

    @Column(name = "low_risk_threshold", nullable = false)
    private Integer lowRiskThreshold;

    @Column(name = "medium_risk_threshold", nullable = false)
    private Integer mediumRiskThreshold;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by_user_id", referencedColumnName = "id", nullable = false)
    private User updatedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status" ,nullable = false)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "creation_date", updatable = false)
    private LocalDate creationDate;

    public WeightConfiguration() {
    }

    public WeightConfiguration(FormulaType formulaType, BigDecimal revenueWeight, BigDecimal timeWeight, BigDecimal defaultWeight, BigDecimal maxRevenueReference, Integer maxTimeReferenceMonths, Integer lowRiskThreshold, Integer mediumRiskThreshold, User user) {
        this.formulaType = formulaType;
        this.revenueWeight = revenueWeight;
        this.timeWeight = timeWeight;
        this.defaultWeight = defaultWeight;
        this.maxRevenueReference = maxRevenueReference;
        this.maxTimeReferenceMonths = maxTimeReferenceMonths;
        this.lowRiskThreshold = lowRiskThreshold;
        this.mediumRiskThreshold = mediumRiskThreshold;
        this.status = EntityStatus.ACTIVE;
        this.updatedByUser = user;
    }

    public WeightConfiguration(FormulaType formulaType, BigDecimal revenueWeight, BigDecimal timeWeight, BigDecimal defaultWeight, BigDecimal maxRevenueReference, Integer maxTimeReferenceMonths, Integer lowRiskThreshold, Integer mediumRiskThreshold) {
        this.formulaType = formulaType;
        this.revenueWeight = revenueWeight;
        this.timeWeight = timeWeight;
        this.defaultWeight = defaultWeight;
        this.maxRevenueReference = maxRevenueReference;
        this.maxTimeReferenceMonths = maxTimeReferenceMonths;
        this.lowRiskThreshold = lowRiskThreshold;
        this.mediumRiskThreshold = mediumRiskThreshold;
        this.status = EntityStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public FormulaType getFormulaType() {
        return formulaType;
    }

    public void setFormulaType(FormulaType formulaType) {
        this.formulaType = formulaType;
    }

    public BigDecimal getRevenueWeight() {
        return revenueWeight;
    }

    public void setRevenueWeight(BigDecimal revenueWeight) {
        this.revenueWeight = revenueWeight;
    }

    public BigDecimal getTimeWeight() {
        return timeWeight;
    }

    public void setTimeWeight(BigDecimal timeWeight) {
        this.timeWeight = timeWeight;
    }

    public BigDecimal getDefaultWeight() {
        return defaultWeight;
    }

    public void setDefaultWeight(BigDecimal defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    public BigDecimal getMaxRevenueReference() {
        return maxRevenueReference;
    }

    public void setMaxRevenueReference(BigDecimal maxRevenueReference) {
        this.maxRevenueReference = maxRevenueReference;
    }

    public Integer getMaxTimeReferenceMonths() {
        return maxTimeReferenceMonths;
    }

    public void setMaxTimeReferenceMonths(Integer maxTimeReferenceMonths) {
        this.maxTimeReferenceMonths = maxTimeReferenceMonths;
    }

    public Integer getLowRiskThreshold() {
        return lowRiskThreshold;
    }

    public void setLowRiskThreshold(Integer lowRiskThreshold) {
        this.lowRiskThreshold = lowRiskThreshold;
    }

    public Integer getMediumRiskThreshold() {
        return mediumRiskThreshold;
    }

    public void setMediumRiskThreshold(Integer mediumRiskThreshold) {
        this.mediumRiskThreshold = mediumRiskThreshold;
    }

    public User getUpdatedByUser() {
        return updatedByUser;
    }

    public void setUpdatedByUser(User updatedByUser) {
        this.updatedByUser = updatedByUser;
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
        if (!(o instanceof WeightConfiguration that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void delete() {
        this.status = EntityStatus.DELETED;
    }
}