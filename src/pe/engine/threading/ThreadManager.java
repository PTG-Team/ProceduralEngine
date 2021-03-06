package pe.engine.threading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pe.util.Timer;

public class ThreadManager {

	public final int TIMEOUT_LENGTH = 2;
	public final int CORES = Runtime.getRuntime().availableProcessors();

	/*
	 * Threads for: Rendering 1 Updating 1 -> CORES Main/Scheduler/Monitor
	 * Thread 0 (Normally 1, but the main thread is created separate from the
	 * thread pool) Networking Thread 1 Audio Thread 1
	 * 
	 * Total 4 -> 3 + CORES
	 */

	public final int MINIMUM_THREADS = 4;
	public final int MAXIMUM_THREADS = 3 + CORES;
	private ThreadPoolExecutor threadPool;
	private Timer timeout;

	public ThreadManager() {
		this.threadPool = new ThreadPoolExecutor(MINIMUM_THREADS, MAXIMUM_THREADS, TIMEOUT_LENGTH, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(MAXIMUM_THREADS));
		this.timeout = new Timer(TIMEOUT_LENGTH);

		timeout.start();
		MasterThread.println("Thread Manager", "Starting Thread Manager");
	}

	public String getCurrentStatus() {
		return String.format(
				"Cores: %d, Active Threads: %d/%d, Completed Tasks: %d/%d, Shutting Down: %s, Terminated: %s", CORES,
				threadPool.getActiveCount(), threadPool.getPoolSize(), threadPool.getCompletedTaskCount(),
				threadPool.getTaskCount(), threadPool.isShutdown(), threadPool.isTerminated());
	}

	public void addTask(Runnable task) {
		threadPool.execute(task);
	}

	/**
	 * Removes the task from the <code>Thread Manager</code>'s list of tasks to
	 * execute. Will throw an {@link IllegalArgumentException} if the task could
	 * not be removed.
	 * 
	 * @param task
	 *            The task to remove from the Thread Manager
	 * 
	 * @throws IllegalArgumentException
	 *             if the <code>task</code> could not be removed.
	 * 
	 * @see #threadPool
	 * @see IllegalArgumentException
	 * 
	 * @since 1.0
	 */
	public void removeTask(Runnable task) {
		if (!threadPool.remove(task))
			throw new IllegalArgumentException("The Runnable " + task + " could not be removed from the Task Manager.");
	}

	public boolean threadsTerminated() {
		if (threadPool.getActiveCount() > 0) {
			timeout.start();
		} else if (timeout.delayPassed()) {
			threadPool.shutdown();
			MasterThread.println("Thread Manager", "Shutting Down Thread Manager");
		}

		return threadPool.isTerminated();
	}
}
