package Estado;

import java.util.BitSet;

public class BitReader {

    private BitSet bs;
    private int size;

    enum Order {
        LITTLE,
        BIG;
    }

    private Order o = Order.LITTLE;

    public BitReader(BitSet bs, int size){
        this.size = size;
        this.bs = bs;
    }

    public void litle(){
        this.o = Order.LITTLE;
    }

    public void big(){
        this.o = Order.BIG;
    }

    public int getInt(){

        return (int)this.get(8*4);
    }

    private long get( int n ){
        long s = 0;

        switch (this.o){
            case BIG: for(int i=(n-1); i> 0 ; i--){ s += this.bs.get(i) ? Math.pow(2,n-1-i):0 ; System.out.println(this.bs.get(i) + " : " + s);}
                s = this.bs.get(0) ? (-1)*s : s ;
                break;
            case LITTLE: for(int i=0; i< (n-1); i++){s += this.bs.get(i) ? Math.pow(2,i):0 ; System.out.println(this.bs.get(i) + " : " + s);}
                s = this.bs.get(n-1) ? (-1)*s : s ;
                break;
        }

        return s;
    }

    public short getShort(){

        return (short)this.get(4*4);
    }

}
