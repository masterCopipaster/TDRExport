package com.example.tdrexport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.EnvironmentCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.net.Uri;
import android.widget.Toast;
import android.widget.ListAdapter;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.R.layout.simple_list_item_1;
import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity {

    private TextView screenText;
    private Button enterButton;
    private EditText enterText;
    private ListView projListView;
    private ArrayAdapter<String> projListAdapter;

    private ArrayList<String> projList;

    public final String tdrDefaultDir = "/storage/emulated/0/TopoDroid";
    public String tdrDir;
    SharedPreferences tdrDirSpref;
    public final String tdrDirSprefKey = "tdr_dir_spref";

    public final String tdrDirNotFoundMsg = "Директория топодроид не найдена введите путь";
    public final String tdrNoProjMsg = "Топосьемки не найдены, возможно путь к папке ошибочный";
    public final String selectProjMsg = "Выберите сьемку для экспорта";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screenText = findViewById(R.id.SCREEN_TEXT);
        enterButton = findViewById(R.id.ENTERBUTTON);
        enterText = findViewById(R.id.ENTERTEXT);
        projListView = findViewById(R.id.PROJLIST);
        projList = new ArrayList<>();
        projListAdapter = new ArrayAdapter<>(this, simple_list_item_1, projList);
        projListView.setAdapter(projListAdapter);
        projListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ProjMng.class);
                String message = ((TextView) itemClicked).getText().toString();
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }

        //load shared pref with TDR dir
        loadTdrDirSpref();
        //check if tdr directory exists
        File dir = new File(tdrDir);
        //if not open string editor
        if(! dir.exists()){
            screenText.setText(tdrDirNotFoundMsg);
            Toast.makeText(this, tdrDirNotFoundMsg, Toast.LENGTH_SHORT).show();
            enterButton.setVisibility(View.VISIBLE);
            enterText.setVisibility(View.VISIBLE);
            return;
        }

        buildProjList();
    }

    public void onEnterClick(View v){
        tdrDir = enterText.getText().toString();
        File dir = new File(tdrDir);
        if(dir.exists()) {
            buildProjList();
        }
    }

    public void buildProjList(){
        String list[] = scanTdrDir();
        if(list.length == 0){
            screenText.setText(tdrNoProjMsg);
            enterButton.setVisibility(View.VISIBLE);
            enterText.setVisibility(View.VISIBLE);
            Toast.makeText(this, tdrNoProjMsg, Toast.LENGTH_SHORT).show();
            return;
        }
        for(String name : list){
            projList.add(name);
        }
        enterButton.setVisibility(View.GONE);
        enterText.setVisibility(View.GONE);
        screenText.setText(selectProjMsg);
        projListAdapter.notifyDataSetChanged();
        saveTdrDirSpref(tdrDir);
    }

    public String[] scanTdrDir() {
        String res[] = {};
        File thFiles[] = new File(tdrDir + "/th").listFiles();
        if(thFiles == null) return res;
        ArrayList<String> groups = new ArrayList<>();;

        for(int i = 0; i < thFiles.length; i++){
            String name = thFiles[i].getName();
            groups.add(name.substring(0, name.indexOf(".")));
        }
        return groups.toArray(res);
    }
    private void saveTdrDirSpref(String param){
        tdrDirSpref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = tdrDirSpref.edit();
        ed.putString(tdrDirSprefKey, param);
        ed.commit();
    }
    private void loadTdrDirSpref(){
        tdrDirSpref = getPreferences(MODE_PRIVATE);
        tdrDir = tdrDirSpref.getString(tdrDirSprefKey, tdrDefaultDir);
    }

}
