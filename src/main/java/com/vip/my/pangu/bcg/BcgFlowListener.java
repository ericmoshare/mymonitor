package com.vip.my.pangu.bcg;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by eric on 2017/4/18.
 */

@Service
public class BcgFlowListener implements FileAlterationListener {

    final static String formular = "%s,心跳:%s, 渠道:%s, 发卡行:%s, 业务类型:%s, 调用:%s, 耗时:%s ms, 结论:%s";

    private static final Logger log = LoggerFactory.getLogger(BcgFlowListener.class);

    private static Map<String, Integer> indexOfMap = new ConcurrentHashMap<>();

    @Override
    public void onStart(FileAlterationObserver observer) {
        //System.out.println("onStart");
    }

    @Override
    public void onDirectoryCreate(File directory) {
        System.out.println("onDirectoryCreate:" + directory.getName());
    }

    @Override
    public void onDirectoryChange(File directory) {
        System.out.println("onDirectoryChange:" + directory.getName());
    }

    @Override
    public void onDirectoryDelete(File directory) {
        System.out.println("onDirectoryDelete:" + directory.getName());
    }

    @Override
    public void onFileCreate(File file) {
        //System.out.println("onFileCreate:" + file.getName());
        printLog(file);
    }

    @Override
    public void onFileChange(File file) {
        //System.out.println("onFileChange : " + file.getName());
        printLog(file);
    }

    @Override
    public void onFileDelete(File file) {
        //System.out.println("onFileDelete :" + file.getName());
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        //System.out.println("onStop");
    }

    private void printLog(File file) {

        List<String> list;
        try {
            list = FileUtils.readLines(file);
        } catch (IOException e) {
            //log.info("read file:{} failed,{}", file.getName(), e.getMessage());
            return;
        }
        String filename = file.getName();
        int size = list.size();

        int index;
        if (indexOfMap.get(file.getName()) == null) {
            //首次打印,只打印倒数2行
            index = size - getRightIndex(size);
        } else {
            // 二次打印, 打印累积数据
            index = indexOfMap.get(file.getName());
        }

        try {
            for (int i = index; i < size; i++) {
                print(prepareData(list, i));
            }
        } catch (Exception e) {
            //log.warn("[prepareData]cause error,{}", e.getMessage());
        } finally {
            indexOfMap.put(filename, size - 1);
        }
    }

    private int getRightIndex(int size) {
        if (size <= 1) {
            return size;
        }
        return 2;
    }

    private String prepareData(List<String> list, int index) throws Exception {
        String content = list.get(index);
        String[] clips = content.split("\\,");
        String respCode = clips[4];
        long costTime = Long.valueOf(getCostTime(clips));
        String result = getResultByCondition(respCode, costTime);
        return String.format(formular, index, getHeartBeat(clips[0]), getRouteOrg(clips[0]), clips[1], clips[2], clips[4], costTime, result);
    }

    private String getRouteOrg(String clip) {
        int index = clip.indexOf("F2P");
        return clip.substring(index, clip.length());
    }

    private String getHeartBeat(String clip) {
        String[] subClips = clip.split("\\ ");
        return subClips[1].substring(0, subClips[1].length() - 1);
    }

    private String getCostTime(String[] clips) {
        return clips[clips.length - 1];
    }

    private String getResultByCondition(String respCode, long costTime) {
        if ("SUCCESS".contains(respCode)) {
            long maxWarningTime = TimeUnit.SECONDS.toMillis(2);
            if (costTime > maxWarningTime) {
                return "耗时超" + TimeUnit.MILLISECONDS.toSeconds(maxWarningTime) + "秒";
            } else {
                return "正常";
            }
        }
        return "不正常";
    }

    private void print(String msg) {
        if (msg.contains("不正常")) {
            log.error(msg);
        } else {
            if (msg.contains("耗时超")) {
                log.warn(msg);
            } else {
                log.info(msg);

            }
        }
    }
}
