package de.androidnewcomer.rankingappv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class RankingLayout extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnKeyListener {
    private static int actualRanking;
    private boolean longClickMode = true;
    private boolean renameMode = false;

    private boolean stepBackMode = false;
    private AdView adView;

    private ArrayList<Ranking> rankings;
    private Ranking ranking;
    private View longClickedItem;
    private int longClickedItemId;

    private ListView lvRanking;
    private TextView tvHead;
    private Button buttonDelete;
    private Button buttonCompare;
    private Button buttonAccept;
    private Button buttonClearAll;
    private Button buttonClearElement;
    private Button buttonRename;
    private EditText etNewElement;

    private String textInField = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_layout);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        loadData();

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        buttonClearAll = findViewById(R.id.buttonCLEARALL);
        buttonClearElement = findViewById(R.id.buttonCLEAR);
        buttonRename = findViewById(R.id.buttonRENAME2);
        buttonDelete = findViewById(R.id.buttonDELETE2);
        buttonCompare = findViewById(R.id.buttonCOMPARE);
        tvHead = findViewById(R.id.textViewRanking);
        lvRanking = findViewById(R.id.ranking);
        buttonAccept = findViewById(R.id.buttonACCEPT3);
        etNewElement = findViewById(R.id.editText2);

        buttonClearAll.setOnClickListener(this);
        buttonClearElement.setOnClickListener(this);
        buttonRename.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        buttonCompare.setOnClickListener(this);
        buttonAccept.setOnClickListener(this);
        lvRanking.setOnItemClickListener(this);
        lvRanking.setOnItemLongClickListener(this);
        lvRanking.setFocusable(false);
        etNewElement.setOnKeyListener(this);
        etNewElement.addTextChangedListener(new TextWatcher() {         //Der ganze Spaß, um eine Fehlermeldung beim Überschreiten der MaxLength von 24 Zeichen zu geben...
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length() >= 24 && !charSequence.toString().equals(textInField)) {
                    Toast.makeText(RankingLayout.this, getResources().getString(R.string.name_TooLong),
                            Toast.LENGTH_SHORT).show();
                    textInField = charSequence.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        changeSet();
        tvHead.setText(ranking.getName());
        etNewElement.setHint(getResources().getString(R.string.new_element));
        buttonAccept.setText(getResources().getString(R.string.add));

        visualisation();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(!stepBackMode) {
            rankingOnTop();
            saveData();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            saveData();
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonDELETE2:
                if(longClickMode) {
                    removeE(longClickedItemId);
                    changeSet();
                    saveData();
                    visualisation();
                }
                break;
            case R.id.buttonCOMPARE:
                if(CheckComparability()) {
                    stepBackMode = true;                //stepBackMode sorgt dafür, dass die MainActivity nicht neu gestartet wird!
                    rankingOnTop();
                    saveData();
                    CompareAktivity.setActualRanking(actualRanking);
                    startActivity(new Intent(this, CompareAktivity.class));
                } else {
                    Toast.makeText(RankingLayout.this, getResources().getString(R.string.tests_completed),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonRENAME2:
                if(renameMode) {
                    buttonRename.setBackgroundResource(R.drawable.resren_buttons_anim);
                    buttonAccept.setBackgroundResource(android.R.drawable.btn_default);
                    renameMode = false;
                    etNewElement.setHint(getResources().getString(R.string.new_element));
                    etNewElement.setText("");
                    buttonAccept.setText(getResources().getString(R.string.add));
                } else {
                    buttonRename.setBackgroundResource(R.drawable.compare_button_anim);
                    buttonAccept.setBackgroundResource(R.drawable.compare_button_anim);
                    String elementName = ranking.makeRankedList().get(longClickedItemId);
                    int placementLength = elementName.indexOf('.');
                    elementName = elementName.substring(placementLength + 4);
                    renameMode = true;
                    etNewElement.setText(elementName);                                          //ff autoselected das EditText
                    etNewElement.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(RankingLayout.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etNewElement, InputMethodManager.SHOW_IMPLICIT);
                    etNewElement.setSelection(etNewElement.getText().length());
                    buttonAccept.setText(getResources().getString(R.string.accept));
                }
                break;
            case R.id.buttonCLEARALL:
                for(RankingElement e: ranking.getRanking()) {
                    Integer[] tmp = e.getComp();
                    for(int n = 0; n < tmp.length; n++) {
                        if(tmp[n] != null && tmp[n] != 0) {
                            tmp[n] = null;
                        }
                    }
                }
                Toast.makeText(RankingLayout.this, getResources().getString(R.string.clear),
                        Toast.LENGTH_SHORT).show();
                saveData();
                visualisation();
                break;
            case R.id.buttonCLEAR:
                if(longClickMode) {
                    clearElement(longClickedItemId);
                    changeSet();
                    Toast.makeText(RankingLayout.this, getResources().getString(R.string.element_cleared),
                            Toast.LENGTH_SHORT).show();
                }
                saveData();
                visualisation();
                break;
            case R.id.buttonACCEPT3:
                if(renameMode) {
                    renameE(longClickedItemId);
                    saveData();
                    visualisation();
                    changeSet();
                } else {
                    loadData();
                    addE();
                    saveData();
                    visualisation();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(longClickMode) {
            changeSet();
            longClickedItem.setBackgroundResource(0);
        } else {

        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(longClickMode) {
            longClickedItemId = i;
            longClickedItem.setBackgroundResource(0);
            longClickedItem = view;
            view.setBackgroundColor(getResources().getColor(R.color.listViewHighlighter2));
            if(!etNewElement.getText().toString().equals("") && renameMode) {
                String elementName = ranking.makeRankedList().get(longClickedItemId);
                int placementLength = elementName.indexOf('.');
                elementName = elementName.substring(placementLength + 4);
                etNewElement.setText(elementName);
            }
        } else {
            longClickedItemId = i;
            longClickedItem = view;
            view.setBackgroundColor(getResources().getColor(R.color.listViewHighlighter2));
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
            stepBackMode = true;
            rankingOnTop();
            saveData();
            startActivity(new Intent(this, MainActivity.class));
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == event.KEYCODE_ENTER && event.getAction() != KeyEvent.ACTION_DOWN) {
            if(renameMode) {
                renameE(longClickedItemId);
                saveData();
                visualisation();
                changeSet();
            } else {
                //loadData();
                addE();
                saveData();
                visualisation();
                return true;
            }
        }
        return false;
    }


    public void clearElement(int i) {
        String elementName = ranking.makeRankedList().get(i);
        int placementLength = elementName.indexOf('.');
        elementName = elementName.substring(placementLength + 4);
        int resetId = 0;
        for(RankingElement e: ranking.getRanking()) {
            if(elementName.equals(e.getName())) {
                resetId = e.getId();
                Integer[] tmp = e.getComp();
                for(int n = 0; n < tmp.length; n++) {
                    if (tmp[n] != null && tmp[n] != 0) {
                        tmp[n] = null;
                    }
                }
            }
        }
        for(RankingElement e: ranking.getRanking()) {
            if(resetId != e.getId()) {
                Integer[] tmp = e.getComp();
                tmp[resetId] = null;
            }
        }
    }

    public void renameE(int i) {
        String elementName = ranking.makeRankedList().get(i);
        int placementLength = elementName.indexOf('.');
        elementName = elementName.substring(placementLength + 4);
        for(RankingElement e: ranking.getRanking()) {
            if (elementName.equals(e.getName())) {
                String newName = etNewElement.getText().toString();
                for(RankingElement otherEs: ranking.getRanking()) {
                    if(otherEs.getName().equalsIgnoreCase(newName)) {
                        Toast.makeText(RankingLayout.this, getResources().getString(R.string.invalid_input),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!newName.equals("") && newName.length() <= 24) {
                        e.rename(newName);
                        Toast.makeText(RankingLayout.this, "\"" + elementName + "\"" +
                                        getResources().getString(R.string.wurde_zu) + "\"" + newName + "\"" +
                                        getResources().getString(R.string.umbenannt), Toast.LENGTH_SHORT).show();
                        hideSoftKeyboard();
                        etNewElement.setHint(getResources().getString(R.string.new_element));
                        buttonAccept.setText(getResources().getString(R.string.add));
                        etNewElement.setText("");
                        renameMode = false;
                        return;
                    } else {
                        Toast.makeText(RankingLayout.this, getResources().getString(R.string.invalid_input),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void removeE(int i) {
        loadData();
        String elementName = ranking.makeRankedList().get(i);
        int placementLength = elementName.indexOf('.');
        elementName = elementName.substring(placementLength + 4);
        Toast.makeText(RankingLayout.this, "\"" + elementName + "\" " + getResources().getString(R.string.has_been_deleted),
                Toast.LENGTH_SHORT).show();
        ranking.removeElement(elementName);
        saveData();
        visualisation();
        tvHead.setText(ranking.getName());
    }

    public String getCompareState() {
        int totalCompares = ranking.getRanking().size() * ranking.getRanking().size() - ranking.getRanking().size();
        int count = 0;
        int completePercent;
        for(RankingElement e: ranking.getRanking()) {
            Integer[] tmp = e.getComp();
            for(int n = 0; n < tmp.length; n++) {
                if (tmp[n] != null && tmp[n] != 0) {
                    count++;
                }
            }
        }
        completePercent = count * 100 / totalCompares ;
        if(completePercent != 100) {          //Hier stand vorher count != 0 && ...
            return "\n(" + completePercent + "%)";
        } else {
            return "";
        }
    }

    public void changeSet() {
        if(longClickMode) {
            buttonCompare.setVisibility(View.VISIBLE);
            buttonClearAll.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.GONE);
            buttonRename.setVisibility(View.GONE);
            buttonClearElement.setVisibility(View.GONE);
            etNewElement.setHint(getResources().getString(R.string.new_element));
            etNewElement.setText("");
            buttonAccept.setText(getResources().getString(R.string.add));
            if(longClickedItem != null) {
                longClickedItem.setBackgroundResource(0);
            }
            buttonRename.setBackgroundResource(R.drawable.resren_buttons_anim);
            buttonAccept.setBackgroundResource(android.R.drawable.btn_default);
            renameMode = false;
            longClickMode = false;
        } else {
            buttonCompare.setVisibility(View.GONE);
            buttonClearAll.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonRename.setVisibility(View.VISIBLE);
            buttonClearElement.setVisibility(View.VISIBLE);
            longClickMode = true;
        }
    }

    public boolean CheckComparability() {
        int check = 0;
        for(RankingElement re: ranking.getRanking()) {                  //For Schleife überprüft, ob min. zwei Elemente noch nicht complete sind
            if(!re.isCompleted()) {
                check++;
            }
        }
        return check >= 2;
    }

    public void rankingOnTop() {
        Ranking tmp = null;
        int id = 0;
        for(int n = 0; n < rankings.size(); n++) {
            if(rankings.get(n).getName().compareTo(ranking.getName()) == 0) {
                tmp = rankings.get(n);
                id = n;
            }
        }
        ArrayList<Ranking> newRanking = new ArrayList<>();
        newRanking.add(tmp);
        for(int n = 0; n < rankings.size(); n++) {
            if(n != id) {
                newRanking.add(rankings.get(n));
            }
        }
        rankings = newRanking;
        actualRanking = 0;
    }

    public static void setActualRanking(int actualRanking) {
        RankingLayout.actualRanking = actualRanking;
    }

    public void visualisation() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RankingLayout.this,
                android.R.layout.simple_list_item_1, ranking.makeRankedList());
        lvRanking.setAdapter(arrayAdapter);
        if(ranking.getRanking().size() >= 2) {
            String buttonCompareText = getResources().getString(R.string.compare) + getCompareState();
            buttonCompare.setText(buttonCompareText);
        }
    }

    public void addE() {
        String name = etNewElement.getText().toString();
        if(name.length() > 24) {
            Toast.makeText(RankingLayout.this, getResources().getString(R.string.name_TooLong),
                    Toast.LENGTH_SHORT).show();
        } else {
            if(ranking.addElement(name)) {
                //ranking.addElement(name);
                Toast.makeText(RankingLayout.this, "\"" + name + "\"" +
                                getResources().getString(R.string.hinzugefügt), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RankingLayout.this, getResources().getString(R.string.invalid_input),
                        Toast.LENGTH_SHORT).show();
            }
            etNewElement.setText("");
        }
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
