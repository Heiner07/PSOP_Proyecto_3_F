/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.util.Date;
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
    Usuario propietario;
    GrupoUsuarios grupoUsuarios;
    int bloqueInicial;
    int idbloquePadre;
    Boolean esCarpeta;
    Boolean estaAbierto;
    Archivo carpetaContenedora;
    List<Archivo> contenido;

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
     * @param estaAbierto 
     * @param contenido 
     */
    public Archivo(int id, int tamanio, String nombre, String ubicacion,
            String permisos, String fechaCreacion, String ultimaModificacion,
            Usuario propietario, GrupoUsuarios grupoUsuarios, int bloque,
            Boolean estaAbierto, List<Archivo> contenido) {
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
        this.estaAbierto = estaAbierto;
        this.contenido = contenido;
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
    }
    
    public Archivo(int bloque, Boolean esCarpeta){
        this.bloqueInicial = bloque;
        this.esCarpeta = esCarpeta;
    }
    
    /*public void asignarCarpetaContenedor(int carpetaContenedor){
        this.idbloquePadre = carpetaContenedor;
    }*/
    public void asignarCarpetaContenedor(Archivo carpetaContenedor){
        this.carpetaContenedora = carpetaContenedor;
    }
}
