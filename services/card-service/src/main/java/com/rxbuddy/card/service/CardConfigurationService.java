package com.rxbuddy.card.service;

import com.rxbuddy.card.dto.CardConfigurationDTO;
import com.rxbuddy.card.dto.CategoryDiscountDTO;
import com.rxbuddy.card.dto.CategoryPointConfigDTO;
import com.rxbuddy.card.entity.CardConfiguration;
import com.rxbuddy.card.entity.CategoryDiscount;
import com.rxbuddy.card.entity.CategoryPointConfig;
import com.rxbuddy.card.exception.ResourceNotFoundException;
import com.rxbuddy.card.repository.CardConfigurationRepository;
import com.rxbuddy.card.repository.CategoryDiscountRepository;
import com.rxbuddy.card.repository.CategoryPointConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardConfigurationService {

    private final CardConfigurationRepository configRepository;
    private final CategoryPointConfigRepository categoryPointRepository;
    private final CategoryDiscountRepository categoryDiscountRepository;

    @Transactional(readOnly = true)
    public CardConfigurationDTO getConfiguration(Long tenantId) {
        CardConfiguration config = configRepository.findByTenantId(tenantId)
                .orElse(CardConfiguration.builder().tenantId(tenantId).build());

        List<CategoryPointConfigDTO> pointConfigs = categoryPointRepository
                .findByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::toCategoryPointConfigDTO)
                .collect(Collectors.toList());

        List<CategoryDiscountDTO> discounts = categoryDiscountRepository
                .findByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::toCategoryDiscountDTO)
                .collect(Collectors.toList());

        return CardConfigurationDTO.builder()
                .tenantId(config.getTenantId())
                .isEnabled(config.getIsEnabled())
                .pointsToAmountRate(config.getPointsToAmountRate())
                .maxRedemptionPercent(config.getMaxRedemptionPercent())
                .minPointsToRedeem(config.getMinPointsToRedeem())
                .referralPointsPercent(config.getReferralPointsPercent())
                .referralEnabled(config.getReferralEnabled())
                .categoryPointConfigs(pointConfigs)
                .categoryDiscounts(discounts)
                .build();
    }

    @Transactional
    public CardConfigurationDTO updateConfiguration(Long tenantId, CardConfigurationDTO dto) {
        CardConfiguration config = configRepository.findByTenantId(tenantId)
                .orElse(CardConfiguration.builder().tenantId(tenantId).build());

        if (dto.getPointsToAmountRate() != null) {
            config.setPointsToAmountRate(dto.getPointsToAmountRate());
        }
        if (dto.getMaxRedemptionPercent() != null) {
            config.setMaxRedemptionPercent(dto.getMaxRedemptionPercent());
        }
        if (dto.getMinPointsToRedeem() != null) {
            config.setMinPointsToRedeem(dto.getMinPointsToRedeem());
        }
        if (dto.getReferralPointsPercent() != null) {
            config.setReferralPointsPercent(dto.getReferralPointsPercent());
        }
        if (dto.getReferralEnabled() != null) {
            config.setReferralEnabled(dto.getReferralEnabled());
        }

        configRepository.save(config);
        log.info("Updated card configuration for tenant {}", tenantId);

        return getConfiguration(tenantId);
    }

    @Transactional
    public CardConfigurationDTO enableCardSystem(Long tenantId) {
        CardConfiguration config = configRepository.findByTenantId(tenantId)
                .orElse(CardConfiguration.builder().tenantId(tenantId).build());

        config.setIsEnabled(true);
        configRepository.save(config);
        log.info("Enabled card system for tenant {}", tenantId);

        return getConfiguration(tenantId);
    }

    @Transactional
    public CardConfigurationDTO disableCardSystem(Long tenantId) {
        CardConfiguration config = configRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Card configuration not found"));

        config.setIsEnabled(false);
        configRepository.save(config);
        log.info("Disabled card system for tenant {}", tenantId);

        return getConfiguration(tenantId);
    }

    public boolean isCardSystemEnabled(Long tenantId) {
        return configRepository.findByTenantId(tenantId)
                .map(CardConfiguration::getIsEnabled)
                .orElse(false);
    }

    // Category Point Configs
    @Transactional(readOnly = true)
    public List<CategoryPointConfigDTO> getCategoryPointConfigs(Long tenantId) {
        return categoryPointRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::toCategoryPointConfigDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryPointConfigDTO saveCategoryPointConfig(Long tenantId, CategoryPointConfigDTO dto) {
        CategoryPointConfig config = categoryPointRepository
                .findByTenantIdAndCategoryId(tenantId, dto.getCategoryId())
                .orElse(CategoryPointConfig.builder()
                        .tenantId(tenantId)
                        .categoryId(dto.getCategoryId())
                        .build());

        config.setCategoryName(dto.getCategoryName());
        config.setPointPercentage(dto.getPointPercentage());
        config.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        config = categoryPointRepository.save(config);
        log.info("Saved category point config for tenant {} category {}", tenantId, dto.getCategoryId());

        return toCategoryPointConfigDTO(config);
    }

    @Transactional
    public void deleteCategoryPointConfig(Long tenantId, Long configId) {
        CategoryPointConfig config = categoryPointRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Category point config not found"));

        if (!config.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Config does not belong to this tenant");
        }

        categoryPointRepository.delete(config);
        log.info("Deleted category point config {} for tenant {}", configId, tenantId);
    }

    public BigDecimal getPointPercentage(Long tenantId, Long categoryId) {
        return categoryPointRepository.findByTenantIdAndCategoryId(tenantId, categoryId)
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .map(CategoryPointConfig::getPointPercentage)
                .orElse(new BigDecimal("1.00")); // Default 1% if not configured
    }

    // Category Discounts
    @Transactional(readOnly = true)
    public List<CategoryDiscountDTO> getCategoryDiscounts(Long tenantId) {
        return categoryDiscountRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::toCategoryDiscountDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDiscountDTO saveCategoryDiscount(Long tenantId, CategoryDiscountDTO dto) {
        CategoryDiscount discount = categoryDiscountRepository
                .findByTenantIdAndCategoryId(tenantId, dto.getCategoryId())
                .orElse(CategoryDiscount.builder()
                        .tenantId(tenantId)
                        .categoryId(dto.getCategoryId())
                        .build());

        discount.setCategoryName(dto.getCategoryName());
        discount.setDiscountPercentage(dto.getDiscountPercentage());
        discount.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        discount = categoryDiscountRepository.save(discount);
        log.info("Saved category discount for tenant {} category {}", tenantId, dto.getCategoryId());

        return toCategoryDiscountDTO(discount);
    }

    @Transactional
    public void deleteCategoryDiscount(Long tenantId, Long discountId) {
        CategoryDiscount discount = categoryDiscountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Category discount not found"));

        if (!discount.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Discount does not belong to this tenant");
        }

        categoryDiscountRepository.delete(discount);
        log.info("Deleted category discount {} for tenant {}", discountId, tenantId);
    }

    private CategoryPointConfigDTO toCategoryPointConfigDTO(CategoryPointConfig config) {
        return CategoryPointConfigDTO.builder()
                .id(config.getId())
                .categoryId(config.getCategoryId())
                .categoryName(config.getCategoryName())
                .pointPercentage(config.getPointPercentage())
                .isActive(config.getIsActive())
                .build();
    }

    private CategoryDiscountDTO toCategoryDiscountDTO(CategoryDiscount discount) {
        return CategoryDiscountDTO.builder()
                .id(discount.getId())
                .categoryId(discount.getCategoryId())
                .categoryName(discount.getCategoryName())
                .discountPercentage(discount.getDiscountPercentage())
                .isActive(discount.getIsActive())
                .build();
    }
}
