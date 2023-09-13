package de.androidnewcomer.rankingappv1;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Ranking {
    private String name;
    //private int id;
    private List<RankingElement> ranking = new ArrayList<>();
    private int elements = 0;

    public Ranking(String name) {           //habe hier die Id entfernt!!! Aus der ganzen Klasse! Feld lasse ich erstmal noch vorrübergehend...
        this.name = name;
        //this.id = id;
    }

    public Ranking(String name, int elements, List<RankingElement> ranking) {
        this.name = name;
        this.elements = elements;
        this.ranking = ranking;
    }

    public boolean addElement(String name) {
        for(int n = 0; n < ranking.size(); n++) {
            if(ranking.get(n).getName().equalsIgnoreCase(name)) {           //Gleiche Namen werden nicht akzeptiert
                return false;
            }
        }
        if(!name.equals("")) {                                              //Leere Eingaben werden nicht akzeptiert
            RankingElement newElement = new RankingElement(elements, name);
            for(RankingElement e: ranking) {
                e.increase();
            }
            ranking.add(newElement);
            elements++;
            return true;
        } else {
            return false;
        }

    }

    public boolean removeElement(String RemovedName) {
        int removedId = -1;
        RankingElement tmp = null;
        for(RankingElement e: ranking) {
            if(e.getName().equals(RemovedName)) {
                removedId = e.getId();
                tmp = e;
            }
        }
        if(removedId >= 0) {                    // Falls wirklich ein Element mit diesem Namen gefunden und entfernt wurde, ist die removedId nicht mehr -1!
            ranking.remove(tmp);
            for(RankingElement e: ranking) {
                e.deleteField(removedId);       // Alle überbliebenden Felder werden angepasst: entsprechendes "comp-Feld" gelöscht, wenn nötig id verändert
                if(e.getId() > removedId) {
                    e.decreaseId();
                }
            }
            elements--;
            return true;
        } else {
            return false;
        }

    }

    /**
     * Gibt eine gerankte Liste aller Elemente zurück
     * @ die Liste
     */
    public List<String> makeRankedList() {
        int placement = 1;
        int rankingSize = ranking.size();
        int maxSize = 1;
        while(rankingSize > 9) {
            maxSize += 1;
            rankingSize = rankingSize / 10;
        }
        List<String> rankedList = new LinkedList<>();
        List<RankingElement> list = new ArrayList<>(ranking);
        while(!list.isEmpty()) {                                    //Das nächste Element der Ranked Liste wird ermittelt und dann (mit Name) der Liste hinzugefügt
            RankingElement tmp = list.get(0);
            for(int n = 1; n < list.size(); n++) {
                if(tmp.getValue() != list.get(n).getValue()) {
                    if(tmp.getValue() < list.get(n).getValue()) {
                        tmp = list.get(n);
                    }
                } else {
                    if(tmp.getName().compareToIgnoreCase(list.get(n).getName()) >= 0) {
                        tmp = list.get(n);
                    }
                }
            }
            int actualPlacement = placement;
            int actualSize = 1;
            while(actualPlacement > 9) {
                actualSize += 1;
                actualPlacement = actualPlacement / 10;
            }
            String gap = "";
            for(; maxSize > actualSize; actualSize++) {
                gap = gap + "   ";
            }
            rankedList.add(gap + placement + "." + "\t\t\t" + tmp.getName());
            list.remove(tmp);
            placement++;
        }
        return rankedList;
    }

    public void isBetterThan(RankingElement better, RankingElement worse) {
        better.setComp(worse.getId(), true);
        worse.setComp(better.getId(), false);
        for(int n = 0; n < better.getComp().length; n++) {
            if(n != worse.getId()) {
                if(better.getComp()[n] != null && worse.getComp()[n] == null && better.getComp()[n] == -1) {
                    isBetterThan(ranking.get(n), worse);
                }
            }
        }
        for(int n = 0; n < worse.getComp().length; n++) {
            if(n != better.getId()) {
                if(worse.getComp()[n] != null && better.getComp()[n] == null && worse.getComp()[n] == 1) {
                    isBetterThan(better, ranking.get(n));
                }
            }
        }
    }

    public RankingElement getElementById(int id) {
        for(RankingElement e: ranking) {
            if(e.getId() == id) {
                return e;
            }
        }
        throw new RuntimeException();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdate() {
        return "" + elements + ranking.isEmpty() + ranking.size();
    }

    /**
     * macht bis jetzt keinen Sinn... ggf. wieder zu einer einfachen Getter Methode ändern!
     * @return
     */
    public List<RankingElement> getCopyRanking() {
        List<RankingElement> copyRanking = new ArrayList<>();
        for(int n = 0; n < ranking.size(); n++) {
            RankingElement tmp = ranking.get(n);
            Integer[] copyComp = Arrays.copyOf(tmp.getComp(), tmp.getComp().length);
            copyRanking.add(new RankingElement(tmp.getId(), tmp.getName(), copyComp));
        }
        return copyRanking;
    }

    public List<RankingElement> getRanking() {
        return ranking;
    }

    public int getElements() {
        return elements;
    }

    /**
    public int getId() {
        return id;
    }

    public void decreaseId() {
        id--;
    }

    public void setId(int id) {
        this.id = id;
    }
     */
}
