package com.example.entrega1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tareas;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Error", "Prueba");
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
        Log.d("Debug", "Añadiendo tarea: " + nuevaTarea + " - Prioridad: " + prioridad);
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

        Log.e("Error", "No se encontró 'Prioridad:' en la cadena de tarea");
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


}