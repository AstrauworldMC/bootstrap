package fr.timeto.astrauworld.bootstrap;

import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.event.SwingerEvent;
import fr.theshark34.swinger.event.SwingerEventListener;
import fr.theshark34.swinger.textured.STexturedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static fr.theshark34.swinger.Swinger.getResourceIgnorePath;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class PopUpMessages extends JPanel implements SwingerEventListener {
    private static final JFrame frame = new JFrame();
    private static JPanel panel = new JPanel();

    private final Image background = getResourceIgnorePath("/PopUpMessages/background.png");

    public static int ERROR_MESSAGE = 0;
    public static int NORMAL_MESSAGE = 1;
    public static int DONE_MESSAGE = 2;
    public static int YES_NO_QUESTION = 3;

    private final STexturedButton okButton = new STexturedButton(Swinger.getResourceIgnorePath("/PopUpMessages/okButton-normal.png"), Swinger.getResourceIgnorePath("/PopUpMessages/okButton-hover.png"));
    private final STexturedButton yesButton = new STexturedButton(Swinger.getResourceIgnorePath("/PopUpMessages/yesButton-normal.png"), Swinger.getResourceIgnorePath("/PopUpMessages/yesButton-hover.png"));
    private final STexturedButton noButton = new STexturedButton(Swinger.getResourceIgnorePath("/PopUpMessages/noButton-normal.png"), Swinger.getResourceIgnorePath("/PopUpMessages/noButton-hover.png"));

    private static Thread ifYesThread = new Thread();
    private static Thread ifNoThread = new Thread();
    private static Thread whenOkClickedThread = new Thread();

    private static void initFrame(String title, String msg, int messageType) {
        Thread t = new Thread(() -> {
            BufferedImage icon;
            if (messageType == ERROR_MESSAGE) {
                icon = Swinger.getResourceIgnorePath("/PopUpMessages/errorIcon.png");
            } else if (messageType == DONE_MESSAGE) {
                icon = Swinger.getResourceIgnorePath("/PopUpMessages/doneIcon.png");
            } else if (messageType == YES_NO_QUESTION) {
                icon = Swinger.getResourceIgnorePath("/PopUpMessages/inWorkIcon.png");
            } else {
                icon = Swinger.getResourceIgnorePath("/PopUpMessages/newIcon.png");
            }

            frame.setTitle(title);
            frame.setSize(350, 225);
            frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setIconImage(icon);
            frame.setResizable(false);
            frame.setContentPane(panel = new PopUpMessages(msg, messageType));

            frame.setVisible(true);
        });
        t.start();
    }

    private PopUpMessages(String message, int messageType) {
        this.setLayout(null);

        Icon icon;
        if (messageType == ERROR_MESSAGE) {
            icon = new ImageIcon(Swinger.getResourceIgnorePath("/PopUpMessages/errorIcon.png"));
        } else if (messageType == DONE_MESSAGE) {
            icon = new ImageIcon(Swinger.getResourceIgnorePath("/PopUpMessages/doneIcon.png"));
        } else if (messageType == YES_NO_QUESTION) {
            icon = new ImageIcon(Swinger.getResourceIgnorePath("/PopUpMessages/inWorkIcon.png"));
        } else {
            icon = new ImageIcon(Swinger.getResourceIgnorePath("/PopUpMessages/newIcon.png"));
        }

        JLabel image = new JLabel();
        image.setBounds(30, 40, 64, 64);
        image.setIcon(icon);
        this.add(image);

        String message1;
        String message2;
        String message3;
        try {
            message1 = message.substring(0, 21);
            try {
                message2 = message.substring(21, 45);
                try {
                    message3 = message.substring(45);
                } catch (Exception e) {
                    message3 = "";
                }
            } catch (Exception e) {
                message2 = message.substring(21);
                message3 = "";
            }
        } catch (Exception e) {
            message1 = message;
            message2 = "";
            message3 = "";
        }


        JTextArea messageArea = new JTextArea();
        if (Objects.equals(message3, "")) {
            if (Objects.equals(message2, "")){
                messageArea.setBounds(125, 65, 180, 60);
            } else {
                messageArea.setBounds(125, 57, 180, 60);
            }
        } else {
            messageArea.setBounds(125, 48, 180, 60);
        }
        messageArea.setForeground(Color.WHITE);
        messageArea.setFont(Bootstrap.kollektifFont.deriveFont(16f));
        messageArea.setCaretColor(Color.RED);
        messageArea.setOpaque(false);
        messageArea.setBorder(null);
        messageArea.setEditable(false);
        messageArea.setAlignmentX(SwingConstants.LEFT);
        messageArea.setAlignmentY(SwingConstants.CENTER);
        messageArea.setText(message1 + System.lineSeparator() + message2 + System.lineSeparator() + message3);
        this.add(messageArea);

        if (Objects.equals(messageType, YES_NO_QUESTION)) {
            yesButton.setBounds(112, 131);
            yesButton.addEventListener(this);
            this.add(yesButton);

            noButton.setBounds(185, 131);
            noButton.addEventListener(this);
            this.add(noButton);
        } else {
            okButton.setBounds(148, 131);
            okButton.addEventListener(this);
            this.add(okButton);
        }

    }

    public static void normalMessage(String title, String message) {
        initFrame(title, message, NORMAL_MESSAGE);
    }

    public static void normalMessage(String title, String message, Thread whenClicked) {
        initFrame(title, message, NORMAL_MESSAGE);

        whenOkClickedThread = whenClicked;
    }

    public static void errorMessage(String title, String message) {
        initFrame(title, message, ERROR_MESSAGE);
    }

    public static void doneMessage(String title, String message) {
        initFrame(title, message, DONE_MESSAGE);
    }

    public static void yesNoMessage(String title, String message, Thread ifYes, Thread ifNo) {
        initFrame(title, message, YES_NO_QUESTION);

        ifYesThread = ifYes;
        ifNoThread = ifNo;

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), this);

    }

    @Override
    public void onEvent(SwingerEvent e) {
        if (e.getSource() == okButton) {
            whenOkClickedThread.start();
            whenOkClickedThread = null;
            frame.dispose();
        } else if (e.getSource() == yesButton) {
            ifYesThread.start();
            ifYesThread = null;
            frame.dispose();
        } else if (e.getSource() == noButton) {
            ifNoThread.start();
            ifNoThread = null;
            frame.dispose();
        }
    }
}
