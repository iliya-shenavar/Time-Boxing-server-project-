import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Home extends JFrame implements ActionListener {

    JFrame home;
    JLabel good;
    JLabel mtn;
    JLabel mtn2;
    JLabel dateLabel;
    JButton start;
    JLabel icon;
    JLabel icon2;
    JLabel timeLabel;
    DateTimeFormatter dateFormatter;
    DateTimeFormatter timeFormatter;




    Home() {
        home = new JFrame();
        home.setSize(900, 700);
        home.setResizable(false);
        home.setLayout(null);
        home.setVisible(true);
        home.getContentPane().setBackground(Color.white);
        home.setTitle("Time Boxing(Home)");

        if (Login.username == null){
            good = new JLabel("Welcome "+ Signup.username);
        } else if (Signup.username == null){
            good = new JLabel("Welcome "+ Login.username);
        }


        good.setFont(new Font(" ", Font.BOLD, 30));
        good.setForeground(Color.black);
        good.setBounds(30, 30, 500, 50);
        home.add(good);


        dateLabel = new JLabel();
        dateLabel.setFont(new Font(" ", Font.BOLD, 15));
        dateLabel.setBounds(35, 70, 200, 30);
        home.add(dateLabel);


        timeLabel = new JLabel();
        timeLabel.setFont(new Font(" ", Font.BOLD, 15));
        timeLabel.setBounds(35, 90, 200, 30);
        home.add(timeLabel);

        ImageIcon image = new ImageIcon("among_us_player.png");
        icon = new JLabel();
        icon.setIcon(image);
        icon.setBounds(0, 250, 250, 250);
        icon.setVisible(true);
        home.add(icon);

        ImageIcon imagee = new ImageIcon("among_us_player2.png");
        icon2 = new JLabel();
        icon2.setIcon(imagee);
        icon2.setBounds(600, 250, 250, 250);
        icon2.setVisible(true);
        home.add(icon2);

        mtn = new JLabel("You haven't time box yet." );
        mtn.setFont(new Font(" ", Font.BOLD, 25));
        mtn.setForeground(Color.black);
        mtn.setBounds(290, 250, 500, 150);
        home.add(mtn);
        mtn2 = new JLabel("Lets make it now!" );
        mtn2.setFont(new Font(" ", Font.BOLD, 25));
        mtn2.setForeground(Color.black);
        mtn2.setBounds(335, 280, 500, 150);
        home.add(mtn2);

        start = new JButton();
        start.addActionListener(this);
        start.setBounds(339, 390, 200, 50);
        start.setText("Start Time-Box");
        start.setFocusable(false);
        start.setForeground(Color.white);
        start.setBackground(Color.black);
        start.setFont(new Font(" ", Font.CENTER_BASELINE, 20));
        home.add(start);

        //set date to shamsi
        Locale iranianLocale = new Locale("fa", "IR");
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", iranianLocale);
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        updateDateTimeLabels();

        //update time
        Timer timer = new Timer(1000, this);
        timer.start();

        home.setSize(900,701);
    }


    private void updateDateTimeLabels() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(dateFormatter);
        dateLabel.setText(formattedDate);

        LocalTime now = LocalTime.now();
        String formattedTime = now.format(timeFormatter);
        timeLabel.setText(formattedTime);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            updateDateTimeLabels();
        }
        if (e.getSource() == start) {
            new Tasks();
            home.dispose();
        }
    }
}
