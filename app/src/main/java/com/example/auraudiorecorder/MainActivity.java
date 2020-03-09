package com.example.auraudiorecorder;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// comentario para probar las ramas y el pull
// mi comentario Jorge
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private Chronometer chronometer;
    private ImageView imageViewRecord, imageViewPlay, imageViewStop;
    private SeekBar seekBar;
    private LinearLayout linearLayoutRecorder, linearLayoutPlay;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String fileName = null;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    private int RECORD_AUDIO_REQUEST_CODE =123 ;
    private boolean isPlaying = false;

    // la siguiente propiedad es para cambiar mas facilmente el formato de nombre que recibiran los audios por defecto
    private String formato_del_nombre_por_defecto = "yyyyMMdd_HHmmss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //establecer la condicion para pedir o no los permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }

        //inicializar las vistas
        initViews();

    }

    //metodo para inicializar las vistas
    private void initViews() {
        //establecer la toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("AUR audio recorder");
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);

        linearLayoutRecorder = findViewById(R.id.linearLayoutRecorder);
        chronometer = findViewById(R.id.chronometerTimer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        imageViewRecord = findViewById(R.id.imageViewRecord);
        imageViewStop = findViewById(R.id.imageViewStop);
        imageViewPlay = findViewById(R.id.imageViewPlay);
        linearLayoutPlay = findViewById(R.id.linearLayoutPlay);
        seekBar = findViewById(R.id.seekBar);

        imageViewRecord.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);
        imageViewPlay.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.actionLista:
                gotoRecodingListActivity();
                return true;
            case R.id.actionPreferencias:
                lanzarPreferenciasActivity(null);
                return true;
            case R.id.actionTips:
                lanzarTipsActivity(null);
                return true;
            case R.id.actionAcercaDe:
                lanzarAcercaDeAction(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void gotoRecodingListActivity() {
        Intent intent = new Intent(this, RecordingListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void lanzarPreferenciasActivity(View view) {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void mostrarPreferencias(View view) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String s = "tema oscuro: " + pref.getBoolean("temaOscuro", false)
                + ", formato de archivo: " + pref.getString("formatoGrabacion", "?")
                + ", muestreo de la grabacion: " + pref.getString("muestreoGrabacion", "?");
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void lanzarTipsActivity(View view) {
        Intent intent = new Intent(this, TipsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void lanzarAcercaDeAction(View view) {
        Intent intent = new Intent(this, AcercaDeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public void onClick(View view) {
        if (view == imageViewRecord) {
            prepareForRecording();
            startRecording();
        } else if (view == imageViewStop) {
            prepareForStop();
            stopRecording();
        } else if (view == imageViewPlay) {
            if (!isPlaying && fileName != null) {
                isPlaying = true;
                startPlaying();
            } else {
                isPlaying = false;
                stopPlaying();
            }
        }
    }

    //los metodos prepareFor se aseguran que se vean los iconos adecuados, y manejan la transicion entre ellos
    private void prepareForStop() {
        TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.GONE);
        linearLayoutPlay.setVisibility(View.VISIBLE);
    }

    private void prepareForRecording() {
        TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.GONE);
        imageViewStop.setVisibility(View.VISIBLE);
        linearLayoutPlay.setVisibility(View.GONE);
    }

    private void stopPlaying() {
        try {
            mPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayer = null;

        //mostrar el boton PLAY
        imageViewPlay.setImageResource(R.drawable.ic_play);
        chronometer.stop();
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //hay que sustituir el metodo para obtener el path completo del archivo guardado
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/AUR/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }

        fileName = root.getAbsolutePath() + "/AUR/Audios/" + nombre_por_defecto() + ".mp3";
        Log.d("filename", fileName);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastProgress = 0;
        seekBar.setProgress(0);
        stopPlaying();
        //el imageview cambia al boton de STOP
        //comenzar el cronometro
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }


    private void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecorder = null;

        //detener el cronometro
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());

        //mostrar mensaje
        Toast.makeText(this, R.string.grabacion_guardada, Toast.LENGTH_SHORT).show();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }

        //mostrar el boton PAUSA en el imageview
        imageViewPlay.setImageResource(R.drawable.ic_pause);

        seekBar.setProgress(lastProgress);
        mPlayer.seekTo(lastProgress);
        seekBar.setMax(mPlayer.getDuration());
        seekUpdate();
        chronometer.start();


        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imageViewPlay.setImageResource(R.drawable.ic_play);
                isPlaying = false;
                chronometer.stop();
            }
        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayer != null && fromUser) {
                    mPlayer.seekTo(progress);
                    chronometer.setBase(SystemClock.elapsedRealtime() - mPlayer.getCurrentPosition());
                    lastProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdate();
        }
    };

    private void seekUpdate() {
        if (mPlayer != null) {
            int mCurrentPosition = mPlayer.getCurrentPosition() ;
            seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
        }
        mHandler.postDelayed(runnable, 100);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        /* 1) Usar la version de la libreria de soporte ContextCompat.checkSelfPermission para evitar tener que verificar
        * la version build ya que Context.checkSelfPermission solo esta disponible en Marshmallow.
        * 2) Siempre hay que verificar los permisos, aunque ya hayan sido concedidos, ya que el usuario puede revocarlos
        * despues de haberlos otorgado.*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {

            /* Los permisos aun no están otorgados.
            * Hay que verificar si ya se le pidieron antes los permisos, y el usuario los denego. Si paso eso, hay que explicar con mas detalle
            * el por que son necesarios.
            * Se lanza una peticion asincronica (async request) para obtener los permisos. Esto mostrara el cuadro de dialogo estandar
            * de solicitud de permisos.*/
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);

        }
    }

    // LLamado con la solicitud de llamar al metodo requestPermissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Asegurarse que es el codigo original del pedido de permisos
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this, R.string.permisos_concedidos, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, R.string.explicar_permisos, Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }



    // metodo para estampar el instante en que se inicia la grabacion
    private String nombre_por_defecto(){

        // usando la clase Date
        Date date = new Date();
        //String fecha = date.toString();

        // usando simple date format
        SimpleDateFormat sdf = new SimpleDateFormat(formato_del_nombre_por_defecto);
        String fecha = sdf.format(date);

        return fecha;

        //Toast.makeText(getApplicationContext(),"La fecha es: " + fecha, Toast.LENGTH_LONG).show();

    }



}