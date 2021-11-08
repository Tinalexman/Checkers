package checkers;

import javafx.scene.image.ImageView;

public class Data
{
    private ImageView[] moves, eliminations;
    private int moveCount, eliminationCount;

    public static final int MOVE = 1;

    private static Data data;

    public static Data getData()
    {
        if(data == null)
            data = new Data();
        return data;
    }

    private Data()
    {
        moves = new ImageView[2];
        eliminations = new ImageView[2];
    }

    public void addImage(ImageView imageView, int flag)
    {
        if(flag == MOVE)
        {
            if(this.moveCount >= this.moves.length)
            {
                ImageView[] temp = (moveCount == 0) ? new ImageView[2] : new ImageView[moveCount * 2];
                System.arraycopy(moves, 0, temp, 0, moves.length);
                moves = temp;
            }
            moves[moveCount] = imageView;
            ++moveCount;
        }
        else
        {
            if(this.eliminationCount >= this.eliminations.length)
            {
                ImageView[] temp = (eliminationCount == 0) ? new ImageView[2] : new ImageView[eliminationCount * 2];
                System.arraycopy(eliminations, 0, temp, 0, eliminations.length);
                eliminations = temp;
            }
            eliminations[eliminationCount] = imageView;
            ++eliminationCount;
        }
    }

    /** This method simply trims away the unused values in the coordinates
     array to conserve memory.
     */
    public void trim(int flag)
    {
        if(flag == MOVE)
        {
            if(moves.length > moveCount)
            {
                ImageView[] temp = new ImageView[moveCount];
                System.arraycopy(moves, 0, temp, 0, moveCount);
                moves = temp;
            }
        }
        else
        {
            if(eliminations.length > eliminationCount)
            {
                ImageView[] temp = new ImageView[eliminationCount];
                System.arraycopy(eliminations, 0, temp, 0, eliminationCount);
                eliminations = temp;
            }
        }
    }

    public void clear(int flag)
    {
        if(flag == MOVE)
        {
            moves = new ImageView[2];
            moveCount = 0;
        }
        else
        {
            eliminations = new ImageView[2];
            eliminationCount = 0;
        }
    }

    public ImageView[] getImages(int flag)
    {
        return flag == MOVE ? moves : eliminations;
    }
}
