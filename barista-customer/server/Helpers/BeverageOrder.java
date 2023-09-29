package Helpers;

public class BeverageOrder {
    private int numberOfTea;
    private int getNumberOfCoffee;
    private Long customerId;

    private String customerName;

    public BeverageOrder() {

    }

    public BeverageOrder(int numberOfTea, int getNumberOfCoffee) {
        this.numberOfTea = numberOfTea;
        this.getNumberOfCoffee = getNumberOfCoffee;
    }

    public int getNumberOfTea() {
        return numberOfTea;
    }

    public int getGetNumberOfCoffee() {
        return getNumberOfCoffee;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void updateOrder(BeverageOrder newOrder) {
        this.numberOfTea += newOrder.numberOfTea;
        this.getNumberOfCoffee += newOrder.getNumberOfCoffee;
    }

    @Override
    public String toString() {
        String teaOrder = this.numberOfTea == 0 ? ""  : String.format("%s %s%s", this.numberOfTea, "tea" , (this.numberOfTea > 1 ? "s" : ""));
        String coffee = this.getNumberOfCoffee == 0 ? ""  : String.format("%s %s%s", this.getNumberOfCoffee, "coffee" , (this.getNumberOfCoffee > 1 ? "s" : ""));
        return String.format("%s %s %s", "Order", teaOrder, coffee);
    }

}
