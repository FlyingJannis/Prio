package de.androidnewcomer.rankingappv1;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse wird sehr wahrscheinlich überflüssig sein! einzelne Methoden wurden
 * schon aus ihr herausgeschnitten!
 */


public class RankingList {
/**
    private List<Ranking> rankingList = new ArrayList<>();
    private int elements = 0;



    public boolean removeRanking(int id) {          //Ist ID hier sinnvoll?
        int removedId = -1;
        for(Ranking e: rankingList) {
            if(e.getId() == id) {
                rankingList.remove(e);
                removedId = e.getId();
                elements--;
            }
        }
        if(removedId >= 0) {
            for(Ranking e: rankingList) {
                if(e.getId() > removedId) {
                    e.decreaseId();
                }
            }
            return true;
        } else {
            return false;
        }
    }



    public Ranking getRankingAt(int x) {
        return rankingList.get(x);
    }
 */
}
