///*
// * Copyright (C) 2014 Qiujuer <qiujuer@live.cn>
// * WebSite http://www.qiujuer.net
// * Created 11/24/2014
// * Changed 01/14/2015
// * Version 2.0.0
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *         http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.pingan.one.framework.demo.host.http;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.os.SystemClock;
//
//import com.paic.hyperion.core.hflog.HFLogger;
//
//import java.util.LinkedList;
//import java.util.Queue;
//
///**
// * 管理同步异步handler
// */
//final class UIKitHandlerPoster extends Handler {
//    private static final String TAG = "UIKitHandlerPoster";
//    private static final int ASYNC = 0x1;
//    private static final int SYNC = 0x2;
//    private final Queue<Runnable> mAsyncPool;
//    private final Queue<UIKitSyncPost> mSyncPool;
//    private final int mMaxMillisInsideHandleMessage;
//    private boolean isAsyncActive;
//    private boolean isSyncActive;
//
//    UIKitHandlerPoster(Looper looper, int maxMillisInsideHandleMessage) {
//        super(looper);
//        this.mMaxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
//        mAsyncPool = new LinkedList<Runnable>();
//        mSyncPool = new LinkedList<UIKitSyncPost>();
//    }
//
//    void dispose() {
//        this.removeCallbacksAndMessages(null);
//        this.mAsyncPool.clear();
//        this.mSyncPool.clear();
//    }
//
//    void async(Runnable runnable) {
//        synchronized (mAsyncPool) {
//            mAsyncPool.offer(runnable);
//            if (!isAsyncActive) {
//                isAsyncActive = true;
//                if (!sendMessage(obtainMessage(ASYNC))) {
//                    HFLogger.e(TAG, "Could not send handler message");
//                }
//            }
//        }
//    }
//
//    void sync(UIKitSyncPost post) {
//        synchronized (mSyncPool) {
//            mSyncPool.offer(post);
//            if (!isSyncActive) {
//                isSyncActive = true;
//                if (!sendMessage(obtainMessage(SYNC))) {
//                    HFLogger.e(TAG, "Could not send handler message");
//                }
//            }
//        }
//    }
//
//    @Override
//    public void handleMessage(Message msg) {
//        try {
//            if (msg.what == ASYNC) {
//                boolean rescheduled = false;
//                try {
//                    long started = SystemClock.uptimeMillis();
//                    while (true) {
//                        Runnable runnable = mAsyncPool.poll();
//                        if (runnable == null) {
//                            synchronized (mAsyncPool) {
//                                // Check again, this time in synchronized
//                                runnable = mAsyncPool.poll();
//                                if (runnable == null) {
//                                    isAsyncActive = false;
//                                    return;
//                                }
//                            }
//                        }
//                        runnable.run();
//                        long timeInMethod = SystemClock.uptimeMillis() - started;
//                        if (timeInMethod >= mMaxMillisInsideHandleMessage) {
//                            if (!sendMessage(obtainMessage(ASYNC))) {
//                                HFLogger.e(TAG, "Could not send handler message");
//                            }
//                            rescheduled = true;
//                            return;
//                        }
//                    }
//                } finally {
//                    isAsyncActive = rescheduled;
//                }
//            } else if (msg.what == SYNC) {
//                boolean rescheduled = false;
//                try {
//                    long started = SystemClock.uptimeMillis();
//                    while (true) {
//                        UIKitSyncPost post = mSyncPool.poll();
//                        if (post == null) {
//                            synchronized (mSyncPool) {
//                                // Check again, this time in synchronized
//                                post = mSyncPool.poll();
//                                if (post == null) {
//                                    isSyncActive = false;
//                                    return;
//                                }
//                            }
//                        }
//                        post.run();
//                        long timeInMethod = SystemClock.uptimeMillis() - started;
//                        if (timeInMethod >= mMaxMillisInsideHandleMessage) {
//                            if (!sendMessage(obtainMessage(SYNC))) {
//                                HFLogger.e(TAG, "Could not send handler message");
//                            }
//                            rescheduled = true;
//                            return;
//                        }
//                    }
//                } finally {
//                    isSyncActive = rescheduled;
//                }
//            } else {
//                super.handleMessage(msg);
//            }
//        } catch(Exception e) {
//            HFLogger.e(TAG, e);
//            super.handleMessage(msg);
//        }
//
//    }
//}