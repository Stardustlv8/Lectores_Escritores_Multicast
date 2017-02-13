
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 *
 * @author avi
 */
public class Peticion {
    private DatagramPacket paquete;
    private InetAddress host;
    private String mensaje;
    private int id;
    
    Peticion(DatagramPacket m){
        this.to_Split(new String(m.getData()));
        host = m.getAddress();
    }
    /**
     * @return String
     * Retorna el mensaje sin los bytes inecesarios
     * solo caracteres alfabeticos
     */
    public String get_Mensaje(){
        return this.mensaje;
    }
    /**
     * @return int
     * Retorna el id del peer que envio la peticion
     * sin bytes adicionales, solo el valor entero
     */
    
    public int get_Id(){
        return this.id;
    }
    /**
     * @param cad cadena que incluye bytes sin valor alfanumerico
     * asigna un mensaje en cadena de texto a la variable mensaje
     * sin bytes no alfabeticos
     */
    public void to_Split(String cad){
        String pet[] = cad.split(" ");
        this.mensaje=pet[0];
        this.id= to_Number(pet[1]);
    }
    
    /**
     * @param cad cadena que contiane bytes sin valor alfanumerico
     * @return int valor numerico contenido en la cadena recibida 
     * en el constructor
     */
    public int to_Number(String cad){
        String alpha="";
        for(char c:cad.toCharArray()){
            if(Character.isDigit(c)){
                alpha+=c;
            }
        }
        return Integer.parseInt(alpha);
    }
    /**
     * @return boolean
     * Determina si la cadena de texto recibida pertenece a una peticion
     * del tipo [Confirma acceso al recurso]
     */
    
    public boolean is_notify(){
        if(this.mensaje.equalsIgnoreCase("In")){
            return true;
        }
        return false;
    }
    
    /**
     * @return boolean
     * Determina si la cadena de texto resibida pertenece a una peticion 
     * del tipo [Confirma Peticion de Ingreso para lector o escritor]
     **/
    public boolean is_In(){
        if(this.mensaje.equalsIgnoreCase("ENTERREAD") || this.mensaje.equalsIgnoreCase("ENTERWRITE")){
            return true;
        }
        return false;
    }
    /**
     * @return boolean
     * Determina si la cadena de texto resibida pertenece a una peticion 
     * del tipo [Confirma Peticion de Ingreso para lector exclusivamente]
     **/
    public boolean is_In_L(){
        if(this.mensaje.equalsIgnoreCase("ENTERREAD")){
            return true;
        }
        return false;
        
    }
    /**
     * @return boolean
     * Determina si la cadena de texto resibida pertenece a una peticion 
     * del tipo [Confirma Peticion de Ingreso para escritor exclusivamente]
     **/
    public boolean is_In_E(){
        if(this.mensaje.equalsIgnoreCase("ENTERWRITE")){
            return true;
        }
        return false;
        
    }    
    /**
     * @return boolean
     * Determina si la cadena de texto resibida pertenece a una peticion 
     * del tipo [Confirma Peticion de Salida para lector escritor]
     **/
    public boolean is_Out(){
        if(this.mensaje.equalsIgnoreCase("EXITREAD") || this.mensaje.equalsIgnoreCase("EXITWRITE")){
            return true;
        }
        return false;
    }
    /**
     * @return boolean
     * @deprecated Su uso fue delegado, sera retirado en la siguiente version,
     * no existen problemas de compatibilidad
     * Compara la peticion actual, mensaje y id, con otro.
     **/
    
    public boolean compare_to(Peticion p){
        if(p.get_Mensaje().equalsIgnoreCase(mensaje) && p.get_Id() == this.id){
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        return "Peticion: "+this.mensaje+" Peer id: "+this.id;
    }
}
