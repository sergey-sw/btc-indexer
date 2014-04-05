package com.ssau.btc.sys;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Author: Sergey42
 * Date: 05.04.14 16:08
 */
@ManagedComponent(name = "Synchronizer")
public class Synchronizer {

    @Inject
    protected DatabaseAPI databaseAPI;
    @Inject
    protected WebLoaderAPI webLoaderAPI;
    @Inject
    protected ThreadManager threadManager;

    protected Date lastSyncTs;
    public static final String LAST_SYNC_CONFIG_NAME = "lastSyncTs";
    public static final int SYNC_INTERVAL = 30;

    public void initTimer() {
        threadManager.scheduleTask(sync, SYNC_INTERVAL, TimeUnit.SECONDS);
    }

    public void doSync() {
        if (needSync()) {
            System.out.println("need sync");
        }
    }

    public boolean needSync() {
        if (lastSyncTs == null) {
            String lastSync = databaseAPI.getConfig(LAST_SYNC_CONFIG_NAME);
            if (lastSync != null && !lastSync.isEmpty()) {
                lastSyncTs = DateUtils.getTime(lastSync);
            } else {
                lastSyncTs = new Date();
                databaseAPI.writeConfig(LAST_SYNC_CONFIG_NAME, DateUtils.formatDateTime(lastSyncTs));
            }
        }

        return new Date().getTime() - lastSyncTs.getTime() > SYNC_INTERVAL;
    }

    private Runnable sync = new Runnable() {
        @Override
        public void run() {
            doSync();
        }
    };
}
