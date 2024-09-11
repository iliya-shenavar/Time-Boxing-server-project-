import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;

public class Home2 extends JFrame implements ActionListener {
    private JFrame home2;
    private JLabel good;
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JList<String> tasksList;
    private DefaultListModel<String> tasksListModel;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private boolean tasksUpdated = false;
    JButton editButton;

    public Home2() {

        home2 = new JFrame();
        home2.setSize(900, 700);
        home2.setResizable(false);
        home2.setLayout(null);
        home2.setVisible(true);
        home2.getContentPane().setBackground(Color.white);
        home2.setTitle("Time Boxing(Home2)");

        if (Login.username == null) {
            good = new JLabel("Welcome back " + Signup.username);
        } else if (Signup.username == null) {
            good = new JLabel("Welcome back " + Login.username);
        }

        good.setFont(new Font(" ", Font.BOLD, 30));
        good.setForeground(Color.black);
        good.setBounds(30, 30, 500, 50);
        home2.add(good);

        dateLabel = new JLabel();
        dateLabel.setFont(new Font(" ", Font.BOLD, 15));
        dateLabel.setBounds(35, 70, 200, 30);
        home2.add(dateLabel);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font(" ", Font.BOLD, 15));
        timeLabel.setBounds(35, 90, 200, 30);
        home2.add(timeLabel);

        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        updateDateTimeLabels();

        tasksListModel = new DefaultListModel<>();
        tasksList = new JList<>(tasksListModel);
        tasksList.setBounds(30, 140, 800, 400);
        tasksList.setBorder(new LineBorder(Color.black));
        tasksList.setFont(new Font(" ", Font.PLAIN, 20));
        home2.add(tasksList);


        editButton = new JButton();
        editButton.addActionListener(this);
        editButton.setBounds(680, 580, 150, 40);
        editButton.setText("Edit Timebox");
        editButton.setFocusable(false);
        editButton.setForeground(Color.white);
        editButton.setBackground(Color.black);
        editButton.setFont(new Font(" ", Font.CENTER_BASELINE, 15));
        home2.add(editButton);


        home2.setSize(900, 701);



        Timer timer = new Timer(1000, this);
        timer.start();

        home2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void updatePrioritizedTasksList(List<String> tasks) {
        List<String> sortedTasks = sortTasksByStartTime(tasks);
        setPrioritizedTasks(sortedTasks);
    }

    public void setPrioritizedTasks(List<String> tasks) {
        tasksListModel.clear();
        tasksListModel.addAll(tasks);
    }

    private List<String> requestTasksFromServer(String username, String currentDate) {
        List<String> tasks = new ArrayList<>();

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send the "get_tasks" command to the server
            out.println("get_tasks");

            // Send user information (username and current date)
            String userInfo = username + "," + currentDate;
            out.println(userInfo);

            // Receive tasks from the server
            String taskInfo;
            StringBuilder receivedTasks = new StringBuilder();
            while (!(taskInfo = in.readLine()).equals("end_response")) {
                receivedTasks.append(taskInfo).append("\n");
                tasks = receivedTasks.toString().lines().toList();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert the received tasks to a list
        return tasks;
    }

    private String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        return currentDate.format(dateFormatter);
    }

    private void updateDateTimeLabels() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(dateFormatter);
        dateLabel.setText(formattedDate);

        LocalTime now = LocalTime.now();
        String formattedTime = now.format(timeFormatter);
        timeLabel.setText(formattedTime);
    }
    private List<String> sortTasksByStartTime(List<String> tasks) {
        List<String> sortedTasks = new ArrayList<>(tasks);

        Collections.sort(sortedTasks, new Comparator<String>() {
            @Override
            public int compare(String task1, String task2) {
                // Extract start times from tasks and compare them
                LocalTime startTime1 = getStartTimeFromTask(task1);
                LocalTime startTime2 = getStartTimeFromTask(task2);
                return startTime1.compareTo(startTime2);
            }
        });

        return sortedTasks;
    }

    private LocalTime getStartTimeFromTask(String task) {
        // Extract start time from the task string
        String[] parts = task.split(":\\s+");
        if (parts.length == 2) {
            String timeRangeStr = parts[1];
            // Assuming timeRangeStr has the format "HH:mm - HH:mm"
            String[] timeRangeParts = timeRangeStr.split("\\s+-\\s+");
            if (timeRangeParts.length == 2) {
                return LocalTime.parse(timeRangeParts[0]);
            }
        }
        // Return a default value or handle the error according to your needs
        return LocalTime.MIN;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            // If this event is from the Timer, update the date and time
            updateDateTimeLabels();
            updateTaskStylesBasedOnTime();
        }
        if (tasksUpdated == false) {
            String username = getUsername();
            String currentDate = getCurrentDate();
            List<String> tasks = requestTasksFromServer(username, currentDate);
            updatePrioritizedTasksList(tasks);
            tasksUpdated = true;
        }
        if (e.getSource() == editButton) {
            new TasksEdit();
            home2.dispose();
        }


    }

    private String getUsername() {
        if (Login.username != null) {
            return Login.username;
        } else {
            return Signup.username;
        }
    }

    private void updateTaskStylesBasedOnTime() {
        LocalTime currentTime = LocalTime.now();

        for (int i = 0; i < tasksListModel.size(); i++) {
            String task = tasksListModel.getElementAt(i);
            LocalTime taskTime = extractTaskTime(task);

            if (taskTime != null) {
                if (currentTime.isAfter(taskTime)) {
                    tasksList.setForeground(Color.GRAY);
                    tasksList.setFont(tasksList.getFont().deriveFont(Font.PLAIN));
                } else {
                    tasksList.setForeground(Color.BLACK);
                    tasksList.setFont(tasksList.getFont().deriveFont(Font.BOLD));
                }
            }
        }
    }
    private LocalTime extractTaskTime(String task) {
        // Assuming the time is formatted as "HH:mm:ss" at the end of the task string
        try {
            String[] parts = task.split("\\s+");
            String timeString = parts[parts.length - 1];
            return LocalTime.parse(timeString, timeFormatter);
        } catch (Exception e) {
            return null;
        }
    }
}