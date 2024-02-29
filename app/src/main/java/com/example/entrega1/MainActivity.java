package com.example.entrega1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tareas;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tareas = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tareas);

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


}