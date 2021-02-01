package com.sa.sid.service;

import com.sa.sid.dto.SnowflakeId;

import java.util.Date;

public interface SnowFlakeIdService {
  
  long nextId();

  
  SnowflakeId expId(long id);

  
  Date transTime(long time);

  
  long makeId(long timeStamp, long sequence);

  
  long makeId(long timeStamp, long datacenter, long worker, long sequence);
}
