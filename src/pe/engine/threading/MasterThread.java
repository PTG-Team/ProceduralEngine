package pe.engine.threading;

import java.io.PrintStream;

import pe.engine.error.ErrorHandler;
import pe.engine.graphics.main.Window;
import pe.engine.input.KeyHandler;
import pe.engine.main.InitializationProcesses;
import pe.util.Timer;
import pe.util.Util;

public class MasterThread {

	private static PrintStream console = System.out;

	/**
	 * Creates a new object with a <code>Print Stream</code> which errors are
	 * printed to.
	 * 
	 * @see ErrorHandler
	 * 
	 * @since 1.0
	 */
	private static ErrorHandler errorHandler = new ErrorHandler();

	private static Timer managerPrintCycle = new Timer(1);

	private static ThreadManager threadManager = null;
	private static RenderingThread renderer = null;
	private static NetworkingThread networking = null;
	private static AudioThread audio = null;

	private static boolean running = false;

	public static void main(String[] args) {
		Timer shutdownTimer = new Timer(3);

		managerPrintCycle.start();
		println("Master Thread", "Procedural Engine Main Thread Started");
		running = true;

		println("Master Thread", "Creating the Thread Manager");
		threadManager = new ThreadManager();

		InitializationProcesses.init();

		shutdownTimer.start();
		
		Window window = new Window(700, 400, "Test", true, true, new KeyHandler());

		while (!threadManager.threadsTerminated()) {
			if (managerPrintCycle.delayPassed()) {
				println("Thread Manager", threadManager.getCurrentStatus());
			}

			if (shutdownTimer.delayPassed()) {
				shutdown();
			}
			
			window.update();
		}
		
		InitializationProcesses.deinit();

		println("Master Thread", "Procedural Engine Main Thread Terminated");
	}

	/**
	 * Returns true if the Master Thread is still running and false if it is
	 * not. The most common way to stop running is to call
	 * <code>shutdown</code>, which will stop the running of the Master Thread.
	 * 
	 * @return whether or not the Master Thread is running.
	 * 
	 * @see #running
	 * @see #shutdown()
	 */
	public static boolean isRunning() {
		return running;
	}

	public synchronized static void shutdown() {
		if (!running)
			return;
		running = false;
		println("Master Thread", "Shutting down all processes");
		println("Master Thread", "Waiting for all Threads to Terminate");
	}

	/**
	 * Adds a new <code>Runnable</code> task to the <code>Thread Manager</code>
	 * for it to run when it can. This method is most often used to add update
	 * tasks.
	 * 
	 * @param task
	 *            The task that should be added to the
	 *            <code>Thread Manager</code> to be executed.
	 * 
	 * @see #threadManager
	 * @see ThreadManager
	 * @see Runnable
	 * 
	 * @since 1.0
	 */
	public static void addTask(Runnable task) {
		threadManager.addTask(task);
	}

	/**
	 * Sets the <code>Master Threads</code>'s Rendering Thread which should be
	 * used to render scenes. Allows only one Rendering Thread to be added.
	 * 
	 * @param renderingThread
	 * 
	 * @throws IllegalArgumentException
	 *             if another <code>Rendering Thread</code> is attempted to be
	 *             added after one has already been added.
	 * 
	 * @see #threadManager
	 * @see #renderer
	 * @see RenderingThread
	 * 
	 * @since 1.0
	 */
	public static void addRenderingThread(RenderingThread renderingThread) {
		if (renderer != null)
			throw new IllegalArgumentException("Cannot add more than one Rendering Thread");

		renderer = renderingThread;
		threadManager.addTask(renderer);
	}

	/**
	 * Sets the <code>Master Threads</code>'s Networking Thread which should be
	 * used to do server-based updates. Allows only one Networking Thread to be
	 * added.
	 * 
	 * @param networkingThread
	 * 
	 * @throws IllegalArgumentException
	 *             if another <code>Networking Thread</code> is attempted to be
	 *             added after one has already been added.
	 * 
	 * @see #threadManager
	 * @see #networking
	 * @see NetworkingThread
	 * 
	 * @since 1.0
	 */
	public static void addNetworkingThread(NetworkingThread networkingThread) {
		if (networking != null)
			throw new IllegalArgumentException("Cannot add more than one Networking Thread");

		networking = networkingThread;
		threadManager.addTask(networking);
	}

	/**
	 * Sets the <code>Master Threads</code>'s Audio Thread which should be used
	 * to play audio sounds. Allows only one Audio Thread to be added.
	 * 
	 * @param audioThread
	 * 
	 * @throws IllegalArgumentException
	 *             if another <code>Audio Thread</code> is attempted to be added
	 *             after one has already been added.
	 * 
	 * @see #threadManager
	 * @see #audio
	 * @see AudioThread
	 * 
	 * @since 1.0
	 */
	public static void addAudioThread(AudioThread audioThread) {
		if (audio != null)
			throw new IllegalArgumentException("Cannot add more than one Audio Thread");

		audio = audioThread;
		threadManager.addTask(audio);
	}

	/**
	 * Returns the <code>ErrorHandler</code> object which will print errors
	 * encountered by GLFW.
	 * 
	 * @return the <code>Master Thread</code>'s <code>errorHandler</code>
	 *         object.
	 * 
	 * @see #errorHandler
	 * @see ErrorHandler
	 * 
	 * @since 1.0
	 */
	public static ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public static void println(String source, String msg) {
		String time = String.format("[%f", managerPrintCycle.getTime());
		String src = String.format("%s]:", source);
		console.println(String.format("%s\t%s", Util.alignStrings(time, "", src, 35), msg));
	}
}
