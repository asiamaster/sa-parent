package com.sa.ip;


public interface IPAcceptFilter {
    public String IPv6KeyWord = ":";
    public boolean accept(String ipAddress);
}
