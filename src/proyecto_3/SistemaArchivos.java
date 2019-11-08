/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heiner
 */
public class SistemaArchivos {
    
    /* Información del sistema de archivos cargado */
    Usuario usuarioActual;
    List<Usuario> usuarios;
    List<GrupoUsuarios> gruposUsuarios;
    List<Bloque> bloques;
    List<Integer> bloquesLibres;
    int tamanioDisco;
    int espacioDisponible;
    Archivo rutaActual; // El archivo (o carpeta) en el que me encuentro
    int cantidadArchivosAbiertos;
    List<Archivo> archivosAbiertos;
    
    /* Información del programa */
    private final String nombreDisco = "miDiscoDuro.fs";
    String historialComandos;
    String lineaActual;
    Scanner entradaComandos;
    private Boolean sistemaCargado;
    
    public SistemaArchivos(){
        usuarios = new ArrayList<>();
        gruposUsuarios = new ArrayList<>();
        bloques = new ArrayList<>();
        archivosAbiertos = new ArrayList<>();
        cantidadArchivosAbiertos = 0;
        historialComandos = "";
        lineaActual = "";
        entradaComandos = new Scanner(System.in);
        sistemaCargado = false;
    }
    
    /**
     * Se encarga de obtener los comando ingresados por el usuario en la consola
     */
    public void ejecucion(){
        while(true){
            if(!sistemaCargado){
                cargarSistemaArchivos();
            }else{
                System.out.print("Usuario: ");
                lineaActual = entradaComandos.nextLine();
                historialComandos += lineaActual + "\n";
                manejadorComandos(lineaActual);
            }
        }
    }
    
    /**
     * Si el sistema de archivos no ha sido cargado, se carga.
     * Si el archivo "miDiscoDuro.fs" no existe, se crea y se solicita la...
     * información del archivo (comando "format").
     */
    private void cargarSistemaArchivos(){
        File archivo = new File(nombreDisco);
        try {
            if(!archivo.exists()){
                archivo.createNewFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(SistemaArchivos.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Sistema de archivos cargado");
        sistemaCargado = true;
    }
    
    private void manejadorComandos(String comando){
        switch (comando){
            case "format":
                // llamado al método
                break;
            case "useradd":
                // llamado al método
                break;
            case "groupadd":
                // llamado al método
                break;
            case "passwd":
                // llamado al método
                break;
            case "su":
                // llamado al método
                break;
            case "whoami":
                // llamado al método
                break;
            case "pwd":
                // llamado al método
                break;
            case "mkdir":
                // llamado al método
                break;
            case "rm":
                // llamado al método
                break;
            case "mv":
                // llamado al método
                break;
            case "ls":
                // llamado al método
                break;
            case "cd":
                // llamado al método
                break;
            case "whereis":
                // llamado al método
                break;
            case "ln":
                // llamado al método
                break;
            case "touch":
                // llamado al método
                break;
            case "cat":
                // llamado al método
                break;
            case "chown":
                // llamado al método
                break;
            case "chgrp":
                // llamado al método
                break;
            case "chmod":
                // llamado al método
                break;
            case "openFile":
                // llamado al método
                break;
            case "closeFile":
                // llamado al método
                break;
            case "viewFilesOpen":
                // llamado al método
                break;
            case "viewFCB":
                // llamado al método
                break;
            case "infoFS":
                // llamado al método
                break;
            default:
                break;
        }
    }
}
