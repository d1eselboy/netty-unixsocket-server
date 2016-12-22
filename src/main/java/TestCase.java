import client.ConsoleClientJunixsocket;
import client.ConstantStringClientJunixsocket;
import server.ConsoleServerJunixsocket;

/**
 * Created by ermolaev on 21/12/16.
 */
public class TestCase {
    public static void main(String args[]) throws Exception {
        Thread serverThread = new Thread(() -> new ConsoleServerJunixsocket().exec());
        serverThread.start();
        Thread.sleep(2000L);

//        Thread clientThread = new Thread(() -> new ConsoleClientJunixsocket().exec(serverThread));
//        clientThread.start();

        Thread clientThread2 = new Thread(() -> new ConstantStringClientJunixsocket().exec(serverThread, "1"));
        clientThread2.start();

        Thread.sleep(2000L);

        Thread clientThread3 = new Thread(() -> new ConstantStringClientJunixsocket().exec(serverThread, "2"));
        clientThread3.start();

    }
}
