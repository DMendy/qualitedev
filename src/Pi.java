import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Approximates PI using the Monte Carlo method.  Demonstrates
 * use of Callables, Futures, and thread pools.
 */
public class Pi {

    private static void runStrongScalingCSV(Master master, int[] workers, int[] problemSizes) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter("strong_scaling.csv"))) {
            out.println("ntot,workers,iter_per_worker,time_ms");

            for (int ntot : problemSizes) {
                System.out.println("\n=== STRONG SCALING - Ntot = " + ntot + " ===");

                for (int p : workers) {
                    int iterPerWorker = ntot / p; // strong scaling
                    double t = master.doRun(iterPerWorker, p);

                    System.out.println("Workers=" + p +
                            " | Iter/worker=" + iterPerWorker +
                            " | Ntot=" + ((long) iterPerWorker * p) +
                            " | Time(ms)=" + t);

                    out.println(ntot + "," + p + "," + iterPerWorker + "," + t);
                }
            }
        }
    }

    private static void runWeakScalingCSV(Master master, int[] workers, int iterationsPerWorker) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter("weak_scaling.csv"))) {
            out.println("iter_per_worker,workers,ntot,time_ms");

            System.out.println("\n=== WEAK SCALING - Iter/worker = " + iterationsPerWorker + " ===");

            for (int p : workers) {
                double t = master.doRun(iterationsPerWorker, p);
                long ntot = (long) iterationsPerWorker * p;

                System.out.println("Workers=" + p +
                        " | Iter/worker=" + iterationsPerWorker +
                        " | Ntot=" + ntot +
                        " | Time(ms)=" + t);

                out.println(iterationsPerWorker + "," + p + "," + ntot + "," + t);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Master master = new Master();
        int[] workers = {1, 2, 4, 8};

        // STRONG: 3 tailles
        int[] strongSizes = {12_000_000, 48_000_000, 120_000_000};

        // WEAK: charge par thread constante
        int weakIterPerWorker = 6_000_000;

        // (Optionnel mais conseillé) warmup simple
        master.doRun(1_000_000, 1);

        runStrongScalingCSV(master, workers, strongSizes);
        runWeakScalingCSV(master, workers, weakIterPerWorker);

        System.out.println("\nCSV générés : strong_scaling.csv et weak_scaling.csv");
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