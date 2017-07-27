package com.ytjojo.practice;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;

/**
 * Created by Administrator on 2017/7/27 0027.
 */

public class LockTest {
	Lock lock =new ReentrantLock();
	//private final Condition notFullCondition = lock.newCondition();
	AtomicBoolean atomicBoolean = new AtomicBoolean(false);
	Object o =new Object();
	int num = 0;
	boolean isssss;
	@Test
	public void testLock(){
		for (int i = 0; i <50 ; i++) {
			new Thread(){
				@Override public void run() {
					super.run();
					while (true){
						testNum();
						try {
							check();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void testNum(){
		num++;
		System.out.println("testNum"+num+isssss);
	}
	private void check() throws InterruptedException {
		if(num > 100){
			if(atomicBoolean.compareAndSet(false,true)){
				//lock.lock();
				System.out.println("lock"+num);
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				isssss = true;
				System.out.println("unlock"+num);
				synchronized (o){
					o.notifyAll();
				}
				//lock.unlock();
			}else{
				synchronized (o){
					o.wait();
				}
			}

		}
	}
}
