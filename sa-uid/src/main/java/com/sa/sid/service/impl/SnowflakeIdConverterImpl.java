





























package com.sa.sid.service.impl;


import com.sa.sid.consts.SnowflakeIdMeta;
import com.sa.sid.dto.SnowflakeId;
import com.sa.sid.service.SnowflakeIdConverter;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdConverterImpl implements SnowflakeIdConverter {
  @Override
  public long convert(SnowflakeId id) {
    long ret = 0;
    ret |= id.getSequence();
    ret |= id.getWorkerId() << SnowflakeIdMeta.SEQUENCE_BITS;
    ret |= id.getDatacenterId() << SnowflakeIdMeta.DATACENTER_ID_SHIFT_BITS;
    ret |= id.getTimeStamp() << SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS;
    return ret;
  }

  @Override
  public SnowflakeId convert(long id) {
    SnowflakeId ret = new SnowflakeId();
    ret.setSequence(id & SnowflakeIdMeta.SEQUENCE_MASK);
    ret.setWorkerId((id >>> SnowflakeIdMeta.SEQUENCE_BITS) & SnowflakeIdMeta.WORKER_ID_MASK);
    ret.setDatacenterId((id >>> SnowflakeIdMeta.DATACENTER_ID_SHIFT_BITS) & SnowflakeIdMeta.WORKER_ID_MASK);
    ret.setTimeStamp((id >>> SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS) & SnowflakeIdMeta.TIMESTAMP_MASK);
    return ret;
  }
}
