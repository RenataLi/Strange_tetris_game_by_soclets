package org.client;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class GameController implements Initializable {

    public static String serverName="localhost";
    public static int serverPort=5000;

    // TODO: вот это всё в отдельный класс GAME!
    Field field;
    Figure newFig;

    // время когда игра началась
    LocalTime startTime;

    // время когда игра должна закончиться
    LocalTime finishTime;
    Timer timer;

    Socket sock;
    DataInputStream in;
    DataOutputStream out;

    boolean gameStopped=true;

    int n_figures; // количество фигур расставленных за 1 игру

    private Stage stage;

    @FXML
    private BorderPane root;

    @FXML
    private HBox hbox;

    @FXML
    private Canvas canvasField;

    @FXML
    private Canvas canvasHighlightField;

    @FXML
    private Canvas canvasNext;

    @FXML
    private Label labelWatch;


    @FXML
    private Label labelPlayers;

    @FXML
    private Label labelName;

    private String name;

    @FXML
    private Label labelMaxTime;

    @FXML
    private Button buttonRestart;

    public GameController() {
        field = new Field();
        //newFig = Figure.getRandomFigure();
        name="Player1";
        n_figures=0;
    }

    private void timerRestart() {
        startTime = LocalTime.now();
        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameStopped) this.cancel();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        onTimer();
                    }
                });
            }
        }, 0, 1000);
    }

    private void timerStop() {
        //timer.cancel();
        timer.purge();
    }

    @FXML
    private void onButtonRestart() {
        finishGame();

    }

    public void setStage(Stage st) {
        stage=st;
        stage.setOnCloseRequest(windowEvent -> {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Are you sure?",
                    ButtonType.YES,ButtonType.NO);
            Optional<ButtonType> res = alert.showAndWait();
            if (res.get()==ButtonType.NO) return;
            try {
                if (out != null ) out.writeUTF("QUIT");
            } catch (Exception e) {
                e.printStackTrace();
            }
            close();
        });
    }

    private void getNextFigure() {
        try {
            System.out.println("GET");
            out.writeUTF("GET");
            String res = in.readUTF();
            System.out.println(res);
            if (res.startsWith("FIGURE ")){
                int nfig=Integer.valueOf(res.substring(7));
                newFig = Figure.getFigure(nfig);
            } else if (res.startsWith("REPORT ")) {
                // пришел досрочный отчет об окончании игры
                gameStopped=true;
                String msg=res.substring(7);
                Alert al = new Alert(Alert.AlertType.NONE,
                        msg, ButtonType.OK );
                al.setTitle("ПОБЕДИТЕЛИ!");
                al.showAndWait();
                al = new Alert(Alert.AlertType.CONFIRMATION,
                        "Начать снова или выйти?",
                        ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> ans=al.showAndWait();
                if (ans.get()==ButtonType.NO) {
                    close();
                } else {
                    newGame();
                }
            } else {
                System.out.println("Wrong server response: "+res);
                Platform.exit();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void processNextMove() {
        //newFig = Figure.getRandomFigure();
        getNextFigure();
        redrawNextFigure();
        if (!field.canPlace(newFig)) {
            finishGame();
        }
    }

    private void finishGame() {
        if (gameStopped) return;
        gameStopped=true;
        timerStop();
        long total_time=Duration.between(startTime, LocalTime.now()).toSeconds();
        try {
            out.writeUTF("FINISH " + n_figures +" "+ total_time);
            String res=in.readUTF();
            if (res.startsWith("REPORT ")) {
                String msg=res.substring(7);
                Alert al = new Alert(Alert.AlertType.NONE,
                        msg, ButtonType.OK );
                al.setTitle("ПОБЕДИТЕЛИ!");
                al.showAndWait();
            }
            Alert msg = new Alert(Alert.AlertType.CONFIRMATION,
                    "Начать снова или выйти?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> ans=msg.showAndWait();
            if (ans.get()==ButtonType.NO) {
                close();
            } else {
                newGame();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Platform.exit();
            close();
        }
    }

    private boolean newGame() {
        field.clean();
        redrawField();
        redrawNextFigure();
        n_figures=0;
        TextInputDialog dlg = new TextInputDialog(name);
        dlg.setHeaderText("Player name");
        dlg.setTitle("");

        Optional<String> result = dlg.showAndWait();
        if (result.isEmpty()) {
            System.exit(1);
        }
        name = result.get();
        labelName.setText(name);

        try {
            if (sock!=null && sock.isConnected()) {
                sock.close();
            }
            sock = new Socket(serverName, serverPort);
        } catch (ConnectException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Failed to connect to server! Retry?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> ans = alert.showAndWait();
            if (ans.get() == ButtonType.NO) {
                //System.exit(1);
                Platform.exit();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
        if (!sock.isConnected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Failed to connect to server! Retry?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> ans = alert.showAndWait();
            if (ans.get() == ButtonType.NO) {
                //System.exit(1);
                Platform.exit();
            } else {
                return false;
            }
        }

        try {
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Communication failed with server! Retry?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> ans = alert.showAndWait();
            if (ans.get() == ButtonType.NO) {
                //System.exit(1);
                Platform.exit();
            } else {
                return false;
            }
        }

        String res="";
        try {
            out.writeUTF("LOGIN " + name);
            res = in.readUTF();
            if (!res.equals("OK")) {
                System.out.println("Server response not OK. the answer is:" + res);
                Platform.exit();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Communication failed with server! Retry?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> ans = alert.showAndWait();
            if (ans.get() == ButtonType.NO) {
                //System.exit(1);
                Platform.exit();
            } else {
                return false;
            }
        }
        System.out.println("OK");

        // ожидание команды START от сервера запускаем в новом потоке чтобы
        // не блокировался интерфейс клиента.
        Task t = new WaitForStartTask();

        Thread th = new Thread(t);
        th.setDaemon(false);
        th.start();

        return true;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //boolean fit=field.isFit(Figure.getFigure(0),1,0);
        hbox.widthProperty().addListener( observable -> onResize());
        hbox.heightProperty().addListener( observable -> onResize());
        canvasField.widthProperty().addListener(observable -> redrawField());
        canvasField.heightProperty().addListener(observable -> redrawField());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                while (!newGame());
            }
        });

    }

    private void startGame() {
        gameStopped=false;
        System.out.println("game starts.");
        getNextFigure();
        startTime=LocalTime.now();
        timerRestart();
        n_figures=0;
        redrawField();
        redrawNextFigure();

    }

    private void close() {
        gameStopped=true; // чтобы таймер самоубился
        if (timer != null ) {
            timer.cancel();
            timer.purge();
        }
        try {
            if (sock !=null && sock.isConnected()) sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    private void onResize() {
        double x = Math.min(hbox.getWidth() - 40,hbox.getHeight() - 40);
        canvasField.setWidth(x);
        canvasField.setHeight(x);
        canvasHighlightField.setWidth(x);
        canvasHighlightField.setHeight(x);
        redrawField();
    }

    private double getFieldCellSize() {
        return canvasField.getWidth()/Field.Ncells;
    }


    @FXML
    private void onDragStart(MouseEvent ev) {
        if (gameStopped) return;
        double x=ev.getX();
        double y=ev.getY();
        canvasNext.startFullDrag();
        //System.out.println("X="+x+" y="+y);

    }

    @FXML
    private void onDragMove(MouseEvent ev) {
        if (gameStopped) return;
        GraphicsContext gc = canvasHighlightField.getGraphicsContext2D();
        gc.clearRect(0,0,canvasHighlightField.getWidth(),canvasHighlightField.getHeight());

        gc.setStroke(Color.YELLOWGREEN);
        gc.setLineWidth(4);
        double sz=getFieldCellSize();
        int xc = (int)Math.floor(ev.getX()/sz);
        int yc = (int)Math.floor(ev.getY()/sz);
        for (int i=0; i< newFig.getNBlocks(); i++) {
            gc.strokeRect(sz*(xc+newFig.getBlock(i).x()),
                    sz*(yc+newFig.getBlock(i).y()),
                    sz,sz);
        }
    }

    @FXML
    private void onDragStop(MouseEvent ev) {
        if (gameStopped) return;
        GraphicsContext gc = canvasHighlightField.getGraphicsContext2D();
        gc.clearRect(0,0,canvasHighlightField.getWidth(),canvasHighlightField.getHeight());
        double sz=getFieldCellSize();
        int xc = (int)Math.floor(ev.getX()/sz);
        int yc = (int)Math.floor(ev.getY()/sz);
        //System.out.println("Drop: x="+xc+" y="+yc);

        // TODO: отделить логику в отдельное место ?
        if (field.isFit(newFig,xc,yc)) {
            field.place(newFig, xc, yc);
            n_figures++;
            redrawField();
            processNextMove();

        }

    }


    private void onTimer() {
        long seconds = Duration.between(startTime, LocalTime.now()).toSeconds();
        labelWatch.setText(String.format("%02d:%02d",seconds/60, seconds%60));
        if (LocalTime.now().isAfter(finishTime)) {
            finishGame();
        }
        if (gameStopped) timer.cancel();
    }

    private void redrawField() {
        double w = canvasField.getWidth();
        double h = canvasField.getHeight();
        int nC = Field.Ncells;
        GraphicsContext gc = canvasField.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.WHEAT);
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.BLUEVIOLET);
        gc.setLineWidth(0.5);
        gc.setLineDashes(2);
        for (int i = 0; i <= nC; i++) {
            gc.strokeLine((i * w) / nC, 0, (i * w) / nC, h);
        }
        for (int i = 0; i <= 9; i++) {
            gc.strokeLine(0, (i * h) / nC, w, (i * h) / nC);
        }
        gc.setLineDashes();
        gc.setLineWidth(3);
        gc.strokeRect(0, 0, w, h);

        // Figure:
        gc.setStroke(Color.ROSYBROWN);
        gc.setFill(Color.DARKSLATEBLUE);
        gc.setLineDashes();
        gc.setLineWidth(2);
        for (int i = 0; i < nC; i++)
            for (int j = 0; j < nC; j++)
                if (field.get(i,j)==1) {
                    gc.fillRect((i * w) / nC, (j * h) / nC, w / nC, h / nC);
                    gc.strokeRect((i * w) / nC, (j * h) / nC, w / nC, h / nC);
                }
    }

    private void redrawNextFigure() {
        double w= canvasNext.getWidth();
        double h=canvasNext.getHeight();
        GraphicsContext gc = canvasNext.getGraphicsContext2D();

        // BackGround:
        gc.clearRect(0,0,w,h);
        gc.setFill(Color.WHEAT);
        gc.fillRect(0,0,w,h);
        gc.setStroke(Color.BLUEVIOLET);
        gc.setLineWidth(0.5);
        gc.setLineDashes(2);
        for (int i=0; i<=3 ; i++) {
            gc.strokeLine((i*w)/3, 0, (i*w)/3, h);
        }
        for (int i=0; i<=3 ; i++) {
            gc.strokeLine(0, (i*h)/3, w, (i*h)/3);
        }
        gc.setLineDashes();
        gc.setLineWidth(3);
        gc.strokeRect(0,0,w,h);

        // Figure:
        if (gameStopped) return;

        gc.setStroke(Color.ROSYBROWN);
        gc.setFill(Color.DARKSLATEBLUE);
        gc.setLineDashes();
        gc.setLineWidth(2);
        for(int i=0; i<newFig.getNBlocks(); i++) {
            Block block=newFig.getBlock(i);
            double x=((block.x()+1)*w)/3;
            double y=((block.y()+1)*h)/3;
            gc.fillRect(x,y,w/3,h/3);
            gc.strokeRect(x,y,w/3,h/3);
        }
    }


//    // задача для ожидания команды REPORT от сервера
//    // REPORT может прийти во время игры (если другие игроки отвалятся)
//    // или после окончания игры (пока ждем пока другие игроки закончат)
//    private class WaitForReportTask extends Task<Void> {
//        @Override
//        protected Void call() throws Exception {
//            // принять REPORT
//            try {
//                gameStopped=true;
//                String res=in.readUTF();
//                if (res.startsWith("REPORT ")) {
//                    String msg=res.substring(7);
//                    Alert al = new Alert(Alert.AlertType.NONE,
//                            msg, ButtonType.OK );
//                    al.setTitle("ПОБЕДИТЕЛИ!");
//                    al.showAndWait();
//                }
//                Alert msg = new Alert(Alert.AlertType.CONFIRMATION,
//                        "Начать снова или выйти?",
//                        ButtonType.YES, ButtonType.NO);
//                Optional<ButtonType> ans=msg.showAndWait();
//                if (ans.get()==ButtonType.NO) {
//                    close();
//                } else {
//                    newGame();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                //Platform.exit();
//                close();
//            }
//            return null;
//        }
//    }



    // задача для ожидания команды START от сервера
    private class WaitForStartTask extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            // принять START
            String res="";
            res = in.readUTF();
            if (!res.startsWith("START ")) {
                System.out.println("Server sent not START. the command is:"+res);
                close();
                return null;
            }
            System.out.println(res);
            int ind=res.indexOf(' ',6);
            if (ind==-1) {
                System.out.println("START format is bad: expected START nnn name1;name2;:"+res);
                close();
                return null;
            }
            int time_secs=Integer.valueOf(res.substring(6,ind));
            finishTime= LocalTime.now().plusSeconds(time_secs);
            String total_time_msg="Общее время:"+String.format("%02d:%02d",time_secs/60, time_secs%60);
            String names[]=res.substring(ind+1).split(";");
            StringBuilder sb=new StringBuilder();
            for(String n : names) {
                if (!n.equals(name)) {
                    sb.append(n);
                    sb.append("\n");
                }
            }
            String names_str="Соперники: " + sb.toString();
            System.out.println(names_str);

            // эта задача должна выполнится в потоке UI
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    labelMaxTime.setText(total_time_msg);
                    labelPlayers.setText(names_str);
                    startGame();
                }
            });
            return null;
        }
    }
}