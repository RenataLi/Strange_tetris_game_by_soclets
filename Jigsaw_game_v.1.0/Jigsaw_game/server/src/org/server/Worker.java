package org.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

// поток на сервере общающийся с клиентом
public class Worker implements Runnable {

    // гнездо для общения с клиентом
    protected Socket sock ;
    protected DataInputStream in;
    protected DataOutputStream out;


    protected GameServer server;

    protected String name;
    protected LocalTime game_started;

    // сколько фигур он успел сыграть
    protected int n_fig;

    private int n_placed_figures;
    private int total_time=0;

    public boolean isGame_in_process() {
        return (sock.isConnected() && game_in_process );
    }


    // разослать всем отчет об игре
    public void finishGame(String res) {
        try {
            System.out.println(name + " REPORT");
            out.writeUTF("REPORT " + res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean game_in_process;

    public boolean isGame_interrupted() {
        return game_interrupted;
    }

    private boolean game_interrupted=false;

    protected int id;

    public Worker(int id, Socket sock, GameServer server) {
        this.server=server;
        this.id=id;
        this.sock=sock;
        game_in_process=false;
        game_interrupted=false;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());

            String cmd = in.readUTF();
            // ожидаем команду LOGIN name
            if (!cmd.startsWith("LOGIN ")) return;
            name=cmd.substring(6);
            out.writeUTF("OK");
            System.out.println(name + " connected from "+sock.getRemoteSocketAddress());
            // TODO: придумать когда можно ответить по-другому и надо ли

            // начинаем игру
            n_fig=0;
            while (true) {
                try {
                    cmd = in.readUTF();
                } catch (SocketTimeoutException e) {
                    continue;
                }
                // анализ CMD
                if (cmd.equals("QUIT")) {
                    System.out.println(name + " QUIT");
                    game_interrupted=true;
                    stop(true);
                    break; // нажал прервать
                }
                if (cmd.startsWith("FINISH ")) {
                    System.out.println(name + " "+ cmd);
                    // закончил игру
                    int ind1=cmd.indexOf(' ');
                    int ind2=cmd.indexOf(' ',ind1+1);
                    n_placed_figures=Integer.valueOf(cmd.substring(ind1+1, ind2));
                    total_time=Integer.valueOf(cmd.substring(ind2+1));
                    stop(false);
                    //break;
                    //TODO: ожидание команды продолжить ?
                }
                if (cmd.equals("GET")) {
                    if (!game_in_process) return;
                    int fig=server.getNextFig(n_fig);
                    n_fig++;
                    out.writeUTF("FIGURE "+fig);
                }
            }
            sock.close();
            server.removeClient(this);
            System.out.println(name + "disconnected");
        }
        catch (EOFException e) {
            System.out.println(name + "connection close");
            server.removeClient(this);
            game_interrupted=true;
            stop(true);
            return;
        }
        catch (SocketException e) {
            System.out.println(name + "connection broke");
            server.removeClient(this);
            game_interrupted=true;
            stop(true);
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
            server.removeClient(this);
            game_interrupted=true;
            stop(true);
            try {
                sock.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

    }

    /**
     * доложить серверу что клиент закончил или прекратил игру
     */
    public void stop(boolean interrupted) {
        if (!game_in_process) return;
        if (total_time==0) {
            total_time = (int) Duration.between(game_started, LocalTime.now()).toSeconds();
        }
        game_in_process = false; // сначала помечаем что игра у нашего клиента остановилась
        server.gameStopped(id, new GameResult(name, n_placed_figures, total_time), interrupted);
    }



    public void startGame(List<String> players,int secs) throws IOException {
        StringBuilder cmd = new StringBuilder("START ");
        cmd.append(secs);
        cmd.append(" ");
        for (String name: players) {
            cmd.append(name);
            cmd.append(";");
        }
        cmd.deleteCharAt(cmd.length()-1);
        //  START t name1;name2
        System.out.println("Send to "+name+":"+cmd.toString());
        game_started=LocalTime.now();
        game_in_process = true;
        try {
            out.writeUTF(cmd.toString());
            game_started = LocalTime.now();
        } catch (Exception e) {
            e.printStackTrace();
            game_interrupted=true;
            game_in_process=false;
            stop(true);
        }

    }


    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
