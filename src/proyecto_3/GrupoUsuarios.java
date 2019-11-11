/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

import java.util.List;

/**
 *
 * @author Heiner
 */
public class GrupoUsuarios {
    
    int id;
    String nombre;
    List<Integer> usuariosId;

    /**
     * 
     * @param id
     * @param nombre
     * @param usuarios 
     */
    public GrupoUsuarios(int id, String nombre, List<Integer> usuariosId) {
        this.id = id;
        this.nombre = nombre;
        this.usuariosId = usuariosId;
    }
}
