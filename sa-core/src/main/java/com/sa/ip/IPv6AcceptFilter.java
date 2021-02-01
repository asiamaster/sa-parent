package com.sa.ip;


public class IPv6AcceptFilter implements IPAcceptFilter{
    private static IPAcceptFilter instance = null;


    private IPv6AcceptFilter(){};


    public static IPAcceptFilter getInstance(){
        if(instance == null){
            instance = new IPv6AcceptFilter();
        }
        return instance;
    }

    @Override
    public boolean accept(String ipAddress) {
        return ipAddress != null && ipAddress.indexOf(IPv6KeyWord) > -1;
    }


}
