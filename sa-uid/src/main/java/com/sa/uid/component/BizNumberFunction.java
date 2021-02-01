package com.sa.uid.component;

import com.sa.dto.DTOUtils;
import com.sa.uid.constants.BizNumberConstant;
import com.sa.uid.domain.BizNumber;
import com.sa.uid.domain.BizNumberRule;
import com.sa.uid.service.BizNumberRuleService;
import com.sa.uid.service.BizNumberService;
import com.sa.uid.util.BizNumberUtils;
import com.sa.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberFunction {
    @Autowired
    private BizNumberService bizNumberService;

    @Autowired
    private BizNumberRuleService bizNumberRuleService;

    public String getBizNumberByType(String bizNumberType){
        return bizNumberService.getBizNumberByRule(getBizNumberRule(bizNumberType));
    }


    public static String format(String format) {
        return format(LocalDateTime.now(ZoneId.of("GMT+08:00")), format);
    }


    public static String format(LocalDateTime localDateTime, String format) {
        return DateTimeFormatter.ofPattern(format).format(localDateTime);
    }


    private BizNumberRule getBizNumberRule(String bizNumberType){
        BizNumberRule bizNumberRule = BizNumberConstant.bizNumberCache.get(bizNumberType);
        return bizNumberRule == null ? initBizNumberAndRule(bizNumberType) : bizNumberRule;
    }


    private synchronized BizNumberRule initBizNumberAndRule(String bizNumberType){

        BizNumberRule bizNumberRule = bizNumberRuleService.getByType(bizNumberType);
        if(bizNumberRule == null){
            return null;
        }
        BizNumber bizNumberCondition = DTOUtils.newInstance(BizNumber.class);
        bizNumberCondition.setType(bizNumberType);
        BizNumber bizNumber = bizNumberService.selectOne(bizNumberCondition);

        if(bizNumber == null){
            bizNumber = DTOUtils.newInstance(BizNumber.class);
            bizNumber.setType(bizNumberRule.getType());
            String dateStr = bizNumberRule.getDateFormat() == null ? null : DateUtils.format(bizNumberRule.getDateFormat());
            bizNumber.setValue(BizNumberUtils.getInitBizNumber(dateStr, bizNumberRule.getLength()));
            bizNumber.setMemo(bizNumberRule.getName());
            bizNumber.setVersion(1L);
            bizNumberService.insertSelective(bizNumber);
        }
        BizNumberConstant.bizNumberCache.put(bizNumberType, bizNumberRule);
        return bizNumberRule;
    }
}
