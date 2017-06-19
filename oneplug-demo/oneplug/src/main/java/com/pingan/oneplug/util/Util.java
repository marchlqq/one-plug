package com.pingan.oneplug.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.ProxyExt;

/**
 * 
 */
public final class Util {
    /** utility class private constructor*/
    private Util() { }
    /**
     * 读取 apk 文件的最后修改时间（生成时间），通过编译命令编译出来的apk第一个 entry 为 
     * META-INF/MANIFEST.MF  所以我们只读取此文件的修改时间可以。
     * 
     * 对于 eclipse 插件打包的 apk 不适用。文件 entry顺序不确定。
     * 
     * @param fis 
     * @throws IOException 
     * @return 返回 {@link SimpleDateTime}
     */
    public static SimpleDateTime readApkModifyTime(InputStream fis) throws IOException {
        
        int LOCHDR = 30; //header 部分信息截止字节 // SUPPRESS CHECKSTYLE
        int LOCVER = 4; //排除掉magic number 后的第四个字节，version部分 // SUPPRESS CHECKSTYLE
        int LOCTIM = 10; //最后修改时间 第10个字节。 // SUPPRESS CHECKSTYLE
        
        byte[] hdrBuf = new byte[LOCHDR - LOCVER];
        
        // Read the local file header.
        byte[] magicNumer = new byte[4]; // SUPPRESS CHECKSTYLE magic number
        fis.read(magicNumer);
        fis.read(hdrBuf, 0, hdrBuf.length);
        
        int time = peekShort(hdrBuf, LOCTIM - LOCVER);
        int modDate = peekShort(hdrBuf, LOCTIM - LOCVER + 2);
        
        SimpleDateTime cal = new SimpleDateTime();
        /*
         * zip中的日期格式为 dos 格式，从 1980年开始计时。
         */
        cal.set(1980 + ((modDate >> 9) & 0x7f), ((modDate >> 5) & 0xf), // SUPPRESS CHECKSTYLE magic number
                modDate & 0x1f, (time >> 11) & 0x1f, (time >> 5) & 0x3f, // SUPPRESS CHECKSTYLE magic number
                (time & 0x1f) << 1);  // SUPPRESS CHECKSTYLE magic number
        
        fis.skip(0);
        
        return cal;
    }
    
    /**
     * 从buffer数组中读取一个 short。
     * @param buffer buffer数组
     * @param offset 偏移量，从这个位置读取一个short。
     * @return short值
     */
    private static int peekShort(byte[] buffer, int offset) {
        short result = (short) ((buffer[offset + 1] << 8) | (buffer[offset] & 0xff)); // SUPPRESS CHECKSTYLE magic number
        
        return result & 0xffff; // SUPPRESS CHECKSTYLE magic number
    }
    
    
    
    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     * 
     * @param inputStream source file inputstream
     * @param destFile destFile
     * 
     * @return success return true
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        
        if (inputStream == null || destFile == null) {
            return false;
        }
        
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096]; // SUPPRESS CHECKSTYLE
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a directory recursively.
     *
     * @param directory  directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            String message =
                "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }
    
    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }
    
    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file  file or directory to delete, must not be <code>null</code>
     * @throws NullPointerException if the directory is <code>null</code>
     * @throws FileNotFoundException if the file was not found
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent){
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message =
                    "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * 比较两个签名是否相同
     * 
     * @param s1
     * @param s2
     * @return
     */
    public static int compareSignatures(Signature[] s1, Signature[] s2) {
        if (s1 == null) {
            return s2 == null ? PackageManager.SIGNATURE_NEITHER_SIGNED : PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
        }
        if (s2 == null) {
            return PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
        }
        HashSet<Signature> set1 = new HashSet<Signature>();
        for (Signature sig : s1) {
            set1.add(sig);
        }
        HashSet<Signature> set2 = new HashSet<Signature>();
        for (Signature sig : s2) {
            set2.add(sig);
        }
        // Make sure s2 contains all signatures in s1.
        if (set1.equals(set2)) {
            return PackageManager.SIGNATURE_MATCH;
        }
        return PackageManager.SIGNATURE_NO_MATCH;
    }

    /**
     * 获取host在MetaData里面声明的类实例
     * 
     * @return 类实例
     */
    public static Object getHostMetaDataClassInstance(Context ctx, String key) {
        Object object = null;
        ApplicationInfo hostAppInfo = null;
        try {
            hostAppInfo = ctx.getPackageManager()
                    .getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e1) {

        }

        if (hostAppInfo != null && hostAppInfo.metaData != null) {
            String clazz = hostAppInfo.metaData.getString(key);
            if (clazz != null && clazz.length() > 0) {
                try {
                    object = Class.forName(clazz).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return object;
    }
    
    /**
     * 获取host在Meta里面的键
     * 
     * @return 类实例
     */
    public static String getHostMetaData(Context ctx, String key) {
        String value = "";
        ApplicationInfo hostAppInfo = null;
        try {
            hostAppInfo = ctx.getPackageManager()
                    .getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e1) {

        }

        if (hostAppInfo != null && hostAppInfo.metaData != null) {
            value = hostAppInfo.metaData.getString(key);
        }
        return value;
    }    
    
    /**
     * 如果代理对象是在独立进程，则为intent添加extra标示
     * @param proxy 代理对象
     * @param intent intent
     * @return intnt
     */
    public static Intent genProxyExtIntent(Object proxy, Intent intent) {
        if (intent == null) {
            return intent;
        }
        boolean useProxy = false;
        if (proxy instanceof ProxyExt) {
            useProxy = true;
        }
        intent.putExtra(ProxyEnvironment.EXTRA_TARGET_PROXY_EXT, useProxy);
        return intent;
    }
    
    /**
     * 读取文件内容
     * @param context context
     * @param filename filename
     * @return 文件内容
     */
    public static String readFile(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str = "";
            StringBuffer sb = new StringBuffer();
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 写文件
     * @param context context
     * @param filename 文件名
     * @param content 写入的内容
     */
    public static void writeFile(Context context, String filename, String content) {
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, 
                    Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取当前进程的进程名
     * @param context context
     * @return 进程名称
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

}
