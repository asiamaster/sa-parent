package com.sa.uid.service.impl;

import com.sa.base.BaseServiceImpl;
import com.sa.domain.BaseOutput;
import com.sa.dto.DTOUtils;
import com.sa.exception.ParamErrorException;
import com.sa.uid.constants.BizNumberConstant;
import com.sa.uid.domain.BizNumber;
import com.sa.uid.domain.BizNumberRule;
import com.sa.uid.mapper.BizNumberRuleMapper;
import com.sa.uid.service.BizNumberRuleService;
import com.sa.uid.service.BizNumberService;
import com.sa.uid.util.BizNumberUtils;
import com.sa.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;


@Service
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberRuleServiceImpl extends BaseServiceImpl<BizNumberRule, Long> implements BizNumberRuleService {

    @Autowired
    private BizNumberService bizNumberService;

    public BizNumberRuleMapper getActualDao() {
        return (BizNumberRuleMapper)getDao();
    }

    @Override
    public BizNumberRule getByType(String type){
        BizNumberRule bizNumberRuleDomain = DTOUtils.newInstance(BizNumberRule.class);
        bizNumberRuleDomain.setType(type);
        return getActualDao().selectOne(bizNumberRuleDomain);
    }

    @Override
    public int updateSelective(BizNumberRule bizNumberRule) {
        String[] ranges = bizNumberRule.getRange().split(",");
        if(ranges.length == 1 && bizNumberRule.getStep() % Long.parseLong(ranges[0]) != 0){
            throw new ParamErrorException("固定步长值必须是范围值的整数倍!");
        }
        int count = super.updateSelective(bizNumberRule);
        BizNumberConstant.bizNumberCache.put(get(bizNumberRule.getId()).getType(), bizNumberRule);
        return count;
    }

    @Override
    public int updateExactSimple(BizNumberRule bizNumberRule) {
        String[] ranges = bizNumberRule.getRange().split(",");
        if(ranges.length == 1 && bizNumberRule.getStep() % Long.parseLong(ranges[0]) != 0){
            throw new ParamErrorException("固定步长值必须是范围值的整数倍!");
        }
        int count = super.updateExactSimple(bizNumberRule);
        BizNumberConstant.bizNumberCache.put(bizNumberRule.getType(), bizNumberRule);
        return count;
    }

    @Override
    public int insertSelective(BizNumberRule bizNumberRule) {
        String[] ranges = bizNumberRule.getRange().split(",");
        if(ranges.length == 1 && bizNumberRule.getStep() % Long.parseLong(ranges[0]) != 0){
            throw new ParamErrorException("固定步长值必须是范围值的整数倍!");
        }
        int count = super.insertSelective(bizNumberRule);
        BizNumber condition = DTOUtils.newInstance(BizNumber.class);
        condition.setType(bizNumberRule.getType());
        BizNumber bizNumber = bizNumberService.selectOne(condition);

        if(bizNumber == null){
            bizNumber = DTOUtils.newInstance(BizNumber.class);
            bizNumber.setType(bizNumberRule.getType());
            String dateStr = bizNumberRule.getDateFormat() == null ? null : DateUtils.format(bizNumberRule.getDateFormat());
            bizNumber.setValue(BizNumberUtils.getInitBizNumber(dateStr, bizNumberRule.getLength()));
            bizNumber.setMemo(bizNumberRule.getName());
            bizNumber.setVersion(1L);
            bizNumberService.insertSelective(bizNumber);
        }

        BizNumberConstant.bizNumberCache.put(bizNumberRule.getType(), bizNumberRule);
        return count;
    }

    @Override
    public int delete(Long key) {
        String type = get(key).getType();
        int count = super.delete(key);
        BizNumberConstant.bizNumberCache.remove(type);
        bizNumberService.clear(type);
        BizNumber bizNumber = DTOUtils.newInstance(BizNumber.class);
        bizNumber.setType(type);
        bizNumberService.deleteByExample(bizNumber);
        return count;
    }

    @Override
    public BaseOutput updateEnable(Long id, Boolean enable) {
        BizNumberRule bizNumberRule = DTOUtils.newInstance(BizNumberRule.class);
        bizNumberRule.setId(id);
        bizNumberRule.setIsEnable(enable);
        getActualDao().updateByPrimaryKeySelective(bizNumberRule);
        return BaseOutput.success();
    }
}