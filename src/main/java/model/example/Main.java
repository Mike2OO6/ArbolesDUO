package model.example;

import model.example.org.TipoNodo;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final GestorArbol gestor = new GestorArbol();

    public static void main(String[] args) {
        System.out.println("Sistema de Gestión de Archivos - Árboles DUO");
        mostrarMenu();
    }

    private static void mostrarMenu() {
        while (true) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("Ruta actual: " + gestor.getRutaActual());
            System.out.println("1. Crear carpeta");
            System.out.println("2. Crear archivo");
            System.out.println("3. Listar contenido");
            System.out.println("4. Buscar por prefijo");
            System.out.println("5. Exportar recorrido preorden");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = leerEntero(1, 6);

            try {
                switch (opcion) {
                    case 1:
                        crearNodo(TipoNodo.CARPETA);
                        break;
                    case 2:
                        crearNodo(TipoNodo.ARCHIVO);
                        break;
                    case 3:
                        listarContenido();
                        break;
                    case 4:
                        buscarPorPrefijo();
                        break;
                    case 5:
                        gestor.exportarPreorden();
                        break;
                    case 6:
                        System.out.println("¡Hasta luego!");
                        return;
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void crearNodo(TipoNodo tipo) {
        System.out.print("Ingrese el nombre del " + (tipo == TipoNodo.CARPETA ? "directorio" : "archivo") + ": ");
        String nombre = scanner.nextLine();
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }
        gestor.crearNodo(nombre, tipo);
        System.out.println((tipo == TipoNodo.CARPETA ? "Directorio" : "Archivo") + " creado exitosamente.");
    }

    private static void listarContenido() {
        List<String> contenido = gestor.listarHijos();
        if (contenido.isEmpty()) {
            System.out.println("El directorio actual está vacío.");
        } else {
            System.out.println("Contenido del directorio actual:");
            for (String item : contenido) {
                System.out.println("- " + item);
            }
        }
    }

    private static void buscarPorPrefijo() {
        System.out.print("Ingrese el prefijo a buscar: ");
        String prefijo = scanner.nextLine();
        List<String> resultados = gestor.buscarPorPrefijo(prefijo);

        if (resultados.isEmpty()) {
            System.out.println("No se encontraron coincidencias.");
        } else {
            System.out.println("Coincidencias encontradas:");
            for (String r : resultados) {
                System.out.println("- " + r);
            }
        }
    }

    private static int leerEntero(int min, int max) {
        while (true) {
            try {
                int valor = Integer.parseInt(scanner.nextLine());
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.print("Por favor ingrese un número entre " + min + " y " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Ingrese un número: ");
            }
        }
    }
}