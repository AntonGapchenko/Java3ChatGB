import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientGUI extends JFrame {
    private final JPanel chatPanel = new JPanel();
    private final JPanel loginPanel = new JPanel();
    private final JTextArea textArea = new JTextArea();
    private final JTextArea clientsInformation = new JTextArea();
    private final JLabel textInputLabel = new JLabel("Your message :");
    private final JTextField textInput = new JTextField();
    private final JScrollPane js = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final JScrollPane jsClients = new JScrollPane(clientsInformation, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private final JButton sendButton = new JButton("Send");
    private String receiver;
    private final String all = "all";
    private String[] clients = {all};
    private final JComboBox<String> selectClient = new JComboBox<>(clients);

    private final ActionListener listener = event -> {
        String message = textInput.getText();
        if (!message.isEmpty()) {
            if (receiver != null && !receiver.equalsIgnoreCase(all)) {

                message = "/w " + receiver + " " + message;
            }
            this.clientNetwork.sendMessage(message);
            textInput.setText("");
            selectClient.setSelectedItem("all");
            receiver = null;
        }
    };
    private final ClientNetwork clientNetwork = new ClientNetwork();


    public ClientGUI() {

        setCallBacks();
        setMainFrame();
        initilizeChatPanel();
        initializeLoginJPanel();
        this.clientNetwork.connect();
        this.setVisible(true);
    }

    private void setMainFrame() {
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new FlowLayout());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                clientNetwork.sendMessage("/end");
                super.windowClosing(event);
            }
        });
    }

    private void initilizeChatPanel() {
        textArea.setEditable(false);
        clientsInformation.setEditable(false);
        chatPanel.setBackground(Color.white);
        chatPanel.setPreferredSize(new Dimension(490, 490));
        js.setPreferredSize(new Dimension(450, 350));
        jsClients.setPreferredSize(new Dimension(450, 35));
        chatPanel.add(jsClients);
        chatPanel.add(js);
        textInput.setPreferredSize(new Dimension(150, 25));
        chatPanel.add(textInputLabel);
        chatPanel.add(textInput);
        textInput.addActionListener(listener);
        sendButton.addActionListener(listener);
        selectClient.addActionListener(e -> {
            receiver = selectClient.getSelectedItem().toString();
        });
        JLabel toWho = new JLabel("to");
        chatPanel.add(toWho);
        chatPanel.add(selectClient);
        chatPanel.add(sendButton);
        chatPanel.setVisible(false);
        this.add(chatPanel);
    }

    private void initializeLoginJPanel() {
        loginPanel.setBackground(Color.white);
        loginPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        loginPanel.setPreferredSize(new Dimension(300, 150));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Authorization"));
        JTextField login = new JTextField();
        JLabel loginLabel = new JLabel("Your connection identifier : ");
        JLabel passwordLabel = new JLabel("Your password : ");
        JPasswordField password = new JPasswordField();
        login.setPreferredSize(new Dimension(100, 25));
        password.setPreferredSize(new Dimension(100, 25));
        loginPanel.add(loginLabel);
        loginPanel.add(login);
        loginPanel.add(passwordLabel);
        loginPanel.add(password);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(280, 35));
        buttonPanel.setBackground(Color.WHITE);
        JButton button = new JButton("Submit");
        buttonPanel.add(button);
        button.addActionListener(e -> {
            clientNetwork.sendMessage("/auth " + login.getText() + " " + String.valueOf(password.getPassword()));
            login.setText("");
            password.setText("");
        });
        loginPanel.add(buttonPanel);
        loginPanel.setVisible(true);
        this.add(loginPanel);
    }


    private void setCallBacks() {

        this.clientNetwork.setCallOnMsgRecieved(message -> textArea.append(message + "\n"));

        this.clientNetwork.setCallOnChangeClientList(clientsList -> {
            clientsInformation.setText(clientsList);
            clients = (all + " " + clientsList).split("\\s");

            selectClient.setModel(new DefaultComboBoxModel(clients));
        });

        this.clientNetwork.setCallOnAuth(s -> {
            loginPanel.setVisible(false);
            chatPanel.setVisible(true);
        });

        this.clientNetwork.setCallOnError(message -> JOptionPane.showMessageDialog(null, message, "We have a problem",
                JOptionPane.ERROR_MESSAGE));
    }

}
