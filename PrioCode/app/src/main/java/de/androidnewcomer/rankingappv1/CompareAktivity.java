 package de.androidnewcomer.rankingappv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CompareAktivity extends AppCompatActivity implements View.OnClickListener {
    private static int actualRanking;
    Random random = new Random();

    private boolean stepBackMode = false;

    private AdView adView;

    private ArrayList<Ranking> rankings;
    private Ranking ranking;

    private RankingElement reButtonTop;
    private RankingElement reButtonBottom;
    private Button buttonTop;
    private Button buttonBottom;
    /**
     * Der untere Teil ist für den Rückgängig Button gedacht
     */
    private Button buttonBefore;
    private Ranking rankingBefore;
    private RankingElement reButtonTopBefore;
    private RankingElement reButtonBottomBefore;
    //private Button buttonTopBefore;
    //private Button buttonBottomBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_aktivity);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        loadData();

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        buttonTop = findViewById(R.id.buttonTOP);
        buttonBottom = findViewById(R.id.buttonBOTTOM);
        buttonBefore = findViewById(R.id.buttonSTEPBEFORE);
        buttonTop.setOnClickListener(this);
        buttonBottom.setOnClickListener(this);
        buttonBefore.setOnClickListener(this);

        setButtons();

    }

    @Override
    public void onStop() {
        super.onStop();
        saveData();
        if(!stepBackMode) {
            startActivity(new Intent(this, MainActivity.class));
        }
        this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            stepBackMode = true;
            startActivity(new Intent(this, RankingLayout.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.buttonTOP:
                actionButtonTop();
                break;
            case R.id.buttonBOTTOM:
                actionButtonBottom();
                break;
            case R.id.buttonSTEPBEFORE:
                if(rankingBefore != null) {
                    setButtonsBefore();
                } else {
                    Toast.makeText(CompareAktivity.this, getResources().getString(R.string.step_back_impossible),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    public static void setActualRanking(int actualRanking) {
        CompareAktivity.actualRanking = actualRanking;
    }

    public void actionButtonTop() {
        rankingBefore = new Ranking(ranking.getName(), ranking.getElements(), ranking.getCopyRanking());
        ranking.isBetterThan(reButtonTop, reButtonBottom);
        saveData();
        if(setButtons()) {
            //setButtons();
        } else {
            Toast.makeText(CompareAktivity.this, "Vergleich abgeschlossen!",
                    Toast.LENGTH_SHORT).show();
            stepBackMode = true;
            RankingLayout.setActualRanking(actualRanking);
            startActivity(new Intent(this, RankingLayout.class));
        }
    }

    public void actionButtonBottom() {
        rankingBefore = new Ranking(ranking.getName(), ranking.getElements(), ranking.getCopyRanking());
        ranking.isBetterThan(reButtonBottom, reButtonTop);
        saveData();
        if(setButtons()) {
            //setButtons();
        } else {
            Toast.makeText(CompareAktivity.this, "Vergleich abgeschlossen!",
                    Toast.LENGTH_SHORT).show();
            stepBackMode = true;
            RankingLayout.setActualRanking(actualRanking);
            startActivity(new Intent(this, RankingLayout.class));
        }
    }

    public boolean setButtons() {
        int check = 0;
        for(RankingElement re: ranking.getRanking()) {                  //For Schleife überprüft, ob min. zwei Elemente noch nicht complete sind
            if(!re.isCompleted()) {
                check++;
            }
        }
        if(check < 2) {
            return false;
        }
        int rdmIdOne = 0;
        for(;;) {
            rdmIdOne = random.nextInt(ranking.getRanking().size());             //Erstes R Element wird ermittelt und gesetzt
            if(!ranking.getElementById(rdmIdOne).isCompleted()) {
                if(reButtonTop != null) {
                    //reButtonTopBefore = reButtonTop;
                    reButtonTopBefore = new RankingElement(reButtonTop.getId(),
                            reButtonTop.getName(), Arrays.copyOf(reButtonTop.getComp(), reButtonTop.getComp().length));
                }
                reButtonTop = ranking.getElementById(rdmIdOne);
                break;
            }
        }
        for(;;) {
            int rdmIdTwo = random.nextInt(ranking.getRanking().size());         //Zweites R Element wird ermittelt und gesetzt
            if(rdmIdOne != rdmIdTwo && ranking.getElementById(rdmIdTwo).getComp()[rdmIdOne] == null) {
                if(reButtonBottom != null) {
                    //reButtonBottomBefore = reButtonBottom;
                    reButtonBottomBefore = new RankingElement(reButtonBottom.getId(),
                            reButtonBottom.getName(), Arrays.copyOf(reButtonBottom.getComp(), reButtonBottom.getComp().length));
                }
                reButtonBottom = ranking.getElementById(rdmIdTwo);
                break;
            }
        }
        buttonTop.setText(reButtonTop.getName());
        if(reButtonTop.getName().length() > 12) {
            buttonTop.setTextSize(35);
        } else {
            buttonTop.setTextSize(40);
        }
        buttonBottom.setText(reButtonBottom.getName());
        if(reButtonBottom.getName().length() > 12) {
            buttonBottom.setTextSize(35);
        } else {
            buttonBottom.setTextSize(40);
        }
        return true;
    }

    public void setButtonsBefore() {
        ranking = new Ranking(rankingBefore.getName(), rankingBefore.getElements(), rankingBefore.getCopyRanking());
        for(RankingElement e: ranking.getRanking()) {
            if(e.getName().equals(reButtonBottomBefore.getName())) {
                reButtonBottom = e;
            }
        }
        for(RankingElement e: ranking.getRanking()) {
            if(e.getName().equals(reButtonTopBefore.getName())) {
                reButtonTop = e;
            }
        }
        //reButtonBottom = new RankingElement(reButtonBottomBefore.getId(),
        //                reButtonBottomBefore.getName(), Arrays.copyOf(reButtonBottomBefore.getComp(), reButtonBottomBefore.getComp().length));
        //reButtonTop = new RankingElement(reButtonTopBefore.getId(),
        //                reButtonTopBefore.getName(), Arrays.copyOf(reButtonTopBefore.getComp(), reButtonTopBefore.getComp().length));
        rankingBefore = null;
        reButtonTopBefore = null;
        reButtonBottomBefore = null;
        buttonTop.setText(reButtonTop.getName());
        if(reButtonTop.getName().length() > 12) {
            buttonTop.setTextSize(35);
        } else {
            buttonTop.setTextSize(40);
        }
        buttonBottom.setText(reButtonBottom.getName());
        if(reButtonBottom.getName().length() > 12) {
            buttonBottom.setTextSize(35);
        } else {
            buttonBottom.setTextSize(40);
        }
        saveData();
    }

    private void saveData() {
        rankings.remove(actualRanking);
        rankings.add(actualRanking, ranking);

        SharedPreferences sharedPreferences = getSharedPreferences("rankings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(rankings);
        editor.putString("ranking list", json);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("rankings", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ranking list", null);
        Type type = new TypeToken<ArrayList<Ranking>>() {}.getType();
        rankings = gson.fromJson(json, type);

        if(rankings == null) {
            rankings = new ArrayList<>();
        }

        ranking = rankings.get(actualRanking);
    }


}
