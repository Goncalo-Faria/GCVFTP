package Transport.Receiver;

public class SimpleSeqChain<V> {

    //private Node head = null;
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;

    public SimpleSeqChain(){
    }

    /*
    public SimpleSeqChain put( Interval<V> x ) throws ValueAlreadyAckedException{

        if( head == null) {
            head = new Node(x);
            min = x.min;
            max = x.max;
        } else {
            this.min = (this.min < x.min) ? this.min : x.min;
            this.max = (this.max > x.max) ? this.max : x.max;

            this.head = this.addins( head, x) ;
        }

        return this;
    }

    public V find( int x ) {
        Interval<V> i = this.head.find(x);

        if( i != null )
            return i.get(x);

        return null;
    }

    public V take( Node n, int x){

        if(n == null)

        if( n.dt.contains(x) ) {

            if( n.dt.isSingleton() ){

            }else{

                Interval leftin = n.dt.splitl(x);
                Interval rightin = n.dt.splitr(x);
                V value = n.dt.get(x);

                if( rightin != null ){
                    n.dt = rightin;

                    if(leftin != null ){
                        n.left = addins(n.left, leftin);
                    }

                }else{
                    n.dt = leftin;
                }



            }


            return dt;
        }

        if( n.dt.less(x) ){
            return take(n.left,x);
        }

        if( n.dt.greater(x) ){
            return take(n.right,x);
        }

        return null;
    }

    private Node addins(Node n, Interval<V> x) throws ValueAlreadyAckedException{

        if( n == null )
            return new Node(x);


        if(n.dt.intersects(x)){
            throw new ValueAlreadyAckedException();
        }

        if (n.dt.less(x)) {

            n.left = addins(n.left,x);

            if ( n.interval().merge(n.left.interval()) ) {
                /*can merge*/

                /* change the tree

                n.left = n.left.left;

            }

        } else if (n.dt.greater(x)) {

            n.right = addins(n.right,x);

            if ( n.interval().merge(n.right.interval()) ) {
                /*can merge*/

                /*change the tree

                n.right = n.right.right;


                if( n.right != null ){

                    Node nr = n.right;
                    Node nrr = n.right.right;
                    Node nrl = n.right.left;

                    n.right = nrl;

                    nr.left = n;

                    n = nr;
                }
            }

        }

        // (unchanged) node
        return n;
    }

    class Node {

        Node left = null;
        Node right = null;

        Interval<V> dt;


        Node(Interval<V> v) {
            dt = v;
        }

        Interval<V> find(int x){
            if( dt.contains(x) )
                return dt;

            if( dt.less(x) && left != null ){
                return left.find(x);
            }

            if( dt.greater(x) && right != null ){
                return right.find(x);
            }

            return null;
        }

        Interval interval() {
            return this.dt;
        }

    }

    public class ValueAlreadyAckedException extends Exception{

        ValueAlreadyAckedException(){
            super();
        }

    }

    */

}
