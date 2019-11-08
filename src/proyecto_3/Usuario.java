/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_3;

/**
 *
 * @author Heiner
 */
public class Usuario {
    
    int id;
    String nombre;
    String contasenia;

    /**
     * 
     * @param id
     * @param nombre
     * @param contasenia 
     */
    public Usuario(int id, String nombre, String contasenia) {
        this.id = id;
        this.nombre = nombre;
        this.contasenia = contasenia;
    }
}
