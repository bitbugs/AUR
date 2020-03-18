package com.example.auraudiorecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Recording> recordingArrayList;
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private int last_index = -1;
    //private int lastProgress = 0;

    public RecordingAdapter(Context context, ArrayList<Recording> recordingArrayList){
        this.context = context;
        this.recordingArrayList = recordingArrayList;
    }

    //verificar comportamiento play-pause
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recording_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        setUpData(holder,position);
    }

    private void setUpData(final ViewHolder holder, final int position) {
        Recording recording = recordingArrayList.get(position);
        holder.textViewName.setText(recording.getFileName());
        holder.editTextName.setText(recording.getFileName().replace(".mp3", ""));

        holder.botonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //crear el menú popup
                PopupMenu popup = new PopupMenu(context, holder.botonMore);

                //inflarlo desde el recurso xml
                popup.inflate(R.menu.menu_recording_item);

                //agregar el escuchador
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.actionRenombrar:
                                //manejar el clic sobre Renombrar

                                String nombre = recordingArrayList.get(position).getFileName();
                                Log.d("Click", "SE SELECCIONO LA OPCION: Renombrar");
                                //Log.d("NOMBRE", "EL ARCHIVO SE LLAMA: "+ nombre);
                                //renombrar(position, nombre);

                                holder.editTextName.setVisibility(View.VISIBLE);
                                holder.textViewName.setVisibility(View.GONE);

                                holder.btnCambiarNombre.setVisibility(View.VISIBLE);
                                holder.btnCancelarCambiarNombre.setVisibility(View.VISIBLE);

                                holder.editTextName.setSelectAllOnFocus(true);
                                //holder.editTextName.selectAll();
                                holder.editTextName.requestFocus();

                                //holder.textViewName.setText("HOLISSSS");

                                break;
                            case R.id.actionCompartir:

                                //manejar el clic sobre Renombrar


                                //manejar el clic sobre Compartir

                                break;
                            case R.id.actionEliminar:
                                String archivo = recordingArrayList.get(position).getFileName();
                                lanzarConfirmarBorrado(archivo, holder, position);

                                //


                                //return true;
                                break;
                            case R.id.actionAdjuntarNota:
                                //manejar el clic sobre Adjuntar nota
                                break;
                            case R.id.actionEtiquetar:
                                //manejar el clic sobre Etiquetar
                                break;
                        }
                        return false;
                    }
                });
                //mostrar el menú popup
                popup.show();
            }
        });



        //***************************************
        // BOTONES PARA CAMBIAR NOMBRE
        holder.btnCambiarNombre.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String nombre = recordingArrayList.get(position).getFileName();
                //Log.d("BOTON", "se hizo click en el btnCambiarNombre!!!!!"+nombre);

                File root = android.os.Environment.getExternalStorageDirectory();
                String path = root.getAbsolutePath() + "/AUR/Audios/";

                File audio = new File(path + nombre);



                //File de destino
                String nuevoNombre = holder.editTextName.getText().toString();
                //el siguiente if maneja el error generado si el nuevo nombre es un string vacio
                if(nuevoNombre.isEmpty()){
                    Log.d("CAMBIO DE NOMBRE", "se debe especificar un nuevo nombre para el archivo!");
                    nuevoNombre = nombre.replace(".mp3","");
                }
                File audioConNuevoNombre = new File(path + nuevoNombre + ".mp3");

                //Rename
                if( audio.renameTo(audioConNuevoNombre) ){
                    holder.textViewName.setText(nuevoNombre + ".mp3");
                    Log.d("CONFIRMACION", "se cambio el nombre correctamente a: "+nuevoNombre);
                } else{
                    Log.d("CONFIRMACION", "ocurrio un error al cambiar el nombre del archivo: "+nombre);
                }



                holder.textViewName.setVisibility(View.VISIBLE);
                holder.editTextName.setVisibility(View.GONE);
                holder.btnCambiarNombre.setVisibility(View.GONE);
                holder.btnCancelarCambiarNombre.setVisibility(View.GONE);
            }
        });

        holder.btnCancelarCambiarNombre.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("BOTON", "se hizo click en el btnCancelarCambiarNombre!!!!!");

                holder.textViewName.setVisibility(View.VISIBLE);
                holder.editTextName.setVisibility(View.GONE);
                holder.btnCambiarNombre.setVisibility(View.GONE);
                holder.btnCancelarCambiarNombre.setVisibility(View.GONE);
            }
        });
        // BOTONES PARA CAMBIAR NOMBRE
        //***************************************



        //manejar los cambios de icono entre play y pause
        if (recording.isPlaying()) {
            holder.imageViewPlay.setImageResource(R.drawable.ic_pause);
            android.transition.TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.seekUpdate(holder);
        } else {
            holder.imageViewPlay.setImageResource(R.drawable.ic_play);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.GONE);
        }

        holder.manageSeekBar(holder);
    }

    private void lanzarConfirmarBorrado(final String archivo, final ViewHolder miViewHolder, final int position) {
        final TextView alerta = new TextView(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.borrar_grabacion)
                .setMessage(R.string.desea_borrar + " -> " + archivo)
                .setView(alerta)
                .setPositiveButton(R.string.borrar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File root = android.os.Environment.getExternalStorageDirectory();
                        String path = root.getAbsolutePath() + "/AUR/Audios/";

                        File audio = new File(path + archivo);
                        //llamar accion de borrar
                        audio.delete();

                        //oculta la vista del audio en la RecordingListActivity o reinicia la activity
                        miViewHolder.imageViewPlay.setVisibility(View.GONE);
                        miViewHolder.textViewName.setVisibility(View.GONE);
                        miViewHolder.botonMore.setVisibility(View.GONE);

                        miViewHolder.linea.setVisibility(View.GONE);
                        miViewHolder.itemGrabacion.setPadding(0,0,0,0);
                        miViewHolder.itemGrabacion.setPaddingRelative(0,0,0,0);

                        miViewHolder.itemGrabacion.setVisibility(View.GONE);
                        //notifyItemRangeRemoved(position+1, position);

                        Toast.makeText(context, R.string.se_eligio_borrar, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //llamar accion de NO borrar
                        Toast.makeText(context, "se eligio NO BORRAR", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return recordingArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPlay;
        SeekBar seekBar;
        TextView textViewName;
        EditText editTextName;

        ImageButton botonMore;

        Button btnCambiarNombre;
        Button btnCancelarCambiarNombre;

        private String recordingUri;
        //private int lastProgress = 0;
        private Handler mHandler = new Handler();
        ViewHolder holder;

        RelativeLayout itemGrabacion;
        View linea;



        public ViewHolder(View itemView) {
            super(itemView);

            itemGrabacion = itemView.findViewById(R.id.itemLayout);
            linea = itemView.findViewById(R.id.linea);

            imageViewPlay = itemView.findViewById(R.id.imageViewPlay);
            seekBar = itemView.findViewById(R.id.seekBar);
            textViewName = itemView.findViewById(R.id.textViewRecordingname);
            editTextName = itemView.findViewById(R.id.editTextRecordingName);
            botonMore = itemView.findViewById(R.id.botonMore);
            btnCambiarNombre = itemView.findViewById(R.id.btnCambiarNombre);
            btnCancelarCambiarNombre = itemView.findViewById(R.id.btnCancelarCambiarNombre);

            editTextName.setVisibility(View.GONE);
            btnCambiarNombre.setVisibility(View.GONE);
            btnCancelarCambiarNombre.setVisibility(View.GONE);

            imageViewPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Recording recording = recordingArrayList.get(position);

                    recordingUri = recording.getUri();

                    if (isPlaying) {

                        //antes de que se detenga capturo el progreso del audio
                        recording.lastProgress = mPlayer.getCurrentPosition();

                        stopPlaying();
                        if (position == last_index) {
                            recording.setPlaying(false);
                            stopPlaying();
                            notifyItemChanged(position);
                        } else {
                            markAllPaused();
                            recording.setPlaying(true);
                            notifyItemChanged(position);
                            startPlaying(recording,position);
                            last_index = position;
                        }
                    } else {
                        if (recording.isPlaying()) {
                            recording.setPlaying(false);

                            //antes de que se detenga capturo el progreso del audio
                            recording.lastProgress = mPlayer.getCurrentPosition();

                            stopPlaying();
                            Log.d("isPlaying","True");
                        } else {

                            startPlaying(recording,position);
                            recording.setPlaying(true);
                            seekBar.setMax(mPlayer.getDuration());
                            Log.d("isPlaying","False");
                        }
                        notifyItemChanged(position);
                        last_index = position;
                    }
                }
            });
        }

        public void manageSeekBar(ViewHolder holder){
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mPlayer!=null && fromUser) {
                        mPlayer.seekTo(progress);
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

        private void markAllPaused() {
            for (int i = 0; i < recordingArrayList.size(); i++ ) {
                recordingArrayList.get(i).setPlaying(false);
                recordingArrayList.set(i,recordingArrayList.get(i));
            }
            notifyDataSetChanged();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekUpdate(holder);
            }
        };

        private void seekUpdate(ViewHolder holder) {
            this.holder = holder;
            if (mPlayer != null) {
                int mCurrentPosition = mPlayer.getCurrentPosition();
                holder.seekBar.setMax(mPlayer.getDuration());
                holder.seekBar.setProgress(mCurrentPosition);
                //lastProgress = mCurrentPosition;
            }
            mHandler.postDelayed(runnable, 100);
        }

        private void stopPlaying() {

            try {
                //lastProgress = mPlayer.getCurrentPosition();

                mPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPlayer = null;
            isPlaying = false;
        }

        private void startPlaying(final Recording audio, final int position) {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(recordingUri);
                mPlayer.prepare();
                mPlayer.seekTo(audio.lastProgress);
                mPlayer.start();
            } catch (IOException e) {
                Log.e("LOG_TAG", "prepare() failed");
            }

            seekBar.setMax(mPlayer.getDuration());
            isPlaying = true;

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audio.setPlaying(false);
                    notifyItemChanged(position);
                }
            });
        }



    }


    //***********************************************
    // FUNCIONES RELATIVAS A LOS ARCHIVOS DE AUDIO
    private void renombrar(int posicion,String nombre){
        Log.d("FUNCION renombrar()", "LA POSICION ES: "+ posicion);
        Log.d("FUNCION renombrar()", "EL ARCHIVO SE LLAMA: "+ nombre);

    }

    // FUNCIONES RELATIVAS A LOS ARCHIVOS DE AUDIO
    //***********************************************


}
