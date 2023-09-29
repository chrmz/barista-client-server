package Helpers;

public class RepurposedBeverages {

    private String ownerName;

    private String newOwnerName;
    private int coffeeQuantityInTray;
    private int teaQuantityInTray;
    private int coffeeQuantityInBrewing;
    private int teaQuantityInBrewing;

    public RepurposedBeverages(String ownerName,
                               String newOwnerName,
                               int teaQuantityInTray,
                               int coffeeQuantityInTray,
                               int coffeeQuantityInBrewing,
                               int teaQuantityInBrewing) {
        this.ownerName = ownerName;
        this.newOwnerName = newOwnerName;
        this.teaQuantityInTray = teaQuantityInTray;
        this.coffeeQuantityInTray = coffeeQuantityInTray;
        this.coffeeQuantityInBrewing = coffeeQuantityInBrewing;
        this.teaQuantityInBrewing = teaQuantityInBrewing;
    }

    public void increaseTeaQuantityInTray() {
        this.teaQuantityInTray ++;
    }

    public void increaseCoffeeQuantityInTray() {
        this.coffeeQuantityInTray ++;
    }


    public void increaseTeaQuantityInBrewing() {
        this.teaQuantityInBrewing ++;
    }

    public void increaseCoffeeQuantityInBrewing() {
        this.coffeeQuantityInBrewing ++;
    }

    public String getRepurposedItems() {
        String brewingRepurposed = (this.coffeeQuantityInBrewing + this.teaQuantityInBrewing == 0)? "" :
                String.format("%s currently brewing for %s has been transferred to %s's order",
                        OrderResponseFormatter.formatOrder(this.coffeeQuantityInBrewing, this.teaQuantityInBrewing),
                        this.ownerName, this.newOwnerName );

        String trayRepurposed =(this.coffeeQuantityInTray + this.teaQuantityInTray == 0)? "" :
                String.format("%s in %s's tray has been transferred to %s's tray",
                        OrderResponseFormatter.formatOrder(this.coffeeQuantityInTray, this.teaQuantityInTray),
                        this.ownerName, this.newOwnerName );
        String separator = brewingRepurposed.isEmpty()? "" : "\n";
        return brewingRepurposed + separator+ trayRepurposed;

    }
}
