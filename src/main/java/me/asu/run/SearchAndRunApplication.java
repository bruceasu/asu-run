package me.asu.run;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import me.asu.run.ui.WordListPanel;

import java.awt.*;

import static me.asu.run.ActionKey.getByKey;

public class SearchAndRunApplication implements Runnable {


    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String cmd = args[0];
            switch (cmd) {
                case "index":
                    String[] newArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                    MakeIndex.main(newArgs);
                    break;
                case "clean":
                    CleanIndex.cleanup();
                    break;
            }
            System.exit(0);
        }

        JIntellitype.getInstance()
                .registerHotKey(ActionKey.GLOBAL_SEARCH.getKey(), JIntellitype.MOD_ALT, 113); // F2

        EventQueue.invokeLater(() -> {
            SearchAndRunApplication searchAndRunApplication = new SearchAndRunApplication();
            searchAndRunApplication.run();
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            JIntellitype.getInstance().cleanUp();
        }));


    }


    WordListPanel wordListPanel;

    public SearchAndRunApplication() {
        addHotKeyListener();
        try {
            wordListPanel = new WordListPanel();
            wordListPanel.addObserver((o, arg) -> {
//                System.out.println("o = " + o);
//                System.out.println("arg = " + arg);
                FileInfo fi = (FileInfo) arg;
                if (fi == null) {return;}
                final String name = fi.getName().toLowerCase();
                if (name.endsWith(".bat") || name.endsWith(".cmd") || name.endsWith(".lnk")) {
                    String[] cmds = new String[]{"cmd", "/c", "start", "\"" + fi.getPath() + "\""};
                    new Thread(new SukProcess(cmds)).start();
                } else {
                    String[] cmds = new String[]{"cmd", "/c", "\"" + fi.getPath() + "\""};
                    new Thread(new SukProcess(cmds)).start();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        wordListPanel.show();
    }

    private void addHotKeyListener() {
        // 第二步：添加热键监听器
        JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {
            @Override
            public void onHotKey(int markCode) {
                System.out.println("markCode:" + markCode);
                ActionKey ak = getByKey(markCode);
                //System.out.println(ak);
                switch (ak) {
                    case GLOBAL_SEARCH:
                        wordListPanel.show();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void unregister() {
        JIntellitype instance = JIntellitype.getInstance();
//        instance.unregisterHotKey(GLOBAL_Test.getKey());

    }

    public void register() {
        JIntellitype instance = JIntellitype.getInstance();
//        instance.registerHotKey(GLOBAL_Test.getKey(), JIntellitype.MOD_ALT, 'T');

    }


}
