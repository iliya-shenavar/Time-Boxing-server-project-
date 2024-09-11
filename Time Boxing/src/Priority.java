import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStreamReader;


public class Priority extends JFrame implements ActionListener {
    private JFrame priority;
    private JLabel step2;
    private JLabel ex;
    private JLabel ex2;
    private JList<String> tasksList;
    private DefaultListModel<String> listModel;
    private JButton back;
    private JButton prioritizeButton;
    private String username;
    private List<String> selectedTasksList = new ArrayList<>();
    private String[] tasksArray;
    int[] selectedIndices;

    public Priority() {
       String tasks = readFromFile();
        tasksArray = tasks.split("\n");

        priority = new JFrame();
        priority.setSize(900, 700);
        priority.setLayout(null);
        priority.setVisible(true);
        priority.getContentPane().setBackground(Color.white);
        priority.setTitle("Time Boxing(Priority)");
        listModel = new DefaultListModel<>();

        // Add tasks from the array to the list model using a loop
        listModel.addAll(Arrays.asList(tasksArray));

        step2 = new JLabel("Step2");
        step2.setFont(new Font(" ", Font.BOLD, 30));
        step2.setForeground(Color.black);
        step2.setBounds(30, 30, 500, 50);
        priority.add(step2);

        ex = new JLabel("Choose the 3 most important tasks for today. This will help you maintain focus, clarity, and reduce");
        ex.setFont(new Font(" ", Font.BOLD, 15));
        ex.setForeground(Color.gray);
        ex.setBounds(30, 70, 800, 50);
        priority.add(ex);
        ex2 = new JLabel("stress by providing a roadmap for your day. (use CTRL + right click for multiselect)");
        ex2.setFont(new Font(" ", Font.BOLD, 15));
        ex2.setForeground(Color.gray);
        ex2.setBounds(30, 90, 800, 50);
        priority.add(ex2);

        tasksList = new JList<>(listModel);
        tasksList.setBounds(30, 140, 800, 400);
        tasksList.setFont(new Font(" ", Font.PLAIN, 20));
        tasksList.setBorder(new LineBorder(Color.black));
        tasksList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        priority.add(tasksList);

        prioritizeButton = new JButton("Prioritize");
        prioritizeButton.setBounds(700, 580, 130, 40);
        prioritizeButton.setFocusable(false);
        prioritizeButton.setForeground(Color.white);
        prioritizeButton.setBackground(Color.black);
        prioritizeButton.setFont(new Font(" ", Font.CENTER_BASELINE, 15));
        priority.add(prioritizeButton);

        back = new JButton("Previous");
        back.setBounds(560, 580, 130, 40);
        back.setFocusable(false);
        back.setForeground(Color.black);
        back.setBackground(Color.LIGHT_GRAY);
        back.setFont(new Font(" ", Font.CENTER_BASELINE, 15));
        priority.add(back);

        priority.setSize(900, 701);
        setupListeners();

        if (Login.username == null) {
            username = Signup.username;
        } else if (Signup.username == null) {
            username = Login.username;
        }else username = null;
    }


    private void setupListeners() {
        priority.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        back.addActionListener(this);
        prioritizeButton.addActionListener(this);
    }

    private String readFromFile() {
        StringBuilder content = new StringBuilder();
        try (Socket socket = new Socket("127.0.0.1", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            LocalDate today = LocalDate.now();
            String currentDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (Login.username == null) {
                username = Signup.username;
            } else if (Signup.username == null) {
                username = Login.username;
            }else username = null;
            // Send command to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("get_tasks");
            out.println(username + "," + currentDate);

            // Read tasks from the server response
            String line;
            while (!(line = in.readLine()).equals("end_response")) {
                content.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString().trim();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == back) {
            priority.dispose();
            new Tasks();
        } else if (e.getSource() == prioritizeButton) {
            prioritizeTasksAsync();

        }
    }

    private void prioritizeTasksAsync() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                prioritizeTasks();
                return null;
            }

            @Override
            protected void done() {
                if (selectedIndices.length == 3) {
                    priority.dispose();
                    new Timing();
                }
            }
        };

        worker.execute();
    }

    private void prioritizeTasks() {
        selectedIndices = tasksList.getSelectedIndices();

        if (selectedIndices.length != 3) {
            JOptionPane.showMessageDialog(this, "Please select exactly 3 tasks for prioritization.");
            return;
        }

        // Clear the selected tasks list
        selectedTasksList.clear();

        // Add selected tasks to the list
        for (int index : selectedIndices) {
            selectedTasksList.add(listModel.get(index));
        }

        // Sort the selected tasks
        Collections.sort(selectedTasksList);

        // Convert listModel to tasksArray
        tasksArray = new String[listModel.size()];
        listModel.copyInto(tasksArray);

        // Rearrange the tasks: selected tasks first, then the remaining ones
        List<String> rearrangedTasks = new ArrayList<>(selectedTasksList);
        for (String task : tasksArray) {
            if (!selectedTasksList.contains(task)) {
                rearrangedTasks.add(task);
            }
        }

        // Convert the rearranged list back to tasksArray
        tasksArray = rearrangedTasks.toArray(new String[0]);

        // Send tasks to the server
        sendTasksToServer(tasksArray);
    }

    private void sendTasksToServer(String[] tasks) {
        new Thread(() -> {
            try (Socket socket = new Socket("127.0.0.1", 8080);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                LocalDate today = LocalDate.now();
                String currentDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


                // Send command to the server
                out.println("tasks2");
                out.println(username + "," + currentDate);

                // Send tasks line by line
                for (String task : tasks) {
                    out.println(task);
                }

                // Send end_tasks to indicate the end of tasks
                out.println("end_tasks");

                System.out.println("Tasks sent to the server.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}