package client;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketException;

import java.io.*;
import java.util.Scanner;

public class ConsoleClientJunixsocket {
    public void exec(Thread serverThread) {
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

            //Scanner вычитывает из System.in и вкидывет в PrintStream, если нажали Enter. Вкидывает строку в OutputStream -  в сокет
            Thread inputThread = new Thread(() -> {
                PrintStream ps = new PrintStream(os, true);
                Scanner scanner = new Scanner(System.in);
                while (!Thread.interrupted()) {
                    if (scanner.hasNextLine())
                        ps.println(scanner.nextLine());
                    else
                        try {
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