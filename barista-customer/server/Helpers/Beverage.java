package Helpers;

public abstract class Beverage {
    private final String name;
    private long ownerId;

    private String ownerName;

    public Beverage(String name, long ownerId, String ownerName) {
        this.ownerId = ownerId;
        this.name = name;
        this.ownerName = ownerName;
    }

    public void setOwnerName(String ownerName){
        this.ownerName = ownerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String name(){
        return this.name;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(long ownerId){
        this.ownerId = ownerId;
    }

    abstract long getPreparationTime();

}
