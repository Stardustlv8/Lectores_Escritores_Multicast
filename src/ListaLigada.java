
import java.util.LinkedList;
/**
 *
 * @author avi
 */
public class ListaLigada extends LinkedList{
    private int Lectores=0;
    private int Escritores=0;

    public ListaLigada(){}
    /**
     * @param b Tipo peticion que se pretende buscar en la Linked List
     * @return int Retornamos la posicion del elemento en la LL en caso de ser
     * encontrado, en caso contrario se devuelve -1
     */
    public int busqueda(Peticion b){
        Peticion p;
        for(int i=0;i<this.size();i++){
            p=((Peticion)this.get(i));
            System.out.println("Comparando: "+b+" con: "+p);
            if(b.get_Id()==p.get_Id()){
                return i;
            }
              
        }
        return -1;
    }
    /**
     * Se realisa el conteo de la cantidad de lectores o escritores
     * que se tienen actualmente en la Linked List que simula
     * el recurso compartido
     */
    public void cont_Recurso(){
        Peticion p;
        this.Lectores=this.Escritores=0;
        for(int i=0;i<this.size();i++){
            p=(Peticion)this.get(i);
            if(p.is_In_L())
                this.Lectores++;
            else if(p.is_In_E())
                this.Escritores++;
            
        }
    }
    /**
     * @return int Cantidad de Lectores en la Linked List
     */
    public int get_Lectores(){
        return this.Lectores;
    }
    /**
     * @param int Cantidad de Escritores en la Linked List
     */
    public int get_Escritores(){
        return this.Escritores;
    }

}
