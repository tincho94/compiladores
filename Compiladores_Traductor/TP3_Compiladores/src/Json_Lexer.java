
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import static java.lang.Character.isDigit;
import java.util.LinkedList;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Martin
 */
public class Json_Lexer {
    
    /* Terminales Json */
    public static final int L_CORCHETE = 1;
    public static final int R_CORCHETE = 2;
    public static final int L_LLAVE = 3;
    public static final int R_LLAVE = 4; 
    public static final int COMA = 5;
    public static final int DOS_PUNTOS = 6; 
    public static final int LITERAL_CADENA = 7;
    public static final int LITERAL_NUM = 8;
    public static final int PR_TRUE = 9; //palabra reservada
    public static final int PR_FALSE = 10; //palabra reservada
    public static final int PR_NULL = 11; //palabra reservada
    public static final int EOF = 12;
    public static final int ERROR = -1;
    
    
    public static Lector archivo;
    public static String literal ="";
    public static Row_reg token = null;
    public static int linea = 1;
    public static Row_reg[] palabra_reservada = new Row_reg[3];  //3 palabras
    public static LinkedList<Row_reg> lista =  new LinkedList<Row_reg>();
    
    
    public static FileWriter f_writer = null;
    public static PrintWriter p_writer = null;
    public static FileReader f_reader = null;
    public static File camino_archivo= null;
     
    public static void main(String[] args) {
        cargar_palabras_reservadas();
        Scanner input = new Scanner(System.in);
        do {
            System.out.println("Por Favor, ingresa la ruta del fuente: ");
            camino_archivo = new File (input.nextLine());
            
        }while(!camino_archivo.canRead());
        
        try {
            f_reader = new FileReader (camino_archivo);
            archivo = new Lector(f_reader);
            do{
                siguiente_lexema();  
                
            }while(!("EOF".equals(lista.get(lista.size()-1).c_l)));
            f_writer = new FileWriter (camino_archivo.getParent()+"/output.txt");
            p_writer = new PrintWriter(f_writer);
            for (Row_reg r : lista){
                p_writer.print(r.lex+"  ");
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try {
                if(f_writer != null)
                    f_writer.close();
                if(f_reader != null)
                    f_reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } 
    }
 
    public static void cargar_palabras_reservadas(){
        palabra_reservada[0] = new Row_reg("PR_TRUE", "TRUE", 9);
        palabra_reservada[1] = new Row_reg("PR_FALSE", "FALSE", 10);
        palabra_reservada[2] = new Row_reg("PR_NULL", "NULL", 11);
    }
      
    public static LinkedList iniciar_lexer(){
        Scanner ingreso = new Scanner(System.in);
        FileReader reader = null;
        
        do{
            System.out.println("Ingrese La ruta del archivo: ");
            camino_archivo = new File(ingreso.nextLine());
        }while(!camino_archivo.canRead());
        
       
        try{
            reader = new FileReader (camino_archivo);
            archivo = new Lector(reader);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return lista;
    }
    
    public static void cerrar_archivo(){
        try{
            if(f_writer != null){
                f_writer.close();
            }
            if(f_reader != null){
                f_reader.close();
            }
            if(archivo != null){
                archivo.close();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public static void mensaje_error(String msg){
        System.out.println(String.format("Linea %-4d"+" "+msg,linea));
        token = new Row_reg("ERROR","ERROR", -1);
    }


    public static void siguiente_lexema() throws IOException{
        char caracter;
        while ((caracter = archivo.obtenerToken()) != (char)-1){
            literal = "";
            if(caracter == ' '|| caracter == '\t' || caracter == '\r'){
                continue;
            }else 
            if(caracter == '\n'){
                linea++;
                continue;
            }else if(caracter == '"'){//empieza reconocimiento String
                int estado = 1 ;
                boolean acepto = false;
                literal = literal + caracter;
                while(!acepto){
                    switch(estado){
                        case(1):
                            caracter = archivo.obtenerToken();
                            if(caracter == '\\'){
                                literal = literal + caracter;
                                estado = 2;
                            }else if(caracter == '"'){
                                literal = literal + caracter;
                                estado = 3;
                            }else if(caracter == '\n'){
                                estado = -1;
                            }else if(caracter == (char)-1){
                                estado = -1;
                            }else{
                                literal = literal + caracter ;
                                estado = 1;
                            }
                            break;
                        case(2):
                            caracter = archivo.obtenerToken();
                            if(caracter == '"'){
                                literal = literal + caracter;
                                estado = 1;
                            }else if(caracter == 'n'){
                                literal = literal + caracter;
                                estado = 1;
                            }else if(caracter == 't'){
                                literal += caracter;
                                estado = 1;
                            }else if (caracter == 'f'){
                                literal += caracter;
                                estado = 1;
                            }else if (caracter == 'b'){
                                literal += caracter;
                                estado = 1;
                            }else if (caracter == 'r'){
                                literal += caracter;
                                estado = 1;
                            }else if (caracter == '\\'){
                                literal += caracter ;
                                estado = 1;
                            }else if (caracter == '/'){
                                literal += caracter;
                                estado = 1;
                            }else if (caracter == 'u'){
                                literal += caracter;
                                estado = 1;
                            }else
                                estado = -2;
                                break;
                        case(3):
                            acepto = true;
                            lista.add(token = new Row_reg("LITERAL_CADENA", literal, LITERAL_CADENA));
                            break;
                        case(-1):
                            if(caracter == '\n'){
                                archivo.devolverToken(); //devuelve el caracter para que no haya perdida de lexemas
                            }
                            mensaje_error("Literal Cadena Incorrecto");
                            return;
                        case(-2):
                            mensaje_error("Caracter de Escape incorrecto encontrado");
                            char c = caracter;
                            while(c!= '\n' && c!= (char)-1){
                                c = archivo.obtenerToken();
                            }
                            archivo.devolverToken();
                            return;
                    }
                }
                break;
            }else if (caracter == ':'){
                lista.add(token = new Row_reg ("DOS_PUNTOS", ":", DOS_PUNTOS));
                break;
            }else if (caracter == '['){
                lista.add(token = new Row_reg ("L_CORCHETE", "[", L_CORCHETE));
                break;
            }else if (caracter == ']'){
                lista.add(token = new Row_reg ("R_CORCHETE", "]", R_CORCHETE));
                break;
            }else if (caracter == '{'){
                lista.add(token = new Row_reg ("L_LLAVE", "{", L_LLAVE));
                break;
            }else if (caracter == '}'){
                lista.add(token = new Row_reg("R_LLAVE", "}", R_LLAVE));
                break;
            }else if (caracter == ','){
                lista.add(token = new Row_reg("COMA", ",", COMA));
                break;
            }else if(Character.isLetter(caracter)){
                do{
                    literal += caracter;
                    caracter = archivo.obtenerToken();
                }while(Character.isLetter(caracter));
                archivo.devolverToken();
                for (Row_reg word : palabra_reservada){
                if(word.lex.equalsIgnoreCase(literal)){
                    lista.add(token = new Row_reg(word.lex, literal, word.idt));
                    return;
                }
            }
            mensaje_error("Lexema no Valido "+ literal);
            return;
            }else if(isDigit(caracter)){ //consulta si es un numero
                int i = 0;
                int estado = 0;
                boolean acepto = false ;
                literal += caracter;
                while(!acepto){
                    switch(estado){
                        case (0): //una secuencia netamente de digitos, puede ocurrir . o e
                            caracter = archivo.obtenerToken();
                            if(isDigit(caracter)){
                                literal += caracter;
                                estado = 0;
                            }else if(caracter == '.'){
                                literal += caracter;
                                estado = 1;
                            }else if(Character.toLowerCase(caracter)== 'e'){
                                literal += caracter;
                                estado = 3;
                            }else{
                                estado = 6;
                            }
                            break;
                        case (1): //punto, debe seguir numero
                            caracter = archivo.obtenerToken();
                            if(isDigit(caracter)){
                                literal += caracter;
                                estado = 2;
                            } else {
                                mensaje_error("no se esperaba "+ literal);
                                estado = -1;
                            }
                            break;
                        case (2): //la fraccion decimal, pueden seguir digitos o e
                            caracter = archivo.obtenerToken();
                            if(isDigit(caracter)){
                                literal += caracter;
                                estado = 2;
                            }else if(Character.toLowerCase(caracter)=='e'){
                                literal += caracter;
                                estado = 3;
                            }else{
                                estado = 6;
                            }
                            break;
                        case (3): //una e, puede seguir +, - o secuencia de digitos
                            caracter = archivo.obtenerToken();
                            if(caracter == '+' || caracter == '-'){
                                literal += caracter;
                                estado = 4;
                            }else if(isDigit(caracter)){
                                literal += caracter;
                                estado = 5;
                            }else{
                                mensaje_error("no se esperaba "+ literal);
                                estado = -1;
                            }
                            break;
                        case (4): //necesariamente debe venir un digito al menos
                            caracter = archivo.obtenerToken();
                            if(isDigit(caracter)){
                                literal += caracter;
                                estado=5;
                            }else{
                                mensaje_error("no se esperaba "+literal);
                                estado = -1;
                            }
                            break;
                        case (5)://exponente
                            caracter = archivo.obtenerToken();
                            if(isDigit(caracter)){
                                literal += caracter;
                                estado = 5;
                            }else{
                                estado = 6;
                            }
                            break;
                        case (6)://estado de aceptacion
                            if (caracter != (char)-1){
                                archivo.devolverToken();
                            }else{
                                caracter = (char)0;
                            }
                            acepto = true;
                            lista.add(token = new Row_reg("LITERAL_NUM", literal, LITERAL_NUM));
                            break;
                        case (-1):
                            if (caracter != (char)-1){
                                mensaje_error("No se esperaba "+ literal);
                            }else if(caracter == '\n'){
                                archivo.devolverToken();
                                mensaje_error("No se esperaba fin de Linea");
                                
                            }
                        return;
                    }
                }
            }else{
                mensaje_error("caracter no valido");
            }
        }if(caracter == (char)-1){
            lista.add(token = new Row_reg("EOF", "eof", EOF));
        }
    }
 
    
}


