package server;
import client.IClient;
import client.Client;
import func.WhiteBoardShape;
import func.WhiteBoardText;
import java.awt.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.geom.*;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;



public class Server extends UnicastRemoteObject implements IServer {   
    File currentFile;
    public boolean show = false;
    private ServerGUI serverGUI;
    private String adminName;
    CopyOnWriteArrayList<IClient> clients;   
    CopyOnWriteArrayList<String> chatMessage;
    CopyOnWriteArrayList<WhiteBoardShape> diagram;
    DefaultListModel<String> userlist = new DefaultListModel<>();
    private HashSet<String> activeUsernames = new HashSet<>();
    
    
    public CopyOnWriteArrayList<WhiteBoardShape> getList(){
    	return diagram;
    }
    
    public Server(String address, int port, String clientName) throws RemoteException {
        super();
        clients = new CopyOnWriteArrayList<>();
        chatMessage = new CopyOnWriteArrayList<>();
        diagram = new CopyOnWriteArrayList<>();
        this.adminName = clientName;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar Server.jar <serverIPAddress> <port> <clientName>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[1]);
            String clientName = args[2];
            String address = args[0];
            Server server = new Server(args[0], port, args[2]);
            try {
                Registry registry = LocateRegistry.createRegistry(port);
                registry.bind("Server", server);
                System.out.println("Server started at " + address + ":" + port + " with admin " + clientName);

                Client client = new Client(address, port, clientName);
                client.runClient(address, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Server ready");
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (server != null) {
                        server.broadcastShutdown();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }));
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid port number");
        } catch (Exception e) {
            System.err.println("Server Exception: " + e.toString());
            e.printStackTrace();
        }
    }
    

    
    public void addClient(IClient client, String UName) throws RemoteException {
        if (!activeUsernames.contains(UName)) {
            clients.add(client);
            activeUsernames.add(UName); // Add the username to the set of active usernames
            userlist.addElement(UName); // Add to user list model for GUI updates
            System.out.println(UName + " has joined");
           
        } else {
            System.out.println("Connection attempt failed: Username '" + UName + "' is already in use.");
            client.receiveMessage("Connection failed: Username is already in use.");           
            throw new RemoteException("Username '" + UName + "' is already in use.");
        }
    }


    public void removeClient(IClient client) throws RemoteException {
        int number = clients.indexOf(client);
        System.out.println("Client " + number + " has been removed");
        clients.remove(client);
        userlist.remove(number);
    }

    
    
    @Override
    public boolean handleRequest(String user) throws RemoteException {
    	System.out.println("Name : " + user);
    	System.out.println("adminName set to: " + this.adminName);
    	if (adminName == null || user == null) {
            System.out.println("Warning: 'adminName' or 'clientName' is not initialized.");
            return false;
        }
        if(user.equals(adminName)) {
            return true;
        } else {
        	int response = JOptionPane.showConfirmDialog(null, user + " is asking for using this whiteboard. Please choose to if accpet user", "Connection Request", JOptionPane.YES_NO_OPTION);
            return response == JOptionPane.YES_OPTION;
        }
    }
    
    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int color) throws RemoteException {
        WhiteBoardShape Line = new WhiteBoardShape(new Line2D.Double(x1, y1, x2, y2), new Color(color));
        diagram.add(Line);
        for (IClient client : clients) {
            client.draw(Line);
            client.loadDiagram(diagram);
        }
    }

    @Override
    public void drawCircle(int x, int y, int diameter, int color) throws RemoteException {
        WhiteBoardShape Circle = new WhiteBoardShape(new Ellipse2D.Double(x, y, diameter, diameter), new Color(color));
        diagram.add(Circle);
        for (IClient client : clients) {
            client.draw(Circle);
            client.loadDiagram(diagram);
        }
    }
    
    @Override
    public void drawFreeLine(int x1, int y1, int x2, int y2, int color) throws RemoteException {
        WhiteBoardShape Line = new WhiteBoardShape(new Line2D.Float(x1, y1, x2, y2), new Color(color));
        diagram.add(Line);

        for (IClient client : clients) {
            client.draw(Line);
            client.loadDiagram(diagram);
        }
    }
    
    @Override
    public void drawEclipse(int x, int y, int width, int height, int color) throws RemoteException {
        WhiteBoardShape Oval = new WhiteBoardShape(new Ellipse2D.Double(x, y, width, height), new Color(color));
        diagram.add(Oval);
        for (IClient client : clients) {
            client.draw(Oval);
            client.loadDiagram(diagram);
        }
    }
    
    @Override
    public void drawRectangle(int x, int y, int width, int height, int color) throws RemoteException {
        WhiteBoardShape Rectangle = new WhiteBoardShape(new Rectangle2D.Double(x, y, width, height), new Color(color));
        diagram.add(Rectangle);
        for (IClient client : clients) {
            client.draw(Rectangle);
            client.loadDiagram(diagram);
        }
    }
    
    

    
    @Override
    public void drawText(String text, int x, int y, int color) throws RemoteException {
        Font font = new Font("Helvetica", Font.PLAIN, 30);  
        WhiteBoardShape Text = new WhiteBoardShape(new WhiteBoardText(text, x, y, new Color(color), font), new Color(color), text, font);
        diagram.add(Text);
        for (IClient client : clients) {
            client.draw(Text);
            client.loadDiagram(diagram);
        }
    }



    
    @Override
    public void message(String message) throws RemoteException {
        for (IClient client : clients) {
            client.receiveMessage(message);
        }
    }
    
    @Override
    public void loadMessage(String clientName, String message) throws RemoteException {
        // 获取当前时间并格式化为 年-月-日 时:分:秒
        String timeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));       
        String fullMessage = String.format("[%s] %s: %s", timeString, clientName, message);      
        System.out.println("Uploading chat channel message: " + fullMessage);      
        chatMessage.add(fullMessage);       
        broadcastChatMessages();
    }


    @Override
    public void clear() throws RemoteException {
    	diagram.clear();
        for (IClient client : clients) {
            client.clear();
        }
    }
       
    public void handleNew(String clientName, JFrame serverFrame) {
        resetCurrentFile();
        updateServerFrameTitle(serverFrame, clientName);
        clearDiagram();
        notifyFileCreated(serverFrame);
        notifyClientsLoadDiagram();
    }
    
    public void handleSaveAs(String clientName, JFrame serverFrame) {
        File selectedFile = chooseFileToSave(serverFrame, clientName);
        if (selectedFile != null) {
            currentFile = selectedFile;
            saveDiagramToFile(serverFrame, currentFile);
        }
    }

    public void handleClose(String clientName, JFrame serverFrame) {
        resetCurrentFile();
        updateServerFrameTitle(serverFrame, clientName);
        clearDiagram();
        notifyFileClosed(serverFrame);
        notifyClientsLoadDiagram();
    }
    
    public void handleOpen(String clientName, JFrame serverFrame) {
        File selectedFile = chooseFileToOpen(serverFrame);
        if (selectedFile != null) {
            currentFile = selectedFile;
            updateServerFrameTitleWithFile(serverFrame, clientName, currentFile.getName());
            loadDiagramFromFile(serverFrame, currentFile);
            notifyClientsLoadDiagram();
        }
    }
    
    public void handleSave(String clientName, JFrame serverFrame) {
        if (currentFile == null) {
            currentFile = chooseFileToSave(serverFrame, clientName);
        }
        if (currentFile != null) {
            saveDiagramToFile(serverFrame, currentFile);
        }
    }
    
    private void resetCurrentFile() {
        currentFile = null;
    }

    private void updateServerFrameTitle(JFrame serverFrame, String clientName) {
        serverFrame.setTitle(clientName);
    }

    private void clearDiagram() {
        diagram.clear();
    }

    private void notifyFileCreated(JFrame serverFrame) {
        JOptionPane.showMessageDialog(serverFrame, "File has been created");
    }

    private void notifyClientsLoadDiagram() {
        for (IClient client : clients) {
            try {
                client.loadDiagram(diagram);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    private File chooseFileToOpen(JFrame serverFrame) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(serverFrame);
        return returnValue == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null;
    }

    private void updateServerFrameTitleWithFile(JFrame serverFrame, String clientName, String fileName) {
        serverFrame.setTitle(clientName + " - " + fileName);
    }

    private void loadDiagramFromFile(JFrame serverFrame, File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            diagram.clear();
            while (true) {
                try {
                    WhiteBoardShape drawing = (WhiteBoardShape) ois.readObject();
                    diagram.add(drawing);
                } catch (EOFException ex) {
                    break;
                }
            }
            JOptionPane.showMessageDialog(serverFrame, "File has been opened successfully");
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private File chooseFileToSave(JFrame serverFrame, String clientName) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(serverFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            updateServerFrameTitleWithFile(serverFrame, clientName, file.getName());
            return file;
        } else {
            JOptionPane.showMessageDialog(serverFrame, "Save session has been aborted");
            return null;
        }
    }

    private void saveDiagramToFile(JFrame serverFrame, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            for (WhiteBoardShape drawing : diagram) {
                oos.writeObject(drawing);
            }
            JOptionPane.showMessageDialog(serverFrame, "File has been saved successfully");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    

    private void notifyFileClosed(JFrame serverFrame) {
        JOptionPane.showMessageDialog(serverFrame, "File has been closed");
    }

    

    @Override
    public void requestCanvasColorChange(int rgb, String clientName) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(null, clientName + " asking for change canvas color, approve?", "Change Color", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                try {
                    broadcastCanvasColorChange(rgb);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void broadcastChatMessages() {
        for (IClient client : clients) {
            try {
                client.loadMessage(chatMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void broadcastCanvasColorChange(int rgb) throws RemoteException {
        for (IClient client : clients) {
            client.setCanvasColor(rgb);
        }
    }

 // Method to broadcast shutdown message to all clients
    public void broadcastShutdown() throws RemoteException {
        for (IClient client : clients) {
            client.receiveMessage("Whiteboard is closed. See you next time.");
        }
    }

    
    @Override
    public void eraseShape(Rectangle2D eraser) throws RemoteException {
        CopyOnWriteArrayList<WhiteBoardShape> shapesToRemove = new CopyOnWriteArrayList<>();
        for (WhiteBoardShape shape : diagram) {
            if (shape.intersects(eraser)) {
                shapesToRemove.add(shape);               
            }
        }
        diagram.removeAll(shapesToRemove);

        for (IClient client : clients) {
            client.loadDiagram(diagram);
        }
    }

    @Override
    public boolean isAdmin(String clientName) throws RemoteException {
        return clientName.equals(this.adminName); 
    }


    
    @Override
    public CopyOnWriteArrayList<WhiteBoardShape> loadDiagram() throws RemoteException {
        return diagram;
    }

    @Override
    public CopyOnWriteArrayList<String> loadChat() throws RemoteException {
        return chatMessage;
    }
    
    @Override
    public CopyOnWriteArrayList<IClient> getClients() throws RemoteException {
        return clients;
    }

	public ServerGUI getServerGUI() {
		// TODO Auto-generated method stub
		return serverGUI;
	}

	@Override
	public void triggerAdminUI(boolean isAdmin) throws RemoteException {
		if(isAdmin) {
			show = true;
		}
		serverGUI = new ServerGUI(this, adminName);
		
	}
	
	


}
