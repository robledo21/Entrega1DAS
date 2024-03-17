package com.example.entrega1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Clase que implementa la base de datos local
public class MiDataBase extends SQLiteOpenHelper {


    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "tareas.db";
    private static final int DATABASE_VERSION = 1;


    // Nombre de la tabla y columnas
    public static final String TABLE_TASKS = "tareas";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TASK = "tarea";
    public static final String COLUMN_PRIORITY = "prioridad";


    // Sentencia SQL para crear la tabla
    private static final String DATABASE_CREATE =
            "create table " + TABLE_TASKS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_TASK + " text not null, " +
                    COLUMN_PRIORITY + " text not null);";

    // Constructora
    public MiDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // Método que se ejecuta al crear la base de datos
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // Método para obtener todas las tareas de la base de datos
    public Cursor obtenerTodasLasTareas() {
        // Obtener todas las tareas de la base de datos
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_TASKS, null, null, null, null, null, null);
    }


    // Método para insertar una nueva tarea en la base de datos
    public long insertarTarea(String tarea, String prioridad) {
        // Insertar tarea en la base de datos
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK, tarea);
        values.put(COLUMN_PRIORITY, prioridad);
        long insertId = db.insert(TABLE_TASKS, null, values);
        db.close();
        return insertId;
    }
}

