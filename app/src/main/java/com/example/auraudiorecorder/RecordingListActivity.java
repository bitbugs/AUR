package com.example.auraudiorecorder;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerViewRecordings;
    private ArrayList<Recording> recordingArraylist;
    private RecordingAdapter recordingAdapter;
    private TextView textViewNoRecordings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textViewNoRecordings = findViewById(R.id.textViewNoRecordings);


        setContentView(R.layout.activity_recording_list);

        recordingArraylist = new ArrayList<>();

        initViews();

        fetchRecordings();
    }

    private void fetchRecordings() {
        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/AUR/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);

        //no era distinto de null, es tamaño mayor que cero!
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/AUR/Audios/" + fileName;

                Recording recording = new Recording(recordingUri, fileName,false);
                recordingArraylist.add(recording);
            }

            textViewNoRecordings.setVisibility(View.GONE);
            recyclerViewRecordings.setVisibility(View.VISIBLE);
            setAdaptertoRecyclerView();

        } else {
            //Toast.makeText(this, "No se han encontrado grabaciones", Toast.LENGTH_SHORT).show();
            textViewNoRecordings.setVisibility(View.VISIBLE);
            recyclerViewRecordings.setVisibility(View.GONE);
        }

    }

    private void setAdaptertoRecyclerView() {
        recordingAdapter = new RecordingAdapter(this,recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
    }

    private void initViews() {

        //establecer la toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.lista_grabaciones_title);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);

        //habilitar el boton de VOLVER
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //establecer el recyclerView
        recyclerViewRecordings = findViewById(R.id.recyclerViewRecordings);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerViewRecordings.setHasFixedSize(true);

        textViewNoRecordings = findViewById(R.id.textViewNoRecordings);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }





}
