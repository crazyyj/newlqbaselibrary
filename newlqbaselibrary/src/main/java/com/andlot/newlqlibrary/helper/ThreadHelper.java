package com.andlot.newlqlibrary.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadHelper {

	private static int mThreads = 5;
	private static ExecutorService threadPool;
	
	public static void runThread(Runnable r) {
		synchronized (r) {
			if (threadPool == null) {
				threadPool = Executors.newFixedThreadPool(mThreads);
			}
		}
		threadPool.execute(r);
	}

	public static void setThreads(int threads) {
		mThreads = threads;
	}
	
}
