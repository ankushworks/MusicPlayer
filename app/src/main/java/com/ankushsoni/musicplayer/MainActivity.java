package com.ankushsoni.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity  {
    private String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE"};
    public static final int requestCode = 80;
    ListView listView;
    int permsRequestCode = 200 ;
    int inc = 0;
    private ProgressBar spinner;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        spinner  = (ProgressBar) findViewById(R.id.progressBar1);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "You have already granted this permission", Toast.LENGTH_SHORT).show();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<File> songs = fetchSongs(Environment.getExternalStorageDirectory());
                        String [] items = new String[songs.size()];
                        for(int i=0 ; i< songs.size() ; i++){
                            items[i] = songs.get(i).getName().replace(".mp3" , "");
                        }
                        ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1  ,items);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listView.setAdapter(arrayAdapter);
                                spinner.setVisibility(View.GONE);
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Intent intent = new Intent(MainActivity.this,PlaySong.class);
                                        String currentSong = listView.getItemAtPosition(i).toString();
                                        intent.putExtra("songList" , songs);
                                        intent.putExtra("currentSong" , currentSong);
                                        intent.putExtra("position" , i);
                                        startActivity(intent);
                                    }
                                });
                            }
                        });
                    }
                });

            } else {
                requestStoragePermission();
            }
        }
    }


    public void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
            new AlertDialog.Builder(this)
                    .setTitle("Request Permission")
                    .setMessage("Without this permission the Music Player is unable to read storage Due to which you are not able to listen music")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,permissions,requestCode);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).create().show();
        }else{
            Toast.makeText(this, "Go To Settings and allow permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,permissions,requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(this.requestCode == requestCode){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted!!", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this, "Permission DENEYED", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    public ArrayAdapter<String> setData(){
        ArrayList<File> songs = fetchSongs(Environment.getExternalStorageDirectory());
        String [] items = new String[songs.size()];
        for(int i=0 ; i< songs.size() ; i++){
            items[i] = songs.get(i).getName().replace(".mp3" , "");
        }
        ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1  ,items);
        return arrayAdapter;
    }


    public ArrayList<File> fetchSongs(File file){
        ArrayList arrayList = new ArrayList();
        File[] songs = file.listFiles();
        if(songs != null){
            for(File myFile : songs){
                if(!myFile.isHidden() && myFile.isDirectory()){
                    arrayList.addAll(fetchSongs(myFile));
                }else {
                    if(myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith(".") && !((myFile.getName().charAt(0) >= '0') && (myFile.getName().charAt(0) <= '9'))){
                        arrayList.add(myFile);
                    }
                }
            }
        }
        return arrayList;
    }

}