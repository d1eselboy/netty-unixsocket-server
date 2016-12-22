package server;

/**
 * Created by ermolaev on 20/12/16.
 */

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConsoleServerJunixsocket {

    public boolean keepConsoleRunning = true;
    public boolean keepConnectionRunning = true;

    public void exec() {
        File socketFile = new File("./console.unixsocket");
        socketFile.deleteOnExit();
        try {
            AFUNIXServerSocket server = AFUNIXServerSocket.newInstance();
            server.bind(new AFUNIXSocketAddress(socketFile));

            while ((!Thread.interrupted()) && keepConsoleRunning) {
                System.err.println("Waiting for connection on consoleCommands socket...");
                Socket sock = null;
                try {
                    sock = server.accept();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                try {
                    System.err.println("Connected: " + sock);

                    InputStream is = sock.getInputStream();
                    OutputStream os = sock.getOutputStream();

                    keepConnectionRunning = true;
                    while (keepConnectionRunning) {
                        Thread.sleep(1000L);
                        os.write("\nConsole> ".getBytes());
                        os.flush();

                        byte[] buf = new byte[512];
                        int read = is.read(buf);
                        if (read < 0) {
                            keepConnectionRunning = false;
                            break;
                        } else {
                            String input = new String(buf, 0, read);
                            System.err.println(input);
                        }
                    }
                    os.close();
                    is.close();

                    sock.close();
                } catch (IOException e) {
                    System.err.println("Received IOException: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("Console server terminated. Shutting down...");
        socketFile.delete();
        System.exit(0);
    }

}