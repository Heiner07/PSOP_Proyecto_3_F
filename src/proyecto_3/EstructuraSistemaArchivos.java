/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.util.Calendar;

/**
 *
 * @author Heiner
 */
public class EstructuraSistemaArchivos {
    public static final String INICIO_BLOQUE            = "[B]";
    public static final String FINAL_BLOQUE             = "[/B]";
    public static final String INICIO_BLOQUE_SIGUIENTE  = "[BS]";
    public static final String FINAL_BLOQUE_SIGUIENTE   = "[/BS]";
    public static final String INICIO_INFORMACION       = "[I]";
    public static final String FINAL_INFORMACION        = "[/I]";
    public static final String INICIO_ID                = "[Id]";
    public static final String FINAL_ID                 = "[/Id]";
    public static final String INICIO_TAMANIO           = "[1]";
    public static final String FINAL_TAMANIO            = "[/1]";
    public static final String INICIO_NUMERO_BLOQUES    = "[2]";
    public static final String FINAL_NUMERO_BLOQUES     = "[/2]";
    public static final String INICIO_BLOQUES_LIBRES    = "[3]";
    public static final String FINAL_BLOQUES_LIBRES     = "[/3]";
    public static final String INICIO_BLOQUE_USUARIOS   = "[4]";
    public static final String FINAL_BLOQUE_USUARIOS    = "[/4]";
    public static final String INICIO_BLOQUE_G_USUARIOS = "[5]";
    public static final String FINAL_BLOQUE_G_USUARIOS  = "[/5]";
    public static final String INICIO_USUARIO           = "[U]";
    public static final String FINAL_USUARIO            = "[/U]";
    public static final String INICIO_G_USUARIO         = "[GU]";
    public static final String FINAL_G_USUARIO          = "[/GU]";
    public static final String INICIO_N_COMPLETO        = "[NC]";
    public static final String FINAL_N_COMPLETO         = "[/NC]";
    public static final String INICIO_NOMBRE            = "[N]";
    public static final String FINAL_NOMBRE             = "[/N]";
    public static final String INICIO_CONTRASENIA       = "[Con]";
    public static final String FINAL_CONTRASENIA        = "[/Con]";
    public static final String INICIO_UBICACION         = "[UB]";
    public static final String FINAL_UBICACION          = "[/UB]";
    public static final String INICIO_PERMISOS          = "[PE]";
    public static final String FINAL_PERMISOS           = "[/PE]";
    public static final String INICIO_FECHA_C           = "[FC]";
    public static final String FINAL_FECHA_C            = "[/FC]";
    public static final String INICIO_FECHA_M           = "[FM]";
    public static final String FINAL_FECHA_M            = "[/FM]";
    public static final String INICIO_CARPETA           = "[C]";
    public static final String FINAL_CARPETA            = "[/C]";
    public static final String INICIO_ARCHIVO           = "[A]";
    public static final String FINAL_ARCHIVO            = "[/A]";
    public static final String CARACTER_RELLENO         = ".";
    
    /**
     * Genera el contenido base del sistema de archivos.
     * @param tamanioDisco
     * @param cantidadBloques
     * @param tamanioBloque
     * @param cBloquesLibres
     * @param nombreCompleto
     * @param contrasenia
     * @return String
     */
    public static String obtenerContenidoInicial(int tamanioDisco,
            int cantidadBloques, int tamanioBloque, String cBloquesLibres,
            String nombreCompleto, String contrasenia){
        Calendar fecha = Calendar.getInstance();
        String fechaActual = fecha.get(Calendar.DATE)
                + "/" + fecha.get(Calendar.MONTH)
                + "/" + fecha.get(Calendar.YEAR);
        String bloquesDisco
                = INICIO_BLOQUE                                             +"\n"
                // Se define el bloque siguiente
                + INICIO_BLOQUE_SIGUIENTE+"\n-1\n"+FINAL_BLOQUE_SIGUIENTE   +"\n"
                // Se define el inicio de la información
                + INICIO_INFORMACION                                        +"\n"
                // Se define el Id del bloque
                + INICIO_ID+"\n0\n"+FINAL_ID                                +"\n"
                // Se define el tamaño del disco (info del sistema de archivos)
                + INICIO_TAMANIO+"\n"+tamanioDisco+"\n"+FINAL_TAMANIO       +"\n"
                // Se define la cantidad de bloques y tamaño de cada bloque
                + INICIO_NUMERO_BLOQUES+"\n"+cantidadBloques                +"\n"
                + tamanioBloque+"\n"+FINAL_NUMERO_BLOQUES                   +"\n"
                // Se definen los bloques libres
                + INICIO_BLOQUES_LIBRES+"\n"+cBloquesLibres                 +"\n"
                + FINAL_BLOQUES_LIBRES                                      +"\n"
                // Se define el bloque de usuarios
                + INICIO_BLOQUE_USUARIOS                                    +"\n"
                // Se define el usuario root
                + INICIO_USUARIO                                            +"\n"
                + INICIO_ID+"\n0\n"+FINAL_ID                                +"\n"
                + INICIO_N_COMPLETO+"\n"+nombreCompleto+"\n"+FINAL_N_COMPLETO+"\n"
                + INICIO_NOMBRE+"\nroot\n"+FINAL_NOMBRE                     +"\n"
                + INICIO_CONTRASENIA+"\n"+contrasenia+"\n"+FINAL_CONTRASENIA+"\n"
                + FINAL_USUARIO                                             +"\n"
                // Se cierra el bloque de usuarios
                + FINAL_BLOQUE_USUARIOS                                     +"\n"
                // Se define el bloque de grupos de usuarios
                + INICIO_BLOQUE_G_USUARIOS                                  +"\n"
                // Se define el grupo root
                + INICIO_G_USUARIO                                          +"\n"
                // Se define el usuario (root) que pertenece a este grupo
                + INICIO_ID+"\n0\n"+FINAL_ID                                +"\n"
                + INICIO_NOMBRE+"\nGrupoRoot\n"+FINAL_NOMBRE                +"\n"
                + INICIO_USUARIO                                            +"\n"
                + INICIO_ID+"\n0\n"+FINAL_ID                                +"\n"
                + FINAL_USUARIO                                             +"\n"
                + FINAL_G_USUARIO                                           +"\n"
                // Se cierra el bloque de usuarios
                + FINAL_BLOQUE_G_USUARIOS                                   +"\n"
                // Se cierra el bloque de información
                + FINAL_INFORMACION                                         +"\n"
                // Se cierra el bloque
                + FINAL_BLOQUE                                              +"\n";
        // Se crea el bloque con la carpeta raíz
        bloquesDisco
                += INICIO_BLOQUE                    + "\n"
                + INICIO_BLOQUE_SIGUIENTE+"\n-1\n"+FINAL_BLOQUE_SIGUIENTE+"\n"
                + INICIO_INFORMACION                + "\n"
                + INICIO_ID+"\n"+1+"\n"+FINAL_ID    + "\n"
                + INICIO_NOMBRE+"\nraiz\n"+FINAL_NOMBRE+"\n"
                + INICIO_UBICACION+"\n/\n"+FINAL_UBICACION+"\n"
                + INICIO_PERMISOS+"\nPERMISOS\n"+FINAL_PERMISOS+"\n"
                + INICIO_FECHA_C+"\n"+fechaActual+"\n"+FINAL_FECHA_C+"\n"
                + INICIO_FECHA_M+"\n"+fechaActual+"\n"+FINAL_FECHA_M+"\n"
                + INICIO_USUARIO+"\n"+0+"\n"+FINAL_USUARIO+"\n"
                + INICIO_G_USUARIO+"\n"+0+"\n"+FINAL_G_USUARIO+"\n"
                + INICIO_CARPETA    + "\n"
                + "2\n"
                + FINAL_CARPETA     + "\n"
                + INICIO_CARPETA    + "\n"
                + "3\n"
                + FINAL_CARPETA     + "\n"
                + FINAL_INFORMACION                 + "\n"
                + FINAL_BLOQUE                      + "\n";
        // Se crea el bloque con la carpeta root
        bloquesDisco
                += INICIO_BLOQUE                    + "\n"
                + INICIO_BLOQUE_SIGUIENTE+"\n-1\n"+FINAL_BLOQUE_SIGUIENTE+"\n"
                + INICIO_INFORMACION                + "\n"
                + INICIO_ID+"\n"+2+"\n"+FINAL_ID    + "\n"
                + INICIO_NOMBRE+"\nroot\n"+FINAL_NOMBRE+"\n"
                + INICIO_UBICACION+"\n/root/\n"+FINAL_UBICACION+"\n"
                + INICIO_PERMISOS+"\nPERMISOS\n"+FINAL_PERMISOS+"\n"
                + INICIO_FECHA_C+"\n"+fechaActual+"\n"+FINAL_FECHA_C+"\n"
                + INICIO_FECHA_M+"\n"+fechaActual+"\n"+FINAL_FECHA_M+"\n"
                + INICIO_USUARIO+"\n"+0+"\n"+FINAL_USUARIO+"\n"
                + INICIO_G_USUARIO+"\n"+0+"\n"+FINAL_G_USUARIO+"\n"
                + FINAL_INFORMACION                 + "\n"
                + FINAL_BLOQUE                      + "\n";
        // Se crea el bloque con la carpeta users
        bloquesDisco
                += INICIO_BLOQUE                    + "\n"
                + INICIO_BLOQUE_SIGUIENTE+"\n-1\n"+FINAL_BLOQUE_SIGUIENTE+"\n"
                + INICIO_INFORMACION                + "\n"
                + INICIO_ID+"\n"+3+"\n"+FINAL_ID    + "\n"
                + INICIO_NOMBRE+"\nusers\n"+FINAL_NOMBRE+"\n"
                + INICIO_UBICACION+"\n/users/\n"+FINAL_UBICACION+"\n"
                + INICIO_PERMISOS+"\nPERMISOS\n"+FINAL_PERMISOS+"\n"
                + INICIO_FECHA_C+"\n"+fechaActual+"\n"+FINAL_FECHA_C+"\n"
                + INICIO_FECHA_M+"\n"+fechaActual+"\n"+FINAL_FECHA_M+"\n"
                + INICIO_USUARIO+"\n"+0+"\n"+FINAL_USUARIO+"\n"
                + INICIO_G_USUARIO+"\n"+0+"\n"+FINAL_G_USUARIO+"\n"
                + FINAL_INFORMACION                 + "\n"
                + FINAL_BLOQUE                      + "\n";
        // Se crean el resto de bloques (libres)
        for(int i = 4; i < cantidadBloques; i++){
            bloquesDisco += generarBloqueLibre(i);
        }
        return bloquesDisco;
    }
    public static String generarBloqueLibre(int i){
        String bloque= "";
        return bloque
                    += INICIO_BLOQUE                    + "\n"
                    + INICIO_BLOQUE_SIGUIENTE+"\n-1\n"+FINAL_BLOQUE_SIGUIENTE+"\n"
                    + INICIO_INFORMACION                + "\n"
                    + INICIO_ID+"\n"+i+"\n"+FINAL_ID    + "\n"
                    + FINAL_INFORMACION                 + "\n"
                    + FINAL_BLOQUE                      + "\n";
    }
    
    public static String generarContenidoCarpetaArchivo(Archivo archivo){
        Calendar fecha = Calendar.getInstance();
        String fechaActual = fecha.get(Calendar.DATE)
                + "/" + fecha.get(Calendar.MONTH)
                + "/" + fecha.get(Calendar.YEAR);
        String carpetaArchivo
                // Se define el nombre
                = INICIO_NOMBRE+"\n"+archivo.nombre+"\n"+FINAL_NOMBRE+"\n"
                // Se define la ubicación
                + INICIO_UBICACION+"\n"+archivo.ubicacion+"\n"+FINAL_UBICACION+"\n"
                // Se definen los permisos
                + INICIO_PERMISOS+"\nPERMISOS\n"+FINAL_PERMISOS+"\n"
                // Se establece la fecha de creación
                + INICIO_FECHA_C+"\n"+fechaActual+"\n"+FINAL_FECHA_C+"\n"
                // Se establece la fecha de modificación
                + INICIO_FECHA_M+"\n"+fechaActual+"\n"+FINAL_FECHA_M+"\n"
                // Se establece el usuario propietario.
                + INICIO_USUARIO+"\n"+archivo.propietario.id+"\n"+FINAL_USUARIO+"\n"
                // Se establece el grupo de usuario del archivo
                + INICIO_G_USUARIO+"\n"+archivo.grupoUsuarios.id+"\n"+FINAL_G_USUARIO;
        return carpetaArchivo;
    }
    
    public static String generarContenidoReferenciaCarpetaArchivo(Archivo archivo, Boolean esCarpeta){
        String referencia
                = ((esCarpeta)? INICIO_CARPETA : INICIO_ARCHIVO)    + "\n"
                + archivo.bloqueInicial        + "\n"
                + ((esCarpeta)? FINAL_CARPETA : FINAL_ARCHIVO);
        return referencia;
    }
    
    /**
     * Genera el string, del usuario nuevo, utilizado para agregar un usuario...
     * ...al archivo (Disco)
     * @param usuario
     * @return String
     */
    public static String generarContenidoUsuario(Usuario usuario){
        String cUsuario
                = INICIO_USUARIO        + "\n"
                // Se define el Id
                + INICIO_ID             + "\n"
                + usuario.id            + "\n"
                + FINAL_ID              + "\n"
                // Se define el nombre completo
                + INICIO_N_COMPLETO     + "\n"
                + usuario.nombreCompleto+ "\n"
                + FINAL_N_COMPLETO      + "\n"
                // Se define el nombre de usuario
                + INICIO_NOMBRE         + "\n"
                + usuario.nombre        + "\n"
                + FINAL_NOMBRE          + "\n"
                // Se define la contraseña
                + INICIO_CONTRASENIA    + "\n"
                + usuario.contrasenia   + "\n"
                + FINAL_CONTRASENIA     + "\n"
                // Se cierra el Usuario
                + FINAL_USUARIO;
        return cUsuario;
    }
    
    /**
     * Genera el string, del grupo usuario nuevo, utilizado para agregar un
     * grupo usuario al archivo (Disco).
     * @param grupoUsuario
     * @return String
     */
    public static String generarContenidoGrupoUsuario(GrupoUsuarios grupoUsuario){
        String cUsuario
                = INICIO_G_USUARIO      + "\n"
                // Se define el Id
                + INICIO_ID             + "\n"
                + grupoUsuario.id       + "\n"
                + FINAL_ID              + "\n"
                // Se define el nombre de grupo
                + INICIO_NOMBRE         + "\n"
                + grupoUsuario.nombre   + "\n"
                + FINAL_NOMBRE          + "\n"
                // Se cierra el Usuario
                + FINAL_G_USUARIO;
        return cUsuario;
    }
}
