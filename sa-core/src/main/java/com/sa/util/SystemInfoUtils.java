package com.sa.util;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSFileStore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class SystemInfoUtils {
    private static final SystemInfo systemInfo = new SystemInfo();


    public static String getCpuUseRatio(){
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        if(totalCpu == 0){
            return "0";
        }
        BigDecimal cpuUseRatio = new BigDecimal(totalCpu-idle).divide(BigDecimal.valueOf(totalCpu), 4, RoundingMode.HALF_UP);
        return new DecimalFormat("#.####").format(cpuUseRatio);
    }


    public static Integer getLogicalProcessorCount(){
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        return processor.getLogicalProcessorCount();
    }


    public static GlobalMemory getMemory(){
        return systemInfo.getHardware().getMemory();
    }


    public static long getMemoryTotalByte(){
        return getMemory().getTotal();
    }


    public static long getMemoryAvailableByte(){
        return getMemory().getAvailable();
    }


    public static String getMemoryUseRatio(){
        GlobalMemory memory = getMemory();
        long totalByte = memory.getTotal();
        long availableByte = memory.getAvailable();
        BigDecimal memoryUseRatio = new BigDecimal(totalByte-availableByte).divide(BigDecimal.valueOf(totalByte), 4, RoundingMode.HALF_UP);
        return new DecimalFormat("#.####").format(memoryUseRatio);
    }


    public static List<HWDiskStore> getDiskStores(){
        return systemInfo.getHardware().getDiskStores();
    }


    public static List<OSFileStore> getFileStores(){
        return systemInfo.getOperatingSystem().getFileSystem().getFileStores();
    }


    public static List<String> getDiskUseRatio(){
        List<OSFileStore> fileStores = getFileStores();
        List<String> diskUseRatios = new ArrayList<>(fileStores.size());
        for (OSFileStore fileStore : fileStores) {

            long usable = fileStore.getUsableSpace();
            long total = fileStore.getTotalSpace();
            BigDecimal diskUseRatio = new BigDecimal(total - usable).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
            diskUseRatios.add(new DecimalFormat("#.####").format(diskUseRatio));
        }
        return diskUseRatios;
    }


    public static SystemInfo getSystemInfo(){
        return systemInfo;
    }


    public static String formatByte(long byteNumber){
        double FORMAT = 1024.0;
        double kbNumber = byteNumber/FORMAT;
        if(kbNumber<FORMAT){
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber/FORMAT;
        if(mbNumber<FORMAT){
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber/FORMAT;
        if(gbNumber<FORMAT){
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber/FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }

}
