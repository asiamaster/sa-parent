package com.sa.ip;


public class IPAllAcceptFilter implements IPAcceptFilter{
    private static IPAcceptFilter instance = null;


    private IPAllAcceptFilter(){};


    public static IPAcceptFilter getInstance(){
        if(instance == null){
            instance = new IPAllAcceptFilter();
        }
        return instance;
    }

    @Override
    public boolean accept(String ipAddress) {
        return true;
    }


}
