package com.example.entrega1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Clase principal de la aplicación
public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tareas;
    private ArrayAdapter<String> adapter;
    private static final String ID_CANAL = "Canal 1"; // Identificador del canal de notificaciones
    private MiDataBase miDB; // Instancia de la base de datos
    private static final String KEY_TAREAS = "key_tareas"; // Clave para guardar y restaurar el estado de la lista de tareas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restaurar datos si hay un estado guardado
        if (savedInstanceState != null) {
            tareas = savedInstanceState.getStringArrayList(KEY_TAREAS);
        } else {
            // Si no hay un estado guardado, carga las tareas desde la base de datos
            tareas = cargarTareasDesdeBaseDeDatos();
        }

        miDB = new MiDataBase(this);

        adapter = new TareaAdapter(this, R.layout.elemento_lista, tareas);

        ListView listView = findViewById(R.id.tareasListView);
        listView.setAdapter(adapter);

        Button botonAnadirTarea = findViewById(R.id.botonAnadirTarea);
        botonAnadirTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogo();
            }
        });

        crearCanalNotificaciones();

        cargarTareasDesdeBD();

    }

    // Adaptador personalizado para la lista de tareas
    private class TareaAdapter extends ArrayAdapter<String> {
        private int layoutResourceId;

        public TareaAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.layoutResourceId = resource;
        }


        // Método para personalizar la vista de cada elemento en la lista de tareas
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

            // Obtén el texto de la tarea y la prioridad
            String tarea = getItem(position);
            String prioridad = obtenerPrioridadDeLaTarea(tarea);

            // Configurar el texto y la imagen según la tarea y la prioridad
            TextView textViewTarea = convertView.findViewById(R.id.textViewTarea);
            ImageView imageViewPrioridad = convertView.findViewById(R.id.imageViewPrioridad);
            Button buttonFinalizar = convertView.findViewById(R.id.botonFinalizar);
            Button buttonEditar = convertView.findViewById(R.id.botonEditar);

            textViewTarea.setText(tarea);
            actualizarImagenPrioridad(imageViewPrioridad, prioridad);

            // Asignar un listener al botón de finalizar
            buttonFinalizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalizarTarea(view);
                }
            });

            // Asignar un listener al botón de editar
            buttonEditar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editarTarea(view);
                }
            });

            return convertView;
        }
    }


    // Método para mostrar el diálogo de agregar tarea
    private void mostrarDialogo() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.anadir_tarea_dialogo, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setView(dialogView)
                .setTitle("Añadir Tarea")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editTextTarea = dialogView.findViewById(R.id.editTextTarea);
                        RadioGroup radioGroupPrioridad = dialogView.findViewById(R.id.radioGroupPrioridad);

                        String nuevaTarea = editTextTarea.getText().toString();
                        int prioridadSeleccionada = radioGroupPrioridad.getCheckedRadioButtonId();

                        if (!nuevaTarea.isEmpty()) {
                            añadirTarea(nuevaTarea, obtenerPrioridadString(prioridadSeleccionada));
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Metodo para añadir una tarea
    private void añadirTarea(String nuevaTarea, String prioridad) {
        // Insertar la tarea en la base de datos
        SQLiteDatabase db = miDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MiDataBase.COLUMN_TASK, nuevaTarea);
        values.put(MiDataBase.COLUMN_PRIORITY, prioridad);

        long insertId = db.insert(MiDataBase.TABLE_TASKS, null, values);
        db.close();

        if (insertId != -1) {
            // Actualizar la lista de tareas desde la base de datos
            cargarTareasDesdeBD();
            adapter.notifyDataSetChanged();
        } else {
            // Manejar error de inserción
            Toast.makeText(this, "Error al agregar la tarea a la base de datos", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para cargar tareas desde la base de datos
    private void cargarTareasDesdeBD() {
        // Load tasks from the database
        tareas.clear();
        SQLiteDatabase db = miDB.getReadableDatabase();
        Cursor cursor = db.query(MiDataBase.TABLE_TASKS,
                new String[]{MiDataBase.COLUMN_TASK, MiDataBase.COLUMN_PRIORITY},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String tarea = cursor.getString(cursor.getColumnIndex(MiDataBase.COLUMN_TASK));
            @SuppressLint("Range") String prioridad = cursor.getString(cursor.getColumnIndex(MiDataBase.COLUMN_PRIORITY));
            tareas.add(tarea + " - Prioridad: " + prioridad);
        }

        cursor.close();
        db.close();
    }

    // Metodo para obtener la prioridad en String
    private String obtenerPrioridadString(int radioButtonId) {
        if (radioButtonId == R.id.radioButtonAlta) {
            return "Alta";
        } else if (radioButtonId == R.id.radioButtonMedia) {
            return "Media";
        } else if (radioButtonId == R.id.radioButtonBaja) {
            return "Baja";
        } else {
            return "No definida";
        }
    }

    // Metodo que devuelve la prioridad de una tarea
    private String obtenerPrioridadDeLaTarea(String tarea) {
        int indiceInicio = tarea.indexOf("Prioridad: ");

        if (indiceInicio != -1) {
            indiceInicio += "Prioridad: ".length();
            int indiceFin = tarea.indexOf(" ", indiceInicio);

            if (indiceFin != -1) {
                return tarea.substring(indiceInicio, indiceFin);
            } else {
                return tarea.substring(indiceInicio);
            }
        }

        return "No definida";
    }



    // Metodo que actualiza la imagen de prioridad de una tarea
    private void actualizarImagenPrioridad(View view, String prioridad) {
        ImageView imageViewPrioridad = view.findViewById(R.id.imageViewPrioridad);
        switch (prioridad) {
            case "Alta":
                imageViewPrioridad.setImageResource(R.drawable.prioritize);
                break;
            case "Media":
                imageViewPrioridad.setImageResource(R.drawable.priority);
                break;
            case "Baja":
                imageViewPrioridad.setImageResource(R.drawable.low);
                break;
            default:
                imageViewPrioridad.setImageResource(R.drawable.priority);
                break;
        }
    }

    // Método para finalizar una tarea
    public void finalizarTarea(View view) {
        // Obtiene la posición de la vista en la lista
        int position = ((ListView) view.getParent().getParent()).getPositionForView((RelativeLayout) view.getParent());

        // Borra la tarea de la lista y de la base de datos
        if (position != ListView.INVALID_POSITION) {
            // Obtener la tarea antes de eliminarla
            String tareaFinalizada = tareas.get(position);

            // Eliminar la tarea de la base de datos
            eliminarTareaDeBD(tareaFinalizada.split(Pattern.quote(" - Prioridad: "))[0]);

            // Eliminar la tarea de la lista
            tareas.remove(position);
            adapter.notifyDataSetChanged();

            // Mostrar la notificación
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                mostrarNotificacionFinalizacion(tareaFinalizada);
            } else {
                // Si no tiene permisos de notificaciones, se piden
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
        }
    }

    // Método para eliminar una tarea de la base de datos
    private void eliminarTareaDeBD(String tarea) {
        SQLiteDatabase db = miDB.getWritableDatabase();
        db.delete(MiDataBase.TABLE_TASKS,
                MiDataBase.COLUMN_TASK + " = ?",
                new String[]{tarea});
        db.close();
    }


    // Metodo que muestra un Toast según se hayan permitido o denegado las notificaciones
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 11)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Notificaciones permitidas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notificaciones denegadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Metodo que muestra las notificaciones de finalización (cuando se termina una tarea)
    @SuppressLint("MissingPermission")
    private void mostrarNotificacionFinalizacion(String tareaFinalizada) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ID_CANAL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Felicidades!")
                .setContentText("Has completado la tarea: " + tareaFinalizada)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Notificar
        notificationManager.notify(1, builder.build());
    }

    // Metodo que crea el canal de notificaciones
    private void crearCanalNotificaciones() {
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MiCanal";
            String description = "Notificaciones App";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ID_CANAL, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar las tareas en el estado para que se pueda restaurar después de girar
        outState.putStringArrayList(KEY_TAREAS, tareas);
    }

    // Metodo para cargar tareas desde la base de datos
    private ArrayList<String> cargarTareasDesdeBaseDeDatos() {
        ArrayList<String> tareas = new ArrayList<>();

        MiDataBase miDB = new MiDataBase(this);
        SQLiteDatabase db = miDB.getReadableDatabase();

        // Obtener todas las tareas de la base de datos
        Cursor cursor = miDB.obtenerTodasLasTareas();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String tarea = cursor.getString(cursor.getColumnIndexOrThrow(MiDataBase.COLUMN_TASK));
                tareas.add(tarea);
            }
            cursor.close();
        }

        return tareas;
    }

    // Metodo para editar una tarea
    private void editarTarea(View view) {
        // Obtener la tarea seleccionada
        int position = ((ListView) view.getParent().getParent()).getPositionForView((RelativeLayout) view.getParent());
        if (position != ListView.INVALID_POSITION) {
            String tareaCompleta = tareas.get(position);
            // Extraer el nombre de la tarea y la prioridad
            String[] partes = tareaCompleta.split(" - Prioridad: ");
            String tarea = partes[0];
            String prioridad = partes[1];

            // Crear diálogo de edición
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.editar_tarea_dialogo, null);
            builder.setView(dialogView);

            EditText editTextTarea = dialogView.findViewById(R.id.editTextTarea);
            editTextTarea.setText(tarea);

            RadioGroup radioGroupPrioridad = dialogView.findViewById(R.id.radioGroupPrioridad);
            // Establecer la prioridad seleccionada por el usuario
            switch (prioridad) {
                case "Alta":
                    radioGroupPrioridad.check(R.id.radioButtonAlta);
                    break;
                case "Media":
                    radioGroupPrioridad.check(R.id.radioButtonMedia);
                    break;
                case "Baja":
                    radioGroupPrioridad.check(R.id.radioButtonBaja);
                    break;
            }

            builder.setTitle("Editar Tarea")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String nuevaTarea = editTextTarea.getText().toString();
                            int radioButtonId = radioGroupPrioridad.getCheckedRadioButtonId();
                            String nuevaPrioridad = obtenerPrioridadString(radioButtonId);
                            if (!nuevaTarea.isEmpty()) {
                                actualizarTareaEnBD(tarea, nuevaTarea, nuevaPrioridad);
                                // Actualizar la lista de tareas en el adaptador
                                tareas.set(position, nuevaTarea + " - Prioridad: " + nuevaPrioridad);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Cancelar la edición
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }



    // Metodo para actualizar/editar una tarea en la BD
    private void actualizarTareaEnBD(String tareaAnterior, String nuevaTarea, String nuevaPrioridad) {
        SQLiteDatabase db = miDB.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MiDataBase.COLUMN_TASK, nuevaTarea);
        values.put(MiDataBase.COLUMN_PRIORITY, nuevaPrioridad); // Agregar la nueva prioridad

        int rowsAffected = db.update(MiDataBase.TABLE_TASKS, values,
                MiDataBase.COLUMN_TASK + " = ?",
                new String[]{tareaAnterior});

        db.close();

        // Toasts que informan del resultado de la edición
        if (rowsAffected > 0) {
            Toast.makeText(this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al actualizar la tarea", Toast.LENGTH_SHORT).show();
        }
    }




}