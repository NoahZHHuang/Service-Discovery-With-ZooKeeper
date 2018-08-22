package com.noah.with_curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorHelper {
	
	 public static CuratorFramework getCuratorClient() {
		 String zkAddress = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
	     System.out.println("create curator client:"+zkAddress);
	     return CuratorFrameworkFactory.newClient(zkAddress, new ExponentialBackoffRetry(1000, 3));
	 }
}
