package com.example.tdrexport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.EnvironmentCompat;

import android.Manifest;
import android.content.Intent;
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


public class MainActivity extends AppCompatActivity {

    private TextView screenText;
    private Button enterButton;
    private EditText enterText;
    private ListView projListView;
    private ArrayAdapter<String> projListAdapter;

    private ArrayList<String> projList;

    public final String tdrDefaultDir = "/storage/emulated/0/TopoDroid";
    public String tdrDir = tdrDefaultDir;

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
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                screenText.setText(((TextView) itemClicked).getText());
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }

        String list[] = scanTdrDir();

        String fullList = "";
        
        for(String name : list){
            projList.add(name);
            fullList += "entry: " + name + "\n";
        }
        projListAdapter.notifyDataSetChanged();
        screenText.setText(fullList);
        enterText.setText(Environment.getDataDirectory().getPath());
    }
    private static final int PICK_PDF_FILE = 2;

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //intent.putExtra(DocumentsContract.);

        startActivityForResult(intent, PICK_PDF_FILE);
    }

    public void onEnterClick(View v)
    {

        File dir;
        try {
            dir = new File(enterText.getText().toString());
        }
        catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG).show();
            return;
        }
        if(dir == null) screenText.setText("нет такого файла ГГ");
        // если объект представляет каталог
        if(dir.isDirectory())
        {
            String list[] = dir.list();
            String fullList = "path: ".concat(dir.getPath().concat("\n"));
            if(list == null)
            {
                screenText.setText("Пусто ебать");// + String.valueOf(dir.listFiles().length));
                return;
            }
            for(int i = 0; i < list.length; i++)
            fullList += "entry: " + list[i] + "\n";
            screenText.setText(fullList);
            //openFile();
        }
    }

    public String[] scanTdrDir() {
        File thFiles[] = new File(tdrDir + "/th").listFiles();
        ArrayList<String> groups = new ArrayList<>();;
        String res[] = {};
        for(int i = 0; i < thFiles.length; i++){
            String name = thFiles[i].getName();
            groups.add(name.substring(0, name.indexOf(".")));
        }
        return groups.toArray(res);
    }
}
