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
//import com.paic.hyperion.core.hflog.HFLogger;
//
///**
// * 同步post
// */
//final class UIKitSyncPost {
//    private Runnable mRunnable;
//    private boolean isEnd = false;
//
//
//    UIKitSyncPost(Runnable runnable) {
//        this.mRunnable = runnable;
//    }
//
//    public void run() {
//        synchronized (UIKitSyncPost.class) {
//            if (!isEnd) {
//                mRunnable.run();
//                isEnd = true;
//                try {
//                    this.notifyAll();
//                } catch (Exception e) {
//                    HFLogger.e(e);
//                }
//            }
//        }
//    }
//
//    public void waitRun() {
//        synchronized (UIKitSyncPost.class) {
//            if (!isEnd) {
//                try {
//                    this.wait();
//                } catch (InterruptedException e) {
//                    HFLogger.e(e);
//                }
//            }
//        }
//    }
//
//    public void waitRun(int time, boolean cancel) {
//        synchronized (UIKitSyncPost.class) {
//            if (!isEnd) {
//                try {
//                    this.wait(time);
//                } catch (InterruptedException e) {
//                    HFLogger.e(e);
//                } finally {
//                    if (!isEnd && cancel)
//                        isEnd = true;
//                }
//            }
//        }
//    }
//}
