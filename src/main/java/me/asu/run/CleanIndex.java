package me.asu.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public class CleanIndex {
     public static void cleanup() throws IOException {
        Path indexPath = Paths.get(System.getProperty("user.home"), ".local", "share", ".asu-run.idx");
        Path indexPathTmp = Paths.get(System.getProperty("user.home"), ".local", "share", ".asu-run.idx.cleanup-tmp");

        if (!Files.exists(indexPath)) {
            System.out.println("Cleaned.");
            return;
        }
        try(BufferedReader reader = Files.newBufferedReader(indexPath, StandardCharsets.UTF_8);
                BufferedWriter writer = Files.newBufferedWriter(indexPathTmp, StandardCharsets.UTF_8)) {
            Set<String> set = new HashSet<>();
            String      line;
            while((line = reader.readLine()) != null) {
                final String[] split = line.split("\\|");
                if (split.length != 2) {
                    continue;
                }
                String file = split[1].trim();
                Path path = Paths.get(file);
                if (Files.exists(path)) {
                    if (OsUtils.WINDOWS) {
                        final String lowerCase = file.toLowerCase();
                        if (set.contains(lowerCase)) continue;

                        set.add(lowerCase);
                        writer.write(line);
                        writer.newLine();
                    } else {
                        if (set.contains(file)) continue;

                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
            Files.move(indexPathTmp, indexPath, StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Cleaned.");
    }


}
