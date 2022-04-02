import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GUIAnalysis {
    private static String analysisDir = "/Volumes/Clear/apk_pure/result";
    private static List<List<String>> tasks = new ArrayList<>();
    private static final int MAX_THREADS = 8;

    private static void getTasks() {
        File analysisFile = new File(analysisDir);
        for (File file : Objects.requireNonNull(analysisFile.listFiles())) {
            if (file.isDirectory()) {
                int count_xml = 0;
                List<String> apk_files = new ArrayList<>();
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.getPath().endsWith(".xml")) {
                        count_xml++;
                    } else if (f.getPath().endsWith(".apk")) {
                        apk_files.add(f.getAbsolutePath());
                    }
                }

                if (count_xml != 2 && apk_files.size() == 2) {
                    tasks.add(apk_files);
                }
            }
        }
    }

    public static void main(String[] args) {
        getTasks();
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        for (List<String> task : tasks) {
            for (String apkPath : task) {
                RunAnalysisThread thread = new RunAnalysisThread(apkPath);
                executorService.execute(thread);
            }
        }
        executorService.shutdown();
    }
}

class RunAnalysisThread extends Thread {
    private final String apkPath;

    public RunAnalysisThread(String apkPath) {
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

        String command = "./gator a -p %s -client GUIHierarchyPrinterClient";
        try {
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            out.println("source ~/.bash_profile ");
            out.println("cd $GatorRoot/gator");
            System.out.println("----------------");
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

            System.out.println("----------------");
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
}