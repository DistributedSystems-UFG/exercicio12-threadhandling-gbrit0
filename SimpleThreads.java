public class SimpleThreads {

    // Display a message, preceded by the name of the current thread
    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class MessageLoop
        implements Runnable {
        public void run() {
            String importantInfo[] = {
                "Mares eat oats",
                "Does eat oats",
                "Little lambs eat ivy",
                "A kid will eat ivy too"
            };
            try {
                for (int i = 0; i < importantInfo.length; i++) {
                    // Pause for 4 seconds
                    Thread.sleep(4000);
                    // Print a message
                    threadMessage(importantInfo[i]);
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done!");
            }
        }
    }

    private static class CpuIntensiveTask
        implements Runnable {
        public void run() {
            long iterations = 0;
            double accumulator = 0.0;

            try {
                while (true) {
                    for (int i = 0; i < 1_000_000; i++) {
                        accumulator += Math.sqrt(i) * Math.sin(i);
                    }
                    iterations++;

                    if (iterations % 5 == 0) {
                        threadMessage("CPU task iterations: " + iterations);
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
            } catch (InterruptedException e) {
                threadMessage("CPU task interrupted after " + iterations + " iterations. Result=" + accumulator);
            }
        }
    }

    

    public static void main(String args[])
        throws InterruptedException {

        // Delay, in milliseconds before we interrupt MessageLoop thread (default one hour)
        long patience = 1000 * 60 * 60 ;

        // If command line argument present, gives patience in seconds
        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("Argument must be an integer.");
                System.exit(1);
            }
        }

    threadMessage("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();
        Thread t = new Thread(new MessageLoop());

	// Put the MessageLoop thread to run
        t.start();

        threadMessage("Waiting for MessageLoop thread to finish");
	
        // loop until MessageLoop thread exits
        while (t.isAlive()) {
            threadMessage("Still waiting...");
            // Wait maximum of 1 second for MessageLoop thread to finish
            t.join(1000);
            if (((System.currentTimeMillis() - startTime) > patience) && t.isAlive()) {
                threadMessage("Tired of waiting!");
		// Force the interruption of the MainLoop thread
                t.interrupt();
                // ...and wait for it to finish -- shouldn't be long now 
                t.join();
            }
        }
        threadMessage("Finally!");

        long cpuTimeLimitMs = Math.min(patience, 10_000);
        threadMessage("Starting CPU-intensive thread (limit " + cpuTimeLimitMs + " ms)");
        long cpuStartTime = System.currentTimeMillis();
        Thread cpuThread = new Thread(new CpuIntensiveTask());
        cpuThread.start();

        while (cpuThread.isAlive()) {
            cpuThread.join(500);
            if (((System.currentTimeMillis() - cpuStartTime) > cpuTimeLimitMs) && cpuThread.isAlive()) {
                threadMessage("Time limit exceeded for CPU task. Interrupting...");
                cpuThread.interrupt();
                cpuThread.join();
            }
        }
        threadMessage("CPU-intensive task finished.");
    }
}
