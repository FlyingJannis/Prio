package de.androidnewcomer.rankingappv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class RankingAdd extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    private ArrayList<Ranking> rankings;
    private Button buttonCreate;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_add);

        loadData();

        buttonCreate = findViewById(R.id.button);
        editText = findViewById(R.id.editText);

        buttonCreate.setOnClickListener(this);
        editText.setOnKeyListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        saveData();
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button) {
            addAndReturn();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == event.KEYCODE_ENTER && event.getAction() != KeyEvent.ACTION_DOWN) {
            addAndReturn();
        }
        return onKeyDown(keyCode, event);
    }

    public boolean addRanking(String name) {
        for(Ranking e: rankings) {
            if(e.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        if(!name.equals("")) {
            rankings.add(new Ranking(name));
            return true;
        } else {
            return false;
        }
    }

    public void addAndReturn() {
        String name = editText.getText().toString();
        if(addRanking(name)) {
            //addRanking(name);

            saveData();

            //startActivity(new Intent(this, MainActivity.class));
            RankingLayout.setActualRanking(rankings.size() - 1);
            startActivity(new Intent(this, RankingLayout.class));
        } else {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, getResources().getString(R.string.invalid_input), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void saveData() {
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
    }
}
