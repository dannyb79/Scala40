package com.plmtmobile.scala40se;

/**
 * Created by daniele on 03/11/13.
 */
public class RankListItem {
    public String   username;
    public int      score;
    public int      totalgames;
    public int      wongames;
    public int      medals;

    RankListItem( String username, int score, int totalgames, int wongames, int medals ) {
        this.username = username;
        this.score = score;
        this.totalgames = totalgames;
        this.wongames = wongames;
        this.medals = medals;
    }
}
