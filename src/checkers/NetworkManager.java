package checkers;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;

public class NetworkManager
{
    private static PrintWriter writer;
    private static volatile boolean blockMessages = true;


    public static void init(int port, Checker checker)
    {
        CheckersConnector connector = new CheckersConnector(port, checker);
        connector.start();

        while(connector.isRunning() && blockMessages)
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ignored)
            {

            }
        }
    }

    private static void startClient(int PORT, Checker checker)
    {
        try
        {
            ServerSocket server = new ServerSocket(PORT);
            Socket socket = server.accept();
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            new Thread(new ListenerTask(inputStream, checker)).start();
            blockMessages = false;
        }
        catch (IOException ignored)
        {

        }
    }

    public static void send(String info)
    {
        if(blockMessages)
            return;

        writer.println(info);
        writer.flush();
    }

    static class CheckersConnector extends Service<Void>
    {
        private final int port;
        private final Checker checker;

        public CheckersConnector(int port, Checker checker)
        {
            this.port = port;
            this.checker = checker;
        }
        @Override
        protected Task<Void> createTask()
        {
            return new CheckersConnection(this.port, this.checker);
        }
    }

    static class CheckersConnection extends Task<Void>
    {
        private final int port;
        private final Checker checker;

        public CheckersConnection(int port, Checker checker)
        {
            this.port = port;
            this.checker = checker;
        }

        @Override
        protected Void call()
        {
            updateProgress(0, 1);
            startClient(this.port, this.checker);
            updateProgress(1,1);
            return null;
        }
    }
}
