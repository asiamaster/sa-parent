package com.sa.dao.sql;

import tk.mybatis.mapper.version.NextVersion;
import tk.mybatis.mapper.version.VersionException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;


public class DateNextVersion implements NextVersion {

    @Override
    public Object nextVersion(Object current) throws VersionException {
        if (current == null) {
            throw new VersionException("当前版本号为空!");
        }
        if (current instanceof Date) {
            return new Date();
        } else if (current instanceof LocalDateTime) {
            return LocalDateTime.now();
        } else if (current instanceof Instant) {
            return Instant.now();
        } else {
            throw new VersionException("DateNextVersion 只支持 Date, LocalDateTime 和 Instant 类型的版本号，如果有需要请自行扩展!");
        }
    }

}