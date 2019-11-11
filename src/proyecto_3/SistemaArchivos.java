/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
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
        System.out.println("Cargando sistema de archivos...");
        cargarArchivoEncabezado();
        sistemaCargado = true;
        System.out.println("¡Sistema de archivos cargado!");
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
        String st;// = archivo.readLine();
        boolean siguienteBloque = false;
        int numSig = -1;
        while((st = archivo.readLine()) != null){    
            if(siguienteBloque){                
                numSig = Integer.parseInt(st);
                siguienteBloque = false;
            }else
            if(st.equals(EstructuraSistemaArchivos.INICIO_BLOQUE_SIGUIENTE)){
                siguienteBloque= true;
                
            }else if(st.equals(EstructuraSistemaArchivos.FINAL_BLOQUE)){
                if(numSig == -1){
                    instrucciones.add(st);                               
                    break;               
                }else{                                     
                    archivo.seek(numSig * (tamanioBloque - numSig));   
                    numSig = -1;
                }
            }else if(st.equals(EstructuraSistemaArchivos.INICIO_NUMERO_BLOQUES)){
                st = archivo.readLine();
                cantidadBloques = Integer.parseInt(st);           
                instrucciones.add(st);
                st = archivo.readLine();
                tamanioBloque = Integer.parseInt(st);           
            }           
            instrucciones.add(st);
        }
        
        cargarDatos(instrucciones);

    }
    
    /**
     * Carga los datos iniciales del archivo, que son necesarios para
     * el funcionamiento correcto del programa.
     */
    private void cargarDatos(List<String> instrucciones){
        int largoInstrucciones = instrucciones.size();
        for(int i=0;i<largoInstrucciones;i++){          
            String tipoInstrucciones = instrucciones.get(i);      
            System.out.println(tipoInstrucciones);
            switch (tipoInstrucciones) {
                
                case EstructuraSistemaArchivos.INICIO_TAMANIO:
                    i++;
                    tamanioDisco = Integer.parseInt(instrucciones.get(i));
                    break;              
                case EstructuraSistemaArchivos.INICIO_BLOQUES_LIBRES:
                    i++;
                    cargarBloquesLibres(instrucciones.get(i));
                    break;
                case EstructuraSistemaArchivos.INICIO_BLOQUE_USUARIOS:
                    i++;
                    i = cargarUsuarios(instrucciones,i);                   
                    break;
                case EstructuraSistemaArchivos.INICIO_BLOQUE_G_USUARIOS:
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
            if(tipoInstrucciones.equals(EstructuraSistemaArchivos.INICIO_BLOQUE)){
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
            System.out.println("usuario: "+us.nombre+" contra: "+us.contrasenia);
        
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
        while(!tipoInstruccion.equals(EstructuraSistemaArchivos.FINAL_BLOQUE_G_USUARIOS)){
            tipoInstruccion = instrucciones.get(indice);
            if(tipoInstruccion.equals(EstructuraSistemaArchivos.INICIO_G_USUARIO)){
                indice+=2;
                id = Integer.parseInt(instrucciones.get(indice));
                indice+=3;
                nombre = instrucciones.get(indice);
                indice++;             
                List<Usuario> usuariosGrupos = new ArrayList<>();
                while(!instrucciones.get(indice).equals(EstructuraSistemaArchivos.FINAL_G_USUARIO)){
                    tipoInstruccion = instrucciones.get(indice);
                    if(tipoInstruccion.equals(EstructuraSistemaArchivos.INICIO_ID)){
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
        while(!tipoInstruccion.equals(EstructuraSistemaArchivos.FINAL_BLOQUE_USUARIOS)){
            tipoInstruccion = instrucciones.get(indice);
            if(tipoInstruccion.equals(EstructuraSistemaArchivos.INICIO_USUARIO)){
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
        System.out.println("¡Formato creado!");
    }

    /**
     * Se encarga de ejecutar el comando "useradd". Si la variable es root,...
     * ...es el usuario root, por lo que solo pide la contraseña, sino, esta...
     * ...agregando un usuario normal.
     * @param root 
     */
    private void comandoUserAdd(Boolean root){
        String nombreUsuario, contrasenia, contraseniaTemp;
        if(root){
            nombreUsuario = "root";
        }else{
            System.out.print("Ingrese el nombre completo: ");
            nombreUsuario = entradaComandos.nextLine();
        }
        do{
            System.out.print("Ingrese la contraseña de "+nombreUsuario+": ");
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
            Usuario usuarioNuevo = new Usuario(usuarios.size(), nombreUsuario, contrasenia);
            usuarios.add(usuarioNuevo);
            if(escribirUsuario(usuarioNuevo)){
                System.out.println("¡Usuario agregado!");
            }else{
                System.out.println("Error al agregar el usuario");
            }
        }
    }
    
    /**
     * Escribe un usuario en el archivo (disco).
     * Genera la información del usuario por agregar y determina el bloque en...
     * ...el que lo va a escribir(por el momento solo el cero). Obtiene el...
     * ...bloque, lo edita, se limpia toda la sección del bloque en el archivo..
     * ...y carga el nuevo en la misma sección ya con el usuario.
     * @param usuario 
     * @return Boolean
     */
    private Boolean escribirUsuario(Usuario usuario){
        String contenido = EstructuraSistemaArchivos.generarContenidoUsuario(usuario);
        RandomAccessFile archivo;
        Boolean concatenar = false;
        String bloque = "", linea, bloqueNuevo;
        int limiteBloque;
        try {
            archivo = new RandomAccessFile(nombreDisco, "rw");
            // Ciclo para buscar el bloque correspondiente
            while((linea = archivo.readLine()) != null){
                // Si la linea es "[B]", empiezo a leer y generar un bloque
                if(linea.startsWith(EstructuraSistemaArchivos.INICIO_BLOQUE)){
                    concatenar = true;
                }// Si es "[/B], termino de leer un bloque y lo analizo"
                else if(linea.startsWith(EstructuraSistemaArchivos.FINAL_BLOQUE)){
                    bloque += linea+"\n";
                    concatenar = false;
                    if(hayEspacioEnBloque(bloque, contenido)){
                        // Limpio el bloque que voy a actualizar
                        archivo.seek(0); // Cero es el inicio del bloque 0
                        limiteBloque = tamanioBloque - 1; // Menos uno para agregar un salto de linea
                        for(int i = 0; i < limiteBloque; i++){
                            archivo.writeBytes(EstructuraSistemaArchivos.CARACTER_RELLENO);
                        }
                        archivo.writeBytes("\n");
                        // Genero el bloque nuevo con el usuario donde corresponde
                        bloqueNuevo = agregarUsuarioCadena(bloque, contenido);
                        archivo.seek(0); // Cero es el inicio del bloque 0
                        archivo.writeBytes(bloqueNuevo);
                        break; // Importante para que no recorra todo el archivo.
                    }
                    bloque = "";
                }
                if(concatenar){
                    bloque += linea+"\n";
                }
            }
            archivo.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    /**
     * Indica si hay espacio para el contenido en el bloque.
     * De acuerdo con el tamaño de bloque.
     * @param bloque
     * @param contenido
     * @return Boolean
     */
    private Boolean hayEspacioEnBloque(String bloque, String contenido){
        String bloqueModificado = bloque + contenido + "\n";
        return bloqueModificado.getBytes().length <= tamanioBloque;
    }
    
    /**
     * Agrega la información de un usuario en el bloque indicado
     * @param bloque
     * @param usuario
     * @return String
     */
    private String agregarUsuarioCadena(String bloque, String usuario){
        String[] lineasBloque = bloque.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.FINAL_BLOQUE_USUARIOS)){
                cadenaFinal += usuario + "\n";
            }
            cadenaFinal += linea + "\n";
        }return cadenaFinal;
    }
    
    /**
     * Genera el contenido base del sistema de archivos.
     * @return String
     */
    private String generarContenido(){
        cantidadBloques = (tamanioDisco * 1024) / 512;
        tamanioBloque = 512 * 1024;
        String cBloquesLibres = "1";
        for(int i = 1; i < cantidadBloques; i++){
            cBloquesLibres += ",0";
        }
        String bloquesDisco = EstructuraSistemaArchivos.obtenerContenidoInicial(
                tamanioDisco, cantidadBloques, tamanioBloque, cBloquesLibres,
                usuarioActual.contrasenia);
        return bloquesDisco;
    }
    
    /**
     * Se encarga de crear el archivo (Disco) y darle el formato correspondiente
     */
    private void crearSistemaArchivos(){
        File archivoTemp = new File(nombreDisco); // Usado para borrar el anterior
        RandomAccessFile archivo;
        String contenido = generarContenido();
        String linea;
        String lineaPorAgregar = "";
        List<String> lineas = Arrays.asList(contenido.split("\n"));
        int indiceContenido = 0;
        int tamanioRestanteBloque;
        try {
            archivoTemp.delete(); // Se borra el archivo anterior (si existe)
            archivo = new RandomAccessFile(nombreDisco, "rw"); // Se crea el nuevo
            archivo.setLength(tamanioDisco * 1000000); // Se asigna el tamaño
            archivo.seek(0); // Se asigna el puntero al inicio del archivo
            // Se recorre cada bloque creado, para asignarle en el archivo el tamaño
            while(indiceContenido < lineas.size()){
                linea = lineas.get(indiceContenido);
                switch (linea) {
                    case EstructuraSistemaArchivos.INICIO_BLOQUE: // Se  empieza a formar cada bloque
                        lineaPorAgregar = ((indiceContenido > 0)? "\n": "") +linea;
                        break;
                    case EstructuraSistemaArchivos.FINAL_BLOQUE: // Cuando se tiene un bloque leido
                        lineaPorAgregar += linea + "\n";
                        // Se escribe el bloque
                        archivo.writeBytes(lineaPorAgregar);
                        // Se calcula el tamanio restante en el bloque.
                        tamanioRestanteBloque = tamanioBloque - lineaPorAgregar.getBytes().length;
                        // Se mueve el puntero del archivo hasta el inicio donde debe ir el siguiente bloque
                        archivo.seek(archivo.getFilePointer() + tamanioRestanteBloque - 1);
                        break;
                    default: // Se va formando cada bloque
                        lineaPorAgregar += linea;
                        break;
                }lineaPorAgregar += "\n";
                indiceContenido++;
            }
            archivo.close();
        } catch (IOException ex) {
            Logger.getLogger(SistemaArchivos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Verifica si un string es número positivo
     * @param numeroTemp
     * @return Boolean
     */
    private Boolean esNumero(String numeroTemp){
        try{
            int numero = Integer.valueOf(numeroTemp);
            return numero > 0;
        }catch(NumberFormatException e){
            return false;
        }
    }
}
