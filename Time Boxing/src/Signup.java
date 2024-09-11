import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Signup extends JFrame implements ActionListener {
    JButton signup;
    JButton back;
    JLabel sug;
    JLabel icon;
    JTextField userField;
    JTextField passField;
    JFrame sign;
    public static String username;



    Signup() {
        sign = new JFrame();
        sign.setSize(420, 600);
        sign.setResizable(false);
        sign.setLayout(null);
        sign.setVisible(true);
        sign.getContentPane().setBackground(Color.white);
        sign.setTitle("Time Boxing(SignUp)");


        ImageIcon image = new ImageIcon("TimeBoxingLogo.png");
        icon = new JLabel();
        icon.setIcon(image);
        icon.setBounds(100, 50, 300, 200);
        icon.setVisible(true);
        sign.add(icon);


        sug = new JLabel("SignUp");
        sug.setFont(new Font(" ", Font.BOLD, 30));
        sug.setForeground(Color.black);
        sug.setBounds(150, 210, 200, 50);
        sign.add(sug);


        JLabel user = new JLabel("Username:");
        user.setForeground(Color.black);
        userField = new JTextField();
        userField.setPreferredSize(new Dimension(50, 30));
        userField.setBounds(100, 310, 200, 30);
        user.setBounds(100, 260, 200, 50);
        sign.add(user);
        sign.add(userField);


        JLabel pass = new JLabel("Password:");
        pass.setForeground(Color.black);
        passField = new JTextField();
        passField.setPreferredSize(new Dimension(50, 30));
        passField.setBounds(100, 390, 200, 30);
        pass.setBounds(100, 340, 200, 50);
        sign.add(pass);
        sign.add(passField);

        signup = new JButton();
        signup.addActionListener(this);
        signup.setBounds(100,450,200,50);
        signup.setText("SignUp");
        signup.setFocusable(false);
        signup.setForeground(Color.white);
        signup.setBackground(Color.black);
        signup.setFont(new Font(" ", Font.CENTER_BASELINE, 20));
        sign.add(signup);

        back = new JButton();
        back.addActionListener(this);
        back.setBounds(20, 20, 50, 50);
        ImageIcon backi = new ImageIcon("back.png");
        back.setIcon(backi);
        back.setBorderPainted(false);
        back.setFocusable(false);
        sign.add(back);

        sign.setSize(425 , 600);
    }
    private void sendToServer(String command, String username, String password) {
        try (Socket socket = new Socket("127.0.0.1", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // ارسال دستور به سرور
            out.println(command);

            // ارسال اطلاعات کاربر به سرور
            out.println(username + "," + password);

            // دریافت پاسخ از سرور
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            if (response.equals("success")) {
                Home home = new Home();
                sign.dispose();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == signup) {
            username = userField.getText();
            String password = passField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a username and password.");
            } else {
                sendToServer("signup", username, password);
            }

        }


        if (e.getSource() == back) {
            Menu menu = new Menu();
            sign.dispose();
        }
    }

}
