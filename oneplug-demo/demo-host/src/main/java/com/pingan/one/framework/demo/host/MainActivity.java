package com.pingan.one.framework.demo.host;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pingan.oneplug.api.HostCallback;
import com.pingan.oneplug.api.ILoginCallback;
import com.pingan.oneplug.api.TargetActivator;
import com.pingan.oneplug.pm.IPackageDeleteObserver;
import com.pingan.oneplug.pm.MAPackageManager;

import java.io.File;
import java.io.FileOutputStream;

//import android.support.design.widget.FloatingActionButton;
//import com.paic.hyperion.core.hfasynchttp.http.HFRequestParam;
//import com.pingan.one.framework.demo.host.http.ADAsyncHttpCilentManager;
//import com.pingan.one.framework.demo.host.http.IADAsyncHttpClientListener;

public class MainActivity extends Activity {


    private String url = "http://10.20.18.148/desktop/index.php?share/fileDownload&user=anydoor&sid=MNiVWsty";
    String ss;
    FileOutputStream output;
//
//    static {
//        //加载库文件
//        System.loadLibrary("hfenginelib");
//        System.loadLibrary("nativemodulelib");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // 新接口替换，初始化任意门参数
//        AnydoorInfo anydoorInfo = new AnydoorInfo();
//        anydoorInfo.appId = "PA1231234";
//        anydoorInfo.appVersion = "1.0";
//        anydoorInfo.environment = "stg2";
//        anydoorInfo.logState = "";
//        PAAnydoor.getInstance().initAnydoorInfo(this, anydoorInfo);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//            }
//        });
        MAPackageManager.getInstance(MainActivity.this).installBuildinApps();//内部安装（assets/oneplug）
        //MAPackageManager.getInstance(MainActivity.this).installApkFile("path");//外部安装


        HostCallback.getInstance().setILonginCallback(new ILoginCallback() {
            @Override
            public void login(String s) {
                Toast.makeText(MainActivity.this.getApplicationContext(), "stg=" + s, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("packageName", s);
                MainActivity.this.startActivity(intent);
            }
        });
//
//       final  File path = Environment.getExternalStorageDirectory();
//        HFRequestParam hfRequestParam = new HFRequestParam();
//        hfRequestParam.addDownloadFile(new File(path.getPath() + "/" + "com.pingan.driverway.apk"));
//
//        ADAsyncHttpCilentManager.getInstance().sendPostRequset(url, hfRequestParam, new IADAsyncHttpClientListener() {
//            @Override
//            public void onSuccess(int statusCode, Map<String, List<String>> headers, String bytesContent) {
//
////                String path = MainActivity.this.getFilesDir().getAbsolutePath();
//
////                ss = path.getPath() + "/" + "com.pingan.driverway.apk";
////                Log.e("zl", "ss = " + ss);
////                delAPK(ss);
//////                if (!isAPKExistence(ss)) {
//////                    File f = new File(ss);
//////                    f.mkdirs();
//////                }
////                saveAPK(bytesContent.getBytes(), ss);
//
//                Log.e("zl","ok!");
//
//            }
//
//            @Override
//            public void onFailure(Throwable
//                                          error, Map<String, List<String>> headers, String bytesContent) {
//
//            }
//
//            @Override
//            public void onProgress(long l, long l1) {
//                Log.e("zl", "l = " + l);
//                Log.e("zl", "l1 = " + l1);
//            }
//        });


//        try {
//            URL u=new URL(url);
//            HttpURLConnection conn = (HttpURLConnection)u.openConnection();
//            File path = Environment.getExternalStorageDirectory();
//
//            ss = path + "/" + "com.pingan.driverway.apk";
//            delAPK(ss);
//            File file=new File(ss);
//            if(!file.exists()){
//              file.createNewFile();
//            }
//
//
//            InputStream input=conn.getInputStream();
//             output=new FileOutputStream(file);
//            //读取大文件
//            byte[] buffer=new byte[4*1024];
//            while(input.read(buffer)!=-1){
//                output.write(buffer);
//            }
//            output.flush();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally{
//            try {
//                output.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Log.e("zl","写入完成！");
//        }


        findViewById(R.id.bt_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                APKDownloadManager.getInstance().downloadAPK(getApplicationContext(), "", url);
//                String s = "com.test.sotg,http://www.baidu.com/";
//                StringTokenizer st = new StringTokenizer(s, ",");
//                while (st.hasMoreTokens()) {
//                    Log.e("zl", st.nextToken());
//                }

//                File path = Environment.getExternalStorageDirectory();
////                String path = MainActivity.this.getFilesDir().getAbsolutePath();
//                ss = path.getPath() + "/" + "com.pingan.driverway.apk";
//                Log.e("zl","ss = "+ss);
//                MAPackageManager.getInstance(MainActivity.this).installApkFile(ss, new IInstallCallBack() {
//
//                    @Override
//                    public void onPacakgeInstalled(String s) {
//                        Log.e("zl", "ok");
//                        TargetActivator.loadTargetAndRun(MainActivity.this, "com.pingan.driverway");
//                    }
//
//                    @Override
//                    public void onPackageInstallFail(String s, String s1) {
//                        Log.e("zl", "fail");
//                    }
//                });



                if (MAPackageManager.getInstance(MainActivity.this).isPackageInstalled("com.pingan.driverway")) {
                    TargetActivator.loadTargetAndRun(MainActivity.this, "com.pingan.driverway");
                } else {
                    Toast.makeText(MainActivity.this, "该插件木有安装", Toast.LENGTH_LONG).show();
                }


            }
        });

        findViewById(R.id.bt_parameter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Map<String,String> map = new HashMap<String, String>();
//                map.put("1","1");
//                map.put("2","2");
//                map.put("3","3");
//
//                TargetActivator.setParameter(map);

//                MAPackageManager.getInstance(MainActivity.this).installBuildinApps();

                if (MAPackageManager.getInstance(MainActivity.this).isPackageInstalled("com.pingan.one.framework.demo.plugin")) {
                    TargetActivator.loadTargetAndRun(MainActivity.this, "com.pingan.one.framework.demo.plugin");
                } else {
                    Toast.makeText(MainActivity.this, "该插件木有安装", Toast.LENGTH_LONG).show();
                }

//                if (MAPackageManager.getInstance(MainActivity.this).isPackageInstalled("com.pingan.anydoor.demoapkfirst")) {
//                    TargetActivator.loadTargetAndRun(MainActivity.this, "com.pingan.anydoor.demoapkfirst.apk");
//                } else {
//                    Toast.makeText(MainActivity.this, "该插件木有安装", Toast.LENGTH_LONG).show();
//                }


            }
        });

        findViewById(R.id.bt_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MAPackageManager.getInstance(MainActivity.this).deletePackage("com.pingan.one.framework.demo.plugin", new IPackageDeleteObserver() {

                    @Override
                    public void packageDeleted(String s, int i) {
                        Log.e("zl5711", "s = " + s);
                        Log.e("zl5711", "i = " + i);
                    }
                }, true);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void delAPK(String apkPath) {
        File f = new File(apkPath);
        if (f.exists()) {
            f.delete();
        }
    }

    private void saveAPK(byte[] bytesContent, String apkPath) {
        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            file = new File(apkPath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytesContent);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fileOutputStream) {
                    fileOutputStream.close();
                    fileOutputStream.flush();
                }
                if (null != file) {
                    file = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("zl","写入完成！");
        }
    }

    public boolean isAPKExistence(String apkPath) {
        File f = new File(apkPath);
        if (f.exists()) {
            return true;
        }
        return false;
    }

}
