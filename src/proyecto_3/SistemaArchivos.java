/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
    int cantidadBloques;
    int tamanioBloque;
    Archivo rutaActual; // El archivo (o carpeta) en el que me encuentro
    int cantidadArchivosAbiertos;
    List<Archivo> archivosAbiertos;

    /* Información del programa */
    private final String nombreDisco = "miDiscoDuro.fs";
    String historialComandos;
    String lineaActual;
    Scanner entradaComandos;
    private Boolean sistemaCargado;

    public SistemaArchivos() {
        usuarios = new ArrayList<>();
        gruposUsuarios = new ArrayList<>();
        bloques = new ArrayList<>();
        bloquesLibres = new ArrayList<>();
        archivosAbiertos = new ArrayList<>();
        cantidadArchivosAbiertos = 0;
        historialComandos = "";
        lineaActual = "";
        entradaComandos = new Scanner(System.in);
        sistemaCargado = false;
    }

    /**
     * Se encarga de obtener los comando ingresados por el usuario en la consola
     *
     * @throws java.io.IOException
     */
    public void ejecucion() throws IOException {
        while (true) {
            if (!sistemaCargado) {
                cargarSistemaArchivos();
            } else {
                System.out.print("Usuario: ");
                lineaActual = entradaComandos.nextLine();
                historialComandos += lineaActual + "\n";
                if (lineaActual.equals("exit")) {
                    break;
                }
                manejadorComandos(lineaActual);
            }
        }
    }

    /**
     * Si el sistema de archivos no ha sido cargado, se carga. Si el archivo
     * "miDiscoDuro.fs" no existe, se crea y se solicita la... información del archivo
     * (comando "format").
     */
    private void cargarSistemaArchivos() throws IOException {
        existeDisco();
        System.out.println("Sistema de archivos cargado");
        sistemaCargado = true;
        cargarArchivoEncabezado();
    }

    private void existeDisco() {
        File archivo = new File(nombreDisco);
        if (!archivo.exists()) {
            comandoFormat();
        }
    }

    private void cargarArchivoEncabezado() throws IOException {
        String cadena;
        List<String> instrucciones = new ArrayList<>();    
        RandomAccessFile archivo = null;
        try{
            archivo = new RandomAccessFile(nombreDisco,"r");
        
        }catch(IOException e){
    
        }
        String st = archivo.readLine();
        while(!st.equals("0000")){
            //System.out.println("linea: "+ st);
            instrucciones.add(st);
            st = archivo.readLine();     
        }
        
        
       /* Stream<String> lines = Files.lines(Paths.get(nombreDisco));
        lines.forEach(l -> {
            if(!l.equals("0000") && !l.equals("[/B]")){
                instrucciones.add(l);
                //System.out.println(l);
            }else{return;}
            
        });*/
        cargarDatos(instrucciones);

    }
    
    /**
     * Carga los datos iniciales del archivo, que son necesarios para
     * el funcionamiento correcto del programa.
     */
    private void cargarDatos(List<String> instrucciones){
        int largoInstrucciones = instrucciones.size();
        int siguienteBloque = -1;
        for(int i=0;i<largoInstrucciones;i++){          
            String tipoInstrucciones = instrucciones.get(i);
            if(tipoInstrucciones.equals("[/B]")){
                //tiene siguiente bloque
                if(siguienteBloque>0){
                    i = obtenerSiguienteBloque(instrucciones,siguienteBloque);
                    if(i<0)break;
                    i--;
                }else{
                    break;
                }
                
            }
            switch (tipoInstrucciones) {
                case "[BS]":
                    i++;
                    siguienteBloque = Integer.parseInt(instrucciones.get(i));
                    break;
                case "[1]":
                    i++;
                    tamanioDisco = Integer.parseInt(instrucciones.get(i));
                    break;
                case "[2]":
                    i++;
                    cantidadBloques = Integer.parseInt(instrucciones.get(i));
                    i++;
                    tamanioBloque = Integer.parseInt(instrucciones.get(i));
                    break;
                case "[3]":
                    i++;
                    cargarBloquesLibres(instrucciones.get(i));
                    break;
                case "[4]":
                    i++;
                    i = cargarUsuarios(instrucciones,i);                   
                    break;
                case "[5]":
                    i++;
                    i = cargarGrupos(instrucciones,i);                   
                    break;
                default:
                    break;
            }
        
        }
        imprimirDatos();
    }
    
    
    
    private int obtenerSiguienteBloque(List<String>instrucciones, int id){
        int largoInstrucciones = instrucciones.size();
        String tipoInstrucciones = null;
        for(int i=0;i<largoInstrucciones;i++){
            tipoInstrucciones = instrucciones.get(i);
            if(tipoInstrucciones.equals("[B]")){
                i +=6;
                tipoInstrucciones = instrucciones.get(i);
                if( Integer.parseInt(tipoInstrucciones) == id){
                    return i-6;
                }
            
            }
        
        }
        return -1;
    
    }
    private void imprimirDatos(){
        System.out.println("TamanioDisco: "+tamanioDisco);
        System.out.println("tamanioBloque: "+tamanioBloque);
        System.out.println("cantidadBloques: "+cantidadBloques);
        for(Usuario us:usuarios){
            System.out.println("usuario: "+us.nombre+" contra: "+us.contasenia);
        
        }
        for(GrupoUsuarios gu:gruposUsuarios){
            System.out.println("grupo: "+gu.id+ " nombre: "+gu.nombre);
            for(Usuario us:gu.usuarios){
                System.out.println("id: "+us.id);
        
            }
        
        }
    
    }
    
    private int cargarGrupos(List<String> instrucciones, int indice){
        String tipoInstruccion = instrucciones.get(indice);
        int id = 0;
        String nombre = null;
        while(!tipoInstruccion.equals("[/5]")){
            tipoInstruccion = instrucciones.get(indice);
            if(tipoInstruccion.equals("[GU]")){
                indice+=2;
                id = Integer.parseInt(instrucciones.get(indice));
                indice+=3;
                nombre = instrucciones.get(indice);
                indice++;             
                List<Usuario> usuariosGrupos = new ArrayList<>();
                while(!instrucciones.get(indice).equals("[/GU]")){
                    tipoInstruccion = instrucciones.get(indice);
                    if(tipoInstruccion.equals("[Id]")){
                        indice++;
                        usuariosGrupos.add(new Usuario(Integer.parseInt(instrucciones.get(indice)),null,null));                   
                    }indice++;
                
                }
                indice++;
                gruposUsuarios.add(new GrupoUsuarios(id,nombre,usuariosGrupos));
            }       
        }
        return indice;
    
    }
    private int cargarUsuarios(List<String> instrucciones, int indice){
        String tipoInstruccion = instrucciones.get(indice);
        int id = 0;
        String nombre = null,contrasenia = null;
        while(!tipoInstruccion.equals("[/4]")){
            tipoInstruccion = instrucciones.get(indice);
            if(tipoInstruccion.equals("[U]")){
                indice+=2;
                id = Integer.parseInt(instrucciones.get(indice));
                indice+=3;
                contrasenia = instrucciones.get(indice);
                indice+=3;
                nombre = instrucciones.get(indice);
                indice+=3;
                usuarios.add(new Usuario(id,nombre,contrasenia));
            }
        }return indice;
    }
    
    
    private void cargarBloquesLibres(String instruccionBloques){
        String[] bloquesSeparados = instruccionBloques.split(",");
        for(int i=0;i<bloquesSeparados.length;i++){
            bloquesLibres.add(Integer.parseInt(bloquesSeparados[i])); 
        }

    
    }
    private void manejadorComandos(String comando) {
        switch (comando) {
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
            case "note":
                //llamado al método             
                break;
            default:
                break;
        }
    }

    private void comandoFormat() {
        String tamanioDiscoTemp;
        do {
            System.out.print("Ingrese el tamaño del disco en MB: ");
            tamanioDiscoTemp = entradaComandos.nextLine();
        } while (!esNumero(tamanioDiscoTemp));

        tamanioDisco = Integer.valueOf(tamanioDiscoTemp);
        comandoUserAdd(true);
        crearSistemaArchivos();
        System.out.println("Formato creado");
    }

    private void comandoUserAdd(Boolean root) {
        String nombreUsuario, contrasenia, contraseniaTemp;
        if (root) {
            nombreUsuario = "root";
        } else {
            System.out.print("Ingrese el nombre completo: ");
            nombreUsuario = entradaComandos.nextLine();
        }
        do {
            System.out.print("Ingrese la contraseña de " + nombreUsuario + ": ");
            contrasenia = entradaComandos.nextLine();
            if (!contrasenia.isEmpty()) {
                System.out.print("Confirme la contraseña: ");
                contraseniaTemp = entradaComandos.nextLine();
                if (!contrasenia.equals(contraseniaTemp)) {
                    System.out.println("Las contraseñas deben ser iguales");
                }
            } else {
                contraseniaTemp = null;
                System.out.println("Ingrese un valor válido.");
            }
        } while (!contrasenia.equals(contraseniaTemp));

        if (root) {
            usuarioActual = new Usuario(0, nombreUsuario, contrasenia);
        } else {

        }
    }

    private String generarContenido() {
        int cantidadBloques = (tamanioDisco * 1024) / 512;
        int tamanioBloques = 512;
        String cBloquesLibres = "1";
        for (int i = 1; i < cantidadBloques; i++) {
            cBloquesLibres += ",0";
        }
        String bloquesDisco
                = "[B]\n"
                + "[BS]\n"
                + "-1\n"
                + "[/BS]\n"
                + "[I]\n"
                + "[Id]\n"
                + "0\n"
                + "[/Id]\n"
                + "[1]\n"
                + tamanioDisco + "\n"
                + "[/1]\n"
                + "[2]\n"
                + cantidadBloques + "\n"
                + tamanioBloques + "\n"
                + "[/2]\n"
                + "[3]\n"
                + cBloquesLibres + "\n"
                + "[/3]\n"
                + "[4]\n"
                + "[U]\n"
                + "[Id]\n"
                + "0\n"
                + "[/Id]\n"
                + "[N]\n"
                + "root\n"
                + "[/N]\n"
                + "[Con]\n"
                + usuarioActual.contasenia + "\n"
                + "[/Con]\n"
                + "[/U]\n"
                + "[/4]\n"
                + "[5]\n"
                + "[GU]\n"
                + "[Id]\n"
                + "0\n"
                + "[/Id]\n"
                + "[N]\n"
                + "GrupoRoot\n"
                + "[/N]\n"
                + "[U]\n"
                + "[Id]\n"
                + "0\n"
                + "[/Id]\n"
                + "[/U]\n"
                + "[/GU]\n"
                + "[/5]\n"
                + "[/I]\n"
                + "[/B]\n";
        for (int i = 1; i < cantidadBloques; i++) {
            bloquesDisco
                    += "[B]\n"
                    + "[BS]\n"
                    + "-1\n"
                    + "[/BS]\n"
                    + "[I]\n"
                    + "[Id]\n"
                    + i + "\n"
                    + "[/Id]\n"
                    + "[/I]\n"
                    + "[/B]\n";
        }
        return bloquesDisco;
    }

    private void crearSistemaArchivos() {
        File archivo = new File(nombreDisco);
        FileWriter fw;
        BufferedWriter bw;
        try {
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
            fw = new FileWriter(archivo);
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

    private Boolean esNumero(String numeroTemp) {
        try {
            int numero = Integer.valueOf(numeroTemp);
            return numero > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
