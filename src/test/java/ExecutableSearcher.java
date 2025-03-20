import me.asu.run.IndexGenerator;
import me.asu.run.IndexSearcher;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * ExecutableSearcher 提供简单的命令行交互：
 * － 用户可选择生成索引文件（输入遍历目录和索引文件保存路径）
 * － 或者基于现有索引文件搜索（输入索引文件路径和搜索关键字），
 *    搜索后展示匹配的 path 列表，并允许用户选择具体的一项（后续可扩展为执行该程序）。
 */
public class ExecutableSearcher {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        try {
            loop:
            do {
                System.out.println("请选择操作：");
                System.out.println("1. 生成索引文件");
                System.out.println("2. 搜索索引文件");
                System.out.println("3. 退出");
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        System.out.print("请输入要遍历的目录路径：");
                        String dirPath = scanner.nextLine();
                        System.out.print("请输入排除的索引文件保存路径：");
                        String excludePath = scanner.nextLine();
                        String indexPath = "c:\\users\\suk\\.asu-run.idx";
                        IndexGenerator generator = new IndexGenerator(indexPath,new String[] {dirPath.trim()}, new String[] {excludePath.trim()});
                        generator.generateIndex();
                        System.out.println("索引文件生成完毕：" + indexPath);
                        break;
                    case "2":
//                    System.out.print("请输入索引文件路径：");
//                    String searchIndexPath = scanner.nextLine();
                        String searchIndexPath = "c:\\users\\suk\\.asu-run.idx";
                        IndexSearcher searcher = new IndexSearcher(searchIndexPath);
                        System.out.print("请输入搜索关键字：");
                        String keyword = scanner.nextLine();
                        List<String> paths = searcher.search(keyword);
                        if (paths.isEmpty()) {
                            System.out.println("未找到匹配项。");
                        } else {
                            System.out.println("匹配到的路径：");
                            for (int i = 0; i < paths.size(); i++) {
                                System.out.println((i + 1) + ". " + paths.get(i));
                            }
                            // 简单交互：允许用户输入编号选择对应路径
                            System.out.print("请选择要执行的路径序号（或直接回车退出）：");
                            String input = scanner.nextLine();
                            if (!input.trim().isEmpty()) {
                                try {
                                    int index = Integer.parseInt(input.trim());
                                    if (index >= 1 && index <= paths.size()) {
                                        String selectedPath = paths.get(index - 1);
                                        System.out.println("您选择的路径：" + selectedPath);
                                        // 此处可调用 Runtime.exec(selectedPath) 执行程序
                                    } else {
                                        System.out.println("序号超出范围。");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("输入不是有效的数字。");
                                }
                            }
                        }
                        break;
                    case "3":
                        System.out.println("退出程序。");
                        break loop;
                    default:
                        System.out.println("无效选择。");
                }
            } while(true);
        } catch (IOException e) {
            System.err.println("发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
