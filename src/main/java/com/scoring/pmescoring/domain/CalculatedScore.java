package com.scoring.pmescoring.domain;

import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.model.ScoreFactorsDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "calculated_score")
public class CalculatedScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "firm_id", referencedColumnName = "id", nullable = false)
    private Firm firm;

    @Column(name = "score_value", nullable = false)
    private Integer scoreValue;

    @Column(name = "risk_band", nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskBand riskBand;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "factors_json", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private ScoreFactorsDTO factorsJson;

    @Column(name = "calculation_date", updatable = false)
    private LocalDate calculationDate;

    public CalculatedScore(){}

    public CalculatedScore(Integer scoreValue, RiskBand riskBand, ScoreFactorsDTO factorsJson) {
        this.scoreValue = scoreValue;
        this.riskBand = riskBand;
        this.active = true;
        this.factorsJson = factorsJson;
    }


    public CalculatedScore(Firm firm, Integer scoreValue, RiskBand riskBand, ScoreFactorsDTO factorsJson) {
        this.firm = firm;
        this.scoreValue = scoreValue;
        this.riskBand = riskBand;
        this.active = true;
        this.factorsJson = factorsJson;
    }

    @PrePersist
    protected void onCreate() {
        this.calculationDate = LocalDate.now();
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

    public Integer getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(Integer scoreValue) {
        this.scoreValue = scoreValue;
    }

    public RiskBand getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(RiskBand riskBand) {
        this.riskBand = riskBand;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ScoreFactorsDTO getFactorsJson() {
        return factorsJson;
    }

    public void setFactorsJson(ScoreFactorsDTO factorsJson) {
        this.factorsJson = factorsJson;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CalculatedScore that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void delete() {
        this.active = false;
    }
}
