package com.movieweb.service;

import java.sql.Timestamp;

import com.movieweb.DAO.PromotionsDAO;
import com.movieweb.model.Promotions;

public class PromotionCodeValidate 
{
    private final PromotionsDAO promotionsDAO = new PromotionsDAO();
    public Promotions validatePromotion(String promotion_code)
    {
        if (promotion_code == null || promotion_code.trim().isEmpty()) 
        {
            return null;
        }
        String code = promotion_code.trim();
        Promotions promotion = promotionsDAO.getPromotionByCode(code);
        if (promotion == null) 
        {
            return null;
        }
        if (!promotion.isActive()) 
        {
            return null;
        }
        if (!isWithinPromotionPeriod(promotion)) 
        {
            return null;
        }
        if (!hasUsageRemaining(promotion)) 
        {
            return null;
        }
        return promotion;
    }

    private boolean isWithinPromotionPeriod(Promotions promotion)
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp startAt = promotion.getStart_at();
        Timestamp endAt = promotion.getEnd_at();
        if (startAt == null || endAt == null) 
        {
            return false;
        }
        return !now.before(startAt) && !now.after(endAt);
    }

    private boolean hasUsageRemaining(Promotions promotion)
    {
        int usageLimit = promotion.getUsage_limit();
        if (usageLimit <= 0) 
        {
            return false;
        }
        int usageCount = promotionsDAO.getPromotionUsageCount(promotion.getPromotion_id());
        if (usageCount < 0) 
        {
            return false;
        }
        return usageCount < usageLimit;
    }
}
