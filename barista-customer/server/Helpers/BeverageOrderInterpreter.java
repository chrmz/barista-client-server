package Helpers;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class BeverageOrderInterpreter {
    public static Optional<BeverageOrder> interpret(String command) {
        String[] commandChunks = command.split(" ");
        if(commandChunks.length == 0 ) {
            return Optional.empty();
        }

        if(commandChunks.length < 3) {
            return Optional.empty();
        }
        return handleCommand(Arrays.copyOfRange(commandChunks, 1, commandChunks.length));
    }

    private static Optional<BeverageOrder> handleCommand(String ...chunks) {
        try {
            int teaOrder = getOrder( chunks, "tea");
            int coffeeOrder = getOrder( chunks,"coffee");
            if((teaOrder + coffeeOrder) == 0 || !isValidOrder(chunks)) {
                return Optional.empty();
            }
            return Optional.of(new BeverageOrder(teaOrder, coffeeOrder));
        } catch (RuntimeException e) {
           //
        }
        return Optional.empty();
    }

    public static int getOrder(String[] chunks, String t)
    {
        int index =  IntStream.range(0, chunks.length)
                .filter(i -> chunks[i].toLowerCase().contains(t))
                .findFirst()
                .orElse(-1);

        if(index < 0 ) return 0;

        try {
            return Integer.parseInt(chunks[index - 1]);
        } catch (NumberFormatException e) {
            throw new RuntimeException();
        }
    }

    private static boolean isValidOrder(String ...chunks){
        List<String> commands = Arrays.asList(chunks);
        return commands.stream()
                .anyMatch(s -> s.equals("tea") || s.equals("teas") || s.equals("coffee") || s.equals("coffees"));
    }
}
