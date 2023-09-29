package Helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class CustomerRunnable implements Runnable {

    private Socket socket;
    private BufferedReader input;
    // private PrintWriter output;

    public CustomerRunnable(Socket s) throws IOException {
        this.socket = s;
        this.input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
    }
    @Override
    public void run() {
        
            try {
                while(true) {
                    if(input == null) {
                        System.out.println("Gracefully shutting down");
                    } else {
                        String response = input.readLine();
                        System.out.println(response);
                    }
                }
            } catch (IOException e) {
                System.out.println("Gracefully shutting down");
            } finally {
                try {
                    if(input != null) {
                        input.close();
                    }
                } catch (Exception e) {
                    System.out.println("Gracefully shutting down");
                }
            }
    }
    
}
