package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.domain.*;
import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.request.calculatedscore.UpdateCalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import com.scoring.pmescoring.mapper.CalculatedScoreMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.model.ScoreFactorsDTO;
import com.scoring.pmescoring.repository.*;
import com.scoring.pmescoring.service.CalculatedScoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class CalculatedScoreServiceImpl implements CalculatedScoreService {

    private final CalculatedScoreRepository calculatedScoreRepository;
    private final CalculatedScoreMapper calculatedScoreMapper;
    private final FirmRepository firmRepository;
    private final UserRepository userRepository;
    private final WeightConfigurationRepository weightConfigurationRepository;
    private final DefaultOccurrenceRepository defaultOccurrenceRepository;

    public CalculatedScoreServiceImpl(CalculatedScoreRepository calculatedScoreRepository,
                                      CalculatedScoreMapper calculatedScoreMapper,
                                      FirmRepository firmRepository,
                                      UserRepository userRepository,
                                      WeightConfigurationRepository weightConfigurationRepository,
                                      DefaultOccurrenceRepository defaultOccurrenceRepository) {
        this.calculatedScoreRepository = calculatedScoreRepository;
        this.calculatedScoreMapper = calculatedScoreMapper;
        this.firmRepository = firmRepository;
        this.userRepository = userRepository;
        this.weightConfigurationRepository = weightConfigurationRepository;
        this.defaultOccurrenceRepository = defaultOccurrenceRepository;
    }

    @Override
    @Transactional
    public CalculatedScoreResponse create(CalculatedScoreRequest calculatedScoreRequest) {
        calculatedScoreRepository.findByFirmIdAndStatus(calculatedScoreRequest.firmId(), EntityStatus.ACTIVE)
                .forEach(this::deactivatePreviousCalculation);

        CalculatedScore calculatedScore = setCalculation(calculatedScoreRequest.firmId(), calculatedScoreRequest.userId());
        calculatedScoreRepository.save(calculatedScore);
        return calculatedScoreMapper.toResponse(calculatedScore);
    }

    private CalculatedScore setCalculation(Long firmId, Long userId) {
        Firm firm = companySearch(firmId);
        User user = userSearch(userId);
        WeightConfiguration weightConfiguration = findLatestActiveWeightConfiguration();
        List<DefaultOccurrence> activeDefaults = findActiveDefaults(firmId);

        BigDecimal scoreRev = calculateRevenueScore(firm, weightConfiguration);
        BigDecimal scoreTime = calculateTimeScore(firm, weightConfiguration);
        BigDecimal scoreDefault = calculateDefaultScore(activeDefaults);
        int calculatedScoreValue = calculateFinalScore(scoreRev, scoreTime, scoreDefault, weightConfiguration);

        RiskBand riskBand = classifyRiskBand(calculatedScoreValue, weightConfiguration);
        boolean inDefault = !activeDefaults.isEmpty();
        BigDecimal amountDue = activeDefaults.stream()
                .map(DefaultOccurrence::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ScoreFactorsDTO scoreFactorsDTO = new ScoreFactorsDTO(
                LocalDate.now(),
                firm.getAverageRevenue(),
                firm.getTimeMonths(),
                activeDefaults.size(),
                weightConfiguration.getRevenueWeight(),
                weightConfiguration.getTimeWeight(),
                weightConfiguration.getDefaultWeight(),
                inDefault,
                amountDue,
                weightConfiguration.getMaxRevenueReference(),
                weightConfiguration.getMaxTimeReferenceMonths()
        );

        String justification =  generateJustification(scoreFactorsDTO, riskBand,  calculatedScoreValue);

        return new CalculatedScore(
                firm,
                user,
                calculatedScoreValue,
                riskBand,
                justification,
                scoreFactorsDTO
        );
    }

    private BigDecimal calculateRevenueScore(Firm firm, WeightConfiguration weightConfiguration) {
        BigDecimal maxRevenueRef = weightConfiguration.getMaxRevenueReference();
        BigDecimal revRatio = firm.getAverageRevenue()
                .min(maxRevenueRef)
                .divide(maxRevenueRef, 4, RoundingMode.HALF_UP);
        return revRatio.multiply(new BigDecimal("1000"));
    }

    private BigDecimal calculateTimeScore(Firm firm, WeightConfiguration weightConfiguration) {
        double maxTimeRef = weightConfiguration.getMaxTimeReferenceMonths().doubleValue();
        double timeValue = Math.min(maxTimeRef, firm.getTimeMonths());
        double timeRatio = timeValue / maxTimeRef;
        return BigDecimal.valueOf(timeRatio * 1000);
    }

    private BigDecimal calculateDefaultScore(List<DefaultOccurrence> activeDefaults) {
        return activeDefaults.isEmpty() ? new BigDecimal("1000") : BigDecimal.ZERO;
    }

    private int calculateFinalScore(BigDecimal scoreRev, BigDecimal scoreTime, BigDecimal scoreDefault, WeightConfiguration weightConfiguration) {
        BigDecimal finalScore = scoreRev.multiply(weightConfiguration.getRevenueWeight())
                .add(scoreTime.multiply(weightConfiguration.getTimeWeight()))
                .add(scoreDefault.multiply(weightConfiguration.getDefaultWeight()));

        return finalScore.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private Firm companySearch(Long id) {
        return firmRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Firm not found with ID: " + id));
    }

    private User userSearch(Long id) {
        return userRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private WeightConfiguration findLatestActiveWeightConfiguration() {
        return weightConfigurationRepository.findFirstByStatusOrderByIdDesc(EntityStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("No active weight configuration found in the system."));
    }

    private List<DefaultOccurrence> findActiveDefaults(Long firmId) {
        return defaultOccurrenceRepository.findByFirmIdAndStatusAndStatusResolvedFalse(firmId, EntityStatus.ACTIVE);
    }

    private RiskBand classifyRiskBand(int score, WeightConfiguration weightConfiguration) {
        if (score >= weightConfiguration.getLowRiskThreshold()) {
            return RiskBand.LOW;
        } else if (score >= weightConfiguration.getMediumRiskThreshold()) {
            return RiskBand.MEDIUM;
        } else {
            return RiskBand.HIGH;
        }
    }

    private String generateJustification(ScoreFactorsDTO factors, RiskBand riskBand, int finalScore) {
        StringBuilder justification = new StringBuilder();

        justification.append(String.format("The final score was %d points, classified in the %s risk band. ", finalScore, riskBand));

        if (factors.inDefault()) {
            justification.append("The factor with the greatest negative impact was the presence of active default occurrences, which zeroed the score for this category. ");
        } else {
            justification.append("The absence of active defaults contributed positively to the maximum score in the payment history category. ");
        }

        justification.append(String.format("The reported average revenue was R$ %.2f (weight of %.0f%%) and the operating time was %d months (weight of %.0f%%).",
                factors.averageRevenue(),
                factors.revenueWeight().multiply(new BigDecimal("100")),
                factors.timeMonths(),
                factors.timeWeight().multiply(new BigDecimal("100"))
        ));

        return justification.toString();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CalculatedScore calculatedScore = calculatedScoreRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("calculated Score not found with ID: " + id));

        long activeCalculatedScoreCount = calculatedScoreRepository.countByStatus(EntityStatus.ACTIVE);
        if (activeCalculatedScoreCount <= 1) {
            throw new BusinessException("Cannot delete the only active calculated score. At least one active score must remain in the system.");
        }

        calculatedScore.inactive();
        calculatedScoreRepository.save(calculatedScore);
    }

    @Override
    @Transactional(readOnly = true)
    public CalculatedScoreResponse findById(Long id) {
        CalculatedScore calculatedScore = calculatedScoreRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("calculated Score not found with ID: " + id));

        return calculatedScoreMapper.toResponse(calculatedScore);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CalculatedScoreResponse> findAll(Pageable pageable) {
        Page<CalculatedScore> calculatedScoreResponsePage = calculatedScoreRepository
                .findByStatusNot(EntityStatus.DELETED, pageable);

        return calculatedScoreResponsePage.map(calculatedScoreMapper::toResponse);
    }

    @Override
    @Transactional
    public CalculatedScoreResponse update(Long id, UpdateCalculatedScoreRequest updateCalculatedScoreRequest) {
        CalculatedScore calculatedScore = calculatedScoreRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("calculated Score not found with ID: " + id));

        deactivatePreviousCalculation(calculatedScore);

        calculatedScoreRepository.findByFirmIdAndStatus(updateCalculatedScoreRequest.firmId(), EntityStatus.ACTIVE)
                .forEach(this::deactivatePreviousCalculation);

        CalculatedScore setCalculation = setCalculation(updateCalculatedScoreRequest.firmId(), updateCalculatedScoreRequest.userId());
        calculatedScoreRepository.save(setCalculation);
        return calculatedScoreMapper.toResponse(setCalculation);
    }

    private void deactivatePreviousCalculation(CalculatedScore calculatedScore) {
        calculatedScore.inactive();
        calculatedScoreRepository.save(calculatedScore);
    }
}