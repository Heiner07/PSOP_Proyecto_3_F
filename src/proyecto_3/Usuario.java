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
    String nombreCompleto;
    String contrasenia;

    /**
     * 
     * @param id
     * @param nombreCompleto
     * @param nombre
     * @param contasenia 
     */
    public Usuario(int id, String nombreCompleto, String nombre, String contasenia) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.nombre = nombre;
        this.contrasenia = contasenia;
    }
}
