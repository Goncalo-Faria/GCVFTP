package Transport.Receiver;

public class LList<V> {

    private No<V> current=null;
    private No<V> head=null;
    private No<V> tail=null;

    public LList(){
    }

    public LList<V> add( V element ){
        
        if( head == null){
            head = tail = current = new No<V>(null, element, null);
            return this;
        }

        if( current == null ){
            No<V> me = new No<V>(tail, element, null);

            tail.next = me;

            tail = me;
            return this;
        }

        No<V> me = new No<V>(current.previous, element, current );
        
        current.previous = me;

        current = me;

        if( current.previous == null ){
            head = current;
        }else{
            current.previous.next = me;
        }

        return this;

    }

    public LList<V> next(){

        if( current == null )
            current = head;
        else if(current != null)
            current = current.next;

        return this;
    }

    public LList<V> previous(){
        if( current == null )
            current = tail;
        else if(current != null)   
            current = current.previous;

        return this;
    }

    public V value(){
        if( current == null ){
            return null;
        }else{
            return current.value;
        }
    }

    public LList<V> start(){
        current = null;
        return this;
    }

    public LList<V> remove(){
        if(current != null){
            
            if(current.previous != null && current.next != null ){
                /* elemento do meio */
                current.previous.next = current.next;
                current = current.previous;
                
            }else if( current.previous != null ){
                /* tail */
                tail = current.previous;
                tail.next = null;
                current = tail;
            }else if( current.next != null ){
                /* head */
                head = current.next;
                head.previous = null;
                current = null;
            }else {
                tail = current = head = null;
            }
        }

        return this;
    }

    public boolean hasNext(){
        if( current == null )
            return (head != null);


        return (current.next!=null);
    }

    public boolean hasPrevious(){

        if( current == null)
            return tail != null;

        return (current.previous!=null);
    }

    public V peek(){
        return head.value;
    }

    public boolean empty(){
        return (head == null ) && (current == null) && (tail == null);
    }

    public boolean singleton(){
        if( head == null)
            return false;
        
        return head.next == null;
    }

    class No<V> {
        No<V> next;
        No<V> previous;
        V value;

        No(No<V> before, V value , No<V> after){
            this.value = value;
            this.next = after;
            this.previous = before;
        }

    }


}