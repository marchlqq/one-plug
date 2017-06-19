package com.pingan.oneplug.ma;

import android.content.Intent;

public abstract class MAIntentService extends MAService {
    abstract public void onHandleIntent(Intent paramIntent);
    
    public MAIntentService(String name) {
    }
    
    public MAIntentService() {
    }
    
}
