package org.server;

//import org.renata.client.Figure;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameServer {
    private static int MaxFigs=100;
    private static int MaxNFig= 30; //Figure.getNumOfPossibleFigures();
    /** максимальное к-во соединений (не равно к-ву игроков в игре) */
    private static int MaxClients = 2; // на будущее -- очередь на игру ?

    // максимальное время игры, сек
    int maxGameTime=30;

    // серверный порт (переделать из main)
    int port=5000;

    int gamePlayers=MaxClients;

    // максимальный ID клиента
    int max_id=0;

    int n_interrupted=0; // к-во игроков которые отвалились (нажали на крестик или клиент убился)

    boolean game_in_process=false;
    // последовательность фигур для игры
    protected int figs[];

    // подключенные клиенты
    Vector<Worker> workers;

    // результаты игр
    Map<Integer, GameResult> results;

    private ServerSocket srvsock;

    int getNextFig(int n_fig){
        return figs[n_fig];
    }

    // вызывается когда подключились n клиентов
    void startGame() throws IOException {
        // сгенерировать фигур
        Random random = new Random();
        figs=random.ints(MaxFigs, 0, MaxNFig)
                .toArray();

        results=new TreeMap<>();
        n_interrupted=0;
        game_in_process=true;

        // пнуть клиентов
        LinkedList<String> players=new LinkedList<>();
        for (Worker w: workers) {
            players.add(w.name);
        }
        for (Worker w: workers) {
            w.startGame(players,maxGameTime);
        }
    }



    // вызывается само когда закончится время или все заакончились
    void gameStopped(int worker_id, GameResult res, boolean interrupted) {
        if (!game_in_process) return; // двойная страховка
        // сейчас если клиент отвалился нештатно или нажал на крестик - его результат не учитывается
        if (interrupted) n_interrupted++;
        else {
            if (results.containsKey(worker_id)) {
                System.out.println("Re-submit record from " + worker_id);
                return;
            } else {
                System.out.println("submit record from " + worker_id);
                results.put(worker_id, res);
            }
        }
        // проверка что все завершились
        int n_playing=0; // к-во игроков у которых не завершена игра штатно

        for(Worker w: workers)
            if (w.isGame_in_process()) n_playing++;

        System.out.println(" playing:"+n_playing+" interrpupted:"+n_interrupted);
        if ( !(gamePlayers>1 && gamePlayers-n_interrupted==1) && n_playing>0 ) return;

        game_in_process=false;
        // если все кроме 1 закончили:
        System.out.println("Game Over");
        // строим отч0т
        GameResult rep[]=results.values().toArray(new GameResult[0]);

        Arrays.sort(rep, new Comparator<GameResult>() {
            @Override
            public int compare(GameResult o1, GameResult o2) {
                if (o1.figs()<o2.figs()) return 1;
                else if (o1.figs()>o2.figs()) return -1;
                else {
                    if (o1.secs()<o2.secs()) return 1;
                    else if (o1.secs()>o2.secs()) return -1;
                }
                return 0;

            }
        });

        StringBuilder sb=new StringBuilder();
        sb.append("Game results:\n\n");
        for(GameResult r : rep) {
            sb.append(r.name()+": "+r.figs()+"  "+r.secs()+"s\n");
        }
        System.out.println(sb.toString());
        for (Worker c : workers) {
            c.finishGame(sb.toString());
        }
        workers.clear(); // новые игроки будут присоединяться заново
        // TODO: продолжить или выйти.

    }

    GameServer(String arg[]) {
        // TODO: анализ параметров командной строки
        workers =new Vector<>();
    }

    void run() throws IOException {
        srvsock=new ServerSocket(port);
        //srvsock.setSoTimeout(10000);
        while (true) {
            Socket sock = srvsock.accept();
            sock.setSoTimeout(1000);
            if (workers.size()>MaxClients) {
                sock.close();
            } else {
                Worker worker =new Worker(max_id,sock, this);
                max_id++;
                workers.add(worker);
                Thread t = new Thread(worker);
                t.start();
            }
            System.out.println("Clients: "+ workers.size());
            if (workers.size()>=gamePlayers) {
                try { // TODO: придумать лучше
                    Thread.sleep(10); // чтобы клиенты успели передать свои имена
                } catch (Exception e) {}
                startGame();
            }
        }
    }

    /**
     * вызывается
     * @param c
     */
    public void removeClient(Worker c) {
        workers.remove(c);
    }

    public static void main(String[] arg) {
        GameServer server=new GameServer(arg);
        try {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
