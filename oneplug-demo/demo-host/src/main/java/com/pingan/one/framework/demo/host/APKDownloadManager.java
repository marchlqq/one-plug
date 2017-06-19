//package com.pingan.one.framework.demo.host;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.text.TextUtils;
//import android.util.Log;
//
//
//import com.pingan.oneplug.api.TargetActivator;
//import com.pingan.oneplug.pm.MAPackageManager;
//
//import org.apache.http.Header;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//
///**
// * Created by zl on 2016/3/7.
// */
//public class APKDownloadManager {
//
//    private String url = "http://nj02all01.baidupcs.com/file/591c693da95e57d421ddb49b84c2ceb0?bkt=p3-1400591c693da95e57d421ddb49b84c2ceb05738654500000018bbc5&fid=505574562-250528-338404626801675&time=1457419366&sign=FDTAXGERLBH-DCb740ccc5511e5e8fedcff06b081203-pafFC6TLDoFr2FGQlxd4yMZzSn4%3D&to=nj2hb&fm=Nan,B,T,t&sta_dx=2&sta_cs=0&sta_ft=apk&sta_ct=0&fm2=Nanjing02,B,T,t&newver=1&newfm=1&secfm=1&flow_ver=3&pkey=1400591c693da95e57d421ddb49b84c2ceb05738654500000018bbc5&sl=74580046&expires=8h&rt=pr&r=354593801&mlogid=1568137176720228356&vuk=505574562&vbdid=3956686426&fin=com.pingan.one.framework.demo.plugin.apk&fn=com.pingan.one.framework.demo.plugin.apk&slt=pm&uta=0&rtype=1&iv=0&isw=0&dp-logid=1568137176720228356&dp-callid=0.1.1";
//    File file = null;
//    private APKDownloadManager() {
//
//    }
//
//    private static class SingleHolder {
//        private static final APKDownloadManager mInstance = new APKDownloadManager();
//    }
//
//    public static final APKDownloadManager getInstance() {
//        return SingleHolder.mInstance;
//    }
//
//    private InputStream getAPK(String pathURL) throws Exception {
//        return new BufferedInputStream(new FileInputStream(new File(pathURL)));
//    }
//
//    public void downloadAPK(final Context context, String pacage, String url) {
//
//        RequestParams requestParams = new RequestParams();
//        ADAsyncHttpCilentManager.getInstance().sendGetRequset(url, requestParams, new IADAsyncHttpClientListener() {
//            @Override
//            public void onSuccess(int i, Header[] headers, byte[] bytes) {
//                Log.e("zl", "onSuccess===>> start");
//                try {
//                    String appPath = context.getApplicationContext().getFilesDir().getParent();
//                    String openPlug = appPath + "/app_oneplug";
//                    File f = new File(openPlug);
//                    if (!f.exists()) {
//                        f.mkdirs();
//                    }
//                    String apkName = openPlug + "/com.pingan.one.framework.demo.plugin.apk";
//                    file = new File(apkName);
//                    if(file.exists()){
//                        file.delete();
//                        Log.e("zl", "onSuccess===>> delete file ");
//                    }
//                    FileOutputStream fileOutputStream = new FileOutputStream(file);
//                    fileOutputStream.write(bytes);
//                    fileOutputStream.close();
//                    fileOutputStream.flush();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                Log.e("zl", "onSuccess===>> ok");
//
//
//                String appPath = context.getApplicationContext().getApplicationContext().getFilesDir().getParent();
//                String openPlug = appPath + "/app_oneplug";
//                String apkName = openPlug + "/com.pingan.one.framework.demo.plugin.apk";
//
//                MAPackageManager.getInstance(context).installApkFile(apkName);
//
//                TargetActivator.loadTargetAndRun(context, "com.pingan.one.framework.demo.plugin");
//
//            }
//
//            @Override
//            public void onFailure(Throwable throwable, Header[] headers, byte[] bytes) {
//                Log.e("zl", "onFailure===>>" + bytes.toString());
//                if(null != file){
//                    file.delete();
//                }
//            }
//        });
//
//
////        String appPath = context.getApplicationContext().getFilesDir().getParent();
////        String openPlug = appPath+"/app_oneplug";
////        String apkName = openPlug+"/com.pingan.one.framework.demo.plugin.apk";
////
////        String sdPath = Environment.getExternalStorageDirectory().getPath();
////        String sdAPPPath = sdPath+"/test";
////        String sdAPKName = sdAPPPath+"/com.pingan.one.framework.demo.plugin.apk";
////
////        Log.i("zl","appPath="+appPath);
////        Log.i("zl","sdPath="+sdPath);
////
////        //if (!isDownloadAPK(context, pacage)) {
////            //download
////            InputStream inputStream = null;
////            OutputStream outputStream = null;
////            try {
////                inputStream = getAPK(sdAPKName);
////                File file = new File(apkName);
////                outputStream = new BufferedOutputStream(new FileOutputStream(file));
////                byte[] bytes = new byte[1024];
////                int temp;
////                while ((temp = inputStream.read(bytes)) != -1) {
////                    outputStream.write(bytes, 0, temp);
////                }
////                outputStream.flush();
////
////            } catch (Exception e) {
////                e.printStackTrace();
////            } finally {
////                try {
////                    if (inputStream != null) {
////                        inputStream.close();
////                    }
////                    if (outputStream != null) {
////                        outputStream.close();
////                    }
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////
////
////            saveAPK();
////       // }
//
//    }
//
//    private void saveAPK() {
//
//    }
//
//    private boolean isDownloadAPK(Context context, String pacage) {
//        String pa = getString("PluginAPKDownload", context, pacage, "");
//        if (TextUtils.isEmpty(pa)) {
//            return false;
//        }
//        return true;
//    }
//
//    private String getString(String name, Context context, String key,
//                             String defaultValue) {
//        if (context == null) return "";
//        SharedPreferences settings = context.getSharedPreferences(
//                name, Context.MODE_PRIVATE);
//        return settings.getString(key, defaultValue);
//    }
//
//
//}
