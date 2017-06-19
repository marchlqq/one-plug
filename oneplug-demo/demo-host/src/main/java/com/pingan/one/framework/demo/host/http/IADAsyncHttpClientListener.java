package com.pingan.one.framework.demo.host.http;

import org.apache.http.Header;

import java.util.List;
import java.util.Map;

/**
 * 异步网络回调监听接口
 * @author yuanbinzhou
 *
 */
public interface IADAsyncHttpClientListener {
	
	/**
	 * 
	 * @param statusCode   返回状态码
	 * @param bytesContent 请求返回的内容：字节数组
	 */
	void onSuccess(int statusCode, Map<String, List<String>> headers,String bytesContent);
	
	/**
	 * 
	 * @param error      错误
	 * @param bytesContent 错误信息
	 */
	void onFailure(Throwable error, Map<String, List<String>> headers,String bytesContent);

	void onProgress(long l, long l1);
	
}
