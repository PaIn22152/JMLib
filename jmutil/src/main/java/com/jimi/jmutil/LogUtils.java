package com.jimi.jmutil;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: Xiaohy
 * Time: 2022/3/29 17:22
 * Describe: 日志工具类
 * 主要为了日志查看方便过滤使用，设置多级TAG标签，根据业务层级使用，比如：
 * TAG1-TAG2-TAG3
 * TAG1:可以为根TAG,比如此项目CarRecorder,在日志内容庞大时可以过滤1级
 * TAG2:根据1级过滤后，可以使用TAG2过滤业务层级，比如推流模块，摄像机模块，编解码模块
 * TAG3:根据TAG2过滤后，可使用TAG3过滤到具体的类
 * 暂时优化如此，如有更好的方式可以更改
 */
public class LogUtils {

    private static final LogUtils gLogUtils  = new LogUtils();
    private static       boolean  DEBUG_MODE = true;

    private static final int V = 0;
    private static final int W = 1;
    private static final int D = 2;
    private static final int I = 3;
    private static final int E = 4;

    public static final String FIRST_TAG_C170DMS_ACTION = "C170DMSAction";
    public static final String FIRST_TAG_RTMP_PUSHER    = "RTMPPusher";
    public static final String FIRST_TAG_DMS_DPX        = "DMSDpx";
    public static final String FIRST_TAG_ADAS           = "AdasModel";
    public static final String FIRST_TAG_WIFI_KIT       = "WifiKit";

    private final String MODE = "Device";
    private final String LOG  = "Log";
    private       String mModuleName;
    private       String mLogPath;

    public static LogUtils getInstance() {
        return gLogUtils;
    }

    /**
     * 按进程模块创建对应的Log路径
     *
     * @param moduleName 模块名
     */
    public void createLogPath(String moduleName) {
        mModuleName = moduleName;
        mLogPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + MODE + LOG + File.separator + mModuleName + LOG + File.separator;
    }

    public static void printV(String secondTag, String msg) {
        printV("", secondTag, msg);
    }

    public static void printV(String firstTag, String secondTag, String msg) {
        getInstance().print(firstTag, secondTag, msg, V);
    }

    public static void printW(String secondTag, String msg) {
        printW("", secondTag, msg);
    }

    public static void printW(String firstTag, String secondTag, String msg) {
        getInstance().print(firstTag, secondTag, msg, W);
    }

    public static void printD(String secondTag, String msg) {
        printD("", secondTag, msg);
    }

    public static void printD(String firstTag, String secondTag, String msg) {
        getInstance().print(firstTag, secondTag, msg, D);
    }


    public static void printI(String secondTag, String msg) {
        printI("", secondTag, msg);
    }

    public static void printI(String firstTag, String secondTag, String msg) {
        getInstance().print(firstTag, secondTag, msg, I);
    }


    public static void printE(String secondTag, String msg) {
        printE("", secondTag, msg);
    }

    public static void printE(String firstTag, String secondTag, String msg) {
        getInstance().print(firstTag, secondTag, msg, E);
    }

    private void print(String firstTag, String secondTag, String msg, int type) {
        StackTraceElement element = getStackElement();
        String TAG;
        if (!TextUtils.isEmpty(firstTag)) {
            TAG = firstTag + "-" + secondTag;
        } else {
            TAG = secondTag;
        }
        TAG = mModuleName + "-" + TAG;
        printConsoleInfo(element, TAG, type, msg);
        printFile(element, TAG, type, msg);
    }

    /**
     * 输出在控制台
     */
    private void printConsoleInfo(StackTraceElement element, String TAG, int type, String info) {
        String consoleInfo = info;
        if (element != null) {
            String fileNameStr = element.getFileName();
            String methodStr = element.getMethodName();
            String lineNumberStr = element.getLineNumber() + "";
            consoleInfo = getConsoleStackInfo(fileNameStr, methodStr, lineNumberStr) + info;
        }
        if (type == V) {
            Log.v(TAG, consoleInfo);
        } else if (type == W) {
            Log.w(TAG, consoleInfo);
        } else if (type == D) {
            Log.d(TAG, consoleInfo);
        } else if (type == I) {
            Log.i(TAG, consoleInfo);
        } else if (type == E) {
            Log.e(TAG, consoleInfo);
        }
    }

    /**
     * 日志输出在文件中
     */
    private void printFile(StackTraceElement element, String TAG, int type, String info) {
        String fileInfo = info;
        if (element != null) {
            String fileName = element.getFileName();
            String methodStr = element.getMethodName();
            String lineNumberStr = element.getLineNumber() + "";
            fileInfo = getPlusBrackets(getLogLevelStr(type)) + " " + getPlusBrackets(
                    TAG + "-" + fileName + ":" + lineNumberStr) +
                    " " + getPlusBrackets("Method-" + methodStr) + " " + getPlusBrackets(fileInfo);
        }
        saveLogFile(fileInfo);
    }

    private StackTraceElement getStackElement() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int methodCount = 1;
        int stackOffset = getStackOffset(trace);
        if (methodCount + stackOffset > trace.length) {
            methodCount = trace.length - stackOffset - 1;
        }
        for (int i = methodCount; i > 0; i--) {
            int stackIndex = i + stackOffset;
            if (stackIndex >= trace.length) {
                continue;
            }
            return trace[stackIndex];
        }
        return null;
    }

    private String getPlusBrackets(String text) {
        return "[" + text + "]";
    }

    private String getWriteFileInfo(String method, String lineNumber) {
        String methodStr = formatStringLength(method, 20, true);
        String lineStr = formatStringLength(lineNumber + "", 4, true);
        return "Method-" + methodStr + " Line-" + lineStr + ": ";
    }

    private String getConsoleStackInfo(String fileName, String method, String lineNumber) {
        return " Method: " + method + "  Line: (" + fileName + ":" + lineNumber + ")" + "  Info: ";
    }

    /**
     * @param str         需要格式的字符串
     * @param maxLength   输出的最大长度
     * @param isAlignLeft true 左对齐; false 右对齐
     * @return 格式化后的字符串
     */
    private String formatStringLength(String str, int maxLength, boolean isAlignLeft) {
        String realStr = "";
        if (str.length() > maxLength) {
            realStr = str.substring(maxLength);
        } else {
            String alignChar = "";
            if (isAlignLeft) {
                alignChar = "-";
            }
            String format = "%" + alignChar + maxLength + "s";
            realStr = String.format(format, str);
        }
        return realStr;
    }

    /**
     * 获取日志等级字符串
     */
    private String getLogLevelStr(int level) {
        String logLevelStr = "";
        switch (level) {
            case V:
                logLevelStr = "VERBOSE";
                break;
            case W:
                logLevelStr = "WARN";
                break;
            case D:
                logLevelStr = "DEBUG";
                break;
            case I:
                logLevelStr = "INFO";
                break;
            case E:
                logLevelStr = "ERROR";
                break;
        }
        return logLevelStr;
    }

    private String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    private int getStackOffset(StackTraceElement[] trace) {
        for (int i = 2; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(LogUtils.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

    /**
     * 保存Log至文件中
     *
     * @param content 日志内容
     */
    private void saveLogFile(String content) {
        if (DEBUG_MODE) {
            try {
                File logFile = new File(mLogPath);
                if (!logFile.exists()) {
                    logFile.mkdirs();
                }
                String name =
                        logFile.getAbsolutePath() + File.separator + LOG + "_" + mModuleName + "_"
                                + getLogDate() + ".txt";
                RandomAccessFile randomFile = new RandomAccessFile(name, "rw");
                long fileLength = randomFile.length();
                randomFile.seek(fileLength);
                randomFile.writeBytes(getPlusBrackets(getLogDatetime()) + " " + content + "\r\n");
                randomFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getLogDatetime() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", new Locale("en"));
        return formatter.format(new Date());
    }

    private static String getLogDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale("en"));
        return formatter.format(new Date());
    }


}
