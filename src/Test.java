import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class Test {
    private static final String analysisDir = "/Volumes/Data/d";
    private static List<String> tasks = new ArrayList<>();

    private static void getTasks() {
        File analysisFile = new File(analysisDir);
        for (File file : Objects.requireNonNull(analysisFile.listFiles())) {
            if (file.isDirectory()) {
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.getName().endsWith(".apk")) {
                        tasks.add(renameBlank(f));
                        break;
                    }
                }
            } else if (file.getName().endsWith(".apk")) {
                tasks.add(file.getAbsolutePath());
            }
        }
    }

    private static String renameBlank(File f) {
        if (f.getName().contains(" ")) {
            File src = new File(f.getAbsolutePath());
            String srcName = src.getAbsolutePath();
            File tgt = new File(f.getParent() + File.separator + f.getName().replace(" ", ""));
            boolean result = src.renameTo(tgt);
            if (result) {
                System.out.println("rename " + srcName + " to " + tgt.getAbsolutePath());
            } else {
                System.out.println("Error!");
            }
            return tgt.getAbsolutePath();
        }
        return f.getAbsolutePath();
    }

    public static void main(String[] args) {
        File analysisFile = new File(analysisDir);
        for (File file : Objects.requireNonNull(analysisFile.listFiles())) {
            renameBlank(file);
//            if (file.isDirectory()) {
//                for (File f : Objects.requireNonNull(file.listFiles())) {
//                    if (f.getName().endsWith(".apk")) {
//                        tasks.add(renameBlank(f));
//                        break;
//                    }
//                }
//            } else if (file.getName().endsWith(".apk")) {
//                tasks.add(file.getAbsolutePath());
//            }
        }
//        getTasks();
//        File taskFile = new File("res/tasks_2");
//        try (OutputStream os = new FileOutputStream(taskFile)){
//            for(String task: tasks) {
//                System.out.println(task);
//                task = task + "\n";
//                os.write(task.getBytes());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
