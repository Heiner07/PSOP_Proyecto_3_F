/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Heiner
 */
public class Archivo {
    
    int id;
    int tamanio;
    String nombre;
    String ubicacion;
    String permisos;
    String fechaCreacion;
    String ultimaModificacion;
    String textoArchivo;
    Usuario propietario;
    GrupoUsuarios grupoUsuarios;
    int bloqueInicial;
    int idbloquePadre;
    Boolean esVinculo;
    Boolean esCarpeta;
    Boolean estaAbierto;
    Archivo carpetaContenedora;
    List<Archivo> contenido;
    int bloqueS;

    /**
     * 
     * @param id
     * @param tamanio
     * @param nombre
     * @param ubicacion
     * @param permisos
     * @param fechaCreacion
     * @param ultimaModificacion
     * @param propietario
     * @param grupoUsuarios 
     * @param bloque 
     * @param esCarpeta 
     * @param contenido 
     * @param bloqueS 
     */
    public Archivo(int id, int tamanio, String nombre, String ubicacion,
            String permisos, String fechaCreacion, String ultimaModificacion,
            Usuario propietario, GrupoUsuarios grupoUsuarios, int bloque,
            Boolean esCarpeta, List<Archivo> contenido,int bloqueS) {
        this.id = id;
        this.tamanio = tamanio;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.permisos = permisos;
        this.fechaCreacion = fechaCreacion;
        this.ultimaModificacion = ultimaModificacion;
        this.propietario = propietario;
        this.grupoUsuarios = grupoUsuarios;
        this.bloqueInicial = bloque;
        this.esCarpeta = esCarpeta;
        this.esVinculo = false;
        this.estaAbierto = false;
        this.contenido = contenido;
        this.carpetaContenedora = null;
        this.bloqueS = bloqueS;
    }
    
    /**
     * 
     * @param id
     * @param tamanio
     * @param nombre
     * @param ubicacion
     * @param permisos
     * @param fechaCreacion
     * @param ultimaModificacion
     * @param propietario
     * @param grupoUsuarios 
     * @param bloque 
     * @param esCarpeta 
     * @param contenido 
     * @param bloqueS 
     */
    public Archivo(int id, int tamanio, String nombre, String ubicacion,
            String permisos, String fechaCreacion, String ultimaModificacion,
            Usuario propietario, GrupoUsuarios grupoUsuarios, int bloque,
            Boolean esCarpeta, String contenido,int bloqueS) {
        this.id = id;
        this.tamanio = tamanio;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.permisos = permisos;
        this.fechaCreacion = fechaCreacion;
        this.ultimaModificacion = ultimaModificacion;
        this.propietario = propietario;
        this.grupoUsuarios = grupoUsuarios;
        this.bloqueInicial = bloque;
        this.esCarpeta = esCarpeta;
        this.esVinculo = false;
        this.estaAbierto = false;
        this.contenido = null;
        this.textoArchivo = contenido;
        this.carpetaContenedora = null;
        this.bloqueS = bloqueS;
    }
    
    public Archivo(int id, int tamanio, String nombre, String ubicacion,
            String permisos, Usuario propietario, GrupoUsuarios grupoUsuarios, int bloque) {
        this.id = id;
        this.tamanio = tamanio;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.permisos = permisos;
        this.propietario = propietario;
        this.grupoUsuarios = grupoUsuarios;
        this.bloqueInicial = bloque;
        this.carpetaContenedora = null;
    }
    
    public Archivo(int bloque, Boolean esCarpeta){
        this.bloqueInicial = bloque;
        this.esCarpeta = esCarpeta;
        this.esVinculo = false;
    }
    
    public Archivo(int bloque, String nombre, Boolean esVinculo, Archivo padre, String ubicacion){
        this.bloqueInicial = bloque;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.esCarpeta = false;
        this.esVinculo = esVinculo;
        this.carpetaContenedora = padre;
    }
    
    public void asignarCarpetaContenedor(Archivo carpetaContenedor){
        this.carpetaContenedora = carpetaContenedor;
    }
}
