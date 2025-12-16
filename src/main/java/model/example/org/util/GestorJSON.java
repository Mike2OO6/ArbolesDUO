package model.example.org.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.example.org.Nodo;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GestorJSON {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void guardarArbol(Nodo raiz, String rutaArchivo) throws IOException {
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            gson.toJson(raiz, writer);
        }
    }

    public static Nodo cargarArbol(String rutaArchivo) throws IOException {
        try (FileReader reader = new FileReader(rutaArchivo)) {
            return gson.fromJson(reader, Nodo.class);
        }
    }
}