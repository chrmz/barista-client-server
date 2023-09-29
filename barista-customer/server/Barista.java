

import java.io.IOException;

public class Barista {

    public static void main(String[] args) throws IOException {
        System.out.println("The 'Virtual Café' is up and running\nType help for available server command");

        Helpers.ServerLogger.clearJsonLogfile();
        Helpers.BaristaProcessor.getInstance().startServer();
    }

}