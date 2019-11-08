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
public class Bloque {
    
    int id;
    int tamanio;
    int tamanioUsado;
    Boolean estaLibre;

    /**
     * 
     * @param id
     * @param tamanio
     * @param tamanioUsado
     * @param estaLibre 
     */
    public Bloque(int id, int tamanio, int tamanioUsado, Boolean estaLibre) {
        this.id = id;
        this.tamanio = tamanio;
        this.tamanioUsado = tamanioUsado;
        this.estaLibre = estaLibre;
    }
}
