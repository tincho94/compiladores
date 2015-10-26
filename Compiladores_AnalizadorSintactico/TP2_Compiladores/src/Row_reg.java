/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Martin
 */
class Row_reg {
    int idt;
    String lex;
    String c_l;
    

    public Row_reg(String c_l, String lex, int idt) {
        this.c_l = c_l;
        this.idt = idt;
        this.lex = lex;
    }

    @Override
    public String toString() {
        return "" + c_l + "  ";
    }
}
