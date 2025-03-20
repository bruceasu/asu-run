package me.asu.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 外部排序工具，针对大文件进行排序和去重（sort and uniq）。
 */
public class ExternalSortUtil {

    /**
     * 对输入文件进行排序并去重，结果输出到输出文件中。
     *
     * @param inputFilePath  输入文件路径
     * @param outputFilePath 输出文件路径
     * @throws IOException 当文件读取或写入出错时抛出异常
     */
    public static void sortAndUniq(String inputFilePath, String outputFilePath) throws IOException {
        // 根据内存限制，每次最多加载的行数（根据实际情况调整）
        final int MAX_LINES_IN_MEMORY = 100000;
        List<File> tempFiles = new ArrayList<>();

        // Step 1：分块读取并排序，生成多个临时文件
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            List<String> lines = new ArrayList<>(MAX_LINES_IN_MEMORY);
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() >= MAX_LINES_IN_MEMORY) {
                    File tempFile = writeSortedChunk(lines);
                    tempFiles.add(tempFile);
                    lines.clear();
                }
            }
            if (!lines.isEmpty()) {
                File tempFile = writeSortedChunk(lines);
                tempFiles.add(tempFile);
                lines.clear();
            }
        }

        // Step 2：归并所有排序后的临时文件，同时去重
        mergeSortedFiles(tempFiles, new File(outputFilePath));

        // 清理临时文件
        for (File f : tempFiles) {
            f.delete();
        }
    }

    /**
     * 对内存中的行数据进行排序，然后写入一个临时文件。
     *
     * @param lines 待排序的行数据
     * @return 排序后的临时文件
     * @throws IOException 当写入文件出错时抛出异常
     */
    private static File writeSortedChunk(List<String> lines) throws IOException {
        Collections.sort(lines);
        File tempFile = File.createTempFile("sortChunk", ".txt");
        tempFile.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
        return tempFile;
    }

    /**
     * 将多个已排序的临时文件进行多路归并，输出到最终文件，并在归并过程中去重。
     *
     * @param sortedFiles 已排序的临时文件列表
     * @param outputFile  最终输出文件
     * @throws IOException 当读写文件出错时抛出异常
     */
    private static void mergeSortedFiles(List<File> sortedFiles, File outputFile) throws IOException {
        PriorityQueue<PQNode> pq = new PriorityQueue<>();
        List<BufferedReader> readers = new ArrayList<>();

        // 打开所有临时文件，读取第一行并加入优先队列
        for (File f : sortedFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            readers.add(reader);
            String line = reader.readLine();
            if (line != null) {
                pq.add(new PQNode(line, reader));
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String lastLine = null;
            while (!pq.isEmpty()) {
                PQNode node = pq.poll();
                String currentLine = node.line;
                // 去重：如果当前行与上一次写入的行相同，则跳过
                if (lastLine == null || !lastLine.equals(currentLine)) {
                    writer.write(currentLine);
                    writer.newLine();
                    lastLine = currentLine;
                }
                // 读取当前节点所在文件的下一行
                String nextLine = node.reader.readLine();
                if (nextLine != null) {
                    pq.add(new PQNode(nextLine, node.reader));
                }
            }
        }

        // 关闭所有读取器
        for (BufferedReader reader : readers) {
            reader.close();
        }
    }

    /**
     * 辅助类：优先队列节点，存储当前行及对应的 BufferedReader。
     */
    private static class PQNode implements Comparable<PQNode> {
        String line;
        BufferedReader reader;

        PQNode(String line, BufferedReader reader) {
            this.line = line;
            this.reader = reader;
        }

        @Override
        public int compareTo(PQNode other) {
            return this.line.compareTo(other.line);
        }
    }
}
