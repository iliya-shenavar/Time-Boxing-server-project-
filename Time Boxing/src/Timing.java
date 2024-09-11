import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Timing extends JFrame implements ActionListener {
    private JFrame timing;
    private JLabel step3;
    private JLabel ex;
    private JLabel ex2;
    private JList<String> prioritizedTasksList;
    private DefaultListModel<String> prioritizedTasksModel;
    private JButton Done;
    private JButton back;

    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;

    private static final String COMMAND_GET_TASKS = "get_tasks";

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private List<TimeRange> enteredTimeRanges;

    public Timing() {
        timing = new JFrame();
        timing.setSize(900, 700);
        timing.setLayout(null);
        timing.setVisible(true);
        timing.getContentPane().setBackground(Color.white);
        timing.setTitle("Timing");

        step3 = new JLabel("Step3");
        step3.setFont(new Font(" ", Font.BOLD, 30));
        step3.setForeground(Color.black);
        step3.setBounds(30, 30, 500, 50);
        timing.add(step3);

        ex = new JLabel("Choose the tasks you want to do today and allocate a time box for each. All your priorities");
        ex.setFont(new Font(" ", Font.BOLD, 15));
        ex.setForeground(Color.gray);
        ex.setBounds(30, 70, 800, 50);
        timing.add(ex);
        ex2 = new JLabel("tasks should chosen.");
        ex2.setFont(new Font(" ", Font.BOLD, 15));
        ex2.setForeground(Color.gray);
        ex2.setBounds(30, 90, 800, 50);
        timing.add(ex2);

        prioritizedTasksModel = new DefaultListModel<>();

        prioritizedTasksList = new JList<>(prioritizedTasksModel);
        prioritizedTasksList.setBounds(30, 140, 800, 400);
        prioritizedTasksList.setFont(new Font(" ", Font.PLAIN, 20));
        prioritizedTasksList.setBorder(new LineBorder(Color.black));
        prioritizedTasksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timing.add(prioritizedTasksList);

        Done = new JButton("Done");
        Done.setBounds(700, 580, 150, 40);
        Done.setFocusable(false);
        Done.setForeground(Color.white);
        Done.setBackground(Color.black);
        Done.setFont(new Font(" ", Font.CENTER_BASELINE, 15));
        timing.add(Done);

        back = new JButton();
        back.addActionListener(this);
        back.setBounds(560, 580, 130, 40);
        back.setText("Previous");
        back.setFocusable(false);
        back.setForeground(Color.black);
        back.setBackground(Color.LIGHT_GRAY);
        back.setFont(new Font(" ", Font.CENTER_BASELINE, 15));
        timing.add(back);

        // Create spinners for start and end times
        startTimeSpinner = createTimeSpinner();
        endTimeSpinner = createTimeSpinner();

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Select start time:"));
        panel.add(startTimeSpinner);
        panel.add(new JLabel("Select end time:"));
        panel.add(endTimeSpinner);

        timing.add(panel);

        timing.setSize(900, 701);

        // Request tasks from the server and update the list immediately upon opening
        String username = getUsername();
        String currentDate = getCurrentDate();
        List<String> tasks = requestTasksFromServer(username, currentDate);
        updatePrioritizedTasksList(tasks);

        enteredTimeRanges = new ArrayList<>();
        setupListeners();
    }

    private void setupListeners() {
        timing.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Done.addActionListener(this);
        prioritizedTasksList.addListSelectionListener(e -> handleTaskSelection());
    }


    private void handleTaskSelection() {
        String selectedTask = prioritizedTasksList.getSelectedValue();
        if (selectedTask != null) {
            // Determine whether the task is empty
            boolean taskIsEmpty = selectedTask.trim().isEmpty();

            // If the task is empty, return
            if (taskIsEmpty) {
                return;
            }

            JPanel panel = new JPanel(new GridLayout(3, 2));

            // Add a spinner for start time
            JSpinner startTimeSpinner = createTimeSpinner();
            panel.add(new JLabel("Select start time for task " + selectedTask + ":"));
            panel.add(startTimeSpinner);

            // Add a spinner for end time
            JSpinner endTimeSpinner = createTimeSpinner();
            panel.add(new JLabel("Select end time for task " + selectedTask + ":"));
            panel.add(endTimeSpinner);

            int result = JOptionPane.showConfirmDialog(null, panel, "Select Times", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                LocalTime startTime = getTimeFromSpinner(startTimeSpinner);
                LocalTime endTime = getTimeFromSpinner(endTimeSpinner);

                // Validate that both start time and end time are non-empty
                if (startTime != null && endTime != null) {
                    // Validate the time range (add your validation logic here)
                    if (isValidTimeRange(selectedTask, startTime, endTime)) {
                        // Update the task with the entered time range
                        updateTaskWithTimes(selectedTask, startTime, endTime);
                    } else {
                        JOptionPane.showMessageDialog(timing, "Invalid time range or overlapping. Please try again.");
                    }
                } else {
                    JOptionPane.showMessageDialog(timing, "Please select both start and end times.");
                }
            }
        }
    }

    private static class TimeRange {
        private final String task;
        private final LocalTime startTime;
        private final LocalTime endTime;

        // Constructor for TimeRange when creating an instance from user input
        public TimeRange(String task, LocalTime startTime, LocalTime endTime) {
            this.task = task;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        // Constructor for TimeRange when creating an instance from server response
        public TimeRange(String taskWithTimes) {
            // Assuming taskWithTimes has the format "task (end_time - start_time)"
            String[] parts = taskWithTimes.split(" ");
            this.task = parts[0];
            this.endTime = LocalTime.parse(parts[2]);
            this.startTime = LocalTime.parse(parts[4]);
        }

        public String getTask() {
            return task;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public boolean overlapsWith(TimeRange other) {
            return !((endTime.compareTo(other.startTime) <= 0) || (startTime.compareTo(other.endTime) >= 0));
        }
    }

    private boolean isValidTimeRange(String task, LocalTime startTime, LocalTime endTime) {
        TimeRange newTimeRange = new TimeRange(task, startTime, endTime);
        for (TimeRange existingTimeRange : enteredTimeRanges) {
            if (newTimeRange.overlapsWith(existingTimeRange)) {
                return false; // Overlapping time ranges
            }
        }
        return startTime.isBefore(endTime);
    }

    private void updateTaskWithTimes(String selectedTask, LocalTime startTime, LocalTime endTime) {
        TimeRange timeRange = new TimeRange(selectedTask, startTime, endTime);

        // Assuming each task has a unique identifier or you can modify your tasks list accordingly
        int selectedIndex = prioritizedTasksModel.indexOf(selectedTask);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String updatedTask = selectedTask + ":     " + startTime.format(formatter) + " - " + endTime.format(formatter) ;
        prioritizedTasksModel.setElementAt(updatedTask, selectedIndex);

        // Add the entered time range to the list
        enteredTimeRanges.add(timeRange);
    }

    private JSpinner createTimeSpinner() {
        SpinnerModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);
        return spinner;
    }
    private LocalTime getTimeFromSpinner(JSpinner spinner) {
        java.util.Date date = (java.util.Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private void saveTasksWithTimesToServer(String username, String currentDate, List<TimeRange> timeRanges) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("save_tasks_with_times");  // New command to handle tasks with times
            out.println(username + "," + currentDate);

            // Send each time range as a separate line
            for (TimeRange timeRange : timeRanges) {
                // Format LocalTime objects to use only HH:mm
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String startTimeStr = timeRange.getStartTime().format(formatter);
                String endTimeStr = timeRange.getEndTime().format(formatter);

                out.println(timeRange.getTask() + ":     " + startTimeStr + " - " + endTimeStr);
            }

            out.println("end_command");
            clearTasksFile();

            // Clear the tasks file on the server
            out.println("clear_tasks_file");

            // Read the server response if needed
            List<String> response = readMultiLineResponse(in);
            // Process the response as needed

        } catch (IOException ex) {
            ex.printStackTrace();
            // Handle exception
        }
    }

    private List<String> readMultiLineResponse(BufferedReader in) throws IOException {
        List<String> responseLines = new ArrayList<>();
        String line;
        while (!(line = in.readLine()).equals("end_response")) {
            responseLines.add(line);
        }
        return responseLines;
    }

    private List<String> sendCommandToServer(String command, String data) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(command);
            out.println(data);
            out.println("end_command");

            return readMultiLineResponse(in);

        } catch (IOException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<String> requestTasksFromServer(String username, String currentDate) {
        return sendCommandToServer(COMMAND_GET_TASKS, username + "," + currentDate);
    }

    private void updatePrioritizedTasksList(List<String> tasks) {
        setPrioritizedTasks(tasks);
    }

    public void setPrioritizedTasks(List<String> tasks) {
        prioritizedTasksModel.clear();
        prioritizedTasksModel.addAll(tasks);
    }

    private String getUsername() {
        String username = "";

        if (Login.username == null) {
            username = Signup.username;
        } else if (Signup.username == null) {
            username = Login.username;
        }

        return username;
    }

    private String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }


    private void clearTasksFile () {
        try (PrintWriter writer = new PrintWriter("tasks.txt")) {
            // Writing nothing to the file will effectively clear its contents
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Done) {
            String username = getUsername();
            String currentDate = getCurrentDate();
            Home2 home2 = new Home2();
            timing.dispose();
            // Use SwingWorker to perform network operations in a separate thread
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Request the server to save the tasks with times
                    saveTasksWithTimesToServer(username, currentDate, enteredTimeRanges);
                    return null;
                }

                @Override
                public void done() {
                    // Handle completion if needed
                }
            };

            // Execute the SwingWorker
            worker.execute();
        }
        if (e.getSource() == back){
            new Priority();
            timing.dispose();
        }
    }
}
