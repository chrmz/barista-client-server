package Helpers;


import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BrewingArea {
    private static final List<Tea> TEAS = Collections.synchronizedList(new ArrayList<>());
    private static final List<Coffee> COFFEES = Collections.synchronizedList(new ArrayList<>());

    private final ExecutorService teaProcessor;
    private final ExecutorService coffeeProcessor;
    private final int teaCapacity;
    private final int coffeeCapacity;

    public BrewingArea(int teaCapacity, int coffeeCapacity) {
        this.teaCapacity = teaCapacity;
        this.coffeeCapacity = coffeeCapacity;
        this.teaProcessor = Executors.newFixedThreadPool(teaCapacity);
        this.coffeeProcessor = Executors.newFixedThreadPool(coffeeCapacity);
    }


    public void brewTea(Tea tea, Consumer<Tea> doneCallback) {
        synchronized (this) {
            TEAS.add(tea);
            printAreaInfo();
            this.teaProcessor.execute(new BeveragePreparer(tea, () -> {
                TEAS.remove(tea);
                printAreaInfo();
                doneCallback.accept(tea);
            }));
        }
    }

    public void brewCoffee(Coffee coffee, Consumer<Coffee> doneCallback) {
        synchronized (this) {
            COFFEES.add(coffee);
            printAreaInfo();
            this.coffeeProcessor.execute(new BeveragePreparer(coffee, () -> {
                COFFEES.remove(coffee);
                printAreaInfo();
                doneCallback.accept(coffee);
            }));
        }
    }

    public synchronized Optional<String> getAreaStatusForCustomer(long customerId) {
        long numberOfCoffees = COFFEES.stream().filter(c -> c.getOwnerId() == customerId).count();
        long numberOfTeas = TEAS.stream().filter(c -> c.getOwnerId() == customerId).count();

        if (numberOfCoffees == 0 && numberOfTeas == 0) {
            return Optional.empty();
        }

        return Optional.of("- " + OrderResponseFormatter.formatOrder((int) numberOfCoffees, (int) numberOfTeas) + " currently being prepared");
    }

    public synchronized boolean isTeaBrewingAreaFull() {
        return TEAS.size() >= this.teaCapacity;
    }

    public synchronized boolean isCoffeeBrewingAreaFull() {
        return COFFEES.size() >= this.coffeeCapacity;
    }


    public void printAreaInfo() {
        ServerLogger.logInfo(String.format("Number of coffees in brewing area: %d\nNumber of teas in brewing area: %d",
                COFFEES.size(), TEAS.size()));
    }

    public List<Beverage> getAllBeveragesForCustomer(long customerId) {
        return Stream.of(TEAS, COFFEES)
                .flatMap(Collection::stream)
                .filter(b -> b.getOwnerId() == customerId)
                .collect(Collectors.toList());
    }

    public void discardBeveragesForCustomer(long customerId) {
        synchronized (this){
            TEAS.removeIf(t -> t.getOwnerId() == customerId);
            COFFEES.removeIf(c -> c.getOwnerId() == customerId);
        }
    }

    public boolean repurposeCoffee(long oldCustomerId, Coffee coffeeToBeUpdated) {
        int index = -1;
        Coffee foundCoffee = null;
        for (int i = 0; i < COFFEES.size(); i++) {
            final Coffee coffee = COFFEES.get(i);

            if(coffee.getOwnerId() == oldCustomerId) {
                index= i;
                coffee.setOwnerId(coffeeToBeUpdated.getOwnerId());
                coffee.setOwnerName(coffeeToBeUpdated.getOwnerName());
                foundCoffee = coffee;
                break;
            }
        }
        if(foundCoffee  != null) {
            COFFEES.set(index, foundCoffee);
        }

        return foundCoffee  != null;
    }

    public boolean repurposeTea(long oldCustomerId, Tea teaToBeUpdated) {
        int index = -1;
        Tea foundTea = null;
        for (int i = 0; i < TEAS.size(); i++) {
            final Tea tea = TEAS.get(i);

            if(tea.getOwnerId() == oldCustomerId) {
                index= i;
                tea.setOwnerId(teaToBeUpdated.getOwnerId());
                tea.setOwnerName(teaToBeUpdated.getOwnerName());
                foundTea = tea;
                break;
            }
        }
        if(foundTea  != null) {
            TEAS.set(index, foundTea);
        }
        return foundTea  != null;
    }
}
