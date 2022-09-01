package me.asu.run.ui;

import static me.asu.run.ui.ActionKey.getByKey;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import java.awt.EventQueue;
import me.asu.run.SukProcess;
import me.asu.run.dao.FileIndexDao;
import me.asu.run.model.FileInfo;
import me.asu.run.model.Words;

public class SearchAndRunApplication implements Runnable {


    public static void main(String[] args) {
        if (args.length > 0) {
            String cmd = args[0];
           switch(cmd) {
               case "index":
                   String[] newArgs = new String[args.length - 1];
                   System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                   MakeIndex.main(newArgs);
                    break;
               case "clean":
                   FileIndexDao dao = new FileIndexDao();
                   dao.cleanNotExists();
                   dao.removeDupIndex();
                   System.out.println("Cleaned.");
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
            System.out.println("call cleanup");
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
                if (fi == null) { return; }
                if (fi.getName().toLowerCase().endsWith("bat")
                        || fi.getName().toLowerCase().endsWith("cmd")) {
                    String[] cmds = new String[]{"cmd", "/c", "start",
                            "\""+fi.getPath()+"\""};
                    new Thread(new SukProcess(cmds)).start();
                } else {
                    String[] cmds = new String[]{"cmd", "/c",
                             "\"" + fi.getPath() + "\""};
                    new Thread(new SukProcess(cmds)).start();
                }
            });

            Words words = new Words(null);
            wordListPanel.setWords(words);

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
