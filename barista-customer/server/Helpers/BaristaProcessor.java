package Helpers;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public final class BaristaProcessor implements CustomerRequestHandler, CustomerOrderProcessorNotification {

    public static final String ORDER = "order";
    public static final String ORDER_STATUS = ORDER + " status";
    private static BaristaProcessor INSTANCE;
    private static boolean SERVER_RUNNING = false;
    private final CustomerOrderProcessor processor = CustomerOrderProcessor.getInstance();
    private static final ConcurrentHashMap<Long, CustomerConnection> CUSTOMER_HANDLERS = new ConcurrentHashMap<>();

    public synchronized static BaristaProcessor getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new BaristaProcessor();
        }
        return INSTANCE;
    }

    private BaristaProcessor() {
        processor.setNotification(this);
    }

    public void startServer() {
        if (!SERVER_RUNNING) {
            listenForInputs();
            SERVER_RUNNING = true;
            try (ServerSocket serversocket = new ServerSocket(5000)) {
                while (true) {
                    Socket socket = serversocket.accept();
                    CustomerConnection customerConnection = new CustomerConnection(socket, this);
                    customerConnection.start();
                }
            } catch (Exception e) {
                ServerLogger.logError("Error occured in main: ");
            }
        } else {
            ServerLogger.logInfo("Barista processor is already running");
        }

    }

    @Override
    public void onRegisterCustomer(CustomerConnection customerConnection) {

        CUSTOMER_HANDLERS.put(customerConnection.getId(), customerConnection);
        customerConnection.sendMessage(String.format("Hello %s, may i take your order?",
                customerConnection.getCustomerName()));
        ServerLogger.logInfo(String.format("%s joined the café\nNumber of customers in café: %d",
                customerConnection.getCustomerName(), CUSTOMER_HANDLERS.size()));
    }

    @Override
    public void onMessageReceived(Long id, String name, String message) {
        final CustomerConnection customerConnection = CUSTOMER_HANDLERS.get(id);
        if (message != null && ORDER.equalsIgnoreCase(message.split(" ")[0])) {
            Optional<BeverageOrder> order = BeverageOrderInterpreter.interpret(message);
            if (message.toLowerCase().trim().equals(ORDER_STATUS)) {
                List<String> statuses = this.processor.getOrderStatus(id);
                if (statuses.isEmpty()) {
                    customerConnection.sendMessage(String.format("No order found for %s:", name));
                } else {
                    customerConnection.sendMessage(String.format("Order status for %s:", name));
                    statuses.forEach(customerConnection::sendMessage);
                }
            } else if (order.isPresent()) {
                BeverageOrder actualOrder = order.get();
                actualOrder.setCustomerId(id);
                actualOrder.setCustomerName(name);
                this.processor.processOrder(actualOrder);
                customerConnection.sendMessage(
                        String.format("order received for %s (%s)", name,
                                OrderResponseFormatter.formatOrder(actualOrder.getGetNumberOfCoffee(),
                                        actualOrder.getNumberOfTea()))
                );
            } else {
                customerConnection.sendMessage(String.format("Sorry %s, we were unable to process your request '%s'." +
                        " Please send a valid order", name, message));
            }
        } else {
            customerConnection.sendMessage(String.format("Sorry %s, we were unable to process your request '%s'." +
                    " We only understand commands beginning with [%s]", name, message, ORDER));
        }
    }

    @Override
    public void onDisconnect(Long customerId) {
        String customerName = CUSTOMER_HANDLERS.get(customerId).getCustomerName();
        CUSTOMER_HANDLERS.remove(customerId);
        this.processor.clearOrRepurposeCustomerOrders(customerId, customerName);
        ServerLogger.logInfo(
                String.format("%s left the café\nNumber of customers in café: %d",
                        customerName, CUSTOMER_HANDLERS.size()));
    }

    @Override
    public void notifyCompletion(long customerId, int numberOfCoffees, int numberOfTeas) {
        final CustomerConnection customerConnection = CUSTOMER_HANDLERS.get(customerId);
        final String customerName = customerConnection.getCustomerName();
        customerConnection.sendMessage(String.format("Order delivered to %s (%s)",
                customerName, OrderResponseFormatter.formatOrder(numberOfCoffees, numberOfTeas)));
    }

    @Override
    public void notifyStatusChange(long customerId, Optional<String> areaInfo) {
        final CustomerConnection customerConnection = CUSTOMER_HANDLERS.get(customerId);
        final String customerName = customerConnection.getCustomerName();
        areaInfo.ifPresent(msg -> customerConnection.sendMessage(String.format("Hello %s, the current status or your order is \n%s", customerName, msg)));
    }

    public void listenForInputs() {
        new Thread(() -> {
            while (true) {
                Scanner scanner = new Scanner(System.in);
                String userInput = scanner.nextLine().trim();
                if ("tray area info".equals(userInput)) {
                    processor.printAreaTrayInfo("tray");
                } else if ("waiting area info".equals(userInput)) {
                    processor.printAreaTrayInfo("waiting");
                } else if ("brewing area info".equals(userInput)) {
                    processor.printAreaTrayInfo("brewing");
                } else if("help".equals(userInput)) {
                    System.out.println(
                            "\n__________________________________________________________________________" +
                                    "\n| Command             | Description                                       |"+
                                    "\n|_____________________|___________________________________________________|" +
                                    "\n| waiting area info   | tells you how many orders are in the waiting area |" +
                                    "\n| brewing area info   | tells you how many orders are in the brewing area |" +
                                    "\n| tray area info      | tells you how many orders are in the tray area    |" +
                                    "\n---------------------------------------------------------------------------\n");
                } else {
                    System.out.println("Unknown command");
                }
            }
        }).start();
    }
}
