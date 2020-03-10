package com.example.auraudiorecorder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;

import android.content.ServiceConnection;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
    private boolean isRecording = false;
    private ServicioGrabacion mService;

    Intent intentGrabacion;



    //***************************
    // NOTIFICACIONES
    //***************************
    NotificationCompat.Builder notificacion;
    private static final int idUnica = 0;
    String channelId = "mi_channel_01";
    //***************************
    // FIN de NOTIFICACIONES
    //***************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Toast.makeText(this, "El MainActivity ejecuto onCreate()" , Toast.LENGTH_SHORT).show();
        //Log.d("metodo", "El MainActivity ejecuto onCreate()");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //establecer la condicion para pedir o no los permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }

        //*********************************************
        //bindService - conecta con ServicioGrabacion
        //*********************************************
        intentGrabacion = new Intent(MainActivity.this, ServicioGrabacion.class);

        //al ejecutar el startService()logro que el servicio permanesca abierto aunque la app haya sido cerrada
        //getApplicationContext().startService(intent);

        bindService(intentGrabacion, sConnection, Context.BIND_AUTO_CREATE);
        //*********************************************
        //bindService - conecta con ServicioGrabacion
        //*********************************************


        //inicializar las vistas
        initViews();


        //crearNotificacion("Bienvenido");



    }

    @Override protected void onStart() {
        //Toast.makeText(this, "El MainActivity ejecuto onStart()", Toast.LENGTH_SHORT).show();
        //Log.d("metodo", "El MainActivity ejecuto onStart()");
        super.onStart();
    }
    @Override
    protected void onResume() {
        //Toast.makeText(this, "El MainActivity ejecuto onResume()", Toast.LENGTH_SHORT).show();
        //Log.d("metodo", "El MainActivity ejecuto onResume()");
        super.onResume();

    }
    @Override protected void onPause() {
        //Toast.makeText(this, "El MainActivity ejecuto onPause()", Toast.LENGTH_SHORT).show();
        //Log.d("metodo", "El MainActivity ejecuto onPause()");
        super.onPause();
    }
    @Override protected void onStop() {
        //Toast.makeText(this, "El MainActivity ejecuto onStop()", Toast.LENGTH_SHORT).show();
        //Log.d("metodo", "El MainActivity ejecuto onStop()");
        super.onStop();
    }
    @Override protected void onRestart() {
        //String estado = mService.estado;

        //Toast.makeText(this, "El MainActivity ejecuto onRestart() y el estado del servicio es: " + estado, Toast.LENGTH_LONG).show();
        //Log.d("metodo", "El MainActivity ejecuto onRestart() y el estado del servicio es: " + estado);
        super.onRestart();
    }
    @Override protected void onDestroy() {
        //Toast.makeText(this, "El MainActivity ejecuto onDestroy()", Toast.LENGTH_SHORT).show();
        //Log.d("metodo", "El MainActivity ejecuto onDestroy()");
        super.onDestroy();
    }


    //*************************************
    // para conectar al servicio
    //*************************************


    private ServiceConnection sConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServicioGrabacion.MiBinder binder = (ServicioGrabacion.MiBinder) service;
            mService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    //*************************************
    // para conectar al servicio
    //*************************************



    @Override
    protected void onSaveInstanceState(Bundle estado){
        super.onSaveInstanceState(estado);
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




    //**************************
    // MANEJADOR DELOS CLICKS
    //**************************

    @Override
    public void onClick(View view) {

        if (view == imageViewRecord) {
            prepareForRecording();

            mService.startRecording();

            lastProgress = 0;
            seekBar.setProgress(0);
            stopPlaying();
            //el imageview cambia al boton de STOP

            //comenzar el cronometro
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();

            //Crea una notificacion en la barra de estado de android
            crearNotificacion("Grabando...");
            fileName = mService.fileName;

        } else if (view == imageViewStop) {
            prepareForStop();

            mService.stopRecording();

            //detener el cronometro
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());

            //Crea una notificacion en la barra de estado de android
            crearNotificacion("Grabacion detenida.");

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
    //**************************
    // MANEJADOR DELOS CLICKS
    //**************************



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



    //********************************
    // FUNCIONES DE REPRODUCCION
    //********************************
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
    //********************************
    // FUNCIONES DE REPRODUCCION
    //********************************





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





    //***************************
    // NOTIFICACIONES
    //***************************
    public void crearNotificacion(String mensaje){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

            CharSequence name = "Texto de CharSenquence name";
            String description = "Texto de String descripcion";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);

            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            //mChannel.enableVibration(true);
            //mChannel.setVibrationPattern(new long[]{100,200,300,400,500,400,300,200,400});

            mNotificationManager.createNotificationChannel(mChannel);
            notificacion = new NotificationCompat.Builder(this, channelId);

            notificacion.setSmallIcon(R.drawable.ic_keyboard_voice_black_24dp);
            notificacion.setTicker(".:AUR:.  " + mensaje);
            //notificacion.setContentTitle("AUR Audio Recorder");
            notificacion.setContentText(mensaje);

            //*****************************
            //*****************************
            Intent intent = getIntent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificacion.setContentIntent(pendingIntent);
            //*****************************
            //*****************************

            mNotificationManager.notify(1, notificacion.build());

        }else {

            notificacion = new NotificationCompat.Builder(this, "miNotificacion");
            notificacion.setAutoCancel(true);
            notificacion.setSmallIcon(R.drawable.ic_keyboard_voice_black_24dp);
            notificacion.setPriority(Notification.PRIORITY_HIGH);
            notificacion.setTicker(".:AUR:.  " + mensaje);
            notificacion.setContentTitle("AUR Audio Recorder");
            notificacion.setContentText(mensaje);
            notificacion.setAutoCancel(false);

            Intent intentNotificacion = getIntent();
            intentNotificacion.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntentNotificacion = PendingIntent.getActivity(MainActivity.this, 0, intentNotificacion, PendingIntent.FLAG_UPDATE_CURRENT);
            notificacion.setContentIntent(pendingIntentNotificacion);

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(idUnica, notificacion.build());

        }



    }

    //***************************
    // FIN de NOTIFICACIONES
    //***************************



}