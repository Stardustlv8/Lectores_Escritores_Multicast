
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author avi
 */
public class Peer implements Runnable{
    /**
     * Atributos relacionados a la comunicacion Multicast
     */
    private MulticastSocket socket;
    private InetAddress host;
    
    /**
     * Atributos relacionados al tipo de peer instanciado
     */
    private String nombre;
    private String entrada;
    private String salida;

    /**
     * Atributos relacionados al control de la logica de manejo del peer
     */
    private boolean Lector=false;
    private boolean Escritor=false;
    private boolean Coordinador=false;
    private boolean notificacion=true;
    
    /**
     * Atributos relacionados al control de las peticiones de entrada/salida
     * y el acceso simulado al recurso compartido
     */
    private LinkedList s_Entrada= new LinkedList();
    private LinkedList s_Salida= new LinkedList();
    private ListaLigada Recurso = new ListaLigada();
    
    /**
     * Atributos relacionados al identificador del peer y el tiempo de 
     * espera para los threads usados
     */
    private int id;
    private int sleep=2000;

    /**
     * Atributos relacionados a la muestra de resultados por pantalla
     * de los resultados obtenidos por el peer
     */
    private JTextArea ar1;
    private String mensaje="";
    /**
     * Constructor encargado de iniciar el socket multicast
     * @param tipo si es Lector,Escritor o Coordinador.
     * @param id identificador del Peer.
     */
    
    public Peer(String tipo,int id) {
        this.nombre=tipo;
        this.id=id;
        
        try {
            socket=new MulticastSocket(5000);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            host = InetAddress.getByName("230.0.0.5");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(tipo.equalsIgnoreCase("Lector")){
            System.out.println("Es un Lector");
            this.entrada="ENTERREAD "+id;
            this.salida="EXITREAD "+id;
            this.Lector=true;
        }
        if(tipo.equalsIgnoreCase("Escritor")){
            System.out.println("Es un Escritor");
            this.entrada="ENTERWRITE "+id;
            this.salida="EXITWRITE "+id;
            this.Escritor=true;
        }
        if(tipo.equalsIgnoreCase("Coordinador")){
            System.out.println("Es un coordinador");
            this.Coordinador=true;
        }
    }
    /**
     * @param ar1 compomente para iniciar muestra en pantalla. JTextArea
     * 
     */
    public void set_Area(JTextArea ar1){
        this.ar1=ar1;
    }
    /**
     * Envio de mensaje [Peticion de entrada(ENTERREAD/ENTERWRITE)]
     * a traves del multicast para el peer coordinador del recurso
     */
    public void send_Entrada(){
        if(this.notificacion){
            DatagramPacket paquete;
            byte [] buffer;
        
            buffer = entrada.getBytes();
            paquete = new DatagramPacket(buffer,buffer.length,host,5000);
            try {
                socket.send(paquete);
                this.mensaje+=("Peer "+id+" Peticion enviada: "+entrada+"\n");
                this.ar1.setText(mensaje);
            } catch (IOException ex) {
            
            }
            this.notificacion=false;
        }
    }
    /**
     * Envio de mensaje [Peticion de salida(EXITREAD/EXITWRITE)]
     * a traves del multicast para el peer coordinador del recurso
     */
    public void send_Salida(){
        if(this.notificacion){
            DatagramPacket paquete;
            byte [] buffer;
        
            buffer = salida.getBytes();
            paquete = new DatagramPacket(buffer,buffer.length,host,5000);
            try {
                socket.send(paquete);
                this.mensaje+=("Peer "+id+" Peticion enviada: "+salida+"\n");
                this.ar1.setText(mensaje);
            } catch (IOException ex) {
            }
        }
    }
    /**
     * Envio de mensaje [notificacion de acceso(In)]
     * a traves del multicast para el peer que solicito el acceso al recurso
     * exclusivamente
     */
    
    public void send_Salida_notify(int p){
            DatagramPacket paquete;
            byte [] buffer;
            if(!this.notificacion){
                buffer = new byte[30];
                buffer = ("In "+p).getBytes();
                //mensaje+=("Mensaje confirmacion entrada enviado..."+new String(buffer)+"\n");
                paquete = new DatagramPacket(buffer,buffer.length,host,5000);
                try {
                    socket.send(paquete);
                
                } catch (IOException ex) {
        
                }
                //this.notificacion=true;
            }
    }
    /**
     *Thread-0 principal.
     *
     */
    @Override
    public synchronized void run() {
        /**
         * Thread-1 si es un Peer coordinador
         * Escucha del grupo multicast al que esta adscrito
         * las peticiones de entrada/salida de los Peer lectores/escritores.
         */
        Runnable cola_admision = ()->{
            try {
                byte []buffer;
                DatagramPacket paquete;
                Peticion p;
                socket.joinGroup(host);
                socket.setSoTimeout(sleep);
                socket.setTimeToLive(0); 
                if(this.Coordinador)
                {
                    while(this.Coordinador){
                        buffer = new byte[20];
                        paquete = new DatagramPacket(buffer,buffer.length);
                        try{
                        socket.receive(paquete);
                        p = new Peticion(paquete);
                        if(p.is_In()){
                            this.s_Entrada.add(p);
                        }
                        if (p.is_Out()){
                            this.s_Salida.add(p);
                        }
                        }catch(IOException ex){
                        
                        }
                        Thread.sleep(sleep);
                    }
                }
        /**
         * Thread-1 si no es Peer coordinador
         * Escucha del grupo multicast al que esta adscrito
         * las peticiones de confirmacion que el coordinador trasmite
         * cuando le concede el acceso al recurso.
         */
                if(!this.Coordinador){
                    while(true){
                        buffer = new byte[20];
                        paquete = new DatagramPacket(buffer,buffer.length);
                        try{

                            socket.receive(paquete);
                            p = new Peticion(paquete);
                            //mensaje+=("Peticion recibida: "+p+"\n");
                            if(p.is_notify()){
                                if(p.get_Id()==this.id){
                                    if(!this.notificacion){
                                        mensaje+="Peer usando el recurso: "+p+"\n";
                                        this.ar1.setText(mensaje);
                                        this.notificacion=true;
                                    }
                                }
                            }
                    
                        }catch(IOException ex){
                        }
                        Thread.sleep(sleep);
                    }
                }
            } catch (IOException ex) {
            } catch (InterruptedException ex) {
                System.out.println("Sin confirmacion");
            }
        };new Thread(cola_admision).start();
        
        /**
         * Thread-2 si es un Peer coordinador
         * Aplica el algoritmo Lectores_Escritores
         * a las peticiones escuchadas previamente del grupo multicast
         * y brinda una simulacion de acceso a recurso compartido.
         */
        Runnable entradas = ()->{
            
            while(this.Coordinador){
                
                try {
                    Peticion a;
                    if(!this.s_Entrada.isEmpty()){
                        
                        a=(Peticion)this.s_Entrada.get(0);
                        do{
                           this.Recurso.cont_Recurso();
                           if(a.is_In_L() && Recurso.get_Escritores()==0){
                                this.Recurso.add(a);
                                this.s_Entrada.remove(0);
                                this.notificacion=false;
                                mensaje+=("Entra: "+a+"\n");
                                this.ar1.setText(mensaje); 
                                a=null;
                            }
                            else if(a.is_In_E() && Recurso.get_Escritores()==0 && Recurso.get_Lectores()==0){
                                this.Recurso.add(a);
                                this.s_Entrada.remove(0);
                                this.notificacion=false;
                                mensaje+=("Entra: "+a+"\n");
                                this.ar1.setText(mensaje); 
                                a=null;
                            }
                            if(a!=null)
                            {
                                if(!this.Recurso.isEmpty()){
                                    mensaje+=("Elementos usando el recurso\n");
                                    mensaje+=(this.view_list(this.Recurso));
                                }
                                if(!this.s_Entrada.isEmpty()){
                                mensaje+=("Elementos en espera del Recurso\n");
                                mensaje+=this.view_list(s_Entrada);
                                }
                                if(this.Recurso.isEmpty() && this.s_Entrada.isEmpty()){
                                    mensaje="";
                                }
                                this.ar1.setText(mensaje);
                            }
                        this.ar1.setText(mensaje);
                        Thread.sleep(sleep+1000);
                        mensaje="";
                        this.ar1.setText(mensaje);
                        }while(a!=null);
                    }
                    if(!this.Recurso.isEmpty()){
                        mensaje+=("Elementos usando el recurso\n");
                        mensaje+=(this.view_list(this.Recurso));
                    }
                    if(!this.s_Entrada.isEmpty()){
                        mensaje+=("Elementos en espera del Recurso\n");
                        mensaje+=this.view_list(s_Entrada);
                    }
                    if(this.Recurso.isEmpty() && this.s_Entrada.isEmpty()){
                        mensaje="";
                    }
                    this.ar1.setText(mensaje);
                    Thread.sleep(sleep+1000);
                    mensaje="";
                    this.ar1.setText(mensaje);
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };new Thread(entradas).start();
        
        /**
         * Thread-3 si es un Peer coordinador
         * Concede la salida a los Peer que a traves del multicas
         * solicitaron la salida del recurso.
         * 
         */
        Runnable salidas= ()->{
            
            while(this.Coordinador)
            {
                try {
                    Peticion s;
                    
                    if(!this.s_Salida.isEmpty()){
                        s = (Peticion)this.s_Salida.removeFirst();
                        int b=(this.Recurso.busqueda(s));
                        
                        if(b!=-1){
                            mensaje+=("Sale: "+s+"\n");
                            System.out.println("Sale: "+s);
                            this.Recurso.remove(b);
                            this.ar1.setText(mensaje);
                        }
                        
                        
                    }
                    //this.ar1.setText(mensaje);
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        };new Thread(salidas).start();

        /***
         * Thread-4 si es un Peer coordinador
         * Itera sobre la lista que simula el acceso al recurso compartido
         * y multidifunde mensajes a los Peer que tienen el acceso concedido
         * actualmente.
         */
        Runnable auto= ()->{
            if(this.Coordinador){
                while(true){
                    try {
                        int p;
                        //System.out.println("elementos: "+this.Recurso.size());
                        for(int i=0;i<this.Recurso.size();i++){
                            p=((Peticion)this.Recurso.get(i)).get_Id(); 
                            //mensaje+=("Enviando confirmacion a: "+p+"\n");
                            this.send_Salida_notify(p);
                        }
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            }
            
        };new Thread(auto).start();
    }
    /**
     * @param list Lista ligada que almacena Peticiones de entrada/salida.
     * @return String cadena con el id y tipo de cada peticion en la LL.
     *
     */
    public String view_list(LinkedList list){
        String alpha="";
        if(!list.isEmpty()){
            for(int i=0;i<list.size();i++){
                alpha+=((Peticion)(list.get(i)))+"\n";
            }
            alpha+="\n";
        }
        return ""+alpha;
    }
}
