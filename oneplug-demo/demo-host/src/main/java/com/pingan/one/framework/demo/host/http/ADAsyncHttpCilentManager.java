//package com.pingan.one.framework.demo.host.http;
//
//import android.os.AsyncTask;
//import android.text.TextUtils;
//
//
//
////import com.paic.hyperion.core.hfasynchttp.http.HFHttpClient;
////import com.paic.hyperion.core.hfasynchttp.http.HFProgressCallback;
////import com.paic.hyperion.core.hfasynchttp.http.HFRequestParam;
////import com.paic.hyperion.core.hflog.HFLogger;
//
//
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 网络请求管理器
// *
// * @author yuanbinzhou
// *
// */
//public class ADAsyncHttpCilentManager {
//    private static final String TAG = "ADAsyncHttpCilentManager";
//
//    private HFHttpClient mClient = new HFHttpClient();
//
//    /**
//     * 私有化构造函数
//     */
//    private ADAsyncHttpCilentManager(){
//        mClient.setTimeOut(10 * 1000);
//    }
//
//    /**
//     * 单利
//     */
//    private static class SingletonHolder {
//        private static final ADAsyncHttpCilentManager instance = new ADAsyncHttpCilentManager();
//    }
//
//    /**
//     * 获取单例
//     *
//     * @return
//     */
//    public static final ADAsyncHttpCilentManager getInstance() {
//        return SingletonHolder.instance;
//    }
//
//    /**
//     * get请求
//     *
//     * @param url
//     *            ：url地址
//     * @param requestParams
//     *            ：请求参数
//     * @param listener
//     *            ：请求回调
//     * @return
//     */
//    public boolean sendGetRequset(final String url, final HFRequestParam requestParams,
//            final IADAsyncHttpClientListener listener) {
//        if (TextUtils.isEmpty(url) || listener == null) {
//            return false;
//        }
//        mClient.setTimeOut(10 * 1000);
//        UIKit.runOnMainThreadAsync(new Runnable() {
//            @Override
//            public void run() {
//                ADAsyncHttpResponseHandler responseHandler = new ADAsyncHttpResponseHandler(listener);
//                try {
//                    mClient.get(url, requestParams, responseHandler);
//                } catch (Exception e) {
//                    HFLogger.e(TAG, "request url=" + url);
//                    HFLogger.e(e);
//                }
//            }
//        });
//
//        return true;
//    }
//
//    /**
//     * Post请求
//     *
//     * @param url
//     *            ：url地址
//     * @param requestParams
//     *            ：请求参数
//     * @param listener
//     *            ：请求回调
//     * @return
//     */
//    public boolean sendPostRequset(final String url, final HFRequestParam requestParams,
//            final IADAsyncHttpClientListener listener) {
//        if (TextUtils.isEmpty(url) || listener == null) {
//            return false;
//        }
//        mClient.setTimeOut(10 * 1000);
////        UIKit.runOnMainThreadAsync(new Runnable() {
////            @Override
////            public void run() {
//                ADAsyncHttpResponseHandler responseHandler = new ADAsyncHttpResponseHandler(listener);
//                try {
//                    mClient.download(url, requestParams, responseHandler);
//                } catch (Exception e) {
//                    HFLogger.e(TAG, "request url=" + url);
//                    HFLogger.e(e);
//                }
////            }
////        });
//
//        return true;
//    }
//
//    /**
//     * 异步网络请求返回处理
//     *
//     * @author yuanbinzhou
//     *
//     */
//    private static class ADAsyncHttpResponseHandler extends HFProgressCallback {
//        private IADAsyncHttpClientListener mListener;
//        public ADAsyncHttpResponseHandler(IADAsyncHttpClientListener listener) {
//            mListener = listener;
//        }
//
////        @Override
////        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
////            new HandleTask(mListener,statusCode,responseBody,headers).execute();
////        }
////
////        @Override
////        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
////            new HandleTask(mListener, error, responseBody,headers).execute();
////        }
//
//        @Override
//        public void onProgress(long l, long l1) {
//            mListener.onProgress(l,l1);
////            new HandleTask(mListener,l,l1);
//        }
//
//        @Override
//        public void onSuccess(String s, Map<String, List<String>> map, int i) {
//            new HandleTask(mListener,i,s,map).execute();
//        }
//
//        @Override
//        public void onFail(String s, Map<String, List<String>> map, int i) {
//            new HandleTask(mListener, i,s,map).execute();
//        }
//    }
//
//    /**
//     * 异步处理网络回调结果处理任务
//     *
//     *
//     */
//    private static class HandleTask extends AsyncTask<Void, Void, String> {
//        private IADAsyncHttpClientListener mListener;
//        private Object mCode = null;
//        private String mContent = null;
//        private  Map<String, List<String>> mHeaders = null;
//
//        private long mL;
//        private long mLll;
//
//        /**
//         *
//         * @param listener
//         *            回调函数
//         * @param code
//         *            返回码
//         * @param content
//         *            返回内容
//         */
//        public HandleTask(IADAsyncHttpClientListener listener, Object code,
//                          String content, Map<String, List<String>> headers) {
//            mListener = listener;
//            mCode = code;
//            mContent = content;
//            if(null == headers){
//                headers = new HashMap<String, List<String>>();
//            }
//            mHeaders = headers;
//        }
//
//        public HandleTask(IADAsyncHttpClientListener listener,long l,long ll){
//            mListener = listener;
//            mL = l;
//            mLll = ll;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            try {
//                if (null != mListener && null != mCode ) {
//                    if (mCode instanceof Throwable) {
//                        if(null != mContent) {
//                            mListener.onFailure((Throwable) mCode, mHeaders,  mContent);
//                        } else {
//                            mListener.onFailure((Throwable) mCode,mHeaders, null);
//                        }
//                    } else {
//                        mListener.onSuccess(((Integer)mCode).intValue(),mHeaders, mContent);
//                    }
//                }else if(null != mListener && -1 != mL  || -1 != mLll){
//                    mListener.onProgress(mL,mLll);
//                }
//
//
//
//            } catch (Exception e) {
//                HFLogger.e(TAG, e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            mListener = null;
//            mCode = null;
//            mContent = null;
//            mL = -1;
//            mLll = -1;
//        }
//    }
//
//}
