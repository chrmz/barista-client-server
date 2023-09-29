package Helpers;


import java.util.*;
import java.util.stream.IntStream;


public final class CustomerOrderProcessor {

    private static final int TEA_CAPACITY = 2;
    private static final int COFFEE_CAPACITY = 2;
    private static final List<BeverageOrder> ORDERS = Collections.synchronizedList(new ArrayList<>());
    private static CustomerOrderProcessor INSTANCE;
    private static final WaitingArea WAITING_AREA = new WaitingArea();
    private static final BrewingArea BREWING_AREA = new BrewingArea(TEA_CAPACITY, COFFEE_CAPACITY);
    private static final TrayArea TRAY_AREA = new TrayArea();

    private CustomerOrderProcessorNotification notification;

    private CustomerOrderProcessor() {

    }

    public synchronized static CustomerOrderProcessor getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new CustomerOrderProcessor();
        }
        return INSTANCE;
    }

    public void setNotification(CustomerOrderProcessorNotification notification) {
        this.notification = notification;
    }

    public void processOrder(BeverageOrder order) {
        synchronized (this) {
            int index = IntStream.range(0, ORDERS.size())
                    .filter(i -> ORDERS.get(i).getCustomerId().equals(order.getCustomerId()))
                    .findFirst()
                    .orElse(-1);
            if (index >= 0) {
                BeverageOrder orderCommand = ORDERS.get(index);
                orderCommand.updateOrder(order);
                ORDERS.set(index, orderCommand);
            } else {
                ORDERS.add(order);
            }
            WAITING_AREA.addOrder(order);
            ServerLogger.logInfo(String.format("Number of customers waiting for orders: %d", ORDERS.size()));
            processNextBeverages();
        }
    }


    public List<String> getOrderStatus(Long id) {
        List<String> messages = new ArrayList<>();
        WAITING_AREA.getAreaStatusForCustomer(id).ifPresent(messages::add);
        BREWING_AREA.getAreaStatusForCustomer(id).ifPresent(messages::add);
        TRAY_AREA.getAreaStatusForCustomer(id).ifPresent(messages::add);

        return messages;
    }

    private void processNextBeverages() {
        if (!BREWING_AREA.isTeaBrewingAreaFull()) {
            WAITING_AREA.getNextTeaForProcessing()
                    .forEach(tea -> {
                        BREWING_AREA.brewTea(tea, this::addBeverageTrayArea);
                        this.notification.notifyStatusChange(tea.getOwnerId(),  BREWING_AREA.getAreaStatusForCustomer(tea.getOwnerId()));

                    });
        }
        if (!BREWING_AREA.isCoffeeBrewingAreaFull()) {
            WAITING_AREA.getNextCoffeeForProcessing()
                    .forEach(coffee -> {
                        BREWING_AREA.brewCoffee(coffee, this::addBeverageTrayArea);
                        this.notification.notifyStatusChange(coffee.getOwnerId(),  BREWING_AREA.getAreaStatusForCustomer(coffee.getOwnerId()));
                    });
        }

    }


    private void addBeverageTrayArea(Beverage beverage) {
        synchronized (this) {
            TRAY_AREA.addBeverage(beverage);
            this.notification.notifyStatusChange(beverage.getOwnerId(),  TRAY_AREA.getAreaStatusForCustomer(beverage.getOwnerId()));

            List<Beverage> beverages = TRAY_AREA.getAllBeveragesForCustomer(beverage.getOwnerId());

            Optional<BeverageOrder> order = ORDERS.stream()
                    .filter(o -> o.getCustomerId() == beverage.getOwnerId())
                    .findFirst();


            if (order.isPresent()) {

                int numberOfTeas = order.get().getNumberOfTea();
                int numberOfCoffees = order.get().getGetNumberOfCoffee();

                if (beverages.size() == (numberOfTeas + numberOfCoffees)) {
                    ORDERS.removeIf(o -> o.getCustomerId() == beverage.getOwnerId());
                    TRAY_AREA.discardBeveragesForCustomer(beverage.getOwnerId());
                    ServerLogger.logInfo(String.format("Number of customers waiting for orders: %d", ORDERS.size()));
                    notification.notifyCompletion(beverage.getOwnerId(), numberOfCoffees, numberOfTeas);
                }
            } else {
                clearOrRepurposeCustomerOrders(beverage.getOwnerId(), beverage.getOwnerName());
            }

            processNextBeverages();
        }
    }

    public void clearOrRepurposeCustomerOrders(long customerId, String ownerName) {
        synchronized (this) {
            Map<Long, RepurposedBeverages> repurposesBeverages = new HashMap<>();

            ORDERS.removeIf(o -> o.getCustomerId() == customerId);
            WAITING_AREA.removeOrdersForCustomer(customerId);
            List<Beverage> allBeveragesForCustomerInTrayArea = TRAY_AREA.getAllBeveragesForCustomer(customerId);
            List<Beverage> allBeveragesForCustomerInBrewingArea = BREWING_AREA.getAllBeveragesForCustomer(customerId);

            allBeveragesForCustomerInTrayArea
                    .forEach(beverage -> {
                        if (beverage.name().equals(Coffee.NAME)) {
                            WAITING_AREA.takeCoffee()
                                    .ifPresent(c -> {
                                        if (TRAY_AREA.repurposeBeverage(customerId, c)) {
                                            RepurposedBeverages response = repurposesBeverages.get(c.getOwnerId());
                                            if (null == response) {
                                                repurposesBeverages.put(c.getOwnerId(), new RepurposedBeverages(ownerName, c.getOwnerName(), 0, 1, 0, 0));
                                            } else {
                                                response.increaseCoffeeQuantityInTray();
                                                repurposesBeverages.replace(c.getOwnerId(), response);
                                            }
                                        }
                                    });
                        } else {
                            WAITING_AREA.takeTea()
                                    .ifPresent(t -> {
                                        if (TRAY_AREA.repurposeBeverage(customerId, t)) {
                                            RepurposedBeverages response = repurposesBeverages.get(t.getOwnerId());
                                            if (null == response) {
                                                repurposesBeverages.put(t.getOwnerId(), new RepurposedBeverages(ownerName, t.getOwnerName(), 1, 0, 0, 0));
                                            } else {
                                                response.increaseTeaQuantityInTray();
                                                repurposesBeverages.replace(t.getOwnerId(), response);
                                            }
                                        }
                                    });
                        }
                    });

            allBeveragesForCustomerInBrewingArea
                    .forEach(beverage -> {
                        if (beverage.name().equals(Coffee.NAME)) {
                            WAITING_AREA.takeCoffee()
                                    .ifPresent(c -> {
                                        if (BREWING_AREA.repurposeCoffee(customerId, c)) {
                                            RepurposedBeverages response = repurposesBeverages.get(c.getOwnerId());
                                            if (null == response) {
                                                repurposesBeverages.put(c.getOwnerId(), new RepurposedBeverages(ownerName, c.getOwnerName(), 0, 0, 1, 0));
                                            } else {
                                                response.increaseCoffeeQuantityInBrewing();
                                                repurposesBeverages.replace(c.getOwnerId(), response);
                                            }
                                        }
                                    });
                        } else {
                            WAITING_AREA.takeTea()
                                    .ifPresent(t -> {
                                        if (BREWING_AREA.repurposeTea(customerId, t)) {
                                            RepurposedBeverages response = repurposesBeverages.get(t.getOwnerId());
                                            if (null == response) {
                                                repurposesBeverages.put(t.getOwnerId(), new RepurposedBeverages(ownerName, t.getOwnerName(), 0, 0, 0, 1));
                                            } else {
                                                response.increaseTeaQuantityInBrewing();
                                                repurposesBeverages.replace(t.getOwnerId(), response);
                                            }
                                        }
                                    });
                        }
                    });


            TRAY_AREA.discardBeveragesForCustomer(customerId);
            BREWING_AREA.discardBeveragesForCustomer(customerId);
            repurposesBeverages.forEach((key, value) -> ServerLogger.logInfo(value.getRepurposedItems()));

        }
    }

    public void printAreaTrayInfo(String info) {
        switch (info) {
            case "tray":
                TRAY_AREA.printAreaInfo();
                break;
            case "brewing":
                BREWING_AREA.printAreaInfo();
                break;
            case "waiting":
                WAITING_AREA.printAreaInfo();
                break;
            default:
        }
    }
}
