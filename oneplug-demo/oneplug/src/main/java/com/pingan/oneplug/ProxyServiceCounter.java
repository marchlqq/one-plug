package com.pingan.oneplug;

import java.util.HashMap;

import com.pingan.oneplug.ma.MAIntentService;
import com.pingan.oneplug.ma.MAService;

/**
 * 
 */
public class ProxyServiceCounter {
    private HashMap<String, Class<?>> serviceMap = new HashMap<String, Class<?>>();
    private static ProxyServiceCounter instance;
    private int serviceCount = 0;
    private int intentServiceCount = 0;

    private ProxyServiceCounter() {

    }

    public static synchronized ProxyServiceCounter getInstance() {
        if (instance == null) {
            instance = new ProxyServiceCounter();
        }
        return instance;
    }

    public Class<?> getAvailableService(Class<?> targetClass, boolean useExt) throws Exception {
        Class<?> proxyClass = serviceMap.get(targetClass.getName());
        if (proxyClass == null) {
            String serviceName = "";
            if (false == useExt) {
                if (MAIntentService.class.isAssignableFrom(targetClass)) {
                    if (intentServiceCount == 10) {
                        throw new Exception("can not find service,Has started 10 Intentservice");
                    }
                    serviceName = "com.pingan.oneplug.proxy.service.IntentServiceProxy";
                    intentServiceCount++;
                    serviceName = serviceName + intentServiceCount + "";

                } else if (MAService.class.isAssignableFrom(targetClass)) {
                    if (serviceCount == 10) {
                        throw new Exception("can not find service,Has started 10 service");
                    }
                    serviceName = "com.pingan.oneplug.proxy.service.ServiceProxy";
                    serviceCount++;
                    serviceName = serviceName + serviceCount + "";
                }
            } else {
                if (MAIntentService.class.isAssignableFrom(targetClass)) {
                    if (intentServiceCount == 10) {
                        throw new Exception("can not find service,Has started 10 Intentservice");
                    }
                    serviceName = "com.pingan.oneplug.proxy.service.IntentServiceProxyExt";
                    intentServiceCount++;
                    serviceName = serviceName + intentServiceCount + "";

                } else if (MAService.class.isAssignableFrom(targetClass)) {
                    if (serviceCount == 10) {
                        throw new Exception("can not find service,Has started 10 service");
                    }
                    serviceName = "com.pingan.oneplug.proxy.service.ServiceProxyExt";
                    serviceCount++;
                    serviceName = serviceName + serviceCount + "";
                }            	
            }
            proxyClass = Class.forName(serviceName);
            serviceMap.put(targetClass.getName(), proxyClass);

        }
        return proxyClass;
    }


}
