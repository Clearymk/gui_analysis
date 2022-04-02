import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetTasks {
    private static final String analysisDir = "/Volumes/Clear/apk_pure/result";
    private static List<List<String>> tasks = new ArrayList<>();


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

                if (count_xml < 2 && apk_files.size() == 2) {
                    tasks.add(apk_files);
                }

                if (count_xml > 2) {
                    System.out.println(file.getAbsolutePath());
                }

            }
        }
    }

    public static void main(String[] args) {
        getTasks();
        System.out.println(tasks.size());
    }
}
