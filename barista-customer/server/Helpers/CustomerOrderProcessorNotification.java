package Helpers;

import java.util.Optional;

public interface CustomerOrderProcessorNotification {
    void notifyCompletion(long customerId, int numberOfCoffees, int numberOfTeas);
    void notifyStatusChange(long customerId, Optional<String> areaStatusForCustomer);
}
