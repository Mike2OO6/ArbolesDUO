package model.example.org.util;

import org.Nodo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public class GestorJSON {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void guardarArbol(Nodo raiz, String rutaArchivo) throws IOException {
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            gson.toJson(raiz, writer);
        }
    }

    public static Nodo cargarArbol(String rutaArchivo) throws IOException {
        try (FileReader reader = new FileReader(rutaArchivo)) {
            Type tipoNodo = new TypeToken<Nodo>(){}.getType();
            return gson.fromJson(reader, tipoNodo);
        }
    }
}