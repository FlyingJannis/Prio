package de.androidnewcomer.rankingappv1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnKeyListener {
    private ArrayList<Ranking> rankings;
    private int longClickedItemId;
    private View longClickedItem;

    private AdView adView;

    private Button buttonAdd;
    private Button buttonDelete;
    private Button buttonRename;
    private Button buttonCopyPrio;
    private Button buttonNewName;
    private EditText etNewName;
    private static ListView lvRankingList;
    private TextView tvHead;

    private boolean longClickMode = false;
    private boolean renameMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        loadData();

        buttonNewName = findViewById(R.id.buttonNEWNAME);
        etNewName = findViewById(R.id.editText3);
        buttonRename = findViewById(R.id.buttonRENAME);
        buttonCopyPrio = findViewById(R.id.buttonCOPYPRIO);
        buttonAdd = findViewById(R.id.buttonADD);
        buttonDelete = findViewById(R.id.buttonDELETE);
        lvRankingList = findViewById(R.id.rankingList);
        tvHead = findViewById(R.id.textViewRankings);

        tvHead.setText(getResources().getString(R.string.myPrios));
        buttonNewName.setVisibility(View.GONE);
        buttonRename.setVisibility(View.GONE);
        buttonCopyPrio.setVisibility(View.GONE);
        etNewName.setVisibility(View.GONE);
        buttonDelete.setVisibility(View.GONE);
        buttonAdd.setVisibility(View.VISIBLE);

        buttonNewName.setOnClickListener(this);
        buttonAdd.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        buttonRename.setOnClickListener(this);
        buttonCopyPrio.setOnClickListener(this);
        lvRankingList.setOnItemClickListener(this);
        lvRankingList.setOnItemLongClickListener(this);
        etNewName.setOnKeyListener(this);

        visualisation();

    }

    @Override
    public void onStop() {
        super.onStop();
        saveData();
        this.finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonADD:
                startActivity(new Intent(this, RankingAdd.class));
                finish();
                break;
            case R.id.buttonDELETE:
                if(longClickMode) {
                    if(getCurrentFocus() != null) {
                        hideSoftKeyboard();
                    }
                    Toast.makeText(MainActivity.this, "\"" + rankings.get(longClickedItemId).getName() + "\" "
                                    + getResources().getString(R.string.has_been_deleted), Toast.LENGTH_SHORT).show();
                    rankings.remove(longClickedItemId);
                    saveData();
                    visualisation();
                    changeSet();
                }
                break;
            case R.id.buttonNEWNAME:
                renameRanking();
                break;
            case R.id.buttonRENAME:
                if(renameMode) {
                    renameRanking();
                } else {
                    etNewName.setText(rankings.get(longClickedItemId).getName());
                    buttonNewName.setVisibility(View.VISIBLE);
                    etNewName.setVisibility(View.VISIBLE);
                    buttonRename.setBackgroundResource(R.drawable.add_button_anim);
                    renameMode = true;
                    etNewName.requestFocus();       //ff autoselected das EditText
                    InputMethodManager imm = (InputMethodManager) getSystemService(RankingLayout.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etNewName, InputMethodManager.SHOW_IMPLICIT);
                    etNewName.setSelection(etNewName.getText().length());
                }

                break;
            case R.id.buttonCOPYPRIO:
                copyRanking(rankings.get(longClickedItemId));
                visualisation();
                changeSet();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(longClickMode) {
            longClickedItem.setBackgroundResource(0);
            changeSet();
        } else {
            RankingLayout.setActualRanking(i);
            startActivity(new Intent(this, RankingLayout.class));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(longClickMode) {
                longClickedItemId = i;
                longClickedItem.setBackgroundResource(0);
                longClickedItem = view;
                etNewName.setText(rankings.get(longClickedItemId).getName());
                view.setBackgroundColor(getResources().getColor(R.color.listViewHighlighter));
            } else {
                longClickedItemId = i;
                longClickedItem = view;
                view.setBackgroundColor(getResources().getColor(R.color.listViewHighlighter));
                changeSet();
            }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(longClickMode) {
                changeSet();
                longClickedItem.setBackgroundResource(0);
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
        //if (keyCode==KeyEvent.KEYCODE_ENTER)
        //{
          //  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
           // return true;
        //}
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == event.KEYCODE_ENTER && event.getAction() != KeyEvent.ACTION_DOWN) {
            if(renameMode) {
                renameRanking();
            }
        }
        return onKeyDown(keyCode, event);
    }

    public void copyRanking(Ranking ranking) {
        String copy = ranking.getName().toUpperCase();
        List<String> rankingList = ranking.makeRankedList();
        for(int i = 0; i < rankingList.size(); i++) {
            copy = copy + "\n" + rankingList.get(i);
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("EditText", copy);
        clipboard.setPrimaryClip(clip);

        int check = 0;
        for(RankingElement re: ranking.getRanking()) {                  //For Schleife überprüft, ob min. zwei Elemente noch nicht complete sind
            if(!re.isCompleted()) {
                check++;
            }
        }
        if(check >= 2) {
            Toast.makeText(MainActivity.this, "\"" + ranking.getName() + "\" "
                            + getResources().getString(R.string.prio_copied) + "\n"
                            + getResources().getString(R.string.prio_not_finished), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "\"" + ranking.getName() + "\" "
                    + getResources().getString(R.string.prio_copied), Toast.LENGTH_LONG).show();
        }
    }

    public void renameRanking() {
        Ranking tmp = rankings.get(longClickedItemId);
        String oldName = tmp.getName();
        String newName = etNewName.getText().toString();
        for(Ranking e: rankings) {
            if(e.getName().equalsIgnoreCase(newName)) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.invalid_input),
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
        if(!newName.equals("") && newName.length() <= 24) {
            tmp.setName(newName);
            hideSoftKeyboard();
            etNewName.setText("");
            etNewName.setVisibility(View.GONE);
            buttonNewName.setVisibility(View.GONE);
            renameMode = false;
            Toast.makeText(MainActivity.this, "\"" + oldName + "\"" + getResources().getString(R.string.wurde_zu)
                    + "\"" + newName + "\"", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.invalid_input),
                    Toast.LENGTH_SHORT).show();
        }
        saveData();
        visualisation();
        changeSet();
    }

    public void changeSet() {
        if(longClickMode) {
            buttonNewName.setVisibility(View.GONE);
            buttonRename.setVisibility(View.GONE);
            buttonCopyPrio.setVisibility(View.GONE);
            etNewName.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
            buttonAdd.setVisibility(View.VISIBLE);
            buttonRename.setBackgroundResource(R.drawable.rename_button_anim);
            renameMode = false;
            longClickMode = false;
        } else {
            buttonRename.setVisibility(View.VISIBLE);
            buttonCopyPrio.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonAdd.setVisibility(View.GONE);
            longClickMode = true;
        }
    }

    public void visualisation() {           //sehr umständlich mit den Maps
        HashMap<String, String> itemSubitem = new HashMap<>();
        ArrayList<String> mainItems = getRankingList();
        for(int i = 0; i < rankings.size(); i++) {
            String str1 = mainItems.get(i);
            String str2 = "";
            List<String> tmpElements = rankings.get(i).makeRankedList();
            if(!tmpElements.isEmpty()) {
                for(int n = 0; n < 3; n++) {
                    if(tmpElements.size() > n) {
                        String tmpElement = tmpElements.get(n);
                        while(tmpElement.substring(0, 3).equals("   ")) {           //Löscht die gaps vor den Zahlen
                            tmpElement = tmpElement.substring(3);
                        }
                        str2 = str2 + "\t\t\t\t" + tmpElement + "\n";

                    }
                }
            }
            itemSubitem.put(str1, str2);
        }
        List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        for(int i = 0; i < rankings.size(); i++) {
            HashMap<String, String> resultMap = new HashMap<>();
            String key = rankings.get(i).getName();
            String value = itemSubitem.get(key);
            resultMap.put("First Line", "   " + key);
            resultMap.put("Second Line", value);
            listItems.add(resultMap);
        }

        lvRankingList.setAdapter(adapter);
        /**
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getRankingList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView item = (TextView) super.getView(position,convertView,parent);
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                item.setTypeface(Typeface.DEFAULT_BOLD);
                item.setTextColor(Color.parseColor("#A0000000"));
                return item;
            }
        };
        lvRankingList.setAdapter(arrayAdapter);
        */
    }

    public ArrayList<String> getRankingList() {
        ArrayList<String> list = new ArrayList<>();
        List<Ranking> tmp = new ArrayList<>(rankings);
        while(!tmp.isEmpty()) {
            list.add(tmp.get(0).getName());
            tmp.remove(0);
        }
        return list;
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
