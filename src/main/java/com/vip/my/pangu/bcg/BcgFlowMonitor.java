package com.vip.my.pangu.bcg;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by eric on 2017/4/18.
 */

public class BcgFlowMonitor {

    FileAlterationMonitor monitor = null;

    public BcgFlowMonitor(long interval) {
        this.monitor = new FileAlterationMonitor(interval);
    }

    public void monitor(String path, FileAlterationListener listener) {
        FileAlterationObserver observer = new FileAlterationObserver(new File(path));
        monitor.addObserver(observer);
        observer.addListener(listener);
    }

    public void stop() throws Exception {
        monitor.stop();
    }

    public void start() throws Exception {
        monitor.start();
    }

    public static void main(String[] args) throws Exception {
        BcgFlowListener listener = new BcgFlowListener(LogLevel.INFO);
        BcgFlowMonitor bcgFlowMonitor = new BcgFlowMonitor(TimeUnit.SECONDS.toMillis(2));
        bcgFlowMonitor.monitor("D:\\download\\", listener);
        bcgFlowMonitor.start();
    }
}
