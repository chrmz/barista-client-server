package Helpers;

public class Coffee extends Beverage{
    public static final String NAME = "Coffee";

    public Coffee(long ownerId, String ownerName) {
        super(NAME, ownerId, ownerName);
    }

    @Override
    public long getPreparationTime() {
        return 45000;
    }
}
