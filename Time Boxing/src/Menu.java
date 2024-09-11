import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;


public class Menu extends JFrame implements ActionListener {
    JButton login;
    JButton signup;
    JButton Exit;
    JLabel sign;
    JLabel icon;
    private Socket socket;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    Menu() {
        this.setSize(420, 600);
        this.setLayout(null);
        this.setResizable(false);
        this.setVisible(true);
        this.getContentPane().setBackground(Color.white);
        this.setTitle("Time Boxing");


        ImageIcon image = new ImageIcon("TimeBoxingLogo.png");
        icon = new JLabel();
        icon.setIcon(image);
        icon.setBounds(100,0,300,200);
        icon.setVisible(true);
        this.add(icon);


        sign = new JLabel("Login/Sign up");
        sign.setFont(new Font(" ",Font.BOLD,25));
        sign.setForeground(Color.black);
        sign.setBounds(120,170,200,50);
        this.add(sign);




        login = new JButton();
        login.addActionListener(this);
        login.setBounds(100,230,200,50);
        login.setText("Login");
        login.setFocusable(false);
        login.setForeground(Color.black);
        login.setBackground(Color.BLUE);
        login.setFont(new Font(" ",Font.CENTER_BASELINE,15));
        this.add(login);

        signup = new JButton();
        signup.addActionListener(this);
        signup.setBounds(100,290,200,50);
        signup.setText("Sign up");
        signup.setFocusable(false);
        signup.setForeground(Color.white);
        signup.setBackground(Color.black);
        signup.setFont(new Font(" ",Font.CENTER_BASELINE,15));
        this.add(signup);

        Exit = new JButton();
        Exit.addActionListener(this);
        Exit.setBounds(100,350,200,50);
        Exit.setText("Exit");
        Exit.setFocusable(false);
        Exit.setBackground(Color.GRAY);
        Exit.setForeground(Color.white);
        Exit.setFont(new Font(" ",Font.CENTER_BASELINE,15));
        this.add(Exit);




        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(425 , 600);




    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==login){
            Login jPanel = new Login();
            this.dispose();

        }
        if(e.getSource()==signup){
            Signup jPanel = new Signup();
            this.dispose();
        }
        if(e.getSource()==Exit){
            try {
                // send shutdown to server
                socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("shutdown");

                socket.close();
                System.exit(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
