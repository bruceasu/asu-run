package me.asu.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
public class SukProcess implements Runnable {

    String[]  cmds;
    OutThread thread;
    Process   process;
    int       exitCode = 0;
    volatile AtomicBoolean completed = new AtomicBoolean(false);

    public SukProcess(String[] cmds) {
        this.cmds = cmds;

    }

    @Override
    public void run() {
        ProcessBuilder builder = new ProcessBuilder(cmds);
        builder.redirectErrorStream(true);

        try {
            process = builder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try(InputStreamReader in = new InputStreamReader(process.getInputStream());
            BufferedReader inReader = new BufferedReader(in)) {
            thread = new OutThread(inReader);
            thread.start();
            // wait for the process to finish and check the exit code
            exitCode = process.waitFor();
            joinThread(thread);
            completed.set(true);
        } catch (Exception ie) {
           ie.printStackTrace();
        } finally {
            if (!completed.get()) {
                thread.interrupt();
                joinThread(thread);
            }
            process.destroy();
        }
    }

    private static void joinThread(Thread t) {
        while (t.isAlive()) {
            try {
                t.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                t.interrupt(); // propagate interrupt
            }
        }
    }

    class OutThread extends Thread {

        BufferedReader reader;

        public OutThread(BufferedReader reader) {
            super();
            setDaemon(true);
            this.reader = reader;
        }


        @Override
        public void run() {
            try {
                String line = reader.readLine();
                while ((line != null) && !isInterrupted()) {
                    System.out.println(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ;
}
