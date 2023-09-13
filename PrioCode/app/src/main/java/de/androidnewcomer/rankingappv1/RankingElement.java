package de.androidnewcomer.rankingappv1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RankingElement {
    private String name;
    private int id;
    private Integer[] comp;

    public RankingElement(int id, String name) {
        this.name = name;
        this.id = id;
        comp = new Integer[id + 1];
        Arrays.fill(comp, null);
        comp[id] = 0;
    }

    public RankingElement(int id, String name, Integer[] comp) {
        this.name = name;
        this.id = id;
        this.comp = comp;
    }

    public void setComp(int pos, boolean better) {
        if(pos >= 0 && pos < comp.length) {
            if(better) {
                comp[pos] = 1;
            } else {
                comp[pos] = -1;
            }
        }
    }

    public void increase() {
        Integer[] newComp = new Integer[comp.length + 1];
        Arrays.fill(newComp, null);
        for(int n = 0; n < comp.length; n++) {
            newComp[n] = comp[n];
        }
        comp = newComp;
    }

    public void isBetterThan(RankingElement element) {
        setComp(element.getId(), true);
        element.setComp(id, false);
        for(int n = 0; n < comp.length; n++) {
            if(n != element.getId()) {
                if(comp[n] != null && comp[n] == -1) {
                    element.setComp(n, false);
                }
            }
        }
        for(int n = 0; n < element.getComp().length; n++) {
            if(n != id) {
                if(element.getComp()[n] != null && element.getComp()[n] == 1) {
                    setComp(n, true);
                }
            }
        }
    }

    public boolean isCompleted() {
        for(int n = 0; n < comp.length; n++) {
            if(comp[n] == null) {
                return false;
            }
        }
        return true;
    }

    public int getValue() {
        int sum = 0;
        for(int n = 0; n < comp.length; n++) {
            Integer tmp = comp[n];
            if(tmp != null) {
                sum = sum + tmp;
            }
        }
        return sum;
    }

    public void deleteField(int x) {
        Integer[] newComp = new Integer[comp.length - 1]; //Der neue Array ist eins kleiner als der alte, die zu entfernende ID x wird ausgeschnitten
        int m = 0;
        for(int n = 0; n < comp.length && m < newComp.length && x < comp.length; n++) {
            if(n != x) {
                newComp[m] = comp[n];
                m++;
            }
        }
        comp = newComp;
    }

    public Integer[] getComp() {
        return comp;
    }


    public String getName() {
        return name;
    }

    public void rename(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void decreaseId() {
        id--;
    }
}
