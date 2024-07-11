package server;


import client.IClient;
import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class ServerGUI {
    private JFrame serverFrame;
    private DefaultListModel<String> listModel;
    private Server server;

    public ServerGUI(Server server, String clientName) {
        this.server = server;
        this.listModel = server.userlist;
        initializeGUI(clientName);
    }

    private void initializeGUI(String clientName) {
        serverFrame = new JFrame("Whiteboard Server - " + clientName);
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.setLayout(new BorderLayout());
        serverFrame.setSize(400, 500);

        setupMenuBar(clientName);
        setupClientList();
        if(server.show) {
        	serverFrame.setVisible(true);
        }
        else {
        	serverFrame.setVisible(false);
        }
    }

    private void setupMenuBar(String clientName) {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(e -> server.handleNew(clientName,serverFrame));
        menu.add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(e -> server.handleOpen(clientName,serverFrame));
        menu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> server.handleSave(clientName,serverFrame));
        menu.add(saveMenuItem);

        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.addActionListener(e -> server.handleSaveAs(clientName,serverFrame));
        menu.add(saveAsMenuItem);

        JMenuItem closeMenuItem = new JMenuItem("Close");
        closeMenuItem.addActionListener(e -> server.handleClose(clientName,serverFrame));
        menu.add(closeMenuItem);

        serverFrame.setJMenuBar(menuBar);
    }
    
    private void setupClientList() {
        JList<String> clientList = createClientList();
        JScrollPane scrollPane = new JScrollPane(clientList);
        serverFrame.add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = createInfoLabel();
        serverFrame.add(infoLabel, BorderLayout.NORTH);

        JButton removeClientButton = createRemoveClientButton(clientList);
        serverFrame.add(removeClientButton, BorderLayout.SOUTH);

        addClientListSelectionListener(clientList, removeClientButton);
        addWindowClosingListener();
    }

    private JList<String> createClientList() {
        JList<String> clientList = new JList<>(listModel);
        return clientList;
    }

    private JLabel createInfoLabel() {
        return new JLabel("Existing users:");
    }

    private JButton createRemoveClientButton(JList<String> clientList) {
        JButton removeClientButton = new JButton("Kick Client Out");
        removeClientButton.addActionListener(e -> {
            int selectedIndex = clientList.getSelectedIndex();
            if (selectedIndex != -1) {
                try {
                    IClient selectedClient = server.getClients().get(selectedIndex);
                    selectedClient.clear();
                    selectedClient.receiveMessage("You have been kicked out.");
                    selectedClient.loadDiagram(server.getList());
                    server.removeClient(selectedClient);
                    listModel.remove(selectedIndex);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
        return removeClientButton;
    }

    private void addClientListSelectionListener(JList<String> clientList, JButton removeClientButton) {
        clientList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = clientList.getSelectedIndex();
                removeClientButton.setEnabled(selectedIndex != -1);
            }
        });
    }

    private void addWindowClosingListener() {
        serverFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
					for (IClient client : server.getClients()) {
					    try {
					        client.receiveMessage("Whiteboard is closed. See you next time.");
					    } catch (RemoteException e) {
					        e.printStackTrace();
					    }
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
            }
        });
    }

    
    public JFrame getServerFrame() {
	    return serverFrame;
	}
}

