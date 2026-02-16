import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MasterSocket {

    static final int[] tab_port = {25545,25546,25547,25548,25549,25550,25551,25552,25553};
    static final String ip = "127.0.0.1";

    static List<BufferedReader> readers = new ArrayList<>();
    static List<PrintWriter> writers = new ArrayList<>();
    static List<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        // 🔎 Connexion dynamique
        for (int port : tab_port) {
            try {
                Socket socket = new Socket(ip, port);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                PrintWriter writer = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);

                sockets.add(socket);
                readers.add(reader);
                writers.add(writer);

                System.out.println("Connected to worker on port " + port);
            }
            catch (IOException e) {
                System.out.println("No worker on port " + port);
            }
        }

        int connectedWorkers = sockets.size();

        if (connectedWorkers == 0) {
            System.out.println("No workers available. Exiting.");
            return;
        }

        System.out.println("\nTotal connected workers: " + connectedWorkers);

        // ⚙ Tous les nombres de workers possibles
        int[] workers = generateWorkerSteps(connectedWorkers);

        int[] strongSizes = {12_000_000, 48_000_000, 120_000_000};
        int weakIterPerWorker = 6_000_000;

        warmup(1_000_000, 1);

        runStrongScalingCSV(workers, strongSizes);
        runWeakScalingCSV(workers, weakIterPerWorker);

        // 🔚 Fermeture propre
        for (int i = 0; i < connectedWorkers; i++) {
            writers.get(i).println("END");
            readers.get(i).close();
            writers.get(i).close();
            sockets.get(i).close();
        }

        System.out.println("\nCSV générés : socket_strong_scaling.csv et socket_weak_scaling.csv");
    }


    private static int[] generateWorkerSteps(int max) {
        int[] steps = new int[max];
        for (int i = 0; i < max; i++) {
            steps[i] = i + 1;
        }
        return steps;
    }

    private static void warmup(int iterations, int workers) throws Exception {
        for (int i = 0; i < workers; i++)
            writers.get(i).println(iterations);

        for (int i = 0; i < workers; i++)
            readers.get(i).readLine();
    }

    private static void runStrongScalingCSV(int[] workers, int[] problemSizes) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter("socket_strong_scaling.csv"))) {
            out.println("ntot,workers,time_ms,pi");

            for (int ntot : problemSizes) {
                System.out.println("\n=== STRONG Ntot = " + ntot + " ===");

                for (int p : workers) {
                    int iterPerWorker = ntot / p;
                    long total = 0;

                    long start = System.nanoTime();

                    for (int i = 0; i < p; i++)
                        writers.get(i).println(iterPerWorker);

                    for (int i = 0; i < p; i++)
                        total += Long.parseLong(readers.get(i).readLine());

                    long stop = System.nanoTime();
                    double timeMs = (stop - start) / 1_000_000.0;

                    double pi = 4.0 * total / ntot;

                    out.println(ntot + "," + p + "," + timeMs + "," + pi);

                    System.out.println("Workers=" + p +
                            " | Iter/worker=" + iterPerWorker +
                            " | Time(ms)=" + timeMs +
                            " | Pi ≈ " + pi);
                }
            }
        }
    }

    private static void runWeakScalingCSV(int[] workers, int iterPerWorker) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter("socket_weak_scaling.csv"))) {
            out.println("workers,iter_per_worker,ntot,time_ms,pi");

            System.out.println("\n=== WEAK scaling Iter/worker = " + iterPerWorker + " ===");

            for (int p : workers) {
                long total = 0;
                long ntot = (long) iterPerWorker * p;

                long start = System.nanoTime();

                for (int i = 0; i < p; i++)
                    writers.get(i).println(iterPerWorker);

                for (int i = 0; i < p; i++)
                    total += Long.parseLong(readers.get(i).readLine());

                long stop = System.nanoTime();
                double timeMs = (stop - start) / 1_000_000.0;

                double pi = 4.0 * total / ntot;

                out.println(p + "," + iterPerWorker + "," + ntot + "," + timeMs + "," + pi);

                System.out.println("Workers=" + p +
                        " | Ntot=" + ntot +
                        " | Time(ms)=" + timeMs +
                        " | Pi ≈ " + pi);
            }
        }
    }
}
