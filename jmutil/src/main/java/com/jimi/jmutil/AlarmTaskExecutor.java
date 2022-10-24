package com.jimi.jmutil;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Project    JMLib
 * Path       com.jimi.jmutil
 * Date       2022/10/21 - 18:05
 * Author     Payne.
 * About      类描述：
 */
public class AlarmTaskExecutor {

    private static final String ACTION_ALARM = "com.jimi.alarm.task";
    private static final String EXTRA_ID     = "extra.id";
    private static final String TAG          = "AlarmTaskExecutor";

    private static void d(String s) {
        Log.d(TAG, "[" + s + "]");
    }

    public static class Task {

        int      id;
        long     delay;
        Runnable runnable;

        private Task(int id, long delay, Runnable runnable) {
            this.id = id;
            this.delay = delay;
            this.runnable = runnable;
        }
    }

    private static class BR extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_ALARM.equals(intent.getAction())) {
                int id = intent.getIntExtra(EXTRA_ID, -1);
                if (-1 != id) {
                    Task task = mTaskMap.get(id);
                    if (task != null && task.runnable != null) {
                        task.runnable.run();
                    }
                }
            }
        }
    }


    private static class Inner {

        static AlarmTaskExecutor instance = new AlarmTaskExecutor();
    }

    public static AlarmTaskExecutor getInstance() {
        return Inner.instance;
    }

    private AlarmTaskExecutor() {

    }


    private          Context                     mCtx;
    private          AlarmManager                mAlarmManager = null;
    private          boolean                     mInit         = false;
    private static   Map<Integer, Task>          mTaskMap      = new HashMap<>();
    private          Map<Integer, PendingIntent> mPiMap        = new HashMap<>();
    private volatile int                         mTaskId       = 0;


    private PendingIntent createIntent(Task task) {
        Intent intent = new Intent(ACTION_ALARM);
        intent.putExtra(EXTRA_ID, task.id);
        return PendingIntent.getBroadcast(mCtx, task.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void init(Context context) {
        d("init");
        if (mInit) {
            return;
        }
        this.mCtx = context;
        mAlarmManager = (AlarmManager) mCtx.getSystemService(Activity.ALARM_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_ALARM);
        BR br = new BR();
        mCtx.registerReceiver(br, filter);
        mInit = true;

    }

    public synchronized Task createTask(long delay, Runnable runnable) {
        if (mTaskId > Integer.MAX_VALUE - 2) {
            mTaskId = 0;
        }
        int id = mTaskId++;
        return new Task(id, delay, runnable);
    }

    /**
     * todo repeat=true时计时不准，待优化
     */
    public void exe(Task task, boolean repeat) {
        mTaskMap.put(task.id, task);
        PendingIntent pi = createIntent(task);
        mPiMap.put(task.id, pi);
        if (repeat) {
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(), task.delay, pi);
        } else {
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + task.delay, pi);
            } else {
                mAlarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + task.delay, pi);
            }
        }
    }

    public void cancel(Task task) {
        mAlarmManager.cancel(mPiMap.get(task.id));
        mTaskMap.remove(task.id);
        mPiMap.remove(task.id);
    }

}
