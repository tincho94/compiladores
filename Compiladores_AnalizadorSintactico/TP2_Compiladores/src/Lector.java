
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Martin
 */
class Lector extends BufferedReader {
    
    public Lector(Reader rdr) {
        super(rdr);
    }

    public char obtenerToken() throws IOException {
        mark(1);
        return ((char) this.read());
    }

    public void devolverToken() throws IOException {
        reset();
    }
}
