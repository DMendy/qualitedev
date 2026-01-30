import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Approximates PI using the Monte Carlo method.  Demonstrates
 * use of Callables, Futures, and thread pools.
 */
public class Pi
{
    public static void main(String[] args) throws Exception {

        //faire un paragraphe explicatif de l'utilisation de la scalabilité forte
        int iterations = 6_000_000;
        int[] workers = {1, 2, 4, 8};
        Master master = new Master();
        double[] times = new double[workers.length];

        System.out.println("=== MEASUREMENTS ===");

        for (int i = 0; i < workers.length; i++) {
            times[i] = master.doRun(iterations, workers[i]);
            System.out.println("Workers=" + workers[i] +
                    " | Time(ms)=" + times[i]);
        }

        double T1 = times[0];

        System.out.println("\n=== SPEEDUP ===");
        for (int i = 0; i < workers.length; i++) {
            double speedup = T1 / times[i];
            System.out.println("Workers=" + workers[i] +
                    " | Speedup=" + speedup);
        }
    }
}

/**
 * Creates workers to run the Monte Carlo simulation
 * and aggregates the results.
 */
class Master {
    public double doRun(int totalCount, int numWorkers) throws InterruptedException, ExecutionException
    {

        long startTime = System.currentTimeMillis();
        long spstarttime = System.nanoTime();

        // Create a collection of tasks
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();
        for (int i = 0; i < numWorkers; ++i)
        {
            tasks.add(new Worker(totalCount));
        }

        // Run them and receive a collection of Futures
        ExecutorService exec = Executors.newFixedThreadPool(numWorkers);
        List<Future<Long>> results = exec.invokeAll(tasks);
        long total = 0;

        // Assemble the results.
        for (Future<Long> f : results)
        {
            // Call to get() is an implicit barrier.  This will block
            // until result from corresponding worker is ready.
            total += f.get();
        }
        double pi = 4.0 * total / totalCount / numWorkers;

        long stopTime = System.currentTimeMillis();
        long spstoptime = System.nanoTime();

        //System.out.println("\nPi : " + pi );
        //System.out.println("Error: " + (Math.abs((pi - Math.PI)) / Math.PI) +"\n");

        //System.out.println("Ntot: " + totalCount*numWorkers);
        //System.out.println("Available processors: " + numWorkers);
        double timeMs = (spstoptime - spstarttime) / 1_000_000.0;

        //System.out.println( (Math.abs((pi - Math.PI)) / Math.PI) +" "+ totalCount*numWorkers +" "+ numWorkers +" "+ (stopTime - startTime));

        exec.shutdown();
        return timeMs;
    }
}

/**
 * Task for running the Monte Carlo simulation.
 */
class Worker implements Callable<Long>
{
    private int numIterations;
    public Worker(int num)
    {
        this.numIterations = num;
    }

    @Override
    public Long call()
    {
        long circleCount = 0;
        Random prng = new Random ();
        for (int j = 0; j < numIterations; j++)
        {
            double x = prng.nextDouble();
            double y = prng.nextDouble();
            if ((x * x + y * y) < 1)  ++circleCount;
        }
        return circleCount;
    }
}