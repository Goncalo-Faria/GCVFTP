package Model;

public class Communication {
    private Packet packet;
    private Customer customer;

    public Communication(Packet packet, Customer customer) {
        this.packet = packet;
        this.customer = customer;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
