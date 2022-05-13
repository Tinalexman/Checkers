package checkers;


import animatefx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable
{
    private Checker checker;

    @FXML
    private TextArea messageView;

    @FXML
    private CheckBox hintBox, musicBox, sfxBox;

    @FXML
    private TextField inputMessage, nameField;

    @FXML
    private Button playerOneBtn, playerTwoBtn;

    @FXML
    private Label playerOneScore, playerTwoScore, playerOneLabel, playerTwoLabel, turnLabel;

    @FXML
    private AnchorPane gamePane, menuPane, settings, rootPane, optionPane;

    @FXML
    private ImageView img1, img2, img3, img4, img5, img6, img7, img8, img9, img10;

    @FXML
    private ImageView img11, img12, img13, img14, img15, img16, img17, img18, img19, img20;

    @FXML
    private ImageView img21, img22, img23, img24,img25, img26, img27, img28, img29, img30;

    @FXML
    private ImageView img31, img32, img33, img34, img35, img36, img37, img38, img39, img40;

    @FXML
    private ImageView img41, img42, img43, img44, img45, img46, img47, img48, img49, img50;

    @FXML
    private Group group;

    private MediaPlayer musicPlayer;

    private static final double SLOW_SPEED = 0.375;
    private static final double FAST_SPEED = 0.75;

    private boolean isShowingHelpMenu = false;

    private String currentPlayerName;

    private List<Media> mediaList;

    @FXML
    public void apply(ActionEvent event)
    {
        this.menuPane.toFront();
        new BounceIn(this.menuPane).setSpeed(SLOW_SPEED).play();
        new SlideInLeft(this.playerOneBtn).setSpeed(SLOW_SPEED).play();
        new SlideInRight(this.playerTwoBtn).setSpeed(SLOW_SPEED).play();
        new SlideInUp(this.nameField).setSpeed(SLOW_SPEED).play();
        if(event != null)
            event.consume();
    }

    @FXML
    public void createGame(ActionEvent event)
    {
        final int PORT_ONE = 2807, PORT_TWO = 7082;

        Button button = (Button) event.getSource();

        String defaultName;
        if(button.equals(playerOneBtn))
        {
            this.checker = new Checker(Type.PLAYER_ONE);

            defaultName = "Player One";

            if(this.nameField.getText().isEmpty())
                this.nameField.setText(defaultName);

            NetworkManager.init(PORT_ONE, this.checker);

            this.playerOneLabel.textProperty().bind(this.nameField.textProperty());
            this.checker.setControls(this.messageView, this.turnLabel, playerOneScore, playerTwoScore, playerTwoLabel);
        }
        else
        {
            this.checker = new Checker(Type.PLAYER_TWO);

            defaultName = "Player Two";

            if(this.nameField.getText().isEmpty())
                this.nameField.setText(defaultName);

            NetworkManager.init(PORT_TWO, this.checker);

            this.playerTwoLabel.textProperty().bind(this.nameField.textProperty());
            this.checker.setControls(this.messageView, this.turnLabel, playerOneScore, playerTwoScore, playerOneLabel);
        }

        this.currentPlayerName = this.nameField.getText();

        NetworkManager.send("NAME: " + this.currentPlayerName);

        this.hintBox.selectedProperty().addListener(observable ->
            this.checker.showHints = this.hintBox.isSelected());

        ImageInfo.setPane(gamePane);
        this.checker.setSfx(this.mediaList);
        this.checker.setImages(
                img1, img2, img3, img4, img5, img6, img7, img8, img9, img10,
                img11, img12, img13, img14, img15, img16, img17, img18, img19, img20,
                img21, img22, img23, img24, img25, img26, img27, img28, img29, img30,
                img31, img32, img33, img34, img35, img36, img37, img38, img39, img40,
                img41, img42, img43, img44, img45, img46, img47, img48, img49, img50);

        this.checker.isConnected.addListener((observableValue, oldValue, newValue) ->
        {
            if(oldValue && !newValue)
            {
                this.checker = null;
                this.menuPane.toFront();
            }
        });

        this.checker.playSfx.bind(this.sfxBox.selectedProperty());

        showGame();
        this.checker.swapTurns();
    }

    @FXML
    public void sendMessage(ActionEvent event)
    {
        String text = inputMessage.getText();
        if(text.isEmpty())
            return;

        this.messageView.appendText(this.currentPlayerName + ": " + text + '\n');

        this.inputMessage.setText("");
        NetworkManager.send("MESSAGE: " + text);
        event.consume();
    }

    @FXML
    public void focusPane(MouseEvent event)
    {
        this.settings.requestFocus();
        event.consume();
    }

    private void showSetting()
    {
        this.settings.toFront();
        new RollIn(this.settings).setSpeed(FAST_SPEED).play();
        new ZoomIn(this.nameField).setSpeed(SLOW_SPEED).play();
        new Shake(this.hintBox).setSpeed(SLOW_SPEED).play();
        new Shake(this.sfxBox).setSpeed(SLOW_SPEED).play();
        new Shake(this.musicBox).setSpeed(SLOW_SPEED).play();
    }

    @FXML
    public void showSettings(KeyEvent event)
    {
        if(event.isAltDown())
        {
            this.musicPlayer.play();
            if(checker == null)
            {
                if(this.isShowingHelpMenu)
                    apply(null);
                else
                    showSetting();
            }
            else
            {
                if(this.nameField.getText().isEmpty())
                    this.nameField.setText(this.currentPlayerName);

                this.nameField.setEditable(false);
                if(!this.isShowingHelpMenu)
                    showSetting();
                else
                    showGame();
            }
            this.isShowingHelpMenu = !this.isShowingHelpMenu;
        }
        event.consume();
    }

    @FXML
    public void quit(Event event)
    {
        NetworkManager.send("QUIT");
        this.menuPane.toFront();

        AnimationFX fx = new RubberBand(this.rootPane);
        fx.setSpeed(FAST_SPEED);
        fx.setOnFinished(e ->
        {
            this.musicPlayer.stop();
            Platform.exit();
            System.exit(0);
        });
        fx.play();
        event.consume();
    }

    private void showGame()
    {
        this.musicPlayer.pause();
        this.gamePane.toFront();
        new FadeIn(group).setSpeed(SLOW_SPEED).play();
        new JackInTheBox(group).setSpeed(FAST_SPEED).play();
        new RotateIn(this.optionPane).setSpeed(FAST_SPEED).play();
        new Pulse(this.optionPane).setSpeed(SLOW_SPEED).play();
    }

    @FXML
    public void pickChecker(MouseEvent event)
    {
        this.group.requestFocus();
        double xPosition = event.getSceneX(), yPosition = event.getSceneY();
        int row = (int) (yPosition * Checker.INVERSE_IMAGE_SIZE), col = (int) (xPosition * Checker.INVERSE_IMAGE_SIZE);
        int xRemainder = (int) (xPosition) % 100;

        if((row % 2 == 0) && (xRemainder >= 0 && xRemainder <= 49) ||
                (row % 2 == 1) && (xRemainder >= 50 && xRemainder <= 99))
            this.checker.pickAndMove(row, col);
        event.consume();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        try
        {
            String path = System.getProperty("user.dir") + "src\\checkers\\";
            String music = "Tears.mp3", move = "move.wav",
                    eliminate = "eliminate.wav", intro = "intro.wav";
            Media media = new Media(new File(path + music).toURI().toString());
            this.mediaList = new ArrayList<>();
            this.mediaList.add(new Media(new File(path + move).toURI().toString()));
            this.mediaList.add(new Media(new File(path + eliminate).toURI().toString()));
            this.mediaList.add(new Media(new File(path + intro).toURI().toString()));
            this.musicPlayer = new MediaPlayer(media);
            this.musicPlayer.setAutoPlay(true);
            this.musicPlayer.setOnEndOfMedia(() -> this.musicPlayer.seek(Duration.ZERO));
            this.musicPlayer.setVolume(0.75);
            this.musicBox.selectedProperty().addListener((observable ->
                    this.musicPlayer.setMute(!this.musicBox.isSelected())));
            this.musicPlayer.play();
        }
        catch (Exception ignored)
        {

        }
        apply(null);
    }

    @FXML
    public void highlight(MouseEvent event)
    {
        Button button = (Button) event.getSource();
        button.setTextFill(Color.BLACK);
        button.setStyle("-fx-background-color: rgb(255, 255, 255);");
    }

    @FXML
    public void unhighlight(MouseEvent event)
    {
        Button button = (Button) event.getSource();
        button.setTextFill(Color.web("#EAEAEA"));
        button.setStyle("-fx-background-color: rgba(255, 255, 255, 0);");
    }

}
