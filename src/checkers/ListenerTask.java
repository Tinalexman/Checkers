package checkers;

import javafx.concurrent.Task;

import java.io.BufferedReader;


public class ListenerTask extends Task<Void>
{
    private final BufferedReader reader;
    private final Checker checker;

    public ListenerTask(BufferedReader reader,Checker checker)
    {
        this.reader = reader;
        this.checker = checker;
    }

    @Override
    protected Void call()
    {
        while(true)
        {
            try
            {
                String info = this.reader.readLine();
                if(info == null)
                    return null;
                this.checker.receiveMessage(info);
            }
            catch(Exception ignored)
            {

            }
        }
    }
}
