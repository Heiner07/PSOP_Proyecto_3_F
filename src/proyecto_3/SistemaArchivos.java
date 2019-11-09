/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
                if(lineaActual.equals("exit")){
                    break;
                }
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
        existeDisco();
        
        System.out.println("Sistema de archivos cargado");
        sistemaCargado = true;
    }
    
    private void existeDisco(){
        File archivo = new File(nombreDisco);
        if(!archivo.exists()){
            comandoFormat();
        }
    }
    
    private void manejadorComandos(String comando){
        switch (comando){
            case "format":
                comandoFormat();
                break;
            case "useradd":
                comandoUserAdd(false);
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
    
    private void comandoFormat(){
        String tamanioDiscoTemp;
        do{
            System.out.print("Ingrese el tamaño del disco en MB: ");
            tamanioDiscoTemp = entradaComandos.nextLine();
        }while(!esNumero(tamanioDiscoTemp));
        
        tamanioDisco = Integer.valueOf(tamanioDiscoTemp);
        comandoUserAdd(true);
        crearSistemaArchivos();
        System.out.println("Formato creado");
    }
    
    private void comandoUserAdd(Boolean root){
        String nombreUsuario, contrasenia, contraseniaTemp;
        if(root){
            nombreUsuario = "root";
        }else{
            System.out.print("Ingrese el nombre completo");
            nombreUsuario = entradaComandos.nextLine();
        }
        do{
            System.out.print("Ingrese la contraseña del "+nombreUsuario+": ");
            contrasenia = entradaComandos.nextLine();
            if(!contrasenia.isEmpty()){
                System.out.print("Confirme la contraseña: ");
                contraseniaTemp = entradaComandos.nextLine();
                if(!contrasenia.equals(contraseniaTemp)){
                    System.out.println("Las contraseñas deben ser iguales");
                }
            }else{
                contraseniaTemp = null;
                System.out.println("Ingrese un valor válido.");
            }
        }while(!contrasenia.equals(contraseniaTemp));
        
        if(root){
            usuarioActual = new Usuario(0, nombreUsuario, contrasenia);
        }else{
            
        }
    }
    
    private String generarContenido(){
        int cantidadBloques = (tamanioDisco * 1024) / 512;
        int tamanioBloques = 512;
        String cBloquesLibres = "1";
        for(int i = 1; i < cantidadBloques; i++){
            cBloquesLibres += ",0";
        }
        String bloquesDisco =
                "[B]\n" +
                "[BS]\n" +
                "-1\n" +
                "[/BS]\n" +
                "[I]\n" +
                "[Id]\n" +
                "0\n" +
                "[/Id]\n" +
                "[1]\n" +
                tamanioDisco+"\n" +
                "[/1]\n" +
                "[2]\n" +
                cantidadBloques+"\n" +
                tamanioBloques+"\n" +
                "[/2]\n" +
                "[3]\n" +
                cBloquesLibres+"\n" +
                "[/3]\n" +
                "[4]\n" +
                "[U]\n" +
                "[Id]\n" +
                "0\n" +
                "[/Id]\n" +
                "[N]\n" +
                "root\n" +
                "[/N]\n" +
                "[Con]\n" +
                usuarioActual.contasenia+"\n" +
                "[/Con]\n" +
                "[/U]\n" +
                "[/4]\n" +
                "[5]\n" +
                "[GU]\n" +
                "[Id]\n" +
                "0\n" +
                "[/Id]\n" +
                "[N]\n" +
                "GrupoRoot\n" +
                "[/N]\n" +
                "[U]\n" +
                "[Id]\n" +
                "0\n" +
                "[/Id]\n" +
                "[/U]\n" +
                "[/GU]\n" +
                "[/5]\n" +
                "[/I]\n" +
                "[/B]\n";
        for(int i = 1; i < cantidadBloques; i++){
            bloquesDisco +=
                    "[B]\n" +
                    "[BS]\n" +
                    "-1\n" +
                    "[/BS]\n" +
                    "[I]\n" +
                    "[Id]\n" +
                    i+"\n" +
                    "[/Id]\n" +
                    "[/I]\n" +
                    "[/B]\n";
        }
        return bloquesDisco;
    }
    
    private void crearSistemaArchivos(){
        File archivo = new File(nombreDisco);
        FileWriter fw;
        BufferedWriter bw;
        try {
            if(!archivo.exists()){
                archivo.createNewFile();
            }fw = new FileWriter(archivo);
            bw = new BufferedWriter(fw);
            String contenido = generarContenido();
            bw.write(contenido);
            // Se calculan los bytes faltantes para el peso del archivo
            long cantidadRelleno = (((long)tamanioDisco * 1024000)-contenido.getBytes().length)/5;
            for(long i = 0; i < cantidadRelleno; i++){
                bw.write("0000\n");
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(SistemaArchivos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private Boolean esNumero(String numeroTemp){
        try{
            int numero = Integer.valueOf(numeroTemp);
            return numero > 0;
        }catch(NumberFormatException e){
            return false;
        }
    }
}
