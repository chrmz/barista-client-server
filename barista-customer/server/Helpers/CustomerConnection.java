package Helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class CustomerConnection extends Thread {

    private boolean disconnected = false;
    private String customerName = "";
    private final PrintWriter output;
    private final BufferedReader input;
    private final CustomerRequestHandler requestHandler;

    public CustomerConnection(Socket socket, CustomerRequestHandler requestHandler) throws IOException {
        this.requestHandler = requestHandler;
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {

            //inifite loop for server
            new Thread(() -> {
                greetCustomer();
                while (true) {

                    String outputString = null;
                    try {
                        outputString = input.readLine();
                        if (outputString == null) {
                            handleDisconnect();
                        } else {
                            this.requestHandler.onMessageReceived(this.getId(), this.customerName, outputString);
                        }
                    } catch (IOException e) {
                        handleDisconnect();
                    }
                    //if user types exit command
                    if (outputString == null || outputString.equals("exit")) {
                        handleDisconnect();
                        break;
                    }
                }
            }).start();

        } catch (Exception e) {
            handleDisconnect();
        }
    }


    public void greetCustomer() {
        try {
            this.output.println("Hello, may i take your name please?");
            this.customerName = input.readLine();
            while (this.customerName.trim().isEmpty()) {
                this.output.println("Sorry, your name cannot be blank. PLease enter your name");
                this.customerName = input.readLine();
            }
        } catch (IOException e) {
            handleDisconnect();
            ServerLogger.logError("Error occured whilst asking name");
        }
        this.requestHandler.onRegisterCustomer(this);
    }

    public String getCustomerName() {
        return customerName;
    }

    public void sendMessage(String message) {
        this.output.println(message);
    }

    private void handleDisconnect() {
        if (!this.disconnected) {
            this.requestHandler.onDisconnect(this.getId());
            this.disconnected = true;
        }
    }

    @Override
    public long getId() {
        return super.getId();
    }
}
