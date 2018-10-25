package plugins;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import plugins.workerPool.basicTask;

//带有一个队列
//会根据积压任务自动扩展工作线程,
//如果一个附加线程拿不到新任务，就会被消除，重新开始扩展进程(可考虑累积因素与清空)
public class workerPool {
	private ConcurrentLinkedQueue<basicTask> taskQueue;
	private int threadCount;
	private Lock lock;
	private Condition c;
	private int maxThread;
	
	public workerPool(int maxThread) {
		this.taskQueue = new ConcurrentLinkedQueue<basicTask>();
		this.threadCount = 1;
		this.lock = new ReentrantLock();
		c = lock.newCondition();
		this.maxThread = maxThread;
		new baseThread(this).start();
	}
	
	public void waitForNewTask() throws InterruptedException {
		this.c.await();
	}
	
	public void addTask(basicTask t) {
		this.taskQueue.offer(t);
		this.c.signal();
	}
	
	public basicTask getTask() {
		if(this.taskQueue.isEmpty()) {
			return null;
		}else{
			return (basicTask)this.taskQueue.poll();
		}
	}
	
	//压力不到则不会增加
	//增加时计数+1
	public void addByThread() {
		if(pressureTest()) {
			new byThread(this).start();
			synchronized(this) {
				this.threadCount++;
			}
		}
	}
	
	public void threadDie() {
		synchronized(this) {
			this.threadCount--;
		}
	}
	
	public boolean pressureTest() {
		synchronized(this) {
			double p = (double)this.taskQueue.size()/(double)this.threadCount;
			return p > 3 && this.threadCount<=this.maxThread;
		}
	}
	
	public static interface basicTask {
		//run的时候，立刻将session状态改为syncing
		void run();
		//取消时，run的内容将失效
		void cancel();
	}
	
}

//最基础的工作线程
//每完成一个任务后(一定要是完成后)，根据线程数和队列中的任务数计算压力，达到阈值则新增一个byThread
//取不到任务会被阻塞（怎么实现？add的时候给信号量？）
class baseThread extends Thread{
	protected workerPool masterPool;
	
	baseThread(workerPool wp){
		this.masterPool = wp;
	}
	
	@Override
	public void run() {
		while(true) {
			basicTask t = this.masterPool.getTask();
			if(t != null) {
				t.run();
				this.masterPool.addTask(t);
			} else {
				try {
					this.masterPool.waitForNewTask();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}

//压力大时的附加线程，同样计算压力增加线程
//但取不到任务时会被清除，即不保留空闲的byThread
class byThread extends baseThread{
	byThread(workerPool wp){
		super(wp);
	}
	
	@Override
	public void run() {
		while(true) {
			basicTask t = this.masterPool.getTask();
			if(t != null) {
				t.run();
				this.masterPool.addTask(t);
			} 
		}
	}
}
