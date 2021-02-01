package com.sa.sid.service;


import com.sa.sid.dto.SnowflakeId;

public interface SnowflakeIdConverter {
  long convert(SnowflakeId id);

  SnowflakeId convert(long id);
}
