package com.anjay.ticktacktoe;

import java.io.Serializable;

/**
 * Created by Anjay on 01-06-2017.
 */
class player implements Serializable {
    char initials;
    int color;
    int points=270;
    int bid;
    boolean bid_placed;
    @Override
    public String    toString(){
        return  "initial:- "+initials+" bid:- "+bid+ " is_bid_placed "+bid_placed;
    }
    player(char initials, int color) {
        this.initials = initials;
        this.color = color;
    }
}
public class encapsulate implements Serializable {

        String msg;
        int ver;
    int [] box_played;
        int current_player=-1;
        player[] players;
        char[][] ttt_arr;
    enum Message_type {
        Turn,Bid,Bid_Tie,Lost_all_points,Ping,Won,Game_Tie


    }
    Message_type msg_typ;
        encapsulate (){
        }
    int get_color (char a){
        return (a==players[0].initials?players[0].color:players[1].color);
    }
    void init (char initials_1, char initials_2,int color_1, int color_2){
        players = new player[2];
        players[0] = new player(initials_1, color_1);
        players[1] = new player(initials_2, color_2);
        ttt_arr = new char[3][3];
    }
        void update_obj (encapsulate updater){
            if (this.ver>updater.ver)return;
            this.msg_typ=updater.msg_typ;
            this.players=updater.players;
            this.box_played=updater.box_played;
            ver++;
        }
}
