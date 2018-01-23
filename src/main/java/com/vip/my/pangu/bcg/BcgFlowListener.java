package com.vip.my.pangu.bcg;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by eric on 2017/4/18.
 */

@Service
public class BcgFlowListener implements FileAlterationListener {

    private static final Logger log = LoggerFactory.getLogger(BcgFlowListener.class);

    private static Map<String, Set<String>> data = new ConcurrentHashMap<>();

    private LogLevel logLevel;

    public BcgFlowListener() {
        this.logLevel = LogLevel.WARN;
    }

    public BcgFlowListener(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

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

            int size = list.size();
            int gap = 20;
            if (size > gap) {
                list = list.subList(size - gap, size);
            }
        } catch (IOException e) {
            return;
        }

        Set<String> set = refreshData(file.getName(), list);

        try {
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                FlowHelper holder = new FlowHelper(it.next());
                print(holder.format());
            }
        } catch (Exception e) {
            //log.warn("[printLog]cause error,{}", e.getMessage(), e);
        }
    }

    private Set<String> refreshData(String key, List<String> target) {
        Set<String> last = data.getOrDefault(key, new TreeSet<>());
        Set<String> set = clean(target);

        data.put(key, set);

        Set<String> tmp = new TreeSet<>(set);
        tmp.removeAll(last);
        return tmp;
    }

    private Set<String> clean(Collection<String> target) {
        Set<String> result = new TreeSet<>();
        Iterator<String> it = target.iterator();
        while (it.hasNext()) {

            String plain = it.next();
            int index = plain.lastIndexOf("[201");
            if (index > 0) {
                plain = plain.substring(index, plain.length());
                //System.out.println(plain);
            }

            result.add(plain);
        }
        return result;
    }

    private void print(String msg) {
        if (msg.contains(FlowHelper.SUCCESS_MSG)) {
            if (logLevel == LogLevel.INFO) {
                log.info(msg);
            }
        } else if (msg.contains(FlowHelper.CONNECTION_ERROR)) {
            log.error(msg);
        } else {
            log.warn(msg);
        }
    }

    public static void main(String[] args) {
        String msg = "1 2 ";
        List<String> list = Arrays.asList(msg.split(" "));
        BcgFlowListener bcg = new BcgFlowListener();

        bcg.refreshData("9", list);
        for (int i = 10; i < 15; i++) {
            bcg.refreshData("9", list);
            msg += i + " ";
            list = Arrays.asList(msg.split(" "));
            //System.out.println(JSON.toJSONString(list));
            bcg.refreshData("9", list);
            //System.out.println(JSON.toJSONString(list));
        }

    }
}


