import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private ServerSocket serverSocket;
    private ExecutorService executorService;


    public static void main(String[] args) {
        new Server().start();
    }

    public Server() {
        executorService = Executors.newFixedThreadPool(10);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is running...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected...");

                executorService.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String command;
            while ((command = in.readLine()) != null) {
                if (command.equals("login")) {
                    String userInfo = in.readLine();
                    String[] userData = userInfo.split(",");
                    String username = userData[0];
                    String password = userData[1];

                    if (isUsernameExists(username) && isPasswordCorrect(username, password)) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("success");

                    } else {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("failure");
                    }
                    if (command.equals("get_username")) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(username);
                    }
                }
                if (command.equals("signup")) {
                    String userInfo = in.readLine();
                    String[] userData = userInfo.split(",");
                    String username = userData[0];
                    String password = userData[1];

                    if (isUsernameExists(username)) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        JOptionPane.showMessageDialog(null, "Username already exists. Please choose a different username.");
                        out.println("username_exists");
                    } else {
                        saveUserToDatabase(username, password);

                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("success");

                    }
                } else if (command.equals("check_file")) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    handleCheckFileCommand(in, out);
                }else if (command.equals("tasks")) {
                    String tasks = in.readLine();
                    System.out.println("Tasks: " + tasks);
                } else if (command.equals("shutdown")) {
                    System.out.println("Shutting down server...");
                    serverSocket.close();
                    System.out.println("Server shutdown successfully.");
                    System.exit(0);
                } else if (command.equals("tasks2")) {
                    String taskInfo;
                    StringBuilder tasks = new StringBuilder();
                    String userInfo = in.readLine();
                    String[] userData = userInfo.split(",");
                    String username = userData[0];
                    String currentDate = userData[1];

                    // Keep reading until the end_tasks string is received
                    while (!(taskInfo = in.readLine()).equals("end_tasks")) {
                        tasks.append(taskInfo).append("\n");
                    }

                    // Save tasks data to file
                    saveTasksToFile(username, currentDate, tasks.toString());

                } else if (command.equals("tasks3")) {
                    String taskInfo;
                    StringBuilder tasks = new StringBuilder();
                    String userInfo = in.readLine();
                    String[] userData = userInfo.split(",");
                    String username = userData[0];
                    String currentDate = userData[1];

                    // Keep reading until the end_tasks string is received
                    while (!(taskInfo = in.readLine()).equals("end_tasks")) {
                        tasks.append(taskInfo).append("\n");
                    }

                    // Save tasks data to file
                     saveEditTasksToFile(username, currentDate, tasks.toString());
                    System.out.println("Tasks received2 and saved.");
                }
                else if (command.equals("save_tasks")) {
                    handleSaveTasksCommand(in);
                }
                else if (command.equals("get_tasks")) {
                    handleGetTasksCommand(in, socket);
                } else if (command.equals("save_tasks_with_times")) {
                    handleSaveTasksCommand(in);
                }
                else if (command.equals("get_tasks_with_times")) {
                    handleGetTasksWithTimesCommand(in, socket);
                }else if (command.equals("read_file")) {
                    handleGetTasksedit(in, socket);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPasswordCorrect(String username, String password) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("user_database.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(",");
                if (userData.length > 1 && userData[0].equals(username) && userData[1].equals(password)) {
                    reader.close();
                    return true; // Correct username and password
                }
            }
            reader.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return false; // Incorrect username or password
    }
    private void handleCheckFileCommand(BufferedReader in, PrintWriter out) throws IOException {
        String filePath = in.readLine();
        File file = new File(filePath);
        if (file.exists()) {
            out.println("file_exists");
        } else {
            out.println("file_not_exists");
        }
    }
    private boolean isUsernameExists(String username) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("user_database.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(",");
                if (userData.length > 0 && userData[0].equals(username)) {
                    reader.close();
                    return true; // Username already exists
                }
            }
            reader.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return false; // Username is not found
    }

    private void saveUserToDatabase(String username, String password) {
        try {
            FileWriter writer = new FileWriter("user_database.txt", true);
            writer.write(username + "," + password + "\n");
            writer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void saveTasksToFile(String username, String currentDate, String tasks) {
        String filePath = username + "_" + currentDate + "_tasks.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Overwrite the existing file with new tasks
            writer.write(tasks);
            System.out.println("Tasks received and saved.");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private void saveEditTasksToFile(String username, String currentDate, String tasks) {
        String filePathh = username + "_" + currentDate + "_edittasks.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePathh))) {
            // Overwrite the existing file with new tasks
            writer.write(tasks);
            System.out.println("Tasks received2 and saved.");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private String readTasksFromFile(String username, String currentDate) {
        String filePath = username + "_" + currentDate + "_tasks.txt";
        StringBuilder tasks = new StringBuilder();

        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    tasks.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tasks.toString();
    }

    private void handleSaveTasksCommand(BufferedReader in) throws IOException {
        String userInfo = in.readLine();
        String[] userData = userInfo.split(",");
        String username = userData[0];
        String currentDate = userData[1];

        String filePath = username + "_" + currentDate + "_tasks.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            String taskWithTimes;
            while (!(taskWithTimes = in.readLine()).equals("end_command")) {
                // Write the new task with times to the file
                writer.write(taskWithTimes + "\n");
            }
            System.out.println("Tasks with times received and saved to: " + filePath);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private void handleGetTasksCommand(BufferedReader in, Socket socket) {
        try {
            String userInfo = in.readLine();
            String[] userData = userInfo.split(",");
            String username = userData[0];
            String currentDate = userData[1];

            // Read tasks from file
            String tasks = readTasksFromFile(username, currentDate);

            // Send tasks to the client
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(tasks);
            out.println("end_response");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetTasksWithTimesCommand(BufferedReader in, Socket socket) {
        try {
            String userInfo = in.readLine();
            String[] userData = userInfo.split(",");
            String username = userData[0];
            String currentDate = userData[1];

            // Read tasks with times from file
            String tasksWithTimes = readTasksWithTimesFromFile(username, currentDate);

            // Send tasks with times to the client
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(tasksWithTimes);
            out.println("end_response");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetTasksedit(BufferedReader in, Socket socket) {
        try {
            String userInfo = in.readLine();
            String[] userData = userInfo.split(",");
            String username = userData[0];
            String currentDate = userData[1];

            // Read tasks from file
            String tasks = readTaskseditFromFile(username, currentDate);

            // Send tasks to the client
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(tasks);
            out.println("end_response");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String readTaskseditFromFile(String username, String currentDate) {
        String filePath = username + "_" + currentDate + "_edittasks.txt";
        StringBuilder tasks = new StringBuilder();

        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    tasks.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tasks.toString();
    }
    private String readTasksWithTimesFromFile(String username, String currentDate) {
        String filePath = username + "_" + currentDate + "_tasks.txt";
        StringBuilder tasksWithTimes = new StringBuilder();

        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    tasksWithTimes.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tasksWithTimes.toString();
    }
}


