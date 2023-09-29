import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Customer {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000)) {

            //Read the output of the server and auto flush the stream to prevent us doing it manually
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);


            new Thread(new Helpers.CustomerRunnable(socket)).start();
            //loop closes when user enters exit command

            do {
                //taking the user input
                Scanner scanner = new Scanner(System.in);
                String userInput = scanner.nextLine().trim();
                if(!userInput.isEmpty()) {
                        output.println(userInput);
                        if (userInput.equals("exit")) {
                            //reading the input from server
                            break;
                        }
                } else {
                    System.out.println("Please enter a valid command");
                }


            } while (true);

        } catch (Exception e) {
            System.out.println("Gracefully shutting down");
        }
    }
}