package com.example.auraudiorecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServicioGrabacion extends Service {


    private File root;
    private File file;
    private MediaRecorder mRecorder;
    public String fileName;

    // la siguiente propiedad es para cambiar mas facilmente el formato de nombre que recibiran los audios por defecto
    private String formato_del_nombre_por_defecto = "yyyyMMdd_HHmmss";

    private final IBinder iBinder = new MiBinder();

    public boolean isRecording = false;
    public String estado = "servicio apagado";


    public class MiBinder extends Binder {
        public ServicioGrabacion getService() {
            return ServicioGrabacion.this;
        }
    }


    @Override
    public void onCreate(){
        //Toast.makeText(this, "El ServicioGrabacion ejecuto onCreate()" , Toast.LENGTH_SHORT).show();
        Log.d("metodo", "El ServicioGrabacion ejecuto onCreate()");

        super.onCreate();

        //*******************************
        // preparacion de la grabacion
        //*******************************
        /*mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);*/

        root = android.os.Environment.getExternalStorageDirectory();
        file = new File(root.getAbsolutePath() + "/AUR/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }


        //*******************************
        // preparacion de la grabacion
        //*******************************

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Toast.makeText(this, "El ServicioGrabacion ejecuto onStartCommand()" , Toast.LENGTH_SHORT).show();
        Log.d("metodo", "El ServicioGrabacion ejecuto onStartCommand()");

        return START_STICKY;
    }


    @Override
    public void onDestroy(){
        //Toast.makeText(this, "El ServicioGrabacion ejecuto onDestroy()" , Toast.LENGTH_SHORT).show();
        Log.d("metodo", "El ServicioGrabacion ejecuto onDestroy()");

        stopRecording();

        super.onDestroy();

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

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Toast.makeText(this, "El ServicioGrabacion ejecuto onBind()" , Toast.LENGTH_SHORT).show();
        Log.d("metodo", "El ServicioGrabacion ejecuto onBind()");

        return iBinder;
    }





    //********************************
    // FUNCIONES DE GRABACION
    //********************************
    public void startRecording() {

        if(!isRecording) {

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //mRecorder.setAudioEncodingBitRate(16000);
            //mRecorder.setAudioSamplingRate(44100);


            /*root = android.os.Environment.getExternalStorageDirectory();
            file = new File(root.getAbsolutePath() + "/AUR/Audios");
            if (!file.exists()) {
            file.mkdirs();
            }*/

            fileName = root.getAbsolutePath() + "/AUR/Audios/" + nombre_por_defecto() + ".mp3";
            //Log.d("filename", fileName);
            mRecorder.setOutputFile(fileName);

            try {
                mRecorder.prepare();
                mRecorder.start();
                isRecording = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            estado = "Grabando...";
            Toast.makeText(this, R.string.grabando, Toast.LENGTH_SHORT).show();
            Log.d("metodo", "Grabando...");
        }

    }


    public void stopRecording() {

        if(isRecording) {
            try {
                mRecorder.stop();
                mRecorder.release();
                isRecording = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRecorder = null;

            estado = "Grabacion detenida";
            Toast.makeText(this, R.string.grabacion_detenida, Toast.LENGTH_SHORT).show();
            Log.d("metodo", "Grabacion detenida.");
        }

    }

    //********************************
    // FUNCIONES DE GRABACION
    //********************************




}
