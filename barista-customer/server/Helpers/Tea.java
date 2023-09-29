package Helpers;

public class Tea extends Beverage{
    public static final String NAME = "Tea";

    public Tea(long ownerId, String ownerName) {
        super(NAME, ownerId, ownerName);
    }

    @Override
    public long getPreparationTime() {
        return 30000;
    }
}
