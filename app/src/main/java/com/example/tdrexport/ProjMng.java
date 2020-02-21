package com.example.tdrexport;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.ArraySet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.R.layout.simple_list_item_multiple_choice;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static java.lang.StrictMath.min;

public class ProjMng extends AppCompatActivity {

    public final String tdrDefaultDir = "/storage/emulated/0/TopoDroid";
    public String tdrDir;
    SharedPreferences tdrDirSpref;
    SharedPreferences exportFilesSpref;
    SharedPreferences exportDirSpref;

    public final String enterSettingsAnnot = "Введите путь и выберите файлы для экспорта или загрузите прошлые настройки";

    public final String exportFilesSprefSuffix = "_files";
    public final String exportDirSprefSuffix = "_dir";
    public final String tdrDirSprefKey = "tdr_dir_spref";

    public String projName;
    private ListView filesListView;
    private ArrayAdapter<String> filesListAdapter;
    private EditText enterPath;
    ArrayList<String>  filesList;
    ArrayList<String>  exportFilesList;
    HashMap<String, String> namePathMap;
    String exportDir;
    Uri exportDirUri;


    TextView debugTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proj_mng);
        debugTextView = findViewById(R.id.DEBUG_TEXT);
        filesListView = findViewById(R.id.FILESLIST);
        enterPath = findViewById(R.id.EXPORTPATH);
        debugTextView.setText("шуршу по файликам");

        namePathMap = new HashMap<>();
        exportFilesList = new ArrayList<>();
        filesList = new ArrayList<>();
        filesListAdapter = new ArrayAdapter<>(this, simple_list_item_multiple_choice, filesList);
        filesListView.setAdapter(filesListAdapter);
        filesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        Intent intent = getIntent();
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        projName = message;
        loadTdrDirSpref();
        buildProjList(message);
        debugTextView.setText(enterSettingsAnnot);
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

    public void onExportClick(View v){
        //openFile();
        exportFilesList.clear();
        SparseBooleanArray sbArray = filesListView.getCheckedItemPositions();
        for (int i = 0; i < sbArray.size(); i++) {
            int key = sbArray.keyAt(i);
            if (sbArray.get(key))
                exportFilesList.add(filesList.get(key));
        }
        exportDir = enterPath.getText().toString();

        if(moveFiles()){
            saveExportFilesSpref();
            saveExportDirSpref();
        }
    }

    private static final int PICK_DIRECTORY = 2;
    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_DIRECTORY);
    }

    public void onLoadPrefClick(View v) {
        loadExportFilesSpref();
        loadExportDirSpref();
        enterPath.setText(exportDir);
        for(String name : filesList){
            if(exportFilesList.contains(name)) filesListView.setItemChecked(filesList.indexOf(name), true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_DIRECTORY && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                //exportDir = convertMediaUriToPath(uri);//uri.getLastPathSegment();
                exportDirUri = uri;
                //moveFiles();
            }
        }
    }


    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public boolean moveFiles(){
        if(exportDir.isEmpty()){
            Toast.makeText(this, "Экспорт фейлед", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            File destdir = new File(exportDir);
            if (!destdir.exists()) {
                destdir.mkdirs();
            }
            for (String filename : exportFilesList) {
                File source = new File(namePathMap.get(filename));
                File dest = new File(exportDir + "/" + filename);
                dest.createNewFile();
                copyFileUsingStream(source, dest);
            }
            Toast.makeText(this, "Экспорт суксессфул", Toast.LENGTH_SHORT).show();
            return true;
        }
        catch ( IOException e){
            Toast.makeText(this, "Экспорт фейлед", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void loadTdrDirSpref(){
        tdrDirSpref = getPreferences(MODE_PRIVATE);
        tdrDir = tdrDirSpref.getString(tdrDirSprefKey, tdrDefaultDir);
    }

    public void saveExportFilesSpref() {
        exportFilesSpref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = exportFilesSpref.edit();
        ed.putStringSet(projName + exportFilesSprefSuffix, new HashSet<>(exportFilesList));
        ed.commit();
    }

    public void loadExportFilesSpref(){
        exportFilesSpref = getPreferences(MODE_PRIVATE);
        exportFilesList = new ArrayList<>(exportFilesSpref.getStringSet(projName + exportFilesSprefSuffix,  new HashSet<>(exportFilesList)));
    }

    private void loadExportDirSpref(){
        exportDirSpref = getPreferences(MODE_PRIVATE);
        exportDir = exportDirSpref.getString(projName + exportDirSprefSuffix, tdrDir);
    }

    public void saveExportDirSpref() {
        exportDirSpref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = exportDirSpref.edit();
        ed.putString(projName + exportDirSprefSuffix, exportDir);
        ed.commit();
    }
}
