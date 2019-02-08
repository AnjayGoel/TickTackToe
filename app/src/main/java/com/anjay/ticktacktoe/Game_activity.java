package com.anjay.ticktacktoe;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

public class Game_activity extends AppCompatActivity {
    // views and others
    Button connect;
    TextView info;
    TextView view_bid;
    Context con;
    Button bid;
    TextView view_points;
    SeekBar set_bid;
    View main;
    int my_index = -1;
    int focus_view;
    TextView[][] text_view_arr;
    final encapsulate dh = new encapsulate();

    //networking
    boolean client_started;

    String local_ip;
    Handler h = new Handler();
    final client c_r = new client();
    final server s_r = new server();
    Thread serv_t = new Thread(s_r), client_t = new Thread(c_r);

    //classes needed
    void clear_and_restart() {
        dh.ttt_arr=new char[3][3];
        dh.players[0].bid=dh.players[1].bid=0;
        dh.players[0].points=dh.players[1].points=270;
        dh.players[0].bid_placed=dh.players[1].bid_placed=false;
        view_points.setText("Points Left: 270");
        update_and_render();

    }

    public class client implements Runnable {
        String server_ip = "";
        Socket server;
        boolean restart;
        int dh_prev_ver;
        boolean disconnect;
        boolean is_connected;
        ObjectOutputStream o_o_s;

        Socket find_host(int no_f_loops) {
            String subnet;
            long time = System.currentTimeMillis();
            subnet = local_ip;
            subnet = subnet.substring(0, subnet.lastIndexOf("."));
            if (!server_ip.equals("")) {
                Socket ss = new Socket();
                Log.d("abz", "serv at " + server_ip);
                for (int j = 0; j < 8; j++) {
                    for (int i = 0; i < 20; i++) {
                        try {
                            ss.connect(new InetSocketAddress(server_ip, 2311));
                            return ss;
                        } catch (IOException e) {
                        }
                    }
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("abz", "error while trying to connect");
                return null;

            }

            for (int i = 1; i < 255; i++) {
                for (int j = 0; j < no_f_loops; j++) {
                    try {
                        if ((subnet + "." + i).equals(local_ip)) continue;
                        Socket ss = new Socket();
                        ss.connect(new InetSocketAddress(subnet + "." + i, 2311), 25);
                        Log.d("abz", (System.currentTimeMillis() - time) + "");
                        return ss;
                    } catch (Exception ex) {
                        Log.d("zab", "error in client --->" + ex.getMessage());

                    }

                }

            }
            Log.d("abz", "not found time taken -->" + (System.currentTimeMillis() - time) + "");
            return null;
        }

        @Override
        public void run() {
            try {
                Log.d("abz", "client started");
                server = find_host(1);
                if (server == null) {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            connect.setText("Connect");
                        }
                    });
                    Log.d("abz", "responce null");
                    loop_before();
                    return;
                }
                o_o_s = new ObjectOutputStream(new DataOutputStream(server.getOutputStream()));
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        connect.setText("Disconnect");
                    }
                });
                is_connected = true;
                loop_after();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("abz", "error in client --->" + e.getMessage());
            }
        }

        void loop_before() {
            while (true) {

                if (restart) {
                    restart = false;
                    disconnect = false;
                    is_connected = false;
                    run();
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log.d("abz", e.toString());
                }
            }

        }

        void loop_after() {
            Log.d("abz", "inside client loop");

            while (true) {

                try {
                    if (disconnect) {
                        server.close();
                        disconnect = false;
                        restart = false;
                        is_connected = false;
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                connect.setText("Connect");
                            }
                        });
                        loop_before();
                        break;
                    }
                    if (dh.ver > dh_prev_ver) {
                        dh_prev_ver++;
                        dh.msg = " " + System.currentTimeMillis();

                        o_o_s.writeUnshared(dh);
                        o_o_s.flush();
                        Log.d("abz", " i played:- "+Arrays.toString(dh.box_played));
                        Log.d("abz", "sent :----  "+dh.current_player+" " + dh.players[0].toString() + "  ---- " + dh.players[1].toString() + " ver:- " + dh.ver);

                    }
                    //    o_o_s.writeUTF(""+System.currentTimeMillis());

                    Thread.sleep(100);
                } catch (Exception e) {
                    Log.d("abz", "in client ----->" + e.toString());
                }

            }
        }
    }

    public class server implements Runnable {
        Socket server;
        ServerSocket server_soc;
        DataOutputStream s_o_s;
        ObjectInputStream o_i_s;

        @Override
        public void run() {
            try {
                server_soc = new ServerSocket(2311);
                server = server_soc.accept();
                Log.d("abz", "serv started");
                String ip = server.getRemoteSocketAddress().toString();
                ip = ip.substring(1, ip.lastIndexOf(":"));
                c_r.server_ip = ip;
                Log.d("abz", "client ip is ---->" + server.getRemoteSocketAddress().toString());
                o_i_s = new ObjectInputStream(new DataInputStream(server.getInputStream()));
                s_o_s = new DataOutputStream(server.getOutputStream());
                loop();
            } catch (Exception e) {
                Log.d("abz", "in serv ----->" + e.toString());
            }

        }

        void loop() {
            while (true) {
                try {
                    //    final String msg = o_i_s.readUTF();
                    final encapsulate d = (encapsulate) o_i_s.readObject();
                    if (my_index == -1) my_index = 1;

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("abz", "recieved :- " + dh.players[0].toString() + "  ----   " + dh.players[1].toString() + " ver:-" + dh.ver);
                            c_r.dh_prev_ver++;
                            dh.update_obj(d);
                            info.setText(d.msg);
                            if (dh.msg_typ== encapsulate.Message_type.Bid){
                                Toast.makeText(con,"Opponent Placed The Bid",Toast.LENGTH_SHORT).show();
                                process_bids();
                            }
                            else  if (dh.msg_typ== encapsulate.Message_type.Turn){
                                dh.ttt_arr[dh.box_played[0]][dh.box_played[1]]=dh.players[1-my_index].initials;
                                update_and_render();
                                Toast.makeText(con, "Play Your Bids Now", Toast.LENGTH_SHORT).show();
                                dh.current_player = -1;
                                Log.d("abz", "turn played");
                                dh.players[0].bid = 0;
                                dh.players[1].bid = 0;
                                dh.players[0].bid_placed = false;
                                dh.players[1].bid_placed = false;
                            }     else  if (dh.msg_typ== encapsulate.Message_type.Lost_all_points){
                                Toast.makeText(con, "You Win,Opponent Lost All Points", Toast.LENGTH_LONG).show();
                                clear_and_restart();

                            }     else  if (dh.msg_typ== encapsulate.Message_type.Bid_Tie){
                                Toast.makeText(con,"Tie place your bids again",Toast.LENGTH_SHORT).show();
                                dh.players[0].bid = 0;
                                dh.players[1].bid = 0;
                                dh.players[0].bid_placed = false;
                                dh.players[1].bid_placed = false;
                                return;
                            }
                            else if (dh.msg_typ== encapsulate.Message_type.Won){
                                dh.ttt_arr[dh.box_played[0]][dh.box_played[1]]=dh.players[1-my_index].initials;
                                update_and_render();

                              try {Thread.sleep(500);}
                              catch (Exception ex){

                              }
                                Toast.makeText(con, "Opponent wins", Toast.LENGTH_LONG).show();
                                clear_and_restart();
                            }
                             else if (dh.msg_typ== encapsulate.Message_type.Game_Tie){

                                try {Thread.sleep(500);}
                                catch (Exception ex){

                                }
                                Toast.makeText(con, "Game Tie", Toast.LENGTH_LONG).show();
                                clear_and_restart();
                            }
                        }
                    });
                    Thread.sleep(100);

                } catch (Exception e) {

                    Log.d("zab", "in serv ----->" + e.toString());

                }
            }
        }
    }

    //funcs needed
    public static String getLocalIpAddress() {
        try {

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    boolean is_arr_filled() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (dh.ttt_arr[i][j] == '\0') return false;
            }
        }

        return true;
    }

    void play_turn(int val) {
        Log.d("abz", my_index + "");
        if (!c_r.is_connected) return;
        if (dh.current_player == -1) {
            Toast.makeText(con, "Both Users Have Not Placed Thier Bids Yet", Toast.LENGTH_SHORT).show();
            return;
        } else if (dh.current_player == 1 - my_index) {
            Toast.makeText(con, "Player has not played the turn yet", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dh.ttt_arr[val / 3][val % 3] != '\0') {
            Toast.makeText(con, "You Cannot Play There", Toast.LENGTH_SHORT).show();
            return;
        }
        dh.current_player = -1;//place bids first;
        dh.ttt_arr[val / 3][val % 3] = dh.players[my_index].initials;
        text_view_arr[val / 3][val % 3].setTextColor(dh.players[my_index].color);
        text_view_arr[val / 3][val % 3].setText("" + dh.players[my_index].initials);
        if (check(dh.players[my_index].initials)) {
            Toast.makeText(con, "You win", Toast.LENGTH_LONG).show();
            try {
                //increment version and wait for opponent;
                dh.box_played=new int[]{val/3,val%3};
                dh.msg_typ= encapsulate.Message_type.Won;
                dh.ver++;
                Thread.sleep(100);//wiat till obj is sent;
                clear_and_restart();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if (is_arr_filled()) {
            Toast.makeText(con, "Tie", Toast.LENGTH_LONG).show();
            try {

                dh.current_player = -1;
                dh.msg_typ= encapsulate.Message_type.Game_Tie;
                dh.ver++;
                Thread.sleep(100);//wiat till obj is sent;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clear_and_restart();
        }
        dh.current_player = -1;//place bids first;
        dh.msg_typ= encapsulate.Message_type.Turn;
        dh.players[0].bid = 0;
        dh.players[1].bid = 0;
        dh.players[0].bid_placed = false;
        dh.players[1].bid_placed = false;
        dh.box_played=new int[]{val/3,val%3};
        if (dh.players[my_index].points==0){
            Toast.makeText(con,"You Lost All Your Points",Toast.LENGTH_SHORT).show();
            dh.msg_typ= encapsulate.Message_type.Lost_all_points;
        }
        dh.ver++;
    }

    void update_and_render() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                text_view_arr[i][j].setText("" + dh.ttt_arr[i][j]);
                text_view_arr[i][j].setTextColor(dh.get_color(dh.ttt_arr[i][j]));
            }
        }


    }

    public boolean check(char val) {
        String val_3 = new String(new char[3]).replace("\0", "" + val);
        for (int i = 0; i < 3; i++) {
            String sum = "";
            for (int j = 0; j < 3; j++) {
                sum += dh.ttt_arr[i][j];
            }
            if (sum.equals(val_3)) return true;
        }
        for (int i = 0; i < 3; i++) {
            String sum = "";
            for (int j = 0; j < 3; j++) {
                sum += dh.ttt_arr[j][i];
            }
            if (sum.equals(val_3)) return true;

        }

        return ("" + dh.ttt_arr[0][0] + dh.ttt_arr[1][1] + dh.ttt_arr[2][2]).equals(val_3) || ("" + dh.ttt_arr[0][2] + dh.ttt_arr[1][1] + dh.ttt_arr[2][0]).equals(val_3);
    }

    void process_bids() {
        if (dh.current_player != -1) {
            return;
        }////
        if (!(dh.players[0].bid_placed && dh.players[1].bid_placed)) {
            Log.d("abz", "both users have not placed bids"+" me -- " + dh.players[my_index].bid_placed+" opponent -- " + dh.players[1-my_index].bid_placed);
            return;
        }

        if (dh.players[my_index].bid > dh.players[1 - my_index].bid) {
            dh.players[my_index].points -= dh.players[my_index].bid;
            view_points.setText("Points left :- " + dh.players[my_index].points);
            Toast.makeText(con, "You won the bid,Play your turn", Toast.LENGTH_SHORT).show();
            dh.current_player = my_index;
        } else if (dh.players[my_index].bid < dh.players[1 - my_index].bid) {
            Toast.makeText(con, "Opponent won the bid", Toast.LENGTH_SHORT).show();
            dh.current_player = 1 - my_index;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long time = System.currentTimeMillis();
        dh.init('X', 'O', Color.RED, Color.BLUE);
        main = getLayoutInflater().inflate(R.layout.activity_game_activity, null);
        con = this;
        text_view_arr = new TextView[3][3];
        bid = (Button) main.findViewById(R.id.bid);
        view_points = (TextView) main.findViewById(R.id.view_points);
        set_bid = (SeekBar) main.findViewById(R.id.set_bid);
        bid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int bid = set_bid.getProgress();
                if (my_index == -1) my_index = 0;
                if (!(c_r.is_connected | dh.current_player == -1)) return;
                if (dh.players[my_index].bid_placed){
                    Toast.makeText(con,"You have Already placed your Bid",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (bid > dh.players[my_index].points) {
                    Toast.makeText(con, "Not Enough Points ", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (bid==0){
                    Toast.makeText(con,"You have to place a bid ",Toast.LENGTH_SHORT).show();
                    return;
                }
                dh.players[my_index].bid = bid;
                dh.players[my_index].bid_placed = true;
                dh.msg_typ= encapsulate.Message_type.Bid;
                if (dh.players[0].bid==dh.players[1].bid){
                    dh.players[0].bid=0;
                    dh.players[1].bid=0;
                    dh.players[0].bid_placed=false;
                    dh.players[0].bid_placed=false;
                    dh.msg_typ= encapsulate.Message_type.Bid_Tie;
                    Toast.makeText(con,"Tie Place Your Bids Again",Toast.LENGTH_SHORT).show();
                }
                else  process_bids();

                dh.ver++;
            }
        });
        view_bid = (TextView) main.findViewById(R.id.view_bid);
        set_bid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(seekBar.getProgress() - seekBar.getProgress() % 5);
                view_bid.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        connect = (Button) main.findViewById(R.id.connect);
        info = (TextView) main.findViewById(R.id.info);
        text_view_arr[0][0] = (TextView) main.findViewById(R.id.t_0);
        text_view_arr[0][1] = (TextView) main.findViewById(R.id.t_1);
        text_view_arr[0][2] = (TextView) main.findViewById(R.id.t_2);
        text_view_arr[1][0] = (TextView) main.findViewById(R.id.t_3);
        text_view_arr[1][1] = (TextView) main.findViewById(R.id.t_4);
        text_view_arr[1][2] = (TextView) main.findViewById(R.id.t_5);
        text_view_arr[2][0] = (TextView) main.findViewById(R.id.t_6);
        text_view_arr[2][1] = (TextView) main.findViewById(R.id.t_7);
        text_view_arr[2][2] = (TextView) main.findViewById(R.id.t_8);
        for (int i = 0; i < 9; i++) {
            final int finalI = i;
            text_view_arr[i / 3][i % 3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    play_turn(finalI);
                    focus_view = finalI;
                }
            });
        }


        setContentView(main);
        //network related
        serv_t.start();
        local_ip = getLocalIpAddress();

        Log.d("abz", "local ip is " + local_ip);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("abz", "is client alive --->" + client_t.isAlive());
                if (client_started) {

                    if (!c_r.is_connected) {
                        connect.setText("Connecting.....");
                        Log.d("abz", "is alive , not connected");
                        c_r.restart = true;
                    } else {
                        c_r.disconnect = true;

                    }
                } else {
                    client_t.start();
                    connect.setText("Connecting.....");
                    client_started = true;
                }
            }
        });
        Log.d("abz", "" + (System.currentTimeMillis() - time));
    }
}