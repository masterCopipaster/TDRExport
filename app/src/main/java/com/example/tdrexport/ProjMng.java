package com.example.tdrexport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.layout.simple_list_item_multiple_choice;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static java.lang.StrictMath.min;

public class ProjMng extends AppCompatActivity {

    public final String tdrDefaultDir = "/storage/emulated/0/TopoDroid";
    public String tdrDir = tdrDefaultDir;

    private ListView filesListView;
    private ArrayAdapter<String> filesListAdapter;
    ArrayList<String>  filesList;
    ArrayList<String>  exportFilesList;
    HashMap<String, String> namePathMap;


    TextView debugTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proj_mng);
        debugTextView = findViewById(R.id.DEBUG_TEXT);
        filesListView = findViewById(R.id.FILESLIST);

        namePathMap = new HashMap<>();
        exportFilesList = new ArrayList<>();
        filesList = new ArrayList<>();
        filesListAdapter = new ArrayAdapter<>(this, simple_list_item_multiple_choice, filesList);
        filesListView.setAdapter(filesListAdapter);
        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                CheckedTextView item = (CheckedTextView) itemClicked;
                item.setChecked(!item.isChecked());
                if(item.isChecked()) exportFilesList.add(item.getText().toString());
                else exportFilesList.remove(item.getText().toString());
                Toast.makeText(ProjMng.this, item.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        debugTextView.setText("managing project " + message);
        buildProjList(message);
    }

    public String[] findAllProjFiles(String name){
        String res[] = {};
        File thDirs[] = new File(tdrDir).listFiles();
        if(thDirs == null) return res;

        ArrayList<String> files = new ArrayList<>();;

        for(File dir : thDirs){
            if(dir.isDirectory())
                for(File file : dir.listFiles())
                    if(file.getName().subSequence(0, min(name.length(), file.getName().length())).toString().equals(name)) {
                        files.add(file.getName());
                        namePathMap.put(file.getName(), file.getPath());
                    }

        }
        return files.toArray(res);
    }

    public void buildProjList(String prname){
        String list[] = findAllProjFiles(prname);
        for(String name : list){
            filesList.add(name);
        }
        filesListAdapter.notifyDataSetChanged();
    }

}
