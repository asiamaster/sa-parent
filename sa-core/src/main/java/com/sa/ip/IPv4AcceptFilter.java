package com.sa.ip;


public class IPv4AcceptFilter implements IPAcceptFilter{
    private static IPAcceptFilter instance = null;


    private IPv4AcceptFilter(){};


    public static IPAcceptFilter getInstance(){
        if(instance == null){
            instance = new IPv4AcceptFilter();
        }
        return instance;
    }

    @Override
    public boolean accept(String ipAddress) {
        return ipAddress != null && ipAddress.indexOf(IPv6KeyWord) == -1;
    }


}
