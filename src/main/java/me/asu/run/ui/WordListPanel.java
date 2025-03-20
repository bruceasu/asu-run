package me.asu.run.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;

import me.asu.run.*;

public class WordListPanel extends Observable {

    JList      wordListPanel;
    JPanel     mainPanel;
    JTextField kw;
    JFrame     frame;

    Words        words   = new Words(null);
    SearchEngine manager = new IndexSearcher(
            Paths.get(System.getProperty("user.home"),
                    ".local", "share", ".asu-run.idx").toString());

    public WordListPanel() {
        addPaths();
        GUITools.initLookAndFeel();
        frame = new JFrame("Word List");
        frame.setAutoRequestFocus(true);
        // 禁用或启用此窗体的修饰。只有在窗体不可显示时才调用此方法。
        frame.setUndecorated(true);
        //        frame.setOpacity(0.7f);
        //frame.setBackground(new Color(0,0,0,0));
        mainPanel.setBackground(new Color(0, 0, 0, 0));
        frame.setLocationRelativeTo(null);
        /*
        JFrame.EXIT_ON_CLOSE -- 退出应用.
        JFrame.DISPOSE_ON_CLOSE -- 关闭和销毁frame, 不退出应用。
        JFrame.DO_NOTHING_ON_CLOSE -- 关闭但不销毁frame, 不退出应用.
         */
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 300));
        frame.pack();
        GUITools.center(frame);
        frame.setContentPane(mainPanel);

        kw.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode   = e.getKeyCode();
                int modifiers = e.getModifiers();
                if (modifiers == KeyEvent.CTRL_MASK) {
//                    System.out.println(keyCode);
                    if (keyCode == KeyEvent.VK_P) {
                        // previous item
                        selectPreviousItem();
                    } else if (keyCode == KeyEvent.VK_N) {
                        // next item
                        selectNextItem();
                    } else if (keyCode == KeyEvent.VK_D) {
                        // next page
                        nextPage();
                    } else if (keyCode == KeyEvent.VK_U) {
                        // previous page
                        previousPage();
                    } else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
                        selectItem(keyCode);
                    }
                } else {
                    switch (keyCode) {
                        case KeyEvent.VK_ENTER:
                            executeItem();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            hide();
                            break;
                        case KeyEvent.VK_UP:
                            selectPreviousItem();
                            break;
                        case KeyEvent.VK_DOWN:
                            selectNextItem();
                            break;
                        case KeyEvent.VK_PAGE_UP:
                            previousPage();
                            break;
                        case KeyEvent.VK_PAGE_DOWN:
                            nextPage();
                            break;
                        default:
                            super.keyPressed(e);
                            // call search
                            SwingUtilities.invokeLater(() -> {
                                search(kw.getText().trim());
                            });

                    }
                }
            }
        });
    }

    private void previousPage() {
        wordListPanel.setModel(words.getModel(words.getPage() - 1));
        wordListPanel.setSelectedIndex(words.getSelectedIndex());
    }

    private void nextPage() {
        wordListPanel.setModel(words.getModel(words.getPage() + 1));
        wordListPanel.setSelectedIndex(words.getSelectedIndex());
    }

    private void selectItem(int keyCode) {
        int idx = keyCode - '0';
        System.out.println(keyCode);
        System.out.println(idx);
        wordListPanel.setSelectedIndex(idx);
        words.setSelectedIndex(idx);
        executeItem();
        return;
    }

    private void executeItem() {
        hide();
        setChanged();
        notifyObservers(words.getSelected());
        SwingUtilities.invokeLater(() -> {
            kw.setText("");
        });
    }

    private void selectNextItem() {
        int idx = wordListPanel.getSelectedIndex();
        idx++;
        if (idx >= wordListPanel.getModel().getSize()) {
            idx = wordListPanel.getModel().getSize() - 1;
        }
        wordListPanel.setSelectedIndex(idx);
        words.setSelectedIndex(idx);
    }

    private void selectPreviousItem() {
        int idx = wordListPanel.getSelectedIndex();
        idx--;
        if (idx == -1) {
            idx = 0;
        }
        wordListPanel.setSelectedIndex(idx);
        words.setSelectedIndex(idx);
    }

    public void hide() {
        miniTray();
    }

    public void show() {
        if (trayIcon != null && tray != null) {tray.remove(trayIcon);}
        frame.setVisible(true);
    }

    public void close() {
        frame.dispose();
    }

    private static final String   TITLE    = "Search And Run";
    private static final Font     FONT     = new Font("Sans Serif", Font.PLAIN, 16);
    private              TrayIcon trayIcon = null;
    SystemTray tray = SystemTray.getSystemTray();

    public void miniTray() {
        frame.setVisible(false);
        GUITools.setUIFont(FONT, "PopupMenu", "MenuItem");
        //窗口最小化到任务栏托盘
        ImageIcon trayImg = getImageIcon();
        //增加托盘右击菜单
        PopupMenu pop  = new PopupMenu();
        MenuItem  show = new MenuItem("Restore");
        MenuItem  exit = new MenuItem("Quit");
        show.addActionListener(e -> {
            tray.remove(trayIcon);
            show();
        });
        // 按下退出键
        exit.addActionListener(e -> {
            tray.remove(trayIcon);
            System.exit(0);
        });
        pop.add(show);
        pop.add(exit);
        trayIcon = new TrayIcon(trayImg.getImage(), TITLE, pop);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    tray.remove(trayIcon);
                    show();
                }
            }
        });
        try {
            tray.add(trayIcon);
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
    }

    private ImageIcon getImageIcon() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/69.gif");
        if (resourceAsStream == null) {
            return new ImageIcon();
        }

        ByteArrayOutputStream s    = new ByteArrayOutputStream();
        byte[]                buff = new byte[8196];
        int                   i    = 0;
        while (true) {
            try {
                if (!((i = resourceAsStream.read(buff)) > 0)) {
                    break;
                }
                s.write(buff, 0, i);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        buff = s.toByteArray();
        //托盘图标
        return new ImageIcon(buff);
    }

    public void setWords(Words data) {
        if (data == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            this.words = data;
            wordListPanel.setModel(data.getModel(1));
            wordListPanel.setSelectedIndex(data.getSelectedIndex());
        });

    }

    CompletableFuture<Void> searchFuture;

    public void search(String kw) {

        //System.out.println("kw: " + kw);
        if (kw == null || kw.trim().isEmpty()) {return;}
        if (searchFuture != null && !searchFuture.isDone()) {
            searchFuture.cancel(true);
        }
        searchFuture = CompletableFuture.supplyAsync(() -> {
            Condition condition = new Condition();
            condition.setName(kw);
            List<FileInfo> result = manager.search(condition);
            return result;
        }).thenAcceptAsync((result) -> {
            words.setData(result);
            setWords(words);
        });


    }

    private void addPaths() {
        HandlerPath instance = HandlerPath.getInstance();
        if (OsUtils.WINDOWS) {
            File[] roots = File.listRoots();
            for (int i = 0; i < roots.length; i++) {
                // 磁盘路径
                instance.addIncludePath(roots[i].getPath());
            }
        } else {
            instance.addIncludePath("/");
        }
        instance.addExcludePath(".*\\.git");
        instance.addExcludePath(".*\\.cvs");
        instance.addExcludePath(".*\\.svn");
        instance.addExcludePath(".*\\.settings");
        instance.addExcludePath(".*\\.idea");
    }

//    public static void main(String[] args) {
//        WordListPanel wordListPanel = new WordListPanel();
//        wordListPanel.addObserver((o, arg) -> {
//            System.out.println("o = " + o);
//            System.out.println("arg = " + arg);
//            FileInfo fi = (FileInfo)arg;
//            if (fi ==null ) return;
//            String[] cmds = new String[] {"cmd", "/c", "start", fi.getPath()};
//            new Thread(new SukProcess(cmds)).start();
//        });
//
//        Words words = new Words(null);
//        wordListPanel.setWords(words);
//        wordListPanel.show();
//    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("WordListPanel");
        frame.setContentPane(new WordListPanel().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        wordListPanel = new JList();
        mainPanel.add(wordListPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(320, 240), null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        kw = new JTextField();
        mainPanel.add(kw, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return mainPanel;}

}
