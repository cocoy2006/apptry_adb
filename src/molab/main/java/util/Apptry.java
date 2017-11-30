package molab.main.java.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Apptry {

	private static ServletContext servletContext;

	public static void setServletContext(ServletContext sc) {
		servletContext = sc;
	}

	public static ServletContext getServletContext() {
		return servletContext;
	}
	
	public static String getProperty(String file, String key) {
		try {
			Properties props = loadProperties(file);
			return props.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getProperty(InputStream is, String key) {
		try {
			Properties props = loadProperties(is);
			return props.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Properties loadProperties(String file) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			return loadProperties(is);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Properties loadProperties(InputStream is) {
		Properties props = new Properties();
		try {
			props.load(is);
			return props;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getUploadDirectory() {
		return servletContext.getRealPath("/upload/");
	}
	
	public static String getApkDirectory() {
		return getUploadDirectory().concat("/application/apk/");
	}
	
	public static String ajaxGet(String url) {
		// 构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		// 创建GET方法的实例
		GetMethod getMethod = new GetMethod(url);
		// 使用系统提供的默认的恢复策略
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		try {
			// 执行getMethod
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
			}
			// 读取内容
			byte[] responseBody = getMethod.getResponseBody();
			// 处理内容
			return new String(responseBody);
		} catch (HttpException e) {
			// 发生致命的异常，可能是协议不对或者返回的内容有问题
			System.out.println("Please check your provided http address!");
			e.printStackTrace();
		} catch (IOException e) {
			// 发生网络异常
			e.printStackTrace();
		} finally {
			// 释放连接
			getMethod.releaseConnection();
		}
		return null;
	}
}
