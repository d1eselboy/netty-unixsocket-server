package client;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketException;

import java.io.*;
import java.util.Scanner;

public class ConstantStringClientJunixsocket {
    public void exec(Thread serverThread, String message) {
        try {
            AFUNIXSocket sock = AFUNIXSocket.newInstance();
            try {
                sock.connect(new AFUNIXSocketAddress(new File("./console.unixsocket")));
            } catch (AFUNIXSocketException e) {
                System.out.println("Cannot connect to server. Have you started it?");
                System.out.flush();
                serverThread.interrupt();
                throw e;
            }

            final InputStream is = sock.getInputStream(); //читать из сокета
            final OutputStream os = sock.getOutputStream(); //писать в сокет

            //PrintStream вкидывает сообщение в OutputStream -  в сокет
            Thread inputThread = new Thread(() -> {
                PrintStream ps = new PrintStream(os, true);
                while (!Thread.interrupted()) {
                    try {
                        ps.print(message);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            inputThread.start();

            //Читает из сокета и выводит на экран содержимое - >>Console
            try {
                while (sock.isConnected()) {
                    byte[] buf = new byte[512];
                    int read = is.read(buf);
                    if (read < 0)
                        break;
                    else {
                        String input = new String(buf, 0, read);
                        System.out.print(input);
                    }
                    Thread.sleep(100);
                }

            } catch (IOException e) {

            }

            serverThread.interrupt();
            inputThread.interrupt();
            os.close();
            is.close();

            sock.close();

            System.out.println("Connection Terminated.");

            System.exit(0);
        } catch (IOException | InterruptedException ie) {
            System.out.println("thread terminated. Exception: " + ie.getMessage());
        }
    }
}