package com.ytjojo.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;

import com.orhanobut.logger.Logger;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class AndroidComponentUtil {

    private static String MIDNIGHT_ALARM_FILTER = "android.alarm.MIDNIGHT_ALARM_FILTER.action";

    public static void toggleComponent(Context context, Class componentClass, boolean enable) {
        ComponentName componentName = new ComponentName(context, componentClass);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> infos = manager.getRunningServices(Integer.MAX_VALUE);
            if (infos != null && !infos.isEmpty()) {
                for (ActivityManager.RunningServiceInfo service : infos) {
                    // 添加Uid验证, 防止服务重名, 当前服务无法启动
                    if (getUid(context) == service.uid) {
                        if (serviceClass.getName().equals(service.service.getClassName())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取应用的Uid, 用于验证服务是否启动
     *
     * @param context 上下文
     * @return uid
     */
    public static int getUid(Context context) {
        if (context == null) {
            return -1;
        }

        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager != null) {
            List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
            if (infos != null && !infos.isEmpty()) {
                for (ActivityManager.RunningAppProcessInfo processInfo : infos) {
                    if (processInfo.pid == pid) {
                        return processInfo.uid;
                    }
                }
            }
        }
        return -1;
    }
    /**
     * 设置午夜定时器, 午夜12点发送广播, MIDNIGHT_ALARM_FILTER.
     * 实际测试可能会有一分钟左右的偏差.
     *
     * @param context 上下文
     */
    public static void setMidnightAlarm(Context context) {
        Context appContext = context.getApplicationContext();
        Intent intent = new Intent(MIDNIGHT_ALARM_FILTER);

        PendingIntent pi = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        // 午夜12点的标准计时, 来源于SO, 实际测试可能会有一分钟左右的偏差.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        // 显示剩余时间
        long now = Calendar.getInstance().getTimeInMillis();
        Logger.i("AlarmManager","剩余时间(秒): " + ((calendar.getTimeInMillis() - now) / 1000));

        // 设置之前先取消前一个PendingIntent
        am.cancel(pi);

        // 设置每一天的计时器
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
    // 获取通知的ID, 防止重复, 可以用于通知的ID
    // 随机生成一个数
    private final static AtomicInteger c = new AtomicInteger(0);

    // 获取一个不重复的数, 从0开始
    public static int getID() {
        return c.incrementAndGet();
    }
    /**
     * 检测屏幕是否开启
     *
     * @param context 上下文
     * @return 是否屏幕开启
     */
    public static boolean isScreenOn(Context context) {
        Context appContext = context.getApplicationContext();
        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            // noinspection all
            return pm.isScreenOn();
        }
    }
    /**
     * 检测计步传感器是否可以使用
     *
     * @param context 上下文
     * @return 是否可用计步传感器
     */
    public static boolean hasStepSensor(Context context) {
        if (context == null) {
            return false;
        }

        Context appContext = context.getApplicationContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false;
        } else {
            boolean hasSensor = false;
            Sensor sensor = null;
            try {
                hasSensor = appContext.getPackageManager().hasSystemFeature("android.hardware.sensor.stepcounter");
                SensorManager sm = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
                sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return hasSensor && sensor != null;
        }
    }
}
