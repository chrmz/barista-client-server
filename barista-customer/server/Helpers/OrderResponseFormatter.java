package Helpers;

public final class OrderResponseFormatter {

    public static String formatOrder(int numberOfCoffees, int numberOfTeas) {

        String coffeeOrders = numberOfCoffees > 0 ? numberOfCoffees + " coffee" + (numberOfCoffees > 1 ? "s" : "") : "";
        String teaOrders = numberOfTeas > 0 ?  numberOfTeas + " tea" + (numberOfTeas > 1 ? "s" : "") : "";
        String joiner = (!coffeeOrders.isEmpty() && !teaOrders.isEmpty()) ? " and " : "";

        return coffeeOrders + joiner + teaOrders;
    }
}
