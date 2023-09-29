package Helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class ServerLogger {

    public static final String SERVER_LOG_FILE = "./Server-log.json";

    public synchronized static void logInfo(String message) {

        System.out.println("--------------- New Event ----------------------");
        System.out.println(message);
        System.out.println("--------------- End of Event -------------------\n");
        try {
            writeToLogFile(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearJsonLogfile() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(SERVER_LOG_FILE);
        writer.print("[]");
        writer.close();
    }

    public static void logError(String message) {
        System.err.println(message);
    }

    private static void writeToLogFile(String message) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // construct Type that tells Gson about the generic type
        Type dtoListType = new TypeToken<List<LogEntry>>() {
        }.getType();
        FileReader fr = new FileReader(SERVER_LOG_FILE);
        List<LogEntry> dtos = gson.fromJson(fr, dtoListType);
        fr.close();
        // If it was an empty one create initial list
        dtos = (null == dtos) ? new ArrayList<>() : dtos;

        // Add new item to the list
        dtos.add(new LogEntry(message));
        // No append replace the whole file
        FileWriter fw = new FileWriter(SERVER_LOG_FILE);
        gson.toJson(dtos, fw);
        fw.close();
    }

    private static class LogEntry {
        private String date;
        private String time;
        private String message;

        public LogEntry() {

        }

        public LogEntry(String message) {
            this.date = LocalDate.now().toString();
            this.time = LocalTime.now().toString();
            this.message = message;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

