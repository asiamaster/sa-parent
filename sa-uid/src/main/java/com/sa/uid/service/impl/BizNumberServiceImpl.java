





























package com.sa.uid.service.impl;

import com.sa.base.BaseServiceImpl;
import com.sa.exception.AppException;
import com.sa.uid.domain.BizNumber;
import com.sa.uid.domain.BizNumberRule;
import com.sa.uid.domain.SequenceNo;
import com.sa.uid.handler.BizNumberComponent;
import com.sa.uid.mapper.BizNumberMapper;
import com.sa.uid.service.BizNumberService;
import com.sa.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


@Service
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberServiceImpl extends BaseServiceImpl<BizNumber, Long> implements BizNumberService {

    public BizNumberMapper getActualDao() {
        return (BizNumberMapper)getDao();
    }

    public static ReentrantLock rangeIdLock = new ReentrantLock(false);
    @Autowired
    private BizNumberComponent bizNumberComponent;


    protected ConcurrentHashMap<String, SequenceNo> bizNumberMap = new ConcurrentHashMap<>();


    protected static final int RETRY = 10;

    @Override
    public void clear(String type){
        if(bizNumberMap.containsKey(type)) {
            bizNumberMap.remove(type);
        }
    }

    @Override
    public BizNumber selectOne(BizNumber bizNumber){
        return getActualDao().selectOne(bizNumber);
    }

    @Override
    public String getBizNumberByRule(BizNumberRule bizNumberRule) {
        if(bizNumberRule == null){
            return null;
        }
        Long bizNumber = getBizNumber(bizNumberRule);
        String bizNumberStr = StringUtils.isBlank(bizNumberRule.getDateFormat()) ? String.format("%0" + bizNumberRule.getLength() + "d", bizNumber) : bizNumber.toString();
        String prefix = bizNumberRule.getPrefix();
        return prefix == null ? bizNumberStr : prefix + bizNumberStr;
    }



    private Long getBizNumber(BizNumberRule bizNumberRule) {

        if(StringUtils.isBlank(bizNumberRule.getRange())){
            bizNumberRule.setRange("1");
        }
        String[] ranges = bizNumberRule.getRange().split(",");
        int increment = ranges.length == 1 ? Integer.parseInt(ranges[0]) : rangeRandom(Integer.parseInt(ranges[0].trim()), Integer.parseInt(ranges[1].trim()));

        long finalStep;

        if (ranges.length == 2) {
            finalStep = Long.parseLong(ranges[1]) * bizNumberRule.getStep();
        } else {

            finalStep = bizNumberRule.getStep();
        }
        return getBizNumberByType(bizNumberRule.getType(), bizNumberRule.getDateFormat(), bizNumberRule.getLength(), finalStep, increment);
    }


    private Long getBizNumberByType(String type, String dateFormat, int length, long step, int increment) {
        String dateStr = dateFormat == null ? "" : DateUtils.format(dateFormat);
        Long orderId = getNextSequenceId(type, null, dateFormat, length, step, increment);

        if (StringUtils.isNotBlank(dateStr) && !dateStr.equals(StringUtils.substring(String.valueOf(orderId), 0, dateStr.length()))) {
            orderId = getNextSequenceId(type, getInitBizNumber(dateStr, length), dateFormat, length, step, increment);
        }
        return orderId;
    }


    private Long getNextSequenceId(String type, Long startSeq, String dateFormat, int length, long step, int increment) {
        Long seqId = getNextSeqId(type, startSeq, dateFormat, length, step, increment);
        int i = 0;
        for (; (seqId < 0 && i < RETRY); i++) {
            bizNumberMap.remove(type);
            seqId = getNextSeqId(type, startSeq, dateFormat, length, step, increment);
        }
        if(i >= RETRY){
            throw new AppException("5002", String.format("业务号乐观锁重试%s次失败", RETRY));
        }
        return seqId;
    }


    private Long getNextSeqId(String type, Long startSeq, String dateFormat, int length, long step, int increment) {
        rangeIdLock.lock();
        try {
            SequenceNo idSequence = bizNumberMap.get(type);
            if (idSequence == null) {
                idSequence = new SequenceNo(step);
                bizNumberMap.putIfAbsent(type, idSequence);
                idSequence = bizNumberMap.get(type);
            }


            if (startSeq != null || idSequence.getStartSeq() >= idSequence.getFinishSeq()) {
                idSequence = bizNumberComponent.getSeqNoByNewTransactional(idSequence, type, startSeq, dateFormat, length, step);
                if (idSequence == null) {
                    return -1L;
                }
            }
            return increment == 1 ? idSequence.next() : idSequence.next(increment);
        } finally {
            rangeIdLock.unlock();
        }
    }


    private Long getInitBizNumber(String dateStr, int length) {
        return StringUtils.isBlank(dateStr) ? 1 : NumberUtils.toLong(dateStr) * new Double(Math.pow(10, length)).longValue() + 1;
    }


    private int rangeRandom(int min, int max){
        return new Random().nextInt(max)%(max-min+1) + min;
    }


}