package com.example.auraudiorecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServicioGrabacion extends Service {

    private MediaRecorder mRecorder;
    private String fileName = null;
    // la siguiente propiedad es para cambiar mas facilmente el formato de nombre que recibiran los audios por defecto
    private String formato_del_nombre_por_defecto = "yyyyMMdd_HHmmss";


    public void onCreate(){
        super.onCreate();
    }


    public int onStartCommand(Intent intent, int flags, int startId){

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

            //llamada a los metodos para lanzar notificaciones en la barra de estado
            //setPendingIntent();
            //createNotification("grabando...");

        } catch (IOException e) {
            e.printStackTrace();
        }


        return START_STICKY;
    }


    public void onDestroy(){

        super.onDestroy();


        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecorder = null;


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
        return null;
    }




}
