package Helpers;



public class BeveragePreparer implements Runnable {
    private final Beverage beverage;
    private final BeverageTaskListener taskListener;

    public BeveragePreparer(Beverage beverage, BeverageTaskListener taskListener) {
        this.beverage = beverage;
        this.taskListener = taskListener;
    }

    @Override
    public void run() {
        try {
            ServerLogger.logInfo("Brewing " + beverage.name() + " for customer (" + beverage.getOwnerName() +")");
            Thread.sleep(beverage.getPreparationTime());
            ServerLogger.logInfo("Done brewing " + beverage.name() + " for customer  ("  + beverage.getOwnerName()+")");
            this.taskListener.doneBrewing();
        } catch (InterruptedException e) {
            //
        }
    }
}

