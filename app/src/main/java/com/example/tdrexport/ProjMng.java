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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
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
import java.util.Map;

import static android.R.layout.simple_list_item_multiple_choice;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static java.lang.StrictMath.min;

public class ProjMng extends AppCompatActivity {

    public final String tdrDefaultDir = "/storage/emulated/0/TopoDroid";
    public String tdrDir;
    SharedPreferences tdrDirSpref;
    public final String tdrDirSprefKey = "tdr_dir_spref";

    private ListView filesListView;
    private ArrayAdapter<String> filesListAdapter;
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
        loadTdrDirSpref();
        debugTextView.setText("managing project " + tdrDir);
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

    public void onExportClick(View v){
        openFile();
    }

    private static final int PICK_DIRECTORY = 2;
    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_DIRECTORY);
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
                moveFiles();
            }
        }
    }


    /*
    Input: URI -- something like content://com.example.app.provider/table2/dataset1
    Output: PATH -- something like /sdcard/DCIM/123242-image.jpg
    */
    public String convertMediaUriToPath(Uri uri) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(uri, proj,  null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
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

    public void moveFiles(){
        //File destdir = new File(exportDirUri);

        for(String filename : exportFilesList){
            File source = new File(namePathMap.get(filename));
            try {
                Uri newkid = DocumentsContract.createDocument(getContentResolver(), exportDirUri, null, filename);
            }
            catch(FileNotFoundException e){
                Toast.makeText(this, "copy failed", Toast.LENGTH_SHORT).show();
            }
            File dest = new File(exportDir + "/" + filename);
            try {
                copyFileUsingStream(source, dest);
            }
            catch ( IOException e)
            {
                Toast.makeText(this, "copy failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadTdrDirSpref(){
        tdrDirSpref = getPreferences(MODE_PRIVATE);
        tdrDir = tdrDirSpref.getString(tdrDirSprefKey, tdrDefaultDir);
    }

}
