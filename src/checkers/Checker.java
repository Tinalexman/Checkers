package checkers;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Checker
{
    public static final int GRIDS_PER_ROW = 5;
    private static final int[] kingPositions = {0, 1, 2, 3, 4, 45, 46, 47, 48, 49};
    public static final double INVERSE_IMAGE_SIZE = 0.02;
    private final Data data;
    private final int size;

    private final Type currentPlayer, otherPlayer, otherKing;

    protected final Image background, playerOne, playerOneKing, playerTwo, playerTwoKing, transparent;
    private final Map<ImageView, ImageInfo> selectMap;
    private ImageView[] imageViews;
    private ImageView selected;
    private TextArea textArea;
    private Label playerOneScore, playerTwoScore, otherPlayerName, turnLabel;
    protected boolean showHints;
    protected final SimpleBooleanProperty isConnected, playSfx;

    private static final int UPPER_RIGHT = 0;
    private static final int UPPER_LEFT = 1;
    private static final int LOWER_RIGHT = 2;
    private static final int LOWER_LEFT = 3;

    protected boolean currentTurn;
    private List<Media> media;

    public Checker(Type type)
    {
        this.size = 10;

        this.background = new Image("checkers//background.jpg");
        this.playerOne = new Image("checkers//blue.png");
        this.playerOneKing = new Image("checkers//blue_king.png");
        this.playerTwo = new Image("checkers//orange.png");
        this.playerTwoKing = new Image("checkers//orange_king.png");
        this.transparent = new Image("checkers//transparent.png");

        this.data = Data.getData();

        this.selectMap = new HashMap<>();
        this.imageViews = new ImageView[GRIDS_PER_ROW * this.size];

        this.currentPlayer = type;
        this.otherPlayer = (this.currentPlayer == Type.PLAYER_ONE ? Type.PLAYER_TWO : Type.PLAYER_ONE);
        this.otherKing = (this.otherPlayer == Type.PLAYER_TWO ? Type.PLAYER_TWO_KING : Type.PLAYER_ONE_KING);

        this.currentTurn = (this.currentPlayer != Type.PLAYER_ONE);

        this.showHints = true;

        this.isConnected = new SimpleBooleanProperty(true);
        this.playSfx = new SimpleBooleanProperty(true);
    }


    public void setImages(ImageView... images)
    {
        this.imageViews = images;

        for (int x = 0; x < images.length; ++x)
        {
            var img = images[x];
            if (x < 20)
            {
                if (this.currentPlayer != Type.PLAYER_ONE)
                {
                    img.setImage(playerOne);
                    this.selectMap.put(img, new ImageInfo(img, Type.PLAYER_ONE));
                } else
                {
                    img.setImage(playerTwo);
                    this.selectMap.put(img, new ImageInfo(img, Type.PLAYER_TWO));
                }
            } else if (x < 30)
            {
                img.setImage(transparent);
                this.selectMap.put(img, new ImageInfo(img, Type.NIL));
            } else
            {
                if (this.currentPlayer != Type.PLAYER_ONE)
                {
                    img.setImage(playerTwo);
                    this.selectMap.put(img, new ImageInfo(img, Type.PLAYER_TWO));
                } else
                {
                    img.setImage(playerOne);
                    this.selectMap.put(img, new ImageInfo(img, Type.PLAYER_ONE));
                }
            }
        }
    }

    private void send(String message)
    {
        NetworkManager.send(message);
    }

    private boolean isBetween(ImageInfo player, ImageInfo opponent, ImageInfo space)
    {
        int playerRow = (int) player.image.getLayoutY(), playerCol = (int) player.image.getLayoutX();
        int spaceRow = (int) space.image.getLayoutY(), spaceCol = (int) space.image.getLayoutX();
        int opponentRow = (int) opponent.image.getLayoutY(), opponentCol = (int) opponent.image.getLayoutX();

        int rowDifference = (playerRow + spaceRow) / 2;
        int columnDifference = (playerCol + spaceCol) / 2;

        if (rowDifference == opponentRow && columnDifference == opponentCol)
        {
            eliminateChecker(player, opponent, space, false);
            return true;
        }

        return false;
    }

    private int[] getRowAndColumnOperations(int row, int col, int flag)
    {
        int[] positions = new int[4];
        if (flag == UPPER_RIGHT)
        {
            positions[0] = -1;
            positions[1] = 1;
            positions[2] = row + positions[0];
            positions[3] = col + positions[1];
        }
        else if (flag == UPPER_LEFT)
        {
            positions[0] = positions[1] = -1;
            positions[2] = row + positions[0];
            positions[3] = col + positions[1];
        }
        else if (flag == LOWER_RIGHT)
        {
            positions[0] = positions[1] = 1;
            positions[2] = row + positions[0];
            positions[3] = col + positions[1];
        }
        else if (flag == LOWER_LEFT)
        {
            positions[0] = 1;
            positions[1] = -1;
            positions[2] = row + positions[0];
            positions[3] = col + positions[1];
        }
        return positions;
    }

    private void checkDiagonal(int row, int col, int flag)
    {
        int[] results = getRowAndColumnOperations(row, col, flag);

        int rowOperation = results[0], colOperation = results[1];
        int tempRow = results[2], tempCol = results[3];

        boolean[] conditions =
        {
            tempRow >= 0 && tempCol < this.size, tempRow >= 0 && tempCol >= 0,
            tempRow < this.size && tempCol < this.size, tempRow < this.size && tempCol >= 0
        };

        while (conditions[flag])
        {
            int position = getPosition(tempRow, tempCol);
            if(position < 0 || position > this.size * GRIDS_PER_ROW)
                return;

            int[] result = checkPositions(position, tempRow, tempCol, rowOperation, colOperation);
            if(result == null)
                return;

            tempRow = results[0];
            tempCol = results[1];

            tempCol += colOperation;
            tempRow += rowOperation;
        }
    }

    private int[] checkPositions(int position, int tempRow, int tempCol, int rowOperation, int colOperation)
    {
        ImageInfo info = this.selectMap.get(this.imageViews[position]);
        if (info.type == Type.NIL)
            this.data.addImage(info.image, Data.MOVE);
        else if(info.type == otherPlayer || info.type == otherKing)
        {
            int newRow = tempRow + rowOperation, newCol = tempCol + colOperation;
            if(newRow < 0 || newRow > this.size - 1 || newCol < 0 || newCol > this.size - 1)
                return null;

            int pos = getPosition(newRow, newCol);

            ImageInfo possiblePosition = this.selectMap.get(this.imageViews[pos]);
            if(possiblePosition.type == Type.NIL)
            {
                this.data.addImage(info.image, 0);
                this.data.addImage(possiblePosition.image, Data.MOVE);
                tempRow = newRow;
                tempCol = newCol;
            }
        }
        return new int[]{tempRow, tempCol};
    }

    public void setSfx(List<Media> mediaList)
    {
        this.media = mediaList;
    }

    public void pickAndMove(int row, int col)
    {
        if(!this.currentTurn)
            return;

        ImageInfo picked = this.selectMap.get(this.imageViews[getPosition(row, col)]);

        if(picked.type == this.otherPlayer || picked.type == this.otherKing)
            return;

        if(this.selected == null)
            nullSelection(picked);
        else if(this.selected.equals(picked.image))
        {
            picked.highlight(0);
            clearData();
            this.selected = null;
        }
        else
        {
            for(var imageView : this.data.getImages(Data.MOVE))
            {
                ImageInfo moveTest = this.selectMap.get(imageView);
                if(moveTest == picked && processPick(picked))
                    break;
            }
        }
    }

    private void nullSelection(ImageInfo picked)
    {
        if(picked.type == Type.NIL)
            return;

        picked.highlight(ImageInfo.SELECT_TARGET);
        this.selected = picked.image;
        if (picked.type == Type.PLAYER_ONE_KING || picked.type == Type.PLAYER_TWO_KING)
            evaluateKingMoves(picked);
        else
            evaluateMoves(picked);
        highlight();
    }

    private boolean processPick(ImageInfo picked)
    {
        for (var image : this.data.getImages(0))
        {
            ImageInfo eliminationTest = this.selectMap.get(image);
            if(eliminationTest == null)
                continue;

            if (isBetween(this.selectMap.get(this.selected), eliminationTest, picked))
            {
                this.selected = null;
                return true;
            }
        }

        for (var image : this.data.getImages(Data.MOVE))
        {
            ImageInfo moveTest = this.selectMap.get(image);
            if (moveTest.equals(picked))
            {
                moveChecker(this.selectMap.get(this.selected), moveTest, false, false);
                clearData();
                this.selected = null;
                return true;
            }
        }
        return false;
    }

    public void setControls(TextArea messageBox, Label label, Label playerOneScore, Label playerTwoScore, Label otherName)
    {
        this.textArea = messageBox;
        this.turnLabel = label;
        this.playerOneScore = playerOneScore;
        this.playerTwoScore = playerTwoScore;
        this.otherPlayerName = otherName;
    }

    public void swapTurns()
    {
        if(this.currentTurn)
        {
            this.turnLabel.setTextFill(Color.RED);
            this.turnLabel.setText("NOT YOUR TURN");
            this.currentTurn = false;
        }
        else
        {
            this.turnLabel.setTextFill(Color.GREEN);
            this.turnLabel.setText("YOUR TURN");
            this.currentTurn = true;
        }
    }

    private void updateScore(Type player, boolean wasReceived)
    {
        if (player == Type.PLAYER_ONE || player == Type.PLAYER_ONE_KING)
        {
            int num = Integer.parseInt(playerOneScore.getText());
            this.playerOneScore.setText(Integer.toString(++num));
        } else if (player == Type.PLAYER_TWO || player == Type.PLAYER_TWO_KING)
        {
            int num = Integer.parseInt(playerTwoScore.getText());
            this.playerTwoScore.setText(Integer.toString(++num));
        }
        if(!wasReceived)
            send("UPDATE: " + player);
    }

    private void clearData()
    {
        for (var img : this.data.getImages(Data.MOVE))
        {
            if(img != null)
                this.selectMap.get(img).highlight(0);
        }

        for (var img : this.data.getImages(0))
        {
            if(img != null)
                this.selectMap.get(img).highlight(0);
        }

        this.data.clear(Data.MOVE);
        this.data.clear(0);
    }

    private void moveChecker(ImageInfo start, ImageInfo end, boolean wasReceived, boolean fromElimination)
    {
        start.highlight(0);
        end.highlight(0);
        end.image.setImage(start.image.getImage());
        end.type = start.type;
        start.image.setImage(transparent);
        start.type = Type.NIL;

        int[] positions = checkAndCrown(start, end);
        checkAndSendMoveMessage(positions, wasReceived, fromElimination);

        if(this.playSfx.get())
            new MediaPlayer(this.media.get(0)).play();
    }

    private void checkAndSendMoveMessage(int[] positions, boolean wasReceived, boolean fromElimination)
    {
        if(!wasReceived)
        {
            if (!fromElimination)
            {
                positions[0] = this.size - 1 - positions[0];
                positions[1] = this.size - 1 - positions[1];
                positions[2] = this.size - 1 - positions[2];
                positions[3] = this.size - 1 - positions[3];
                send("MOVE: " + positions[0] + " " + positions[1] + " " + positions[2] + " " + positions[3]);
            }
            swapTurns();
            send("SWAP");
        }
    }

    private int[] checkAndCrown(ImageInfo start, ImageInfo end)
    {
        int[] positions =
                {
                        (int) (start.image.getLayoutY() * INVERSE_IMAGE_SIZE),
                        (int) (start.image.getLayoutX() * INVERSE_IMAGE_SIZE),
                        (int) (end.image.getLayoutY() * INVERSE_IMAGE_SIZE),
                        (int) (end.image.getLayoutX() * INVERSE_IMAGE_SIZE)
                };
        int pos = getPosition(positions[2], positions[3]);
        for (int num : Checker.kingPositions)
        {
            if (num == pos)
            {
                ImageInfo.crownKing(end, playerOneKing, playerTwoKing);
                break;
            }
        }
        return positions;
    }

    private int getPosition(int row, int col)
    {
        if (row % 2 == 0)
            return (row * GRIDS_PER_ROW) + (col / 2);
        else
            return ((row * GRIDS_PER_ROW) - 1) + ((col + 1) / 2);
    }

    private void checkAndAddMoves(int row, int col)
    {
        if (row < 0 || row > this.size - 1 || col < 0 || col > this.size - 1)
            return;

        int pos = getPosition(row, col);

        ImageInfo info = this.selectMap.get(this.imageViews[pos]);
        if (info.type != Type.NIL)
            return;

        this.data.addImage(info.image, Data.MOVE);
    }

    private void evaluateMoves(ImageInfo info)
    {
        int col = (int) (info.image.getLayoutX() * INVERSE_IMAGE_SIZE);
        int row = (int) (info.image.getLayoutY() * INVERSE_IMAGE_SIZE);

        checkAndAddMoves(row - 1, col + 1); // upper right
        checkAndAddMoves(row - 1, col - 1); // upper left

        evaluateElimination(col, row, otherPlayer);
        evaluateElimination(col + 2, row + 2, otherPlayer);
        evaluateElimination(col + 2, row - 2, otherPlayer);
        evaluateElimination(col - 2, row - 2, otherPlayer);
        evaluateElimination(col - 2, row + 2, otherPlayer);
    }

    private void checkAndAddElimination(Type otherPlayer, int row, int col, int row2, int col2)
    {
        if (col2 < 0 || col2 > this.size - 1 || row2 < 0 || row2 > this.size - 1)
            return;

        int pos1 = getPosition(row, col), pos2 = getPosition(row2, col2);

        ImageInfo info1 = this.selectMap.get(this.imageViews[pos1]);
        ImageInfo info2 = this.selectMap.get(this.imageViews[pos2]);

        if (info2.type != Type.NIL || info1.type != otherPlayer)
            return;

        this.data.addImage(info1.image, 0);
        this.data.addImage(info2.image, Data.MOVE);
    }

    private void evaluateElimination(int col, int row, Type otherPLayer)
    {
        if(col < 0 || col > this.size - 1 || row < 0 || row > this.size - 1)
            return;

        int[] positions =
        {
                row - 1, col + 1, row - 1, col - 1, row + 1, col + 1, row + 1, col - 1,
                row - 2, col + 2, row - 2, col - 2, row + 2, col + 2, row + 2, col - 2
        }; // ur ul lr ll uur uul llr lll

        checkAndAddElimination(otherPLayer, positions[0], positions[1], positions[8], positions[9]);
        checkAndAddElimination(otherPLayer, positions[2], positions[3], positions[10], positions[11]);
        checkAndAddElimination(otherPLayer, positions[4], positions[5], positions[12], positions[13]);
        checkAndAddElimination(otherPLayer, positions[6], positions[7], positions[14], positions[15]);
    }

    private void eliminateChecker(ImageInfo player, ImageInfo opponent, ImageInfo space, boolean wasReceived)
    {
        checkAndSendEliminationMessage(player, opponent, space, wasReceived);
        moveChecker(player, space, wasReceived, true);

        opponent.highlight(0);
        opponent.image.setImage(transparent);
        opponent.type = Type.NIL;

        clearData();

        if(this.playSfx.get())
            new MediaPlayer(this.media.get(1)).play();
    }

    private void checkAndSendEliminationMessage(ImageInfo player, ImageInfo opponent, ImageInfo space, boolean wasReceived)
    {
        if(!wasReceived)
        {
            int[] position = new int[6];
            position[0] = this.size - 1 - (int) (player.image.getLayoutY() * INVERSE_IMAGE_SIZE);
            position[1] = this.size - 1 - (int) (player.image.getLayoutX() * INVERSE_IMAGE_SIZE);
            position[2] = this.size - 1 - (int) (space.image.getLayoutY() * INVERSE_IMAGE_SIZE);
            position[3] = this.size - 1 - (int) (space.image.getLayoutX() * INVERSE_IMAGE_SIZE);
            position[4] = this.size - 1 - (int) (opponent.image.getLayoutY() * INVERSE_IMAGE_SIZE);
            position[5] = this.size - 1 - (int) (opponent.image.getLayoutX() * INVERSE_IMAGE_SIZE);
            send("ELIMINATE: " + position[0] + " " + position[1] + " " + position[2] + " " +
                    position[3] + " " + position[4] + " " + position[5]);
            updateScore(this.currentPlayer, false);
        }
    }

    public void receiveMessage(String info)
    {
        Platform.runLater(() ->
        {
            if(info.startsWith("MESSAGE: "))
            {
                String text = info.substring(9);
                if(!text.isEmpty())
                    textArea.appendText(this.otherPlayerName.getText() + ": " + text + '\n');
            }
            else if(info.startsWith("NAME: "))
            {
                String text = info.substring(6);
                if(!text.isEmpty())
                    this.otherPlayerName.setText(text);
            }
            else if(info.startsWith("MOVE: "))
            {
                if(this.currentTurn)
                    return;

                String[] texts = info.substring(6).split(" ");
                if(texts.length == 4)
                {
                    int startRow = Integer.parseInt(texts[0]), startCol = Integer.parseInt(texts[1]);
                    int endRow = Integer.parseInt(texts[2]), endCol = Integer.parseInt(texts[3]);
                    ImageInfo start = this.selectMap.get(this.imageViews[getPosition(startRow, startCol)]);
                    ImageInfo end = this.selectMap.get(this.imageViews[getPosition(endRow, endCol)]);
                    moveChecker(start, end, true, false);
                }
            }
            else if(info.startsWith("UPDATE: "))
            {
                String text = info.substring(8);
                if(!text.isEmpty())
                {
                    Type type = Type.valueOf(text);
                    updateScore(type, true);
                }
            }
            else if(info.startsWith("ELIMINATE: "))
            {
                if(this.currentTurn)
                    return;

                String[] texts = info.substring(11).split(" ");
                if(texts.length == 6)
                {
                    int startRow = Integer.parseInt(texts[0]), startCol = Integer.parseInt(texts[1]);
                    int midRow = Integer.parseInt(texts[2]), midCol = Integer.parseInt(texts[3]);
                    int endRow = Integer.parseInt(texts[4]), endCol = Integer.parseInt(texts[5]);
                    ImageInfo player = this.selectMap.get(this.imageViews[getPosition(startRow, startCol)]);
                    ImageInfo opponent = this.selectMap.get(this.imageViews[getPosition(midRow, midCol)]);
                    ImageInfo space = this.selectMap.get(this.imageViews[getPosition(endRow, endCol)]);
                    eliminateChecker(player, opponent, space, true);
                }
            }
            else if(info.equals("SWAP"))
            {
                if(this.currentTurn)
                    return;

                swapTurns();
            }
            else if(info.equals("QUIT"))
                this.isConnected.set(false);
        });
    }

    private void evaluateKingMoves(ImageInfo info)
    {
        int x = (int) (info.image.getLayoutX() * INVERSE_IMAGE_SIZE);
        int y = (int) (info.image.getLayoutY() * INVERSE_IMAGE_SIZE);

        checkDiagonal(y, x, UPPER_RIGHT);
        checkDiagonal(y, x, UPPER_LEFT);
        checkDiagonal(y, x, LOWER_RIGHT);
        checkDiagonal(y, x, LOWER_LEFT);
    }

    private void highlight()
    {
        if (!this.showHints)
            return;

        this.data.trim(Data.MOVE);
        this.data.trim(0);

        ImageView[] moves = this.data.getImages(Data.MOVE), eliminations = this.data.getImages(0);

        if (moves != null)
        {
            for (var img : moves)
            {
                ImageInfo info = selectMap.get(img);
                info.highlight(ImageInfo.SELECT);
            }
        }

        if (eliminations != null)
        {
            for (var img : eliminations)
            {
                ImageInfo info = selectMap.get(img);
                info.highlight(ImageInfo.SELECT_ELIMINATION);
            }
        }
    }

}
