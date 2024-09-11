import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Login extends JFrame implements ActionListener {
    JButton loginb;

    JButton back;
    JLabel logi;
    JLabel icon;
    public static String username;
    JTextField userField;
    JTextField passField;
    JFrame login;

    Login() {
        login = new JFrame();
        login.setSize(420, 600);
        login.setLayout(null);
        login.setResizable(false);
        login.setVisible(true);
        login.getContentPane().setBackground(Color.white);
        login.setTitle("Time Boxing(Login)");


        ImageIcon image = new ImageIcon("TimeBoxingLogo.png");
        icon = new JLabel();
        icon.setIcon(image);
        icon.setBounds(100, 50, 300, 200);
        icon.setVisible(true);
        login.add(icon);


        logi = new JLabel("Login");
        logi.setFont(new Font(" ", Font.BOLD, 30));
        logi.setForeground(Color.black);
        logi.setBounds(160, 210, 200, 50);
        login.add(logi);

        JLabel user = new JLabel("Username:");
        user.setForeground(Color.black);
        userField = new JTextField();
        userField.setBounds(100, 300, 200, 30);
        user.setBounds(100, 250, 200, 50);
        login.add(user);
        login.add(userField);


        JLabel pass = new JLabel("Password:");
        pass.setForeground(Color.black);
        passField = new JTextField();
        passField.setPreferredSize(new Dimension(50, 30));
        passField.setBounds(100, 370, 200, 30);
        pass.setBounds(100, 325, 200, 50);
        login.add(pass);
        login.add(passField);


        loginb = new JButton();
        loginb.addActionListener(this);
        loginb.setBounds(100, 450, 200, 50);
        loginb.setText("Login");
        loginb.setFocusable(false);
        loginb.setForeground(Color.white);
        loginb.setBackground(Color.black);
        loginb.setFont(new Font(" ", Font.CENTER_BASELINE, 20));
        login.add(loginb);

        login.setSize(425, 600);


        back = new JButton();
        back.addActionListener(this);
        back.setBounds(20, 20, 50, 50);
        ImageIcon backi = new ImageIcon("back.png");
        back.setIcon(backi);
        back.setBorderPainted(false);
        back.setFocusable(false);
        login.add(back);

        login.setSize(425, 600);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginb) {
            // Get inputs and use them
            username = userField.getText();
            String password = passField.getText();
            LocalDate today = LocalDate.now();
            String currentDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            try (Socket socket = new Socket("127.0.0.1", 8080);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a username and password.");
                } else {
                    out.println("login");
                    out.println(username + "," + password);

                    String response = in.readLine();
                    if (response.equals("success")) {
                        // Check if home2 class file exists
                        String filePath = username + "_" + currentDate + "_tasks.txt";
                        out.println("check_file");
                        out.println(filePath);

                        // Give the server some time to process the command
                        Thread.sleep(500);

                        String fileCheckResponse = in.readLine();

                        // Open the appropriate class based on file existence
                        if (fileCheckResponse.equals("file_exists")) {
                            login.dispose();
                            SwingUtilities.invokeLater(() -> new Home2());
                        } else {
                            login.dispose();
                            SwingUtilities.invokeLater(Home::new);
                        }
                    } else {
                        // Authentication failed
                        JOptionPane.showMessageDialog(null, "Invalid username or password. Please try again.");
                    }
                }
            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        if (e.getSource() == back) {
            Menu menu = new Menu();
            login.dispose();
        }
    }
}
