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
                System.out.print(usuarioActual.nombre+"@miFS: ");
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
        rutaActual = cargarCarpetaArchivo(1, true); // El bloque 1 contiene la carpeta raíz por cargar.
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
                // llamado al método
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
    
    private boolean esArchivo(String nombreDocumento){
        try{
            int cont = 0;
            for(char c : nombreDocumento.toCharArray()){
                if('.' == c){
                    cont++;
                    if(cont == 2)return false;
                } 
            }
            return true;
        }catch(Exception e){           
            return false;
        
        }
        
    }
    
    
    
   
    private boolean verificarDocumento(Bloque bloque,String nombreDocumento) throws IOException{
        String[] lineasBloque = bloque.contenido.split("\n");
        int cantidadLineas = lineasBloque.length,idBloqueEliminar=1;
        String linea,contenido = "";
        boolean actualizarArchivo = false;
        for(int i = 0; i < cantidadLineas; i++){
            linea = lineasBloque[i];
            if(linea.equals(EstructuraSistemaArchivos.INICIO_CARPETA) || linea.equals(EstructuraSistemaArchivos.INICIO_ARCHIVO) ){
                String lineaEstructura = linea;
                i++; //Para posicionarme en el id de la carpeta o el archivo
                linea = lineasBloque[i];
                //Verificamos que ese id este ocupado
                int id = Integer.parseInt(linea);
                if(bloquesLibres.get(id) == 1){
                        Bloque bloqueVerificar = ObtenerBloque(id);   
                        String[] lineasBloqueVerificar = bloqueVerificar.contenido.split("\n");
                        //MODIFICAR LA COMPARACION DEL LARGO, LO COMPARO PARA VERIFICAR QUE ES UN ARCHIVO O CARPETA SIN NADA DENTRO
                        //DEBO VERIFICAR LOS PERMISOS QUE TIENE
                        if(bloqueVerificar.nombre.equals(nombreDocumento) && lineasBloqueVerificar.length == 31 && bloqueVerificar.bloqueSiguiente==-1){
                            //Eliminamos el archivo o carpeta
                            bloquesLibres.set(id,0);
                            idBloqueEliminar = id;
                            actualizarBloquesLibres();
                            actualizarArchivo = true;
                            i++;
                        }else{
                            contenido += (lineaEstructura + "\n");
                            contenido += (linea + "\n");
                        }
                }
            }else{
                contenido += (linea + "\n");
            
            }
        }
        if(actualizarArchivo){
            escribirBloque(bloque.id, contenido);
            escribirBloque(idBloqueEliminar, EstructuraSistemaArchivos.generarBloqueLibre(idBloqueEliminar));
            return true;
            
        }
        else{
            return false;
        }

    
    
    
    }
    private void comandoRm(String[] elementos) throws IOException{
        if(elementos.length > 1){
             String nombreDocumento = elementos[1];
             if(esArchivo(nombreDocumento)){
                 Bloque bloque = ObtenerBloque(1);//LA RUTA ACTUAL
                 while(true){
                     if(!verificarDocumento(bloque,nombreDocumento)){
                         if(bloque.bloqueSiguiente != -1){
                            bloque = ObtenerBloque(bloque.bloqueSiguiente);//Accedemos al siguiente bloque a verificar si esta ahi                        
                         }else{
                            System.out.println("No se encontró "+nombreDocumento);
                            break;
                         }
                     }else{
                        System.out.println("Se eliminó correctamente "+nombreDocumento);
                        break;
                     }
                 
                 }
             }else{
                
             
             
             }
            
        
        
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
                // Comprobar si no existe otra carpeta con el mismo nombre.
                bloqueLibre = ObtenerBloqueLibre();
                if(bloqueLibre != -1){
                    carpetaNueva = new Archivo(0, 0, nombreCarpeta,
                            rutaActual.ubicacion + nombreCarpeta + "/",
                            "PERMISOS", rutaActual.propietario,
                            rutaActual.grupoUsuarios, bloqueLibre);
                    if(escribirCarpetaArchivo(carpetaNueva, true)){
                        System.out.println("¡Carpeta creada!");
                    }else{
                        System.out.println("Error al crear la carpeta.");
                    }
                }else{
                    System.out.println("Error no hay espacio.");
                }
            }
        }else{
            System.out.println("Especifique un nombre de carpeta");
        }
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
                    carpeta = cargarCarpetaArchivo(carpetaTem.bloqueInicial, true);
                    if(carpeta.nombre.equals(nombre)){
                        carpeta.asignarCarpetaContenedor(rutaActual);
                        rutaActual = carpeta;
                        System.out.println("Dentro de la carpeta "+nombre);
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
                // Comprobar si no existe otra carpeta con el mismo nombre.
                bloqueLibre = ObtenerBloqueLibre();
                if(bloqueLibre != -1){
                    archivoNuevo = new Archivo(0, 0, nombreArchivo,
                            rutaActual.ubicacion + nombreArchivo,
                            "PERMISOS", rutaActual.propietario,
                            rutaActual.grupoUsuarios, bloqueLibre);
                    if(escribirCarpetaArchivo(archivoNuevo, false)){
                        System.out.println("¡Archivo creado!");
                    }else{
                        System.out.println("Error al crear el archivo.");
                    }
                }else{
                    System.out.println("Error no hay espacio.");
                }
            }
        }else{
            System.out.println("Especifique un nombre de archivo");
        }
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
                            carpetaActual.contenido.get(indiceActual).bloqueInicial, true);
                        indiceCarpeta.add(0);
                        carpetaActual.asignarCarpetaContenedor(bloquePadre);
                        System.out.println("Carpeta: "+carpetaActual.nombre);
                        System.out.println("-- Contenido carpeta "+carpetaActual.nombre+":");
                    }else{
                        if(indiceActual == 0){
                            System.out.println("-- Contenido carpeta "+carpetaActual.nombre+":");
                        }
                        carpetaTemp = cargarCarpetaArchivo(
                            carpetaActual.contenido.get(indiceActual).bloqueInicial, true);
                        System.out.println("Carpeta: "+carpetaTemp.nombre);
                    }
                }else{
                    archivoTemp = cargarCarpetaArchivo(
                            carpetaActual.contenido.get(indiceActual).bloqueInicial, false);
                    System.out.println("Archivo: "+archivoTemp.nombre);
                }
            }
        }catch(IOException e){
            System.out.println("Error leyendo el disco.");
        }
    }
    
    private Boolean escribirCarpetaArchivo(Archivo carpetaArchivoNuevo, Boolean esCarpeta){
        String contenidoCarpeta = EstructuraSistemaArchivos.generarContenidoCarpetaArchivo(carpetaArchivoNuevo);
        Bloque bloqueDestino, bloqueActual;
        String bloqueCarpeta, referenciaCarpeta;
        String contenidoReferencia =
                EstructuraSistemaArchivos.generarContenidoReferenciaCarpetaArchivo(carpetaArchivoNuevo, esCarpeta);
        Boolean hayEspacio = false;
        int idBloqueBuscado = rutaActual.bloqueInicial; // EL bloque con la carpeta actual
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
                rutaActual = cargarCarpetaArchivo(rutaActual.bloqueInicial, true);
                    
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
        String contenidoB = "", linea,nombre="";
        Boolean leyendoBloque = false, leyendoBloqueS = false;
        RandomAccessFile archivo = new RandomAccessFile(nombreDisco, "r");
        archivo.seek(tamanioBloque * numeroBloque);
        while((linea = archivo.readLine()) != null){
            if(leyendoBloqueS){
                leyendoBloqueS = false;
                bloqueS = Integer.parseInt(linea);
            }
            if(linea.equals(EstructuraSistemaArchivos.INICIO_NOMBRE)){     
                contenidoB += (linea + "\n");
                linea = archivo.readLine();
                nombre = linea;
            }else if(linea.equals(EstructuraSistemaArchivos.INICIO_BLOQUE)){                
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
        return new Bloque(numeroBloque, bloqueS, contenidoB,nombre);
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
    
    private Archivo cargarCarpetaArchivo(int numeroBloque, Boolean esCarpeta) throws IOException{
        Archivo carpetaArchivo;
        Bloque bloqueCarpeta;
        String[] lineasBloque;
        int bloqueBuscado = numeroBloque;
        int cantidadLineas;
        int idUsuario, idGrupoUsuario;
        int idArchivoCarpeta;
        String linea, nombre, ubicacion, permisos, fechaC, fechaM;
        List<Archivo> archivos = new ArrayList<>();
        // Se obtiene la información de la carpeta o archivo.
        bloqueCarpeta = ObtenerBloque(bloqueBuscado);
        lineasBloque = bloqueCarpeta.contenido.split("\n");
        cantidadLineas = lineasBloque.length;
        int i = 9; // A partir de esta línea, empieza la info de la carpeta o archivo.
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
        }else{
            
        }
        
        carpetaArchivo = new Archivo(0, 0, nombre, ubicacion, permisos, fechaC, fechaM,
                usuarios.get(idUsuario), gruposUsuarios.get(idGrupoUsuario), numeroBloque,
                esCarpeta, archivos);
        
        return carpetaArchivo;
    }
    
    /**
     * Genera el contenido base del sistema de archivos.
     * @return String
     */
    private String generarContenido(){
        cantidadBloques = (tamanioDisco * 1024) / 512;
        tamanioBloque = 512 * 1024;
        String cBloquesLibres = "1,1,1";
        for(int i = 3; i < cantidadBloques; i++){
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
