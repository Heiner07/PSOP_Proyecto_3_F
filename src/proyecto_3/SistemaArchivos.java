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
import java.util.Map;
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
    int bloqueUbicadoContrasenia;
    boolean yaEntroContrasenia;
    String informacionBloqueContrasenia;

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
        yaEntroContrasenia = false;
        bloqueUbicadoContrasenia = -1;
        informacionBloqueContrasenia = "";
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
                System.out.print(usuarioActual.nombre+"@miFS:"+rutaActual.ubicacion+"$ ");
                lineaActual = entradaComandos.nextLine();
                historialComandos += lineaActual + "\n";
                if (lineaActual.equals("poweroff")) {
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
                    archivo.seek(tamanioBloque * numSig);
                    numSig = -1;
                    
                }
            }else if(st.equals(EstructuraSistemaArchivos.INICIO_NUMERO_BLOQUES)){
                instrucciones.add(st);
                st = archivo.readLine();
                cantidadBloques = Integer.parseInt(st);           
                instrucciones.add(st);
                st = archivo.readLine();
                tamanioBloque = Integer.parseInt(st);           
            }           
            instrucciones.add(st);
        }
        
        cargarDatos(instrucciones);
        rutaActual = cargarCarpetaArchivo(1, true, null); // El bloque 1 contiene la carpeta raíz por cargar.
        comandoCD(new String[]{"", "root"});
    }
    
    /**
     * Carga los datos iniciales del archivo, que son necesarios para
     * el funcionamiento correcto del programa.
     */
    private void cargarDatos(List<String> instrucciones){
        int largoInstrucciones = instrucciones.size();
       // for(String st:instrucciones){System.out.println(st);}
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
    
    private void manejadorComandos(String linea) throws IOException {
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
                comandoPwd();
                break;
            case "mkdir":
                comandoMkdir(elementos);
                break;
            case "rm":
                comandoRm(elementos);
                // llamado al método
                break;
            case "mv":
                comandoMV(elementos);
                break;
            case "ls":
                comandoLS(elementos);
                break;
            case "cd":
                comandoCD(elementos);
                break;
            case "whereis":
                // llamado al método
                break;
            case "ln":
                // llamado al método
                break;
            case "touch":
                comandoTouch(elementos);
                break;
            case "cat":
                // llamado al método
                break;
            case "chown":
                comandoChown(elementos);
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
        // Se reinician los valores
        usuarios.clear();
        gruposUsuarios.clear();
        bloques.clear();
        bloquesLibres.clear();
        archivosAbiertos.clear();
        cantidadArchivosAbiertos = 0;
        historialComandos = "";
        lineaActual = "";
        sistemaCargado = false;
        yaEntroContrasenia = false;
        bloqueUbicadoContrasenia = -1;
        informacionBloqueContrasenia = "";
        
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
                    crearUsuarioCarpetaGrupo(usuarioNuevo);
                }
            }else{
                System.out.println("El nombre de usuario ya existe.");
            }
        }else{
            System.out.println("Especifique un nombre de usuario.");
        }
    }
    
    private void crearUsuarioCarpetaGrupo(Usuario usuarioNuevo){
        Archivo rutaActualTemp;
        try {
            rutaActualTemp = cargarCarpetaArchivo(3, true, null);
            int bloqueLibre = ObtenerBloqueLibre();
            if(bloqueLibre != -1){
                if(!elementoRepetidoEnCarpeta(usuarioNuevo.nombre, rutaActualTemp)){
                    if(!grupoRepetido("Grupo"+usuarioNuevo.nombre)){
                        List<Integer> idUsuario = new ArrayList<>();
                        idUsuario.add(usuarioNuevo.id);
                        GrupoUsuarios grupoNuevo =
                                new GrupoUsuarios(gruposUsuarios.size(),
                                        "Grupo"+usuarioNuevo.nombre, idUsuario);
                        if(escribirGrupoUsuario(grupoNuevo)){
                            gruposUsuarios.add(grupoNuevo);
                            Archivo carpetaNueva = new Archivo(0, 0, usuarioNuevo.nombre,
                                        rutaActualTemp.ubicacion + usuarioNuevo.nombre + "/",
                                        "SI", usuarioNuevo,
                                        grupoNuevo, bloqueLibre);
                            // Dentro de esta función se actualizan los bloques libres
                            if(escribirCarpetaArchivo(carpetaNueva, true, rutaActualTemp)){
                                if(escribirUsuario(usuarioNuevo)){
                                    usuarios.add(usuarioNuevo);
                                    System.out.println("¡Usuario agregado!");
                                    return;
                                }else{
                                    System.out.println("Error creando el usuario.");
                                }return;// Para que no libere el bloque usado por la carpeta.
                            }else{
                                System.out.println("Error al crear la carpeta del usuario.");
                            }
                        }else{
                            System.out.println("No se puede crear el usuario porque"
                                    + " sucedió un error creando el grupo de este usuario.");
                        }
                    }else{
                        System.out.println("No se puede crear el usuario porque hay"
                                + " un grupo con el mismo nombre del usuario.");
                    }
                }else{
                    System.out.println("Error, no se puede crear el usuario porque"
                            + " hay una carpeta creada con el mismo nombre de usuario"
                            + " en la carpeta /root/users/.");
                }bloquesLibres.set(bloqueLibre, 0);// Libera el bloque usado por la carpeta (si hay error)
            }else{
                System.out.println("Error, no hay espacio para la carpeta de usuario.");
            }
        } catch (IOException ex) {
            //Logger.getLogger(SistemaArchivos.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error leyendo el disco");
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
                if(escribirGrupoUsuario(grupoNuevo)){
                    gruposUsuarios.add(grupoNuevo);
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
                    cambiarContrasenia(usuario,contrasenia);
                    break;
                }    
            }if(!existe){
                System.out.println("El usuario no existe");      
            }
        }else{
            System.out.println("Especifique un nombre de usuario.");
        }
        
    }
    
    private void cambiarContrasenia(Usuario usuario,String contrasenia){
        String contenido = EstructuraSistemaArchivos.generarContenidoUsuario(new Usuario(usuario.id,usuario.nombreCompleto,usuario.nombre,contrasenia));
        Bloque bloqueDestino;
        RandomAccessFile archivo;
        Boolean esBloqueNuevo = false;
        String bloqueNuevo; 
        int bloqueBuscado = 0;
        int bloqueLibre;
        try {
            archivo = new RandomAccessFile(nombreDisco, "rw");
            while(true){
                bloqueDestino = ObtenerBloque(bloqueBuscado);
                // Genero el bloque nuevo con el usuario donde corresponde
                bloqueNuevo = cambiarContraseniaCadena(bloqueDestino, contenido,esBloqueNuevo,usuario);
                if(hayEspacioEnBloque(bloqueDestino, bloqueNuevo) && bloqueUbicadoContrasenia != -1){
                    escribirBloque(bloqueDestino.id, bloqueNuevo);
                    bloqueUbicadoContrasenia = -1;
                    yaEntroContrasenia = false;
                    usuario.contrasenia = contrasenia;
                    System.out.println("Se ha cambiado la contraseña con éxito");
                    break; // Importante para que no recorra todo el archivo.
                }else{
                    //Debo guardar el bloque pero ahora sin el usuario porque lo voy a pasar de bloque
                   // escribirBloque(bloqueDestino, bloqueSinUsuario);
                    if(bloqueDestino.bloqueSiguiente != -1){
                        bloqueBuscado = bloqueDestino.bloqueSiguiente;
                    }else{
                        bloqueLibre = ObtenerBloqueLibre();
                        if(bloqueLibre != -1){
                            // Importante llamarla solo después de ObtenerBloqueLibre
                           // y si es distinto de -1                         
                            actualizarIdSiguienteBloque(new Bloque(bloqueUbicadoContrasenia,bloqueDestino.bloqueSiguiente,informacionBloqueContrasenia), bloqueLibre);
                            actualizarBloquesLibres();
                            bloqueBuscado = bloqueLibre;
                            esBloqueNuevo = true;
                            
                        }else{
                            System.out.println("Error, no hay espacio");
                            bloqueUbicadoContrasenia = -1;
                            yaEntroContrasenia = false;
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
    
    
    
    
    private boolean obtenerEsCarpeta(Archivo archivo, int id){
        int largoDocumentos = archivo.contenido.size();
        for(int i=0;i<largoDocumentos;i++){  
            int bloqueI = archivo.contenido.get(i).bloqueInicial;
            if( bloqueI == id){
                return archivo.contenido.get(i).esCarpeta;               
            }   
        }return false;
    
    }
   
    private boolean verificarDocumento(Bloque bloque,Archivo archivo,String nombreDocumento) throws IOException{
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length,idBloqueEliminar=1;
        String linea,contenido = "";
        boolean actualizarArchivo = false;
        while(true){
            for(int i = 0; i < cantidadLineas; i++){
                linea = lineasBloque[i];
                if(linea.equals(EstructuraSistemaArchivos.INICIO_CARPETA) || linea.equals(EstructuraSistemaArchivos.INICIO_ARCHIVO) ){
                    String lineaEstructura = linea;
                    i++; //Para posicionarme en el id de la carpeta o el archivo
                    linea = lineasBloque[i];
                    //Verificamos que ese id este ocupado
                    int id = Integer.parseInt(linea);
                    if(bloquesLibres.get(id) == 1){
                            Archivo bloqueVerificar = cargarCarpetaArchivo(id,obtenerEsCarpeta(archivo,id),archivo.ubicacion);
                            if(bloqueVerificar.nombre.equals(nombreDocumento) && bloqueVerificar.contenido.isEmpty() && bloqueVerificar.bloqueS==-1){
                            //Verifico que el archivo no este abierto y que tenga los permisos de usuario o grupo para eliminar
                                if(PermisosEliminar(bloqueVerificar)){
                                    //Eliminamos el archivo o carpeta
                                    //Lo volvemos a la raíz
                                    if(bloqueVerificar.ubicacion.equals((rutaActual.ubicacion+"/")) || bloqueVerificar.ubicacion.equals(rutaActual.ubicacion))
                                        rutaActual = cargarCarpetaArchivo(1, true,null); 
                                        bloquesLibres.set(id,0);
                                        idBloqueEliminar = id;
                                        actualizarBloquesLibres();
                                        actualizarArchivo = true;
                                        i++;
                                }
                            }else{
                                contenido += (lineaEstructura + "\n");
                                contenido += (linea + "\n");
                            }
                    }
                }else{
                    contenido += (linea + "\n");

                }
            }
            if(!actualizarArchivo && bloque.bloqueSiguiente != -1){
               bloque = ObtenerBloque(bloque.bloqueSiguiente);
               contenido = "";
               lineasBloque = bloque.contenido.split("\n");
               cantidadLineas = lineasBloque.length;
            }else{break;}
        }
        if(actualizarArchivo){
            escribirBloque(bloque.id, contenido);
            escribirBloque(idBloqueEliminar, EstructuraSistemaArchivos.generarBloqueLibre(idBloqueEliminar));
            actualizarContenidoRutaActual(idBloqueEliminar);
           // System.out.println("Elimina: "+nombreDocumento);
            return true;  
        }
        else{
           // System.out.println("NO elimina: "+nombreDocumento);
            return false;
        }
    }
    private void actualizarContenidoRutaActual(int idBloqueEliminar){
        List<Archivo> archivos = new ArrayList<>();
        for(Archivo archivo:rutaActual.contenido){
            if(archivo.bloqueInicial != idBloqueEliminar)
                archivos.add(archivo);
        }
        rutaActual.contenido = archivos;
    }
    
    
    
    private boolean VerificarBloquesRm(Archivo archivo, String cadena) throws IOException{      
        Bloque bloque = ObtenerBloque(archivo.bloqueInicial);
        if(verificarDocumento(bloque,archivo, cadena)){
            return true;       
        }return false;
    }
    private List<Boolean> eliminarEnRutaAux(Archivo carpeta){
        int indiceFinal, indiceActual;
        Archivo carpetaActual = carpeta, carpetaTemp, bloquePadre, archivoTemp,carpetaContenedoraTemp;
        List<Integer> carpetasRevisadas = new ArrayList<>();
        List<Integer> indiceCarpeta = new ArrayList<>();
        indiceCarpeta.add(0);
        List<Boolean> estadosArchivos = new ArrayList<>();
        try{
            while(!carpetasRevisadas.contains(carpeta.bloqueInicial)){
                indiceFinal = indiceCarpeta.size()-1;
                indiceActual = indiceCarpeta.get(indiceFinal);
                indiceCarpeta.set(indiceFinal, indiceActual+1);
                if(indiceActual == carpetaActual.contenido.size()){
                    indiceCarpeta.remove(indiceFinal);
                    carpetasRevisadas.add(carpetaActual.bloqueInicial);
                    Bloque bloque = ObtenerBloque(carpetaActual.carpetaContenedora.bloqueInicial);
                    estadosArchivos.add(verificarDocumento(bloque,carpetaActual.carpetaContenedora,carpetaActual.nombre));
                    carpetaActual = carpetaActual.carpetaContenedora;
                }else if(carpetaActual.contenido.get(indiceActual).esCarpeta){
                        bloquePadre = carpetaActual;
                        carpetaActual = cargarCarpetaArchivo(carpetaActual.contenido.get(indiceActual).bloqueInicial, true,bloquePadre.ubicacion);
                        indiceCarpeta.add(0);
                        carpetaActual.asignarCarpetaContenedor(bloquePadre);
                }else{
                    archivoTemp = cargarCarpetaArchivo(carpetaActual.contenido.get(indiceActual).bloqueInicial, false,carpetaActual.ubicacion);
                    Bloque bloque = ObtenerBloque(carpetaActual.bloqueInicial);
                    estadosArchivos.add(verificarDocumento(bloque,carpetaActual,archivoTemp.nombre));
                }
            }
            
        }catch(IOException e){
            System.out.println("Error leyendo el disco.");
        }
        return estadosArchivos;
    
    }
    
    private boolean PermisosEliminar(Archivo archivoAnalizar){
        Usuario usuario = archivoAnalizar.propietario;
        List<Integer> grupoUsuariosId = archivoAnalizar.grupoUsuarios.usuariosId;
        String permisos = archivoAnalizar.permisos;
        int permisoGrupo = 7; //MODIFICAR ESTO
        int permisoUsuario = 7;//MODIFICAR ESTO
        if (archivosAbiertos.stream().anyMatch((archivo) -> (archivo.bloqueInicial == archivoAnalizar.bloqueInicial))) {
            return false;
        }
        if(grupoUsuariosId.contains(usuarioActual.id)){
            return permisoGrupo==7;     
        }else{
            if(usuario.id == usuarioActual.id){
                return permisoUsuario == 7;
            }
        }
        return false;
    
    }
    
    
    private void verificarEliminadosEnRuta(List<Boolean> archivosEliminados){
        if(archivosEliminados.get(archivosEliminados.size()-1))System.out.println("Se eliminó correctamente");
        else 
        for(Boolean archivoEliminado:archivosEliminados){
            if(archivoEliminado){
                System.out.println("Se eliminaron archivos|carpetas pero no la carpeta especificada.");
                break;
            }
        }
    
    }
    private String obtenerArchivoPadre(int id){
        String ubicacion = "/";
        try{
            for(int i=2;i<bloquesLibres.size();i++){
                int idOcupado = bloquesLibres.get(i);
                if(idOcupado == 1){
                    List<Archivo> contenidoAnalizar = cargarCarpetaArchivo(i,true,null).contenido;
                    for(Archivo ar:contenidoAnalizar){
                        if(ar.bloqueInicial==id){
                            ubicacion += cargarCarpetaArchivo(i,true,null).ubicacion;
                            return ubicacion;
                        }
                    }
                }
            }
            return ubicacion;
        }catch(Exception e){
            System.out.println("ERROR");
        
        }
        return ubicacion;
    
    }
    private List<Archivo> obtenerArchivoPorCadena(String ruta) throws IOException{
        List<Archivo> archivos = new ArrayList<>();
        try{
            for(int i=2;i<bloquesLibres.size();i++){
                int idOcupado = bloquesLibres.get(i);
                if(idOcupado == 1){
                    Archivo archivo = cargarCarpetaArchivo(i,true,obtenerArchivoPadre(i));
                    if(archivo.nombre.equals(ruta)){
                        archivos.add(archivo);
                    }
                }
            }
            return archivos;
        }catch(Exception e){
            System.out.println("ERROR");
        
        }
        return archivos;
    
    }
    private List<Archivo> encontrarRuta(String[] rutas){
        int largoRuta = rutas.length-1;
        List<Archivo> archivos;
        List<Archivo> archivosRetornar = new ArrayList<>();
        int indice = 1;
        try {
            archivos = obtenerArchivoPorCadena(rutas[indice]);
            indice++;
            int largoArchivos = archivos.size();
            for(int i=0;i<largoArchivos;i++){
                Archivo archivo = archivos.get(i);
                int largoContenido = archivo.contenido.size();
                int k = 0;
                while(k<largoContenido){
                    Archivo archivoAnalizar = cargarCarpetaArchivo(archivo.contenido.get(k).bloqueInicial, archivo.contenido.get(k).esCarpeta,archivo.ubicacion);
                    if(archivoAnalizar.nombre.equals(rutas[indice])){
                        if(indice>=largoRuta){
                            archivosRetornar.add(archivo);
                            archivosRetornar.add(archivoAnalizar);
                            return archivosRetornar;
                        }else{
                            archivo = archivoAnalizar;
                            largoContenido = archivo.contenido.size();
                            k = 0;
                        } 
                        indice++;
                    }else k++;
                }
            }
        } catch (IOException ex) {
                System.out.println("ERROR");
        }
        return archivosRetornar;
    
    }
    private void eliminarEnRuta(List<Archivo> archivos,boolean recursivo) throws IOException{      
       
        boolean rutaNoEncontrada = true,tienePadre=false;
        try{
            int largoArchivos = archivos.size();
            Archivo archivoDirectorio = archivos.get(largoArchivos-1);
            Archivo archivoPadre = archivos.get(largoArchivos-2);
            if(recursivo){
                rutaNoEncontrada = false;
                archivoDirectorio.carpetaContenedora = archivoPadre;
                verificarEliminadosEnRuta(eliminarEnRutaAux(archivoDirectorio));
            }else{
                rutaNoEncontrada = false;
                if(VerificarBloquesRm(archivoPadre,archivoDirectorio.nombre)){
                    System.out.println("Se eliminó correctamente "+archivoDirectorio.nombre);     
                }else{System.out.println("Error al eliminar "+archivoDirectorio.nombre);}
            
            }
            if(rutaNoEncontrada){System.out.println("Error al eliminar");}
        }catch(Exception e){
            System.out.println("ERROR");
        }
        
    }
    private boolean esRuta(String cadena){   
        for(char c : cadena.toCharArray()){
            if('/' == c){
                return true;
            } 
        }
        return false; 
    }
    
    private boolean esArchivo(String cadena){
        for(char c : cadena.toCharArray()){
            if('.' == c){
                return true;
            } 
        }
        return false; 
    
    }
    private void comandoRm(String[] elementos) throws IOException {
        if(elementos.length > 1 && elementos.length < 4){
            String cadena;
            if(elementos.length == 2){
                cadena = elementos[1];
                if(!esRuta(cadena) && !esArchivo(rutaActual.nombre)){
                     Archivo archivo = cargarCarpetaArchivo(rutaActual.bloqueInicial,true,null);
                     if(VerificarBloquesRm(archivo,cadena)){
                         System.out.println("Se eliminó correctamente "+cadena);
                     }
                     else{System.out.println("Error al eliminar "+cadena);}
                }
            }else{
                
                    String[] lineasRuta;
                    boolean recursivo = false;
                    int indice = 1;
                    if(elementos[1].equals("-r") || elementos[1].equals("-R")){
                        indice = 2;
                        recursivo = true;
                    }else{indice = 1;}
                    lineasRuta = elementos[indice].split("/");
                    if(!elementos[indice].equals("/root") && lineasRuta.length>2){
                       for(int i=0;i<lineasRuta.length-1;i++){
                           if(esArchivo(lineasRuta[i])){
                               System.out.println("Error, la ruta tiene un archivo como carpeta");
                               return;
                           }
                       }
                       List<Archivo> archivosRuta = encontrarRuta(lineasRuta);
                       eliminarEnRuta(archivosRuta,recursivo); 
                    }else System.out.println("Error de comando");
                
            }
        }
    }
    
    
    
    private void comandoChown(String[] elementos){
        if(elementos.length > 1 && elementos.length < 4){
        
        
        
        }
    
    }
    private void comandoWhoAmI(){
        System.out.println("username: "+usuarioActual.nombre);
        System.out.println("full name: "+usuarioActual.nombreCompleto);
    }
    
    private void comandoPwd(){
        System.out.println(rutaActual.ubicacion);
        
    }
    
    private void comandoMkdir(String[] elementos){
        int cantidadElementos = elementos.length;
        if(cantidadElementos > 1){
            String nombreCarpeta;
            int bloqueLibre;
            Archivo carpetaNueva;
            for(int i = 1; i < cantidadElementos; i++){
                nombreCarpeta = elementos[i];
                if(!elementoRepetidoEnCarpeta(nombreCarpeta, null)){
                    bloqueLibre = ObtenerBloqueLibre();
                    if(bloqueLibre != -1){
                        carpetaNueva = new Archivo(0, 0, nombreCarpeta,
                                rutaActual.ubicacion + nombreCarpeta + "/",
                                "SI", rutaActual.propietario,
                                rutaActual.grupoUsuarios, bloqueLibre);
                        if(escribirCarpetaArchivo(carpetaNueva, true, null)){
                            System.out.println("¡Carpeta creada!");
                        }else{
                            System.out.println("Error al crear la carpeta.");
                        }
                    }else{
                        System.out.println("Error no hay espacio.");
                    }
                }
            }
        }else{
            System.out.println("Especifique un nombre de carpeta");
        }
    }
    
    /**
     * Busca si existe un elemento en la carpeta con el nombre indicado.
     * Si la variable carpetaUsuarios es nulo, es en la carpeta actual, sino,
     * es la carpeta de usuarios (agregando un usuario)
     * @param nombre
     * @param carpetaUsuarios
     * @return 
     */
    private Boolean elementoRepetidoEnCarpeta(String nombre, Archivo carpetaUsuarios){
        List<Archivo> elementos = (carpetaUsuarios == null)? rutaActual.contenido
                : carpetaUsuarios.contenido;
        int cantidadElementos = elementos.size();
        Archivo elemento;
        try{
            for(int i = 0; i < cantidadElementos; i++){
                elemento = elementos.get(i);
                elemento = cargarCarpetaArchivo(elemento.bloqueInicial, elemento.esCarpeta, null);
                if(elemento.nombre.equals(nombre)){
                    System.out.println("Error. Ya existe un elemento con el nombre "+nombre);
                    return true;
                }
            }
        }catch(IOException e){
            System.out.println("Error leyendo la carpeta.");
            return true;
        }
        return false;
    }
    
    private void comandoCD(String[] elementos){
        int cantidadElementos = elementos.length;
        if(cantidadElementos > 1){
            String parametro = elementos[1];
            if(parametro.equals("..")){
                salirDeCarpeta();
            }else{
                entrarEnCarpeta(parametro);
            }
        }else{
            System.out.println("Especifique una carpeta.");
        }
    }
    
    private void salirDeCarpeta(){
        if(rutaActual.carpetaContenedora != null){
            rutaActual = rutaActual.carpetaContenedora;
            Archivo padre = rutaActual.carpetaContenedora;
            try {
                rutaActual = cargarCarpetaArchivo(rutaActual.bloqueInicial,
                        true, (padre != null)? padre.ubicacion: null);
                rutaActual.asignarCarpetaContenedor(padre);
            } catch (IOException ex) {
                System.out.println("Error actualizando la carpeta.");
            }
        }else{
            System.out.println("Ya está ubicado en la raíz");
        }
    }
    
    private void entrarEnCarpeta(String nombre){
        int cantidadElementosCarpeta = rutaActual.contenido.size();
        Archivo carpeta, carpetaTem;
        try{
            for(int i = 0; i < cantidadElementosCarpeta; i++){
                carpetaTem = rutaActual.contenido.get(i);
                if(carpetaTem.esCarpeta){
                    carpeta = cargarCarpetaArchivo(carpetaTem.bloqueInicial,
                            true, rutaActual.ubicacion);
                    if(carpeta.nombre.equals(nombre)){
                        carpeta.asignarCarpetaContenedor(rutaActual);
                        rutaActual = carpeta;
                        return;
                    }
                }
            }
            System.out.println("No existe carpeta con ese nombre.");
        }catch(IOException e){
            System.out.println("Error leyendo el disco.");
        }
    }
    
    private void comandoTouch(String[] elementos){
        int cantidadElementos = elementos.length;
        if(cantidadElementos > 1){
            String nombreArchivo;
            int bloqueLibre;
            Archivo archivoNuevo;
            for(int i = 1; i < cantidadElementos; i++){
                nombreArchivo = elementos[i];
                if(!elementoRepetidoEnCarpeta(nombreArchivo, null)){
                    bloqueLibre = ObtenerBloqueLibre();
                    if(bloqueLibre != -1){
                        archivoNuevo = new Archivo(0, 0, nombreArchivo,
                                rutaActual.ubicacion + nombreArchivo,
                                "SI", rutaActual.propietario,
                                rutaActual.grupoUsuarios, bloqueLibre);
                        if(escribirCarpetaArchivo(archivoNuevo, false, null)){
                            System.out.println("¡Archivo creado!");
                        }else{
                            System.out.println("Error al crear el archivo.");
                        }
                    }else{
                        System.out.println("Error no hay espacio.");
                    }
                }
            }
        }else{
            System.out.println("Especifique un nombre de archivo");
        }
    }
    
    private void comandoMV(String[] elementos){
        int cantidadElementos = elementos.length;
        if(cantidadElementos > 1){
            if(cantidadElementos > 2){
                String nombreArchivo = elementos[1];
                String nombreNuevoRuta = elementos[2];
                cambiarNombreRutaArchivoCarpeta(nombreArchivo, nombreNuevoRuta);
            }else{
                System.out.println("Especifique un nuevo nombre o directorio.");
            }
        }else{
            System.out.println("Especifique parámetros para el comando.");
        }
    }
    
    private void cambiarNombreRutaArchivoCarpeta(String nombre, String nombreRutaNuevo){
        Archivo archivoModificar = obtenerArchivoCarpetaDeCarpeta(rutaActual, nombre);
        if(archivoModificar != null){
            if(nombreRutaNuevo.contains("/")){
                Archivo carpetaDestino;
                try{
                    if(nombreRutaNuevo.startsWith("/")){
                        carpetaDestino = obtenerCarpetaDeRuta(
                                cargarCarpetaArchivo(1, true, null), nombreRutaNuevo);
                    }else{
                        carpetaDestino = obtenerCarpetaDeRuta(
                                rutaActual, nombreRutaNuevo);
                    }
                    if(carpetaDestino != null){//comprobar nombre repetido en carpetadestino
                        if(!elementoRepetidoEnCarpeta(nombre, carpetaDestino)){
                            moverArchivoCarpeta(
                                    archivoModificar, carpetaDestino.bloqueInicial);
                        }else{
                            System.out.println("No se puede mover porque ya existe un elemento con el mismo nombre");
                        }
                    }else{
                        System.out.println("La carpeta no existe.");
                    }
                }catch(IOException e){
                    System.out.println("Error leyendo el disco.");
                }
            }else{
                if(!elementoRepetidoEnCarpeta(nombreRutaNuevo, rutaActual)){
                    cambiarNombreArchivoCarpeta(archivoModificar, nombreRutaNuevo);
                }else{
                    System.out.println("No se puede cambiar porque ya existe un elemento con el mismo nombre.");
                }
            }
        }else{
            System.out.println("El archivo indicado no existe.");
        }
    }
    
    private void cambiarNombreArchivoCarpeta(Archivo archivoModificar, String nombreNuevo){
        Bloque bloque;
        String bloqueNombreCambiado;
        try{
            bloque = ObtenerBloque(archivoModificar.bloqueInicial);
            bloqueNombreCambiado = sustituirNombreCadena(bloque.contenido, nombreNuevo);
            escribirBloque(bloque.id, bloqueNombreCambiado);// Falta comprobar que quepa en el bloque
            System.out.println("Se cambió el nombre correctamente.");
        }catch(IOException e){
            System.out.println("Error leyendo el disco.");
        }
    }
    
    private String sustituirNombreCadena(String contenidoBloque, String nombreNuevo){
        String cadenaFinal = "", linea;
        String[] lineas = contenidoBloque.split("\n");
        int cantidadLineas = lineas.length;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineas[i];
            if(linea.equals(EstructuraSistemaArchivos.INICIO_NOMBRE)){
                cadenaFinal += linea + "\n";
                cadenaFinal += nombreNuevo + "\n";
                i++;
            }else{
                cadenaFinal += linea + "\n";
            }
        }
        return cadenaFinal;
    }
    
    private void moverArchivoCarpeta(Archivo archivoCarpetaModificar, int idBloqueDestino){
        Bloque bloqueCarpetaDestino;
        String referencia = 
                EstructuraSistemaArchivos.
                        generarContenidoReferenciaCarpetaArchivo(
                                archivoCarpetaModificar, archivoCarpetaModificar.esCarpeta);
        String bloqueNuevo;
        Boolean hayEspacio = false, esBloqueNuevo = false;
        int bloqueBuscado = idBloqueDestino;
        try{
            while(true){
                bloqueCarpetaDestino = ObtenerBloque(bloqueBuscado);
                bloqueNuevo = agregarReferenciaCarpeta(bloqueCarpetaDestino, referencia);
                if(hayEspacioEnBloque(bloqueCarpetaDestino, bloqueNuevo)){
                   hayEspacio = true;
                   break;
                }else if(bloqueCarpetaDestino.bloqueSiguiente == -1){
                    bloqueBuscado = ObtenerBloqueLibre();
                    if(bloqueBuscado == -1){
                        hayEspacio = false;
                        break;
                    }else{
                        // Si es un bloque nuevo, entonces le actualizo el bloque siguiente al bloque destino.
                        actualizarIdSiguienteBloque(bloqueCarpetaDestino, bloqueBuscado);
                        esBloqueNuevo = true;
                    }
                }else{
                    bloqueBuscado = bloqueCarpetaDestino.bloqueSiguiente;
                }
            }if(hayEspacio){
                escribirBloque(bloqueCarpetaDestino.id, bloqueNuevo);// Se escribe la referencia a la carpeta.
                if(esBloqueNuevo) actualizarBloquesLibres();
                eliminarArchivoCarpetaDeCarpeta(archivoCarpetaModificar);// Se elimina la referencia en la carpeta que estaba
                String carpetaActual = rutaActual.nombre;
                comandoCD(new String[]{"", ".."});
                comandoCD(new String[]{"", carpetaActual});
                if(archivoCarpetaModificar.esCarpeta){// Simple precisión en el mensaje
                    System.out.println("¡Carpeta movida!");
                }else{
                    System.out.println("¡Archivo movido!");
                }
            }else{
                if(archivoCarpetaModificar.esCarpeta){// Simple precisión en el mensaje
                    System.out.println("Error, no hay espacio para mover la carpeta.");
                }else{
                    System.out.println("Error, no hay espacio para mover el archivo.");
                }
            }
        }catch(IOException e){
            System.out.println("Error leyendo el disco.");
        }
    }
    
    private void eliminarArchivoCarpetaDeCarpeta(Archivo archivoCarpeta){
        String bloqueSinReferencia =
                eliminarReferenciaArchivoCarpeta(rutaActual, archivoCarpeta);
        try {
            if(bloqueSinReferencia != null){
                escribirBloque(rutaActual.bloqueInicial, bloqueSinReferencia);
            }else{
                System.out.println("Error limpiando la referencia en la carpeta.");
            }
        } catch (IOException ex) {
            System.out.println("Error limpiando la referencia en la carpeta.");
        }
    }
    
    /**
     * archivoEliminar tiene el bloque donde se ubica, usado para eliminar la referencia.
     * La carpetaContenedora es donde se quiere eliminar dicha referencia.
     * @param carpetaContenedora
     * @param idBloque
     * @return 
     */
    private String eliminarReferenciaArchivoCarpeta(Archivo carpetaContenedora,
            Archivo archivoEliminar){
        Bloque bloqueContenedor;
        String[] contenido;
        String linea, contenidoSinReferencia, idValor;
        Boolean encontrado = false;
        int bloqueBuscado = carpetaContenedora.bloqueInicial;
        int cantidadLineas;
        try{
            while(true){
                bloqueContenedor = ObtenerBloque(bloqueBuscado);
                contenido = bloqueContenedor.contenido.split("\n");
                cantidadLineas = contenido.length;
                contenidoSinReferencia = "";
                for(int i = 0; i < cantidadLineas; i++){
                    linea = contenido[i];
                    if((linea.equals(EstructuraSistemaArchivos.INICIO_ARCHIVO)
                            && !archivoEliminar.esCarpeta) ||
                            (linea.equals(EstructuraSistemaArchivos.INICIO_CARPETA)
                            && archivoEliminar.esCarpeta)){
                        i++;
                        idValor = contenido[i];
                        if(idValor.equals(String.valueOf(archivoEliminar.bloqueInicial))){
                            i++;
                            encontrado = true;
                        }else{
                            contenidoSinReferencia += linea + "\n" + idValor + "\n";
                        }
                    }else{
                        contenidoSinReferencia += linea + "\n";
                    }
                }
                if(encontrado){
                    break;// Falata validar si la refencia era la unica (quedaria el bloque vacio), para liberar el bloque si este era el siguiente
                }else{
                    bloqueBuscado = bloqueContenedor.bloqueSiguiente;
                    if(bloqueBuscado == -1){
                        break;
                    }
                }
            }
        }catch(IOException e){
            return null;
        }return contenidoSinReferencia;
    }
    
    private Archivo obtenerCarpetaDeRuta(Archivo carpetaInicio, String ruta){
        String[] carpetas = ruta.split("/");
        int cantidadCarpetas = carpetas.length;
        Archivo carpetaTemp = carpetaInicio;
        int i = 0;
        if(ruta.startsWith("/")){// Para que no considere el primer elemento que es vacio
            i++;
        }
        for(; i < cantidadCarpetas; i++){
            carpetaTemp =
                    obtenerArchivoCarpetaDeCarpeta(carpetaTemp, carpetas[i]);
            if(carpetaTemp == null){
                return null;
            }
        }return carpetaTemp;
    }
    
    private Archivo obtenerArchivoCarpetaDeCarpeta(Archivo carpeta, String nombre){
        List<Archivo> archivos = carpeta.contenido;
        int cantidadArchivos = archivos.size();
        Archivo archivoCarpeta;
        try{
            for(int i = 0; i < cantidadArchivos; i++){
                archivoCarpeta = archivos.get(i);
                archivoCarpeta = cargarCarpetaArchivo(
                        archivoCarpeta.bloqueInicial, archivoCarpeta.esCarpeta,
                        carpeta.ubicacion);
                if(archivoCarpeta.nombre.equals(nombre)){
                    return archivoCarpeta;
                }
            }
        }catch(IOException e){
            System.out.println("Error leyendo el disco.");
            return null;
        }return null;
    }
    
    private void comandoLS(String[] elementos){
        int cantidadElementos = elementos.length;
        if(cantidadElementos > 1){
            if(elementos[1].equals("-R")){
                listarContenidoCarpeta(rutaActual, true);
            }else{
                System.out.println("Parámetro de comando no válido.");
            }
        }else{
            listarContenidoCarpeta(rutaActual, false);
        }
    }
    
    private void listarContenidoCarpeta(Archivo carpeta, Boolean recursivo){
        int indiceFinal, indiceActual;
        Archivo carpetaActual = carpeta, carpetaTemp, bloquePadre, archivoTemp;
        List<Integer> carpetasRevisadas = new ArrayList<>();
        List<Integer> indiceCarpeta = new ArrayList<>();
        indiceCarpeta.add(0);
        try{
            while(!carpetasRevisadas.contains(carpeta.bloqueInicial)){
                indiceFinal = indiceCarpeta.size()-1;
                indiceActual = indiceCarpeta.get(indiceFinal);
                indiceCarpeta.set(indiceFinal, indiceActual+1);
                if(indiceActual == carpetaActual.contenido.size()){
                    indiceCarpeta.remove(indiceFinal);
                    carpetasRevisadas.add(carpetaActual.bloqueInicial);
                    carpetaActual = carpetaActual.carpetaContenedora;
                    System.out.println("-- Fin contenido");
                }else if(carpetaActual.contenido.get(indiceActual).esCarpeta){
                    if(recursivo){
                        bloquePadre = carpetaActual;
                        carpetaActual = cargarCarpetaArchivo(
                            carpetaActual.contenido.get(indiceActual).bloqueInicial,
                                true, null);
                        indiceCarpeta.add(0);
                        carpetaActual.asignarCarpetaContenedor(bloquePadre);
                        System.out.println("Carpeta: "+carpetaActual.nombre);
                        System.out.println("-- Contenido carpeta "+carpetaActual.nombre+":");
                    }else{
                        if(indiceActual == 0){
                            System.out.println("-- Contenido carpeta "+carpetaActual.nombre+":");
                        }
                        carpetaTemp = cargarCarpetaArchivo(
                            carpetaActual.contenido.get(indiceActual).bloqueInicial,
                                true, null);
                        System.out.println("Carpeta: "+carpetaTemp.nombre);
                    }
                }else{
                    archivoTemp = cargarCarpetaArchivo(
                            carpetaActual.contenido.get(indiceActual).bloqueInicial,
                            false, null);
                    System.out.println("Archivo: "+archivoTemp.nombre);
                }
            }
        }catch(IOException e){
            System.out.println("Error leyendo el disco.");
        }
    }
    
    /**
     * Escribe una carpeta o archivo en el disco.
     * Si la variable carpetaUsuarios es nulo, es en la carpeta actual, sino,
     * es la carpeta de usuarios (agregando un usuario).
     * @param carpetaArchivoNuevo
     * @param esCarpeta
     * @param carpetaUsuarios
     * @return 
     */
    private Boolean escribirCarpetaArchivo(Archivo carpetaArchivoNuevo,
            Boolean esCarpeta, Archivo carpetaUsuarios){
        String contenidoCarpeta = EstructuraSistemaArchivos.generarContenidoCarpetaArchivo(carpetaArchivoNuevo);
        Bloque bloqueDestino, bloqueActual;
        String bloqueCarpeta, referenciaCarpeta;
        String contenidoReferencia =
                EstructuraSistemaArchivos.generarContenidoReferenciaCarpetaArchivo(carpetaArchivoNuevo, esCarpeta);
        Boolean hayEspacio = false;
        int idBloqueBuscado = (carpetaUsuarios == null)? rutaActual.bloqueInicial // EL bloque con la carpeta actual
                : carpetaUsuarios.bloqueInicial;
        try{
            while(true){
                // Ciclo utilizado para buscar un bloque nuevo, si la referencia no cabe en el actual.
                bloqueActual = ObtenerBloque(idBloqueBuscado);
                referenciaCarpeta = agregarReferenciaCarpeta(bloqueActual, contenidoReferencia);
                if(hayEspacioEnBloque(bloqueActual, referenciaCarpeta)){
                    hayEspacio = true;
                    break;
                }else if(bloqueActual.bloqueSiguiente == -1){
                    idBloqueBuscado = ObtenerBloqueLibre();
                    if(idBloqueBuscado == -1){
                        hayEspacio = false;
                        break;
                    }else{
                        // Si es un bloque nuevo, entonces le actualizo el bloque siguiente al bloque actual.
                        actualizarIdSiguienteBloque(bloqueActual, idBloqueBuscado);
                    }
                }else{
                    idBloqueBuscado = bloqueActual.bloqueSiguiente;
                }
            }
            if(hayEspacio){
                escribirBloque(bloqueActual.id, referenciaCarpeta);// Se escribe la referencia a la carpeta.
                actualizarBloquesLibres();// En el comando mkdir se busca un bloque libre, aquí se actualizan en el archivo
                bloqueDestino = ObtenerBloque(carpetaArchivoNuevo.bloqueInicial);
                bloqueCarpeta = agregarCarpetaCadena(bloqueDestino, contenidoCarpeta);
                escribirBloque(bloqueDestino.id, bloqueCarpeta);// Se escribe el bloque con la carpeta.
                
                // Se recarga la carpeta actual para obtener la refencia nueva.
                if(carpetaUsuarios == null ||
                        carpetaUsuarios.bloqueInicial == rutaActual.bloqueInicial){
                    String carpetaActual = rutaActual.nombre;
                    comandoCD(new String[]{ "", ".."});
                    comandoCD(new String[]{ "", carpetaActual});
                }
                    
                return true;
            }else{
                // Si no hay espacio, se libera el bloque que se iba a utilizar para la carpeta nueva.
                bloquesLibres.set(carpetaArchivoNuevo.bloqueInicial, 0);
                return false;
            }
        }catch(IOException e){
            return false;
        }
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
    private String cambiarContraseniaCadena(Bloque bloque, String usuario,
        Boolean esBloqueNuevo,Usuario user){
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        String cadenaSinUsuario ="";
        boolean enUsuarios = true;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.INICIO_USUARIO) && enUsuarios){
                i += 2;
                linea = lineasBloque[i];
                if(Integer.parseInt(linea) == user.id ){                       
                    bloqueUbicadoContrasenia = bloque.id;
                    cadenaFinal += usuario + "\n";      
                    enUsuarios= false;
                    i += 11;
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
        }
        if(bloqueUbicadoContrasenia != -1 && !yaEntroContrasenia){
            informacionBloqueContrasenia = cadenaSinUsuario; 
            yaEntroContrasenia = true;       
        }
        return cadenaFinal;
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
                    escribirBloque(bloqueDestino.id, bloqueNuevo);
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
                    escribirBloque(bloqueDestino.id, bloqueNuevo);
                   
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
        String contenidoB = "", linea,nombre="",ubicacion = "";
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
     * Agrega la información de una carpeta en el bloque indicado.
     * Se utiliza para darle el formato de carpeta a un bloque.
     * @param bloque
     * @param carpeta
     * @return String
     */
    private String agregarCarpetaCadena(Bloque bloque, String carpeta){
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.FINAL_INFORMACION)){
                cadenaFinal += carpeta + "\n";
            }
            cadenaFinal += linea + "\n";
        }return cadenaFinal;
    }
    
    /**
     * Agrega la referencia de una carpeta en el bloque indicado.
     * Se utiliza para "agregar" una carpeta a una carpeta en un bloque.
     * @param bloque
     * @param carpeta
     * @return String
     */
    private String agregarReferenciaCarpeta(Bloque bloque, String carpeta){
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length;
        String cadenaFinal = "", linea;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.FINAL_INFORMACION)){
                cadenaFinal += carpeta + "\n";
            }
            cadenaFinal += linea + "\n";
        }return cadenaFinal;
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
                cadenaFinal += linea +  "\n";
            }
        }escribirBloque(bloque.id, cadenaFinal);
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
                cadenaFinal += linea +  "\n";
            }
        }escribirBloque(bloque.id, cadenaFinal);
    }
    
    private void escribirBloque(int id, String contenido) throws FileNotFoundException, IOException{
        RandomAccessFile archivo = new RandomAccessFile(nombreDisco, "rw");
        archivo.seek(tamanioBloque * id);
        archivo.writeBytes(contenido);
        archivo.close();
    }
    
    private Archivo cargarCarpetaArchivo(int numeroBloque, Boolean esCarpeta, String rutaAnterior) throws IOException{
        Archivo carpetaArchivo;
        Bloque bloqueCarpeta;
        String[] lineasBloque;
        int bloqueBuscado = numeroBloque;
        int cantidadLineas;
        int idUsuario, idGrupoUsuario;
        int idArchivoCarpeta;
        int bloqueS = 1;
        String linea, nombre, ubicacion, permisos, fechaC, fechaM;
        List<Archivo> archivos = new ArrayList<>();
        // Se obtiene la información de la carpeta o archivo.
        bloqueCarpeta = ObtenerBloque(bloqueBuscado);
        lineasBloque = bloqueCarpeta.contenido.split("\n");
        cantidadLineas = lineasBloque.length;
        bloqueS = bloqueCarpeta.bloqueSiguiente;
        int bloqueSD= bloqueCarpeta.bloqueSiguiente;
        int i = 9; // A partir de esta línea, empieza la info de la carpeta.
        nombre = lineasBloque[i];
        i += 3;
        ubicacion = lineasBloque[i];
        i += 3;
        permisos = lineasBloque[i];
        i += 3;
        fechaC = lineasBloque[i];
        i += 3;
        fechaM = lineasBloque[i];
        i += 3;
        idUsuario = Integer.parseInt(lineasBloque[i]);
        i += 3;
        idGrupoUsuario = Integer.parseInt(lineasBloque[i]);
        
        if(esCarpeta){
            while(true){
                for(; i < cantidadLineas; i++){
                    linea = lineasBloque[i];
                    if(linea.equals(EstructuraSistemaArchivos.INICIO_CARPETA)){
                        i++;
                        idArchivoCarpeta = Integer.parseInt(lineasBloque[i]);
                        archivos.add(new Archivo(idArchivoCarpeta, true));
                        //System.out.println("Carpeta, id: "+idArchivoCarpeta);
                        i++;
                    }else if(linea.equals(EstructuraSistemaArchivos.INICIO_ARCHIVO)){
                        i++;
                        idArchivoCarpeta = Integer.parseInt(lineasBloque[i]);
                        archivos.add(new Archivo(idArchivoCarpeta, false));
                        //System.out.println("Archivo, id: "+idArchivoCarpeta);
                        i++;
                    }
                }
                if(bloqueS != -1){
                    bloqueCarpeta = ObtenerBloque(bloqueS);
                    lineasBloque = bloqueCarpeta.contenido.split("\n");
                    cantidadLineas = lineasBloque.length;
                    i = 0;
                    bloqueS = bloqueCarpeta.bloqueSiguiente;
                }else{break;}
            
            }
            
        }else{
            
        }
        
        carpetaArchivo = new Archivo(0, 0, nombre, (rutaAnterior!=null)?rutaAnterior+nombre+"/" : "/",
                permisos, fechaC, fechaM, usuarios.get(idUsuario),
                gruposUsuarios.get(idGrupoUsuario), numeroBloque,
                esCarpeta, archivos,bloqueSD);
        
        return carpetaArchivo;
    }
    
    /**
     * Genera el contenido base del sistema de archivos.
     * @return String
     */
    private String generarContenido(){
        cantidadBloques = (tamanioDisco * 1024) / 512;
        tamanioBloque = 512 * 1024;
        String cBloquesLibres = "1,1,1,1";
        for(int i = 4; i < cantidadBloques; i++){
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
