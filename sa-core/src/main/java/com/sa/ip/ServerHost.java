package com.sa.ip;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerHost {

    private static final ServerHost instance = new ServerHost();

    private ServerHost(){};

    public static ServerHost getInstance() {
        return instance;
    }

    public String getExtranetIPv4Address(){
        return searchNetworkInterfaces(IPAcceptFilterFactory.getIPv4AcceptFilter());
    }


    public String getExtranetIPv6Address(){
        return searchNetworkInterfaces(IPAcceptFilterFactory.getIPv6AcceptFilter());
    }


    public String getExtranetIPAddress(){
        return searchNetworkInterfaces(IPAcceptFilterFactory.getIPAllAcceptFilter());
    }

    private String searchNetworkInterfaces(IPAcceptFilter ipFilter){
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();

                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
                while (addressEnumeration.hasMoreElements()) {
                    InetAddress inetAddress = addressEnumeration.nextElement();
                    String address = inetAddress.getHostAddress();
                    if(ipFilter.accept(address)){
                        return address;
                    }
                }
            }
        } catch (SocketException e) {

        }
        return "";
    }

    public static String getRealIp() throws SocketException {
        String localip = null;
        String netip = null;

        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        boolean finded = false;
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                        && ip.getHostAddress().indexOf(":") == -1) {
                    localip = ip.getHostAddress();
                }
            }
        }

        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    public static void main1(String[] args) throws SocketException {
        System.out.println(ServerHost.getInstance().getExtranetIPAddress());
        System.out.println(ServerHost.getInstance().getExtranetIPv4Address());
        System.out.println(ServerHost.getInstance().getExtranetIPv6Address());

        System.out.println(ServerHost.getRealIp());
    }
}
