package com.jimi.jmutil;

import android.util.Log;

/**
 * Project    JMLib
 * Path       com.jimi.jmutil
 * Date       2022/10/21 - 17:58
 * Author     Payne.
 * About      类描述：
 */
public class L {

    private static boolean SHOW_LOG = true;

    public static void showLog(boolean b) {
        SHOW_LOG = b;
    }

    public static void d(String tag, String content) {
        if (SHOW_LOG) {
            Log.d(tag, content);
        }
    }

    public static void d(String content) {
        d("jmlibtag", content);
    }

}