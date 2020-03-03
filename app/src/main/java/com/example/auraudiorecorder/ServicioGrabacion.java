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


    File root = android.os.Environment.getExternalStorageDirectory();
    private MediaRecorder mRecorder;
    private String fileName = null;
    // la siguiente propiedad es para cambiar mas facilmente el formato de nombre que recibiran los audios por defecto
    private String formato_del_nombre_por_defecto = "yyyyMMdd_HHmmss";

    private final IBinder iBinder = new MiBinder();

    public boolean isRecording = false;

    public String saludo = "hola soy una propiedad del servicio";


    public class MiBinder extends Binder {
        public ServicioGrabacion getService() {
            return ServicioGrabacion.this;
        }
    }


    @Override
    public void onCreate(){
        //super.onCreate();
        Toast.makeText(this, "ServicioGrabacion.onCreate().", Toast.LENGTH_SHORT).show();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        //hay que sustituir el metodo para obtener el path completo del archivo guardado
        //File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/AUR/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }



        Toast.makeText(this, "ServicioGrabacion.onStartCommand().", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this, "ServicioGrabacion.onDestroy().", Toast.LENGTH_SHORT).show();
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

        return iBinder;
    }


    //********************************
    // FUNCIONES DE GRABACION
    //********************************
    public String startRecording() {

        /*fileName = root.getAbsolutePath() + "/AUR/Audios/" + nombre_por_defecto() + ".mp3";
        Log.d("filename", fileName);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return "Service: grabando...";
    }


    public String stopRecording() {

        /*try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecorder = null;*/

        return "Service: grabacion finalizada.";
    }

    //********************************
    // FUNCIONES DE GRABACION
    //********************************




}
