package Helpers;

public interface CustomerRequestHandler {
    void onRegisterCustomer(CustomerConnection customerConnection);
    void onMessageReceived(Long customerId, String name, String message);

    void onDisconnect(Long customerId);
}
