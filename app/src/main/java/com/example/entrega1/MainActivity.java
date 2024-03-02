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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tareas;
    private ArrayAdapter<String> adapter;
    private static final String ID_CANAL = "Canal 1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tareas = new ArrayList<>();
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

    }

    private class TareaAdapter extends ArrayAdapter<String> {
        private int layoutResourceId;

        public TareaAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.layoutResourceId = resource;
        }

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

            textViewTarea.setText(tarea);
            actualizarImagenPrioridad(imageViewPrioridad, prioridad);

            return convertView;
        }
    }



    private void mostrarDialogo() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.anadir_tarea_dialogo, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        // Cancelar
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void añadirTarea(String nuevaTarea, String prioridad) {
        String tareaConPrioridad = nuevaTarea + " - Prioridad: " + prioridad;
        tareas.add(tareaConPrioridad);
        adapter.notifyDataSetChanged();
    }

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

    public void finalizarTarea(View view) {
        // Obtiene la posición de la vista en la lista
        int position = ((ListView) view.getParent().getParent()).getPositionForView((RelativeLayout) view.getParent());

        // Remueve la tarea de la lista
        if (position != ListView.INVALID_POSITION) {
            // Obtener la tarea antes de eliminarla
            String tareaFinalizada = tareas.get(position);

            // Eliminar la tarea de la lista
            tareas.remove(position);
            adapter.notifyDataSetChanged();

            // Mostrar la notificación
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // Notifications are enabled, proceed with posting notifications
                mostrarNotificacionFinalizacion(tareaFinalizada);
            } else {
                // Notifications are not enabled, request permission or handle accordingly
                // You can prompt the user to enable notifications or navigate to settings
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
        }
    }

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

    @SuppressLint("MissingPermission")
    private void mostrarNotificacionFinalizacion(String tareaFinalizada) {
        // Create an explicit intent for an activity in your app
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

}