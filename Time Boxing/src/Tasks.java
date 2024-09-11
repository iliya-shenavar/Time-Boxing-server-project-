import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.InputStreamReader;



public class Tasks extends JFrame implements ActionListener {
    JFrame tasks;
    JLabel step1;
    JLabel ex;
    JLabel ex2;
    JTextArea tasksField;
    JButton saveButton;
    JButton back;

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private String username;
    Tasks() {

        tasks = new JFrame();
        tasks.setSize(900, 700);
        tasks.setResizable(false);
        tasks.setLayout(null);
        tasks.setVisible(true);
        tasks.getContentPane().setBackground(Color.white);
        tasks.setTitle("Time Boxing(Tasks)");


        step1 = new JLabel("Step1");
        step1.setFont(new Font(" ", Font.BOLD, 30));
        step1.setForeground(Color.black);
        step1.setBounds(30, 30, 500, 50);
        tasks.add(step1);

        ex = new JLabel("Write any tasks you want to do. Write each task on one line.They will be saved and");
        ex.setFont(new Font(" ", Font.BOLD, 15));
        ex.setForeground(Color.gray);
        ex.setBounds(30, 70, 800, 50);
        tasks.add(ex);
        ex2 = new JLabel("displayed here every time you return to this step until you choose them in step3.");
        ex2.setFont(new Font(" ", Font.BOLD, 15));
        ex2.setForeground(Color.gray);
        ex2.setBounds(30, 90, 800, 50);
        tasks.add(ex2);


        tasksField = new JTextArea();
        tasksField.setBounds(30, 140,800, 400);
        tasksField.setBorder(new LineBorder(Color.black));
        tasksField.setFont(new Font(" ", Font.PLAIN, 20));
        tasks.add(tasksField);



        saveButton = new JButton();
        saveButton.addActionListener(this);
        saveButton.setBounds(700, 580, 130, 40);
        saveButton.setText("Next");
        saveButton.setFocusable(false);
        saveButton.setForeground(Color.white);
        saveButton.setBackground(Color.black);
        saveButton.setFont(new Font(" ", Font.CENTER_BASELINE, 15));
        tasks.add(saveButton);


        back = new JButton();
        back.addActionListener(this);
        back.setBounds(560, 580, 130, 40);
        back.setText("Previous");
        back.setFocusable(false);
        back.setForeground(Color.black);
        back.setBackground(Color.LIGHT_GRAY);
        back.setFont(new Font(" ", Font.CENTER_BASELINE, 15));
        tasks.add(back);
        tasks.setSize(900,701);

        if (Login.username == null){
            username =  Signup.username;
        } else if (Signup.username == null){
            username = Login.username;
        } else username = null;
        requestFileContent();

    }

    private void requestFileContent() {
        StringBuilder content = new StringBuilder();
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            LocalDate today = LocalDate.now();
            String currentDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Send command to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("read_file");
            out.println(username + "," + currentDate);

            // Read tasks from the server response
            String line;
            while (!(line = in.readLine()).equals("end_response")) {
                content.append(line).append("\n");
            }

            // Display the received content in the TextArea
            tasksField.setText(content.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            try {
                //Send what in filed to server
                String tasks = tasksField.getText();


                if (Login.username == null){
                    username =  Signup.username;
                } else if (Signup.username == null){
                    username = Login.username;
                } else username = null;
                LocalDate today = LocalDate.now();
                String currentDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                //Send information to server
                String[] lines = tasks.split("\n");
                socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("tasks2");
                out.println(username + "," + currentDate) ;
                out.println(tasks);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                out.println("end_tasks");
                out.println("tasks3");
                out.println(username + "," + currentDate) ;
                out.println(tasks);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                out.println("end_tasks");

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            new Priority();
            tasks.dispose();
        }
        if (e.getSource() == back){
            new Home();
            tasks.dispose();
        }

    }
}
