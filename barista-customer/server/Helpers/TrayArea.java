package Helpers;


import java.util.*;
import java.util.stream.Collectors;

public class TrayArea {
    private static final List<Beverage> BEVERAGES = Collections.synchronizedList(new ArrayList<>());

    public Optional<String> getAreaStatusForCustomer(long customerId) {
        long numberOfCoffees = BEVERAGES.stream().filter(c -> (c instanceof Coffee) && c.getOwnerId() == customerId).count();
        long numberOfTeas = BEVERAGES.stream().filter(c -> (c instanceof Tea) && c.getOwnerId() == customerId).count();

        if(numberOfCoffees == 0 && numberOfTeas == 0) {
            return Optional.empty();
        }

        return Optional.of("- " + OrderResponseFormatter.formatOrder((int) numberOfCoffees, (int) numberOfTeas) + " in the tray");
    }

    public void discardBeveragesForCustomer(Long customerId) {
        synchronized (this) {
            if(BEVERAGES.removeIf(coffee -> Objects.equals(coffee.getOwnerId(), customerId))) {
                printAreaInfo();
            }
        }
    }

    public void addBeverage(Beverage beverage) {
        synchronized (this) {
            BEVERAGES.add(beverage);
            printAreaInfo();
        }
    }

    public synchronized List<Beverage> getAllBeveragesForCustomer(long ownerId) {
        return BEVERAGES.stream().filter(b -> b.getOwnerId() == ownerId).collect(Collectors.toList());
    }

    public void printAreaInfo(){
        long numberOfCoffees = BEVERAGES.stream().filter(c -> (c instanceof Coffee)).count();
        long numberOfTeas = BEVERAGES.stream().filter(c -> (c instanceof Tea)).count();
        ServerLogger.logInfo(String.format("Number of coffees in tray area: %d\nNumber of teas in tray area: %d",
                numberOfCoffees, numberOfTeas));
    }

    public boolean repurposeBeverage(long oldCustomerId, Beverage beverageToBeUpdated){
        int index = -1;
        Beverage foundBeverage = null;
        for (int i = 0; i < BEVERAGES.size(); i++) {
            final Beverage beverage = BEVERAGES.get(i);

            if(beverage.getClass().equals(beverageToBeUpdated.getClass()) && beverage.getOwnerId() == oldCustomerId){
                index= i;
                beverage.setOwnerId(beverageToBeUpdated.getOwnerId());
                beverage.setOwnerName(beverageToBeUpdated.getOwnerName());
                foundBeverage = beverage;
                break;
            }
        }
        if(foundBeverage  != null) {
            BEVERAGES.set(index, foundBeverage);
        }
        return foundBeverage  != null;
    }
}
