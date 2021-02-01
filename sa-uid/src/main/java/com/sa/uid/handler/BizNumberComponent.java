





























package com.sa.uid.handler;

import com.sa.uid.constants.BizNumberConstant;
import com.sa.uid.domain.BizNumberAndRule;
import com.sa.uid.domain.BizNumberRule;
import com.sa.uid.domain.SequenceNo;
import com.sa.uid.mapper.BizNumberMapper;
import com.sa.uid.mapper.BizNumberRuleMapper;
import com.sa.uid.util.BizNumberUtils;
import com.sa.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberComponent {

    protected static final Logger log = LoggerFactory.getLogger(BizNumberComponent.class);
    @Autowired
    BizNumberMapper bizNumberMapper;
    @Autowired
    BizNumberRuleMapper bizNumberRuleMapper;



    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
    public SequenceNo getSeqNoByNewTransactional(SequenceNo idSequence, String type, Long startSeq, String dateFormat, int length, long step){













        BizNumberAndRule bizNumberAndRule = bizNumberMapper.getBizNumberAndRule(type);
        if(bizNumberAndRule == null){
            log.error("业务号类型[{}]不存在!", type);
            return null;
        }

        int max = new Double(Math.pow(10, bizNumberAndRule.getLength())).intValue();
        String dateStr = bizNumberAndRule.getDateFormat() == null ? null : DateUtils.format(bizNumberAndRule.getDateFormat());
        Long initBizNumber = BizNumberUtils.getInitBizNumber(dateStr, bizNumberAndRule.getLength());

        Long tempStartSeq = 0L;

        if(startSeq != null){

            Long currentDateValue = dateStr == null ? bizNumberAndRule.getValue() : Long.parseLong(bizNumberAndRule.getValue().toString().substring(0, dateStr.length()));
            Long startSeqDateValue = dateStr == null ? startSeq : Long.parseLong(startSeq.toString().substring(0, dateStr.length()));

            if(startSeqDateValue > currentDateValue){
                tempStartSeq = startSeq;
            }



            else{
                tempStartSeq = bizNumberAndRule.getValue();
            }
            if(tempStartSeq > initBizNumber + max - 1){

                log.error("[{}]当天业务编码分配数超过{},无法分配!", type, max);
                return null;
            }
        }else{
            tempStartSeq = bizNumberAndRule.getValue();
        }


        bizNumberAndRule.setValue(tempStartSeq + step);
        try {

            int count = bizNumberMapper.updateByPrimaryKeySelective(bizNumberAndRule);
            if (count < 1) {
                log.info("乐观锁更新失败后，返回空，外层进行重试!");
                return null;
            }
        }catch (RuntimeException e){
            log.error("当更新失败后，返回空，外层进行重试:{}", e.getMessage());
            return null;
        }

        idSequence.setStartSeq(tempStartSeq);
        idSequence.setStep(bizNumberAndRule.getStep());
        idSequence.setFinishSeq(tempStartSeq + bizNumberAndRule.getStep());

        updateCachedBizNumberRule(bizNumberAndRule, type);
        return idSequence;
    }


    private void updateCachedBizNumberRule(BizNumberAndRule bizNumberAndRule, String type){
        BizNumberRule cachedBizNumberRule = BizNumberConstant.bizNumberCache.get(type);
        cachedBizNumberRule.setPrefix(bizNumberAndRule.getPrefix());
        cachedBizNumberRule.setDateFormat(bizNumberAndRule.getDateFormat());
        cachedBizNumberRule.setLength(bizNumberAndRule.getLength());
        cachedBizNumberRule.setStep(bizNumberAndRule.getStep());
        cachedBizNumberRule.setRange(bizNumberAndRule.getRange());
    }

}
