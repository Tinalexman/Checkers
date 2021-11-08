package checkers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class ImageInfo
{
    public ImageView image;
    public Type type;

    private final Line up;
    private final Line right;
    private final Line left;
    private final Line down;
    private Color color;

    public static final int SELECT_TARGET = 1;
    public static final int SELECT = 2;
    public static final int SELECT_ELIMINATION = 3;
    private static final double LINE_WIDTH = 3;

    private static AnchorPane rootPane;

    public ImageInfo(ImageView image, Type type)
    {
        this.image = image;
        this.type = type;

        int xPos = (int) this.image.getLayoutX(), yPos = (int) this.image.getLayoutY();
        this.color = Color.TRANSPARENT;
        this.up = new Line(xPos, yPos, xPos + 50, yPos);
        this.up.setStrokeWidth(LINE_WIDTH);
        this.up.setStroke(this.color);
        this.right = new Line(xPos + 50, yPos, xPos + 50, yPos + 50);
        this.right.setStrokeWidth(LINE_WIDTH);
        this.right.setStroke(this.color);
        this.down = new Line(xPos + 50, yPos + 50, xPos, yPos + 50);
        this.down.setStrokeWidth(LINE_WIDTH);
        this.down.setStroke(this.color);
        this.left = new Line(xPos, yPos + 50, xPos, yPos);
        this.left.setStrokeWidth(LINE_WIDTH);
        this.left.setStroke(this.color);

        rootPane.getChildren().addAll(up, right, down, left);
        this.image.toFront();
    }

    @Override
    public boolean equals(Object object)
    {
        if(object == null || object.getClass() != ImageInfo.class)
            return false;

        ImageInfo obj = (ImageInfo) object;
        return this.image == obj.image && this.type == obj.type;
    }

    public static void setPane(AnchorPane root)
    {
        rootPane = root;
    }

    public void highlight(int flag)
    {
        if(flag == SELECT_TARGET)
            this.color = Color.YELLOW;
        else if(flag == SELECT)
            this.color = Color.BLUE;
        else if(flag == SELECT_ELIMINATION)
            this.color = Color.GREEN;
        else
            this.color = Color.TRANSPARENT;

        this.up.setStroke(this.color);
        this.right.setStroke(this.color);
        this.down.setStroke(this.color);
        this.left.setStroke(this.color);
    }

    public static void crownKing(ImageInfo info, Image ... kingImages)
    {
        if(info.type == Type.PLAYER_ONE)
        {
            info.type = Type.PLAYER_ONE_KING;
            info.image.setImage(kingImages[0]);
        }
        else if(info.type == Type.PLAYER_TWO)
        {
            info.type = Type.PLAYER_TWO_KING;
            info.image.setImage(kingImages[1]);
        }
    }
}
