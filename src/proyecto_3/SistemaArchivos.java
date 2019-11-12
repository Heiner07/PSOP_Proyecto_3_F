/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.io.File;
import java.io.FileNotFoundException;
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
                System.out.print(usuarioActual.nombre+"@miFS: ");
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
    
   
    
    private void imprimirDatos(){
        System.out.println("TamanioDisco: "+tamanioDisco);
        System.out.println("tamanioBloque: "+tamanioBloque);
        System.out.println("cantidadBloques: "+cantidadBloques);
        for(Usuario us:usuarios){
            System.out.println("usuario: "+us.nombre+" contra: "+us.contrasenia);
        
        }
        for(GrupoUsuarios gu:gruposUsuarios){
            System.out.println("grupo: "+gu.id+ " nombre: "+gu.nombre);
           
        
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
                List<Integer> usuariosId = new ArrayList<Integer>(); 
                while(!instrucciones.get(indice).equals(EstructuraSistemaArchivos.FINAL_G_USUARIO)){
                    tipoInstruccion = instrucciones.get(indice);
                    if(tipoInstruccion.equals(EstructuraSistemaArchivos.INICIO_ID)){
                        indice++;
                        usuariosId.add(Integer.parseInt(instrucciones.get(indice)));                   
                    }indice++;
                
                }
                indice++;
                gruposUsuarios.add(new GrupoUsuarios(id,nombre,usuariosId));
            }       
        }
        return indice;
    }
    
    private int cargarUsuarios(List<String> instrucciones, int indice){
        String tipoInstruccion = instrucciones.get(indice);
        int id;
        String nombre = null,contrasenia = null,nombreCompleto=null;
        while(!tipoInstruccion.equals(EstructuraSistemaArchivos.FINAL_BLOQUE_USUARIOS)){
            tipoInstruccion = instrucciones.get(indice);
            if(tipoInstruccion.equals(EstructuraSistemaArchivos.INICIO_USUARIO)){
                indice+=2;
                id = Integer.parseInt(instrucciones.get(indice));
                indice+=3;
                nombreCompleto = instrucciones.get(indice);
                indice+=3;
                nombre = instrucciones.get(indice);
                indice+=3;
                contrasenia = instrucciones.get(indice);
                indice+=3;
                usuarios.add(new Usuario(id,nombreCompleto,nombre,contrasenia));
            }
        }usuarioActual = usuarios.get(0);// Se establece el usuario root como el actual
        return indice;
    }
    
    private void cargarBloquesLibres(String instruccionBloques){
        String[] bloquesSeparados = instruccionBloques.split(",");
        for(int i=0;i<bloquesSeparados.length;i++){
            bloquesLibres.add(Integer.parseInt(bloquesSeparados[i])); 
        }
    }
    
    private void manejadorComandos(String linea) {
        String[] elementos = linea.split(" ");
        String comando = elementos[0];
        switch (comando) {
            case "format":
                comandoFormat();
                break;
            case "useradd":
                comandoUserAdd(elementos,false);
                break;
            case "groupadd":
                comandoGroupAdd(elementos);
                break;
            case "passwd":
                comandoPasswd(elementos);
                // llamado al método
                break;
            case "su":
                comandoSU(elementos);
                break;
            case "whoami":
                comandoWhoAmI();
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
        comandoUserAdd(null, true);
        crearSistemaArchivos();
        System.out.println("¡Formato creado!");
    }

    /**
     * Se encarga de ejecutar el comando "useradd". Si la variable es root,...
     * ...es el usuario root, por lo que solo pide la contraseña, sino, esta...
     * ...agregando un usuario normal.
     * * Los elementos son la linea introduccida en la consola.
     * @param elementos
     * @param root 
     */
    private void comandoUserAdd(String[] elementos, Boolean root){
        if((!root && elementos.length > 1) || root){
            String nombre, nombreUsuario, contrasenia, contraseniaTemp;
            if(root){
                nombreUsuario = "root";
            }else{
                nombreUsuario = elementos[1];
            }
            if(!usuarioRepetido(nombreUsuario) || root){
                while(true){
                    System.out.print("Ingrese el nombre completo: ");
                    nombre = entradaComandos.nextLine();
                    /*if(usuarioRepetido(nombre)){
                        System.out.println("Error, ingrese un nombre válido");
                    }else{
                        break;
                    }*/
                    break;
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
                    usuarioActual = new Usuario(0, nombre, nombreUsuario, contrasenia);
                }else{
                    Usuario usuarioNuevo = new Usuario(usuarios.size(), nombre, nombreUsuario, contrasenia);
                    usuarios.add(usuarioNuevo);
                    if(escribirUsuario(usuarioNuevo)){
                        System.out.println("¡Usuario agregado!");
                    }else{
                        System.out.println("Error al agregar el usuario");
                    }
                }
            }else{
                System.out.println("El nombre de usuario ya existe.");
            }
        }else{
            System.out.println("Especifique un nombre de usuario.");
        }
    }
    
    /**
     * Se encarga de ejecutar el comando "groupadd". Agrega un grupo al sistema.
     * Los elementos son la linea introduccida en la consola.
     * @param elementos
     */
    private void comandoGroupAdd(String[] elementos){
        if(elementos.length > 1){
            String nombreGrupo;
            nombreGrupo = elementos[1];
            if(!grupoRepetido(nombreGrupo)){
                GrupoUsuarios grupoNuevo = new GrupoUsuarios(gruposUsuarios.size(),nombreGrupo, null);
                gruposUsuarios.add(grupoNuevo);
                if(escribirGrupoUsuario(grupoNuevo)){
                    System.out.println("¡Grupo agregado!");
                }else{
                    System.out.println("Error al agregar el grupo");
                }
            }else{
                System.out.println("El nombre de grupo ya existe.");
            }
        }else{
            System.out.println("Especifique un nombre de grupo.");
        }
    }
    
    private Boolean grupoRepetido(String grupo){
        int cantidadGrupos = gruposUsuarios.size();
        for (int i = 0; i < cantidadGrupos; i++) {
            if(gruposUsuarios.get(i).nombre.equals(grupo)){
                return true;
            }
        }
        return false;
    }
    
    private void comandoPasswd(String[] elementos){
        String nombreUsuario, contrasenia, contraseniaTemp;
        if(elementos.length > 1){
            nombreUsuario = elementos[1];
            boolean existe = false;
            for(Usuario usuario:usuarios){
                if(usuario.nombre.equals(nombreUsuario)){
                    existe = true;
                    do{
                        System.out.print("Ingrese la nueva contraseña de "+nombreUsuario+": ");
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
                    //Cambiamos la contraseña
                    usuario.contrasenia = contraseniaTemp;
                    cambiarContrasenia(usuario);
                    break;



                }    
            }if(!existe){
                System.out.println("El usuario no existe");      
            }
        }else{
            System.out.println("Especifique un nombre de usuario.");
        }
    }
    
    private void cambiarContrasenia(Usuario usuario){
        String contenido = EstructuraSistemaArchivos.generarContenidoUsuario(usuario);
        Bloque bloqueDestino;
        RandomAccessFile archivo;
        Boolean esBloqueNuevo = false;
        String bloquesLibresActualizados,bloqueSinUsuario,bloqueNuevo;
        String [] bloqueNuevoArreglo; 
        int bloqueBuscado = 0;
        int bloqueLibre;
        try {
            archivo = new RandomAccessFile(nombreDisco, "rw");
            while(true){
                bloqueDestino = ObtenerBloque(bloqueBuscado);
                // Genero el bloque nuevo con el usuario donde corresponde
                bloqueNuevoArreglo = cambiarContraseniaCadena(bloqueDestino, contenido,esBloqueNuevo,usuario);
                bloqueSinUsuario = bloqueNuevoArreglo[1];
                bloqueNuevo = bloqueNuevoArreglo[0];
                if(hayEspacioEnBloque(bloqueDestino, bloqueNuevo)){
                    escribirBloque(bloqueDestino, bloqueNuevo);
                    break; // Importante para que no recorra todo el archivo.
                }else{
                    if(bloqueDestino.bloqueSiguiente != -1){
                        bloqueBuscado = bloqueDestino.bloqueSiguiente;
                    }else{
                        bloqueLibre = ObtenerBloqueLibre();
                        if(bloqueLibre != -1){
                            // Importante llamarla solo después de ObtenerBloqueLibre
                            // y si es distinto de -1
                            bloquesLibresActualizados = actualizarBloquesLibres();
                            if(bloqueDestino.id == 0){
                                bloqueDestino.contenido = bloquesLibresActualizados;
                            }
                            actualizarIdSiguienteBloque(bloqueDestino, bloqueLibre);
                            bloqueBuscado = bloqueLibre;
                            esBloqueNuevo = true;
                        }else{
                            System.out.println("Error, no hay espacio");
                            archivo.close();
                            break;
                        }
                    }
                }
            }
            archivo.close();
            
        } catch (IOException ex) {
            
        }       
    }
    
    private void comandoSU(String[] elementos){
        Usuario usuarioTem;
        String contrasenia;
        if(elementos.length > 1){
            usuarioTem = obtenerUsuario(elementos[1]);
            if(usuarioTem != null){
                usuarioActual = usuarioTem;
            }else{
                System.out.println("El usuario no existe");
            }
        }else{
            usuarioTem = usuarios.get(0);
        }
        if(usuarioTem != null){
            System.out.print("Ingrese la contraseña: ");
            contrasenia = entradaComandos.nextLine();
            if(contrasenia.equals(usuarioTem.contrasenia)){
                usuarioActual = usuarioTem;
            }else{
                System.out.println("La contraseña no es correcta.");
            }
        }
    }
    
    private void comandoWhoAmI(){
        System.out.println("username: "+usuarioActual.nombre);
        System.out.println("full name: "+usuarioActual.nombreCompleto);
    }
    
    private Usuario obtenerUsuario(String usuario){
        int cantidadUsuarios = usuarios.size();
        for (int i = 0; i < cantidadUsuarios; i++) {
            if(usuarios.get(i).nombre.equals(usuario)){
                return usuarios.get(i);
            }
        }
        return null;
    }
    
    /**
     * Agrega la información de un usuario en el bloque indicado
     * @param bloque
     * @param usuario
     * @return String
     */
    private String[] cambiarContraseniaCadena(Bloque bloque, String usuario,
            Boolean esBloqueNuevo,Usuario user){
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        String cadenaSinUsuario ="";
        System.out.println("largo: "+cantidadLineas);
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.INICIO_USUARIO)){
                i += 2;
                linea = lineasBloque[i];
                if(Integer.parseInt(linea) == user.id ){                       
                    cadenaFinal += usuario + "\n";                  
                    i += 8;
                }else{
                    i -= 2;
                    linea = lineasBloque[i];
                    cadenaFinal += linea + "\n";
                    cadenaSinUsuario  += linea + "\n";
                }
               // cadenaFinal += usuario + "\n";
            }else if(esBloqueNuevo && linea.equals(EstructuraSistemaArchivos.FINAL_INFORMACION)){
                cadenaFinal
                        += EstructuraSistemaArchivos.INICIO_BLOQUE_USUARIOS + "\n"
                        + usuario + "\n"
                        + EstructuraSistemaArchivos.FINAL_BLOQUE_USUARIOS + "\n"
                        + EstructuraSistemaArchivos.INICIO_BLOQUE_G_USUARIOS + "\n"
                        + EstructuraSistemaArchivos.FINAL_BLOQUE_G_USUARIOS + "\n";
            }else{cadenaFinal += linea + "\n";cadenaSinUsuario  += linea + "\n";}
        }System.out.println(cadenaFinal);return new String[]{cadenaFinal,cadenaSinUsuario};
    }
    
    
    private Boolean usuarioRepetido(String usuario){
        int cantidadUsuarios = usuarios.size();
        for (int i = 0; i < cantidadUsuarios; i++) {
            if(usuarios.get(i).nombre.equals(usuario)){
                return true;
            }
        }
        return false;
    }
    
    private Boolean escribirGrupoUsuario(GrupoUsuarios gruposUsuarios){
        String contenido = EstructuraSistemaArchivos.generarContenidoGrupoUsuario(gruposUsuarios);
        Bloque bloqueDestino;
        RandomAccessFile archivo;
        Boolean esBloqueNuevo = false;
        String bloqueNuevo, bloquesLibresActualizados;
        int bloqueBuscado = 0;
        int bloqueLibre;
        try {
            archivo = new RandomAccessFile(nombreDisco, "rw");
            while(true){
                bloqueDestino = ObtenerBloque(bloqueBuscado);
                // Genero el bloque nuevo con el usuario donde corresponde
                bloqueNuevo = agregarGrupoUsuarioCadena(bloqueDestino, contenido, esBloqueNuevo);
                if(hayEspacioEnBloque(bloqueDestino, bloqueNuevo)){
                    escribirBloque(bloqueDestino, bloqueNuevo);
                    break; // Importante para que no recorra todo el archivo.
                }else{
                    if(bloqueDestino.bloqueSiguiente != -1){
                        bloqueBuscado = bloqueDestino.bloqueSiguiente;
                    }else{
                        bloqueLibre = ObtenerBloqueLibre();
                        if(bloqueLibre != -1){
                            // Importante llamarla solo después de ObtenerBloqueLibre
                            // y si es distinto de -1
                            bloquesLibresActualizados = actualizarBloquesLibres();
                            if(bloqueDestino.id == 0){
                                bloqueDestino.contenido = bloquesLibresActualizados;
                            }
                            actualizarIdSiguienteBloque(bloqueDestino, bloqueLibre);
                            bloqueBuscado = bloqueLibre;
                            esBloqueNuevo = true;
                        }else{
                            System.out.println("Error, no hay espacio");
                            archivo.close();
                            return false;
                        }
                    }
                }
            }
            archivo.close();
            return true;
        } catch (IOException ex) {
            return false;
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
        Bloque bloqueDestino;
        RandomAccessFile archivo;
        Boolean esBloqueNuevo = false;
        String bloqueNuevo, bloquesLibresActualizados;
        int bloqueBuscado = 0;
        int bloqueLibre;
        try {
            archivo = new RandomAccessFile(nombreDisco, "rw");
            while(true){
                bloqueDestino = ObtenerBloque(bloqueBuscado);
                // Genero el bloque nuevo con el usuario donde corresponde
                bloqueNuevo = agregarUsuarioCadena(bloqueDestino, contenido, esBloqueNuevo);
                if(hayEspacioEnBloque(bloqueDestino, bloqueNuevo)){
                    escribirBloque(bloqueDestino, bloqueNuevo);
                    break; // Importante para que no recorra todo el archivo.
                }else{
                    if(bloqueDestino.bloqueSiguiente != -1){
                        bloqueBuscado = bloqueDestino.bloqueSiguiente;
                    }else{
                        bloqueLibre = ObtenerBloqueLibre();
                        if(bloqueLibre != -1){
                            // Importante llamarla solo después de ObtenerBloqueLibre
                            // y si es distinto de -1
                            bloquesLibresActualizados = actualizarBloquesLibres();
                            if(bloqueDestino.id == 0){
                                bloqueDestino.contenido = bloquesLibresActualizados;
                            }
                            actualizarIdSiguienteBloque(bloqueDestino, bloqueLibre);
                            bloqueBuscado = bloqueLibre;
                            esBloqueNuevo = true;
                        }else{
                            System.out.println("Error, no hay espacio");
                            archivo.close();
                            return false;
                        }
                    }
                }
            }
            archivo.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    private Bloque ObtenerBloque(int numeroBloque) throws FileNotFoundException, IOException{
        int bloqueS = -1;
        String contenidoB = "", linea;
        Boolean leyendoBloque = false, leyendoBloqueS = false;
        RandomAccessFile archivo = new RandomAccessFile(nombreDisco, "r");
        archivo.seek(tamanioBloque * numeroBloque);
        while((linea = archivo.readLine()) != null){
            if(leyendoBloqueS){
                leyendoBloqueS = false;
                bloqueS = Integer.parseInt(linea);
            }
            
            if(linea.equals(EstructuraSistemaArchivos.INICIO_BLOQUE)){
                leyendoBloque = true;
            }else if(linea.equals(EstructuraSistemaArchivos.INICIO_BLOQUE_SIGUIENTE)){
                leyendoBloqueS = true;
            }else if(leyendoBloque && linea.equals(EstructuraSistemaArchivos.FINAL_BLOQUE)){
                contenidoB += (linea + "\n");
                break;
            }           
            if(leyendoBloque){
                contenidoB += (linea + "\n");
            }
        }
        archivo.close();
        return new Bloque(numeroBloque, bloqueS, contenidoB);
    }
    
    private int ObtenerBloqueLibre(){
        int cantidad = bloquesLibres.size();
        for(int i = 0; i < cantidad; i++){
            if(bloquesLibres.get(i)==0){
                bloquesLibres.set(i, 1);
                return i;
            }
        }return -1;
    }
    
    /**
     * Indica si hay espacio para el contenido en el bloque.
     * De acuerdo con el tamaño de bloque. Se utiliza el bloque para ajustar...
     * ...los bytes requeridos de acuerdo con el valor de id bloque siguiente
     * @param bloque
     * @param contenBloqueNuevo
     * @return Boolean
     */
    private Boolean hayEspacioEnBloque(Bloque bloque, String contenBloqueNuevo){
        // Se reservan 80 bytes para almacenar los carácteres del valor máximo de int
        int tamanioReservaIdS = 80 - String.valueOf(bloque.bloqueSiguiente).getBytes().length;
        return contenBloqueNuevo.getBytes().length + tamanioReservaIdS < tamanioBloque;
    }
    
    /**
     * Agrega la información de un grupo usuarios en el bloque indicado
     * @param bloque
     * @param grupoUsuario
     * @return String
     */
    private String agregarGrupoUsuarioCadena(Bloque bloque, String grupoUsuario,
            Boolean esBloqueNuevo){
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.FINAL_BLOQUE_G_USUARIOS)){
                cadenaFinal += grupoUsuario + "\n";
            }else if(esBloqueNuevo && linea.equals(EstructuraSistemaArchivos.FINAL_INFORMACION)){
                cadenaFinal
                        += EstructuraSistemaArchivos.INICIO_BLOQUE_USUARIOS + "\n"
                        + EstructuraSistemaArchivos.FINAL_BLOQUE_USUARIOS + "\n"
                        + EstructuraSistemaArchivos.INICIO_BLOQUE_G_USUARIOS + "\n"
                        + grupoUsuario + "\n"
                        + EstructuraSistemaArchivos.FINAL_BLOQUE_G_USUARIOS + "\n";
            }
            cadenaFinal += linea + "\n";
        }return cadenaFinal;
    }
    
    /**
     * Agrega la información de un usuario en el bloque indicado
     * @param bloque
     * @param usuario
     * @return String
     */
    private String agregarUsuarioCadena(Bloque bloque, String usuario,
            Boolean esBloqueNuevo){
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.FINAL_BLOQUE_USUARIOS)){
                cadenaFinal += usuario + "\n";
            }else if(esBloqueNuevo && linea.equals(EstructuraSistemaArchivos.FINAL_INFORMACION)){
                cadenaFinal
                        += EstructuraSistemaArchivos.INICIO_BLOQUE_USUARIOS + "\n"
                        + usuario + "\n"
                        + EstructuraSistemaArchivos.FINAL_BLOQUE_USUARIOS + "\n"
                        + EstructuraSistemaArchivos.INICIO_BLOQUE_G_USUARIOS + "\n"
                        + EstructuraSistemaArchivos.FINAL_BLOQUE_G_USUARIOS + "\n";
            }
            cadenaFinal += linea + "\n";
        }return cadenaFinal;
    }
    
    private String actualizarBloquesLibres() throws IOException{
        Bloque bloque = ObtenerBloque(0);
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        int cantidadBloquesLibres = bloquesLibres.size();
        String cadenaFinal = "", linea;
        String cadenabloquesLibres = "" + bloquesLibres.get(0);
        for(int i = 1; i < cantidadBloquesLibres; i++){
            cadenabloquesLibres += ","+bloquesLibres.get(i);
        }
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.INICIO_BLOQUES_LIBRES)){
                cadenaFinal += linea + "\n";
                cadenaFinal += cadenabloquesLibres + "\n";
                i++;
            }else{
                cadenaFinal += linea + ((i+1<cantidadLineas)? "\n" : "");
            }
        }escribirBloque(bloque, cadenaFinal);
        return cadenaFinal;
    }
    
    private void actualizarIdSiguienteBloque(Bloque bloque, int id) throws IOException{
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.INICIO_BLOQUE_SIGUIENTE)){
                cadenaFinal += linea + "\n";
                cadenaFinal += id + "\n";
                i++;
            }else{
                cadenaFinal += linea + ((i+1<cantidadLineas)? "\n" : "");
            }
        }escribirBloque(bloque, cadenaFinal);
    }
    
    private void escribirBloque(Bloque bloque, String contenido) throws FileNotFoundException, IOException{
        RandomAccessFile archivo = new RandomAccessFile(nombreDisco, "rw");
        archivo.seek(tamanioBloque * bloque.id);
        archivo.writeBytes(contenido);
        archivo.close();
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
                usuarioActual.nombreCompleto, usuarioActual.contrasenia);
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
        int numeroBloque = 1;
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
                        // Se mueve el puntero del archivo hasta el inicio donde debe ir el siguiente bloque
                        archivo.seek(tamanioBloque * numeroBloque);
                        numeroBloque++;
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
