package Helpers;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WaitingArea {
    private static final List<Tea> TEAS = Collections.synchronizedList(new ArrayList<>());
    private static final List<Coffee> COFFEES = Collections.synchronizedList(new ArrayList<>());


    public Optional<String> getAreaStatusForCustomer(long customerId) {
        synchronized (this) {
            long numberOfCoffees = COFFEES.stream().filter(c -> c.getOwnerId() == customerId).count();
            long numberOfTeas = TEAS.stream().filter(c -> c.getOwnerId() == customerId).count();

            if (numberOfCoffees == 0 && numberOfTeas == 0) {
                return Optional.empty();
            }

            return Optional.of("- " + OrderResponseFormatter.formatOrder((int) numberOfCoffees, (int) numberOfTeas) + " in waiting area");
        }
    }


    public void removeOrdersForCustomer(Long customerId) {
        synchronized (this) {
            COFFEES.removeIf(coffee -> Objects.equals(coffee.getOwnerId(), customerId));
            TEAS.removeIf(tea -> Objects.equals(tea.getOwnerId(), customerId));
        }
    }

    public void addOrder(BeverageOrder order) {
        synchronized (this) {
            IntStream.range(0, order.getNumberOfTea())
                    .mapToObj(i -> new Tea(order.getCustomerId(), order.getCustomerName())).forEach(TEAS::add);
            IntStream.range(0, order.getGetNumberOfCoffee())
                    .mapToObj(i -> new Coffee(order.getCustomerId(), order.getCustomerName())).forEach(COFFEES::add);
            printAreaInfo();
        }
    }


    public List<Tea> getNextTeaForProcessing() {
        synchronized (this) {
            List<Tea> nextTwo = TEAS.stream().limit(2).collect(Collectors.toList());
            if (!nextTwo.isEmpty()) {
                TEAS.remove(0);
                if (nextTwo.size() > 1) {
                    TEAS.remove(0);
                }
                printAreaInfo();
            }
            return nextTwo;
        }
    }

    public List<Coffee> getNextCoffeeForProcessing() {
        synchronized (this) {
            List<Coffee> nextTwo = COFFEES.stream().limit(2).collect(Collectors.toList());
            if (!nextTwo.isEmpty()) {
                COFFEES.remove(0);
                if (nextTwo.size() > 1) {
                    COFFEES.remove(0);
                }
                printAreaInfo();
            }
            return nextTwo;
        }
    }

    public void printAreaInfo() {
        ServerLogger.logInfo(String.format("Number of coffees in waiting area: %d\nNumber of teas in waiting area: %d",
                COFFEES.size(), TEAS.size()));
    }

    public  Optional<Tea> takeTea() {
        synchronized (this) {
            Optional<Tea> tea = TEAS.stream().findFirst();
            if (tea.isPresent()) {
                TEAS.remove(0);
            }
            return tea;
        }
    }

    public  Optional<Coffee> takeCoffee() {
        synchronized (this) {
            Optional<Coffee> coffee = COFFEES.stream().findFirst();
            if (coffee.isPresent()) {
                COFFEES.remove(0);
            }
            return coffee;
        }
    }
}
