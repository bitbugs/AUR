package com.example.auraudiorecorder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

// comentario para probar las ramas y el pull
// mi comentario Jorge
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View rootView;
    private Toolbar toolbar;
    private Chronometer chronometer;
    private ImageView imageViewRecord, imageViewPlay, imageViewStop, imageViewPause;
    private SeekBar seekBar;
    private LinearLayout linearLayoutRecorder, linearLayoutPlay;
    // private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String fileName = null;
    private int lastProgress = 0;
    private long pauseProgress;
    private Handler mHandler = new Handler();
    private int RECORD_AUDIO_REQUEST_CODE =1234 ;
    private boolean isPlaying = false;
    private boolean isRecording = false;
    private ServicioGrabacion mService;

    SharedPreferences preferencias;
    private boolean temaOscuro = false;
    //private Window window;

    //para poder silenciar el dispositivo mientras se esta grabando audio
    AudioManager audioManager;


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
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefTemaOscuro = preferencias.getBoolean("temaOscuro", false);
        //if(prefTemaOscuro){
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppTheme_DarkTheme);
        }else{
            setTheme(R.style.AppTheme);
        }


        super.onCreate(savedInstanceState);

        //this.window = getWindow();

        setContentView(R.layout.activity_main);

        //establecer la condicion para pedir o no los permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }

        //se inicializa el audioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //*********************************************
        //bindService - conecta con ServicioGrabacion
        //*********************************************
        intentGrabacion = new Intent(MainActivity.this, ServicioGrabacion.class);

        //al ejecutar el startService()logro que el servicio permanezca abierto aunque la app haya sido cerrada
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

        /*==================================
            RE-SETEA LAS CONFIGURACIONES
        ==================================*/
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefTemaOscuro = preferencias.getBoolean("temaOscuro", false);
        //String prefFormatoGrabacion = preferencias.getString("formatoGrabacion", "1");
        //String prefMuestreoGrabacion = preferencias.getString("muestreoGrabacion", "2");

        if(temaOscuro != prefTemaOscuro){

            if(prefTemaOscuro){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                setTheme(R.style.AppTheme_DarkTheme);
                //Toast.makeText(this, R.string.modo_oscuro, Toast.LENGTH_SHORT).show();
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                setTheme(R.style.AppTheme);
                //Toast.makeText(this, R.string.modo_claro, Toast.LENGTH_SHORT).show();
            }

            setContentView(R.layout.activity_main);
            initViews();
            temaOscuro = prefTemaOscuro;
        }

        /*==================================
            RE-SETEA LAS CONFIGURACIONES
        ==================================*/

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

        //Toast.makeText(this, "El MainActivity ejecuto onRestart()", Toast.LENGTH_LONG).show();
        //Log.d("metodo", "El MainActivity ejecuto onRestart() y el estado del servicio es: " + estado);
        super.onRestart();
    }
    @Override protected void onDestroy() {
        //Toast.makeText(this, "El MainActivity ejecuto onDestroy()", Toast.LENGTH_SHORT).show();
        //Log.d("metodo", "El MainActivity ejecuto onDestroy()");

        //antes de terminar la app regresa el audio del dispositivo a modo normal
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

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

    //*************************************
    //metodo para inicializar las vistas
    //*************************************
    private void initViews() {

        //establecer la toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("AUR audio recorder");
        //toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //toolbar.setTitleTextColor(getResources().getColor(R.color.grisTexto));

        setSupportActionBar(toolbar);

        linearLayoutRecorder = findViewById(R.id.linearLayoutRecorder);
        //rootView = linearLayoutRecorder.getRootView();
        rootView = findViewById(R.id.root_ActivityMain);

        chronometer = findViewById(R.id.chronometerTimer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        imageViewRecord = findViewById(R.id.imageViewRecord);
        imageViewStop = findViewById(R.id.imageViewStop);
        imageViewPlay = findViewById(R.id.imageViewPlay);
        imageViewPause = findViewById(R.id.imageViewPause);
        linearLayoutPlay = findViewById(R.id.linearLayoutPlay);
        seekBar = findViewById(R.id.seekBar);

        imageViewRecord.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);
        imageViewPlay.setOnClickListener(this);
        imageViewPause.setOnClickListener(this);

        //**************************
        // DINAMICA DE LA SEEKBAR
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayer != null && fromUser) {
                    lastProgress = (progress / 1000) * 1000;
                    pauseProgress = (long) lastProgress;

                    if (isPlaying) {
                        chronometer.stop();
                        mPlayer.pause();
                    }

                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseProgress);
                    mPlayer.seekTo(lastProgress);

                    if (isPlaying) {
                        chronometer.start();
                        mPlayer.start();
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // DINAMICA DE LA SEEKBAR
        //**************************

    }
    //*************************************
    //metodo para inicializar las vistas
    //*************************************


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
    // MANEJADOR DE LOS CLICKS
    //**************************
    @Override
    public void onClick(View view) {

        if (view == imageViewRecord) {
            prepareForRecording();

            //pone el dispositivo en modo silencioso
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

            //comenzar el cronometro
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();

            mService.startRecording();

            lastProgress = 0;
            seekBar.setProgress(0);
            stopPlaying();
            //el imageview cambia al boton de STOP

            //Crea una notificacion en la barra de estado de android
            crearNotificacion(getString(R.string.grabando));
            fileName = mService.fileName;

        } else if (view == imageViewStop) {
            prepareForStop();

            //regresa el dispositivo al modo normal de audio
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

            mService.stopRecording();

            //detener el cronometro
            chronometer.stop();
            //chronometer.setBase(SystemClock.elapsedRealtime());

            //Crea una notificacion en la barra de estado de android
            crearNotificacion(getString(R.string.grabacion_detenida));

            inicializaAudio();

        } else if (view == imageViewPlay) {

            if (!isPlaying && mPlayer!=null) {

                lastProgress = (lastProgress / 1000) * 1000;
                pauseProgress = (long) lastProgress;

                //Toast.makeText(this, "Play: pauseProgress = "+pauseProgress+" y lastProgress = "+lastProgress, Toast.LENGTH_LONG).show();
                Toast.makeText(this, R.string.reproduciendo, Toast.LENGTH_SHORT).show();

                chronometer.setBase(SystemClock.elapsedRealtime() - pauseProgress);
                chronometer.start();
                seekBar.setProgress(lastProgress);
                mPlayer.seekTo(lastProgress);

                try {
                    mPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                isPlaying = true;
                imageViewPause.setVisibility(View.VISIBLE);
                imageViewPlay.setVisibility(View.GONE);
            }

        }else if (view == imageViewPause) {
            if (isPlaying) {
                chronometer.stop();
                mPlayer.pause();

                lastProgress = (int) (SystemClock.elapsedRealtime() - chronometer.getBase());
                lastProgress = (lastProgress / 1000) * 1000;
                pauseProgress = (long) lastProgress;

                //Toast.makeText(this, "Pause: pauseProgress = "+pauseProgress+" y lastProgress = "+lastProgress, Toast.LENGTH_LONG).show();
                Toast.makeText(this, R.string.pausado, Toast.LENGTH_SHORT).show();

                isPlaying = false;

                imageViewPlay.setVisibility(View.VISIBLE);
                imageViewPause.setVisibility(View.GONE);
            }
        }

    }
    //**************************
    // MANEJADOR DE LOS CLICKS
    //**************************


    //*****************************************
    // INICIALIZACION DEL AUDIO A REPRODUCIR
    //*****************************************
    private void inicializaAudio(){

        try {
            if(mPlayer != null){
                mPlayer.release();
                mPlayer = null;
            }

            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LOG_TAG", "Algo fallo al inicializar el audio.");
        }
        pauseProgress = 0;
        lastProgress = 0;

        isPlaying = false;

        //mPlayer.seekTo(lastProgress);
        seekBar.setMax(mPlayer.getDuration());
        //seekBar.setProgress(lastProgress);
        seekUpdate();


        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imageViewPlay.setVisibility(View.VISIBLE);
                imageViewPause.setVisibility(View.GONE);
                isPlaying = false;
                chronometer.stop();

                pauseProgress = 0;
                lastProgress = 0;

                mPlayer.seekTo(0);
                seekBar.setProgress(0);

            }
        });



    }
    //*****************************************
    // INICIALIZACION DEL AUDIO A REPRODUCIR
    //*****************************************





    //*******************************************************
    // metodos para manejar la transicion entre los iconos

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
    // metodos para manejar la transicion entre los iconos
    //*******************************************************



    //********************************
    // FUNCIONES DE REPRODUCCION
    //********************************
    private void stopPlaying() {
        try {
            mPlayer.stop();
            mPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayer = null;
        pauseProgress = 0;
    }



    private void startPlaying() {
        mPlayer.start();

        seekBar.setProgress(lastProgress);
        mPlayer.seekTo(lastProgress);
        seekUpdate();
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        /* 1) Usar la version de la libreria de soporte ContextCompat.checkSelfPermission para evitar tener que verificar
        * la version build ya que Context.checkSelfPermission solo esta disponible en Marshmallow.
        * 2) Siempre hay que verificar los permisos, aunque ya hayan sido concedidos, ya que el usuario puede revocarlos
        * despues de haberlos otorgado.*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {

            /* Los permisos aun no están otorgados.
            * Hay que verificar si ya se le pidieron antes los permisos, y el usuario los denego. Si paso eso, hay que explicar con mas detalle
            * el por que son necesarios.
            * Se lanza una peticion asincronica (async request) para obtener los permisos. Esto mostrara el cuadro de dialogo estandar
            * de solicitud de permisos.*/
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_NOTIFICATION_POLICY},
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
            if (grantResults.length == 4 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED){

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
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);

            mChannel.setDescription(description);
            //mChannel.enableLights(true);
            //mChannel.setLightColor(Color.RED);
            //mChannel.set
            mChannel.enableVibration(false);
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


            //notificacion.setSmallIcon(R.drawable.ic_keyboard_voice_black_24dp);
            //no permite establecer un icono en xml, solo admite png
            notificacion.setSmallIcon(R.drawable.ic_stat_name);
            //notificacion.setSmallIcon(R.drawable.ic_microphone);


            notificacion.setPriority(Notification.PRIORITY_LOW);
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