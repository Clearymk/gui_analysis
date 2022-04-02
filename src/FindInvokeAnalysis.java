import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class FindInvokeAnalysis {
    private static final String analysisDir = "/Volumes/Clear/apk_pure/download";
    private static List<String> tasks = new ArrayList<>();
    private static final int MAX_THREADS = 20;

    private static void getTasks() {
        File analysisFile = new File(analysisDir);
        for (File file : Objects.requireNonNull(analysisFile.listFiles())) {
            if (file.isDirectory()) {
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.getName().endsWith(".apk")) {
                        tasks.add(f.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static void getTaskFromFile() {
        try (FileInputStream fis = new FileInputStream("res/tasks"); InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
            String line = "";

            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    tasks.add(line.strip());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        getTaskFromFile();
//        getTasks();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);

        YouMonitorThread monitorThread = new YouMonitorThread(executor, 10);
        new Thread(monitorThread).start();

        for (String apkPath : tasks) {
            RunFindAPIThread thread = new RunFindAPIThread(apkPath);
            executor.execute(thread);
        }


        executor.shutdown();
    }
}

class RunFindAPIThread extends Thread {
    private final String apkPath;
    private static final String APK_PATH = "/Users/clear/Library/Android/sdk/platforms";
    private static List<String> tasks;
//    private static List<String> runningTasks;

    public RunFindAPIThread(String apkPath) {
        this.apkPath = apkPath;
    }

    @Override
    public void run() {
        Process process = null;
        BufferedReader br = null;
        PrintWriter out = null;

        try {
            process = Runtime.getRuntime().exec("/bin/sh", null, null);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateFileViolistStart(apkPath);

        String command = "java -Xss256m -jar FindAPI.jar " + APK_PATH + " %s";

        try {
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            out.println("cd /Volumes/Clear/apk_pure");
            System.out.printf((command) + "%n", apkPath);
            out.println(String.format(command, apkPath));
            out.println("exit");
            String inline;
            while ((inline = br.readLine()) != null) {
                System.out.println(inline);
            }

            br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            while ((inline = br.readLine()) != null) {
                System.out.println(inline);
            }

            updateFileViolistFinish(apkPath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateFileViolistStart(String runningTask) {
        File runningFile = new File("res/running_tasks");
        try (OutputStream os = new FileOutputStream(runningFile, true); OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            runningTask = runningTask + "\n";
            writer.write(runningTask);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateFileViolistFinish(String finishedTask) {
        try (FileInputStream fis = new FileInputStream("res/tasks"); InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
            String line = "";
            tasks = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (!line.strip().equals(finishedTask) && !line.equals("")) {
                    tasks.add(line.strip());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File taskFile = new File("res/tasks");

        try (OutputStream os = new FileOutputStream(taskFile); OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            for (String task : tasks) {
                task = task + "\n";
                writer.write(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File completeFile = new File("res/completed_tasks");
        try (OutputStream os = new FileOutputStream(completeFile, true); OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            finishedTask += "\n";
            writer.write(finishedTask);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        File runningFile = new File("res/running_tasks");
//
//        try (FileInputStream fis = new FileInputStream(runningFile); InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
//            String line = "";
//            runningTasks = new ArrayList<>();
//            while ((line = reader.readLine()) != null) {
//                if (!line.strip().equals(finishedTask) && !line.strip().equals("")) {
//                    runningTasks.add(line.strip());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try (OutputStream os = new FileOutputStream(runningFile); OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
//            for (String task : runningTasks) {
//                task = task + "\n";
//                writer.write(task);
//            }
//            writer.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}

class YouMonitorThread implements Runnable {
    private ThreadPoolExecutor executor;
    private int seconds;
    private boolean run = true;
//    private List<Thread> runningThread = new ArrayList<>();

    public YouMonitorThread(ThreadPoolExecutor executor, int delay) {
        this.executor = executor;
        this.seconds = delay;
    }

    public void shutdown() {
        this.run = false;
    }

    @Override
    public void run() {
        while (run) {
            System.out.printf("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s%n",
                    this.executor.getPoolSize(),
                    this.executor.getCorePoolSize(),
                    this.executor.getActiveCount(),
                    this.executor.getCompletedTaskCount(),
                    this.executor.getTaskCount(),
                    this.executor.isShutdown(),
                    this.executor.isTerminated());
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}