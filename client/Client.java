package client;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import javax.swing.*;
import java.awt.Insets;
import server.IServer;
import server.Server;
import func.WhiteBoardShape;
import func.WhiteBoardText;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.ConnectException;
import java.util.concurrent.CopyOnWriteArrayList;



public class Client extends UnicastRemoteObject implements IClient {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IServer server;
	private JFrame frame;
    private JPanel panel;
    private int startX, startY;
    private BufferedImage drawingCanvas;
    private Graphics2D canvasGraphics;
    private String clientName;
    private JTextArea chattingFrame;
    private JTextField chattingField;
    private JPanel chattingPanel;
    private JLabel usingTool;
    private Shape tempShape = null;
    private boolean ifDrawing = false;
    private boolean ifTexting = false;
    private boolean ifServerAlive = true;
    private Color drawColor = Color.BLACK;
    private Color canvasBackgroundColor = Color.WHITE;
    private Tool currentTool = Tool.FREE_DRAW;
    
    
    
    
    public Client(String serverIPAddress, int serverPort, String clientName) throws RemoteException {
        super();
        this.clientName = clientName;
        // init the drawing canvas with defined width, height, and color model
        drawingCanvas = new BufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB);
        canvasGraphics = (Graphics2D) drawingCanvas.getGraphics();
        // set the background color of the canvas to white and fill it
        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar Client.jar <serverIPAddress> <serverPort> <clientName>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[1]);
            String clientName = args[2];
            String address = args[0];
            
            try {
                Client client = new Client(address, port, clientName);
                client.runClient(address, port);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid port number, please double check");
        } catch (Exception e) {
            System.err.println("Client Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    
    public void runClient(String serverIPAddress, int serverPort) {
        if (!tryConnectToServer(serverIPAddress, serverPort)) {
            System.exit(0);
        }

        initializeGUI();
        configureEventHandlers();
        loadDataFromServer();
    }
    


 // 初始化图形用户界面组件
    private void initializeGUI() {
        frame = new JFrame("Whiteboard: " + clientName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JToolBar toolbar = createToolbar(); // 创建工具栏
        JToolBar colorbar = createColorBar(); // 创建颜色选择器
        panel = createCanvas(); // 创建绘图面板
        panel.setPreferredSize(new java.awt.Dimension(900, 500));
        // 设置drawingPanel的布局，并将颜色选择器放在顶部
        JPanel drawing = new JPanel(new BorderLayout());
        drawing.add(colorbar, BorderLayout.NORTH);
        drawing.add(panel, BorderLayout.CENTER);

        // 创建主面板，将工具栏放在左侧，drawingPanel放在中间
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.WEST);
        mainPanel.add(drawing, BorderLayout.CENTER);
        createchattingPanel(); // 初始化聊天面板
        setupSplitPane(mainPanel, chattingPanel); // 设置带有聊天面板的拆分窗格
        frame.setVisible(true);
    }

    

    // 从服务器加载初始数据
    private void loadDataFromServer() {
        try {
            loadDiagram(server.loadDiagram());
            loadMessage(server.loadChat());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 创建绘图面板
    private JPanel createCanvas() {
        return new JPanel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics graphics) {
            	super.paintComponent(graphics);               
                drawComponents(graphics);
            }
        };
    }

    public void drawComponents(Graphics graphics) {
        Graphics2D grap = (Graphics2D) graphics;
        grap.drawImage(drawingCanvas, 0, 0, null);
        drawTemporaryShape(grap);
        try {
            drawShapesFromServer(grap);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void drawTemporaryShape(Graphics2D grap) {
        if (ifDrawing && tempShape != null) {
            grap.setColor(drawColor);
            grap.draw(tempShape);
        }
    }

    private void drawShapesFromServer(Graphics2D grap) throws RemoteException {
        for (WhiteBoardShape shape : server.loadDiagram()) {
            grap.setColor(shape.getColor());
            if (shape.getShape() instanceof WhiteBoardText) {
                drawTextShape(grap, (WhiteBoardText) shape.getShape());
            } else {
                grap.draw(shape.getShape());
            }
        }
    }

    private void drawTextShape(Graphics2D grap, WhiteBoardText textShape) {
        grap.setFont(textShape.getFont());
        grap.drawString(textShape.getText(), (float) textShape.getX(), (float) textShape.getY());
    }

    private void setupSplitPane(JPanel drawingPanel, JPanel chattingPanel) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, drawingPanel, chattingPanel);
        splitPane.setDividerLocation(750);
        frame.setContentPane(splitPane);
        frame.pack();
    }
    
 // 尝试连接到服务器，并处理可能的连接问题
    private boolean tryConnectToServer(String ip, int port) {
        try {
            Registry registry = LocateRegistry.getRegistry(ip, port);
            server = (IServer) registry.lookup("Server");
            if (server.handleRequest(clientName)) {
                System.out.println("Connection has been established");
                server.addClient(this, clientName);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Connection has been rejected");
                return false;
            }
        } catch (RemoteException e) {
            handleRemoteException(e);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection failed.");
            e.printStackTrace();
            return false;
        }
    }
    
    public enum Tool {       
        LINE,
        CIRCLE,
        ECLIPSE,
        TEXT,
        ERASER,
        RECTANGLE,    	 
    	FREE_DRAW; 
    }
    
 // 配置事件处理器    
    private void configureEventHandlers() { 	
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!ifDrawing) {
                    return;
                }

                int currentX = e.getX();
                int currentY = e.getY();

                switch (currentTool) {
                    case LINE:
                        tempShape = new Line2D.Float(startX, startY, currentX, currentY);
                        break;
                    case CIRCLE:
                        int radius = (int) Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                        tempShape = new Ellipse2D.Float(startX - radius, startY - radius, radius * 2, radius * 2);
                        break;
                    case RECTANGLE:
                        tempShape = new Rectangle2D.Float(Math.min(startX, currentX), Math.min(startY, currentY),
                                                          Math.abs(currentX - startX), Math.abs(currentY - startY));
                        break;
                    case ECLIPSE:
                        tempShape = new Ellipse2D.Float(Math.min(startX, currentX), Math.min(startY, currentY),
                                                        Math.abs(currentX - startX), Math.abs(currentY - startY));
                        break;
                    case ERASER:
                        int eraserSize = 20;
                        Rectangle2D eraser = new Rectangle2D.Float(currentX - eraserSize / 2, currentY - eraserSize / 2, eraserSize, eraserSize);
                        try {
                            server.eraseShape(eraser);
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case FREE_DRAW:
                        Line2D line = new Line2D.Float(startX, startY, currentX, currentY);
                        try {
                            server.drawFreeLine(startX, startY, currentX, currentY, drawColor.getRGB());
                        } catch (RemoteException remoteException) {
                            remoteException.printStackTrace();
                        }
                        startX = currentX;
                        startY = currentY;
                        break;
                    default:
                        break;
                }
                canvasGraphics.setColor(canvasBackgroundColor);
                canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                panel.repaint();
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();
                if (!ifServerAlive) {
                    return;
                }
                startX = e.getX();
                startY = e.getY();
                ifDrawing = true;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (ifTexting) {
                    int x = e.getX();
                    int y = e.getY();
                    String text = JOptionPane.showInputDialog("Enter text:");
                    if (text != null && !text.isEmpty()) {
                        sendText(text, x, y);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!ifServerAlive || currentTool == null || ifTexting) {
                    return;
                }

                ifDrawing = false;
                int x = e.getX();
                int y = e.getY();
                int width = Math.abs(x - startX);
                int height = Math.abs(y - startY);
                int topLeftX = Math.min(startX, x);
                int topLeftY = Math.min(startY, y);

                try {
                    switch (currentTool) {
                        case LINE:
                            server.drawLine(startX, startY, x, y, drawColor.getRGB());
                            break;
                        case CIRCLE:
                            int diameter = Math.max(width, height);
                            topLeftX = startX < x ? startX : startX - diameter;
                            topLeftY = startY < y ? startY : startY - diameter;
                            server.drawCircle(topLeftX, topLeftY, diameter, drawColor.getRGB());
                            break;
                        case RECTANGLE:
                            server.drawRectangle(topLeftX, topLeftY, width, height, drawColor.getRGB());
                            break;
                        case ECLIPSE:
                            server.drawEclipse(topLeftX, topLeftY, width, height, drawColor.getRGB());
                            break;
                        default:
                            break;
                    }
                } catch (RemoteException remoteException) {
                    remoteException.printStackTrace();
                }

                tempShape = null;
                canvasGraphics.setColor(canvasBackgroundColor);
                canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                panel.repaint();
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (ifTexting) {
                    String text = Character.toString(e.getKeyChar());
                    Font font = new Font("Helvetica", Font.PLAIN, 30);
                    try {
                        server.drawText(text, startX, startY, drawColor.getRGB());
                        startX = startX + canvasGraphics.getFontMetrics(font).stringWidth(text);
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                }
            }
        });
    }

    private void createchattingPanel() {
    	class BottomScrollingTextArea extends JTextArea {
    		private static final long serialVersionUID = 1L;

    		@Override
            public void append(String str) {
                super.append(str);
                setCaretPosition(getDocument().getLength());
            }
        }
    	chattingFrame = new BottomScrollingTextArea();
    	chattingFrame.setEditable(false);
    	chattingFrame.setLineWrap(true);
    	chattingFrame.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chattingFrame);

        chattingField = new JTextField();
        JLabel chatLabel = new JLabel("Chat Room");
        chatLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        chattingField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chattingField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chattingPanel = new JPanel(new BorderLayout());
        chattingPanel.add(chatLabel, BorderLayout.NORTH);
        chattingPanel.add(chatScrollPane, BorderLayout.CENTER);
        chattingPanel.add(inputPanel, BorderLayout.SOUTH);
    }


    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL); // 将工具栏方向设置为垂直
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setLayout(new GridBagLayout()); // 使用 GridBagLayout 布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // 按钮之间的间距
        usingTool = new JLabel(getStatusText());
        usingTool.setPreferredSize(new Dimension(100, usingTool.getPreferredSize().height)); // 设置固定宽度
        usingTool.setMaximumSize(new Dimension(100, usingTool.getPreferredSize().height)); // 设置固定宽度
        gbc.gridy = 0;
        toolbar.add(usingTool, gbc);

        String[] labels = {"Clear", "Line", "Circle", "Oval", "Rectangle", "Eraser", "Free Draw"};
        Tool[] shapes = {null, Tool.LINE, Tool.CIRCLE, Tool.ECLIPSE, Tool.RECTANGLE, Tool.ERASER, Tool.FREE_DRAW};

        for (int i = 0; i < labels.length; i++) {
            JButton button;
            if (labels[i].equals("Clear")) {
                button = createClearButton();
            } else {
                button = createDrawingButton(labels[i], shapes[i]);
            }
            gbc.gridy = i + 1;
            toolbar.add(button, gbc);
        }

        return toolbar;
    }



    private JButton createClearButton() {
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            try {
                server.clear();  // Notify the server to clear all clients' canvases
                clearCanvas();   // Clear local canvas immediately
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
        return clearButton;
    }

    private void clearCanvas() {
        canvasGraphics.setColor(canvasBackgroundColor);
        canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        panel.repaint();
    }


    private JButton createDrawingButton(String label, Tool shape) {
        JButton button = new JButton(label);
        button.addActionListener(e -> {
        	currentTool = shape;
            updateusingTool();
        });
        return button;
    }

    private JToggleButton createTextToggleButton() {
        JToggleButton toggleButton = new JToggleButton("Text");
        toggleButton.addItemListener(e -> {
        	ifTexting = e.getStateChange() == ItemEvent.SELECTED;
        });
        return toggleButton;
    }

    private void updateusingTool() {
        usingTool.setText(getStatusText());
    }

    private String getStatusText() {
        return "Tool : " + (currentTool != null ? currentTool : "No Tool Selected");
    }

    
    private JToolBar createColorBar() {
        JPanel colorPanel = new JPanel();      
        // Adding a button for custom color selection
        JButton customColorButton = new JButton("Draw Color");
        customColorButton.addActionListener(e -> openColorChooser());
        colorPanel.add(customColorButton);
        
     // Adding a button for canvas color selection
        JButton customCanvasButton = new JButton("Canvas Color");
        customCanvasButton.addActionListener(e -> openCanvasColorChooser());
        colorPanel.add(customCanvasButton);
        
     // 添加用户列表按钮
        JButton userListButton = new JButton("User list");
        userListButton.addActionListener(e -> showUserList());
        colorPanel.add(userListButton);
        
        JToggleButton textToggleButton = createTextToggleButton();
        colorPanel.add(textToggleButton);

        JToolBar colorChooserToolbar = new JToolBar();
        colorChooserToolbar.setFloatable(false);
        colorChooserToolbar.setRollover(true);
        colorChooserToolbar.add(colorPanel);

        return colorChooserToolbar;
    }
    
    private void showUserList() {
        try {
        	if(server.isAdmin(clientName)) {
        		server.triggerAdminUI(true);
        	}
        	else {
        		CopyOnWriteArrayList<IClient> clients = server.getClients();
                String[] clientNames = new String[clients.size()];

                for (int i = 0; i < clients.size(); i++) {
                    clientNames[i] = clients.get(i).getclientName();
                }

                JList<String> clientList = new JList<>(clientNames);
                JScrollPane scrollPane = new JScrollPane(clientList);

                JFrame userListFrame = new JFrame("Connected Clients");
                userListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                userListFrame.getContentPane().setLayout(new BorderLayout());
                userListFrame.setSize(400, 500);
                userListFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

                userListFrame.setVisible(true);
        	}         
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void draw(WhiteBoardShape coloredShape) throws RemoteException {
        Shape shape = coloredShape.getShape();
        Color color = coloredShape.getColor();
        String str = coloredShape.getText();
        canvasGraphics.setColor(color);

        if (shape instanceof Line2D) {
            drawLine((Line2D) shape);
        } else if (shape instanceof Ellipse2D) {
            drawEllipse((Ellipse2D) shape);
        } else if (shape instanceof Rectangle2D) {
            drawRectangle((Rectangle2D) shape);
        } else if (str != null) {
            drawText(coloredShape);
        } 
        // 重绘画布背景色和面板
        clearCanvas();
    }

    @Override
    public String getclientName() throws RemoteException {
        return clientName;
    }


    private void openColorChooser() {
        Color initialColor = drawColor; 
        Color newColor = JColorChooser.showDialog(null, "Choose a color", initialColor);
        if (newColor != null) {
        	drawColor = newColor;
            updateusingTool();
        }
    }
   
    
    @Override
    public void setCanvasColor(int rgb) throws RemoteException {
        Color newColor = new Color(rgb);
        SwingUtilities.invokeLater(() -> {
        	canvasBackgroundColor = newColor;  // 更新全局画布颜色
        	canvasGraphics.setColor(canvasBackgroundColor);
            canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
            panel.repaint();
        });
    }

  

    private void openCanvasColorChooser() {
        Color initialColor = canvasBackgroundColor;
        Color newColor = JColorChooser.showDialog(null, "Choose Canvas Color", initialColor);
        if (newColor != null && !newColor.equals(canvasBackgroundColor)) {
            try {
                server.requestCanvasColorChange(newColor.getRGB(), clientName); // 使用 RGB 整数值
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessage() {
        if (ifServerAlive) {
        	String message = chattingField.getText().trim();
            if (!message.isEmpty()) {
                try {
                    server.loadMessage(clientName, message);
                    chattingField.setText("");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }        
        }
        else {
        	return;
        }
        
    }

    private void drawLine(Line2D line) {
        canvasGraphics.draw(line);
    }

    private void drawEllipse(Ellipse2D ellipse) {
        if (ellipse.getWidth() == ellipse.getHeight()) { 
            canvasGraphics.drawOval((int) ellipse.getX(), (int) ellipse.getY(), (int) ellipse.getWidth(), (int) ellipse.getHeight());
        } else {
            canvasGraphics.drawOval((int) ellipse.getX(), (int) ellipse.getY(), (int) ellipse.getWidth(), (int) ellipse.getHeight());
        }
    }

    private void drawRectangle(Rectangle2D rectangle) {
        canvasGraphics.drawRect((int) rectangle.getX(), (int) rectangle.getY(), (int) rectangle.getWidth(), (int) rectangle.getHeight());
    }

    private void drawText(WhiteBoardShape textShape) {
        canvasGraphics.setFont(textShape.getFont());
        canvasGraphics.drawString(textShape.getText(), (int) textShape.getShape().getBounds2D().getX(), (int) textShape.getShape().getBounds2D().getY());
    }

    
    
    @Override
    public void loadDiagram(CopyOnWriteArrayList<WhiteBoardShape> graphs) throws RemoteException {
    	canvasGraphics.setColor(Color.WHITE);
    	canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        for (WhiteBoardShape coloredShape : graphs) {
            draw(coloredShape);
        }
        canvasGraphics.setColor(canvasBackgroundColor);
        canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        panel.repaint();
    }
    
    @Override
    public void receiveMessage(String message) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (message.equals("You have been kicked out.")) {
                int result = JOptionPane.showOptionDialog(frame, message, "Disconnected", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        shutDown();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } 
            if (message.equals("Connection failed: Username is already in use.")) {
            	JOptionPane.showMessageDialog(frame, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0); // 关闭客户端
            }
            if (message.equals("Whiteboard is closed. See you next time.")) {
                int result = JOptionPane.showOptionDialog(frame, message, "Disconnected", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        shutDown();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, message);
            }
        });
    }

    @Override
    public void shutDown() throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            frame.dispose(); // Close the GUI
            System.exit(0);  // Exit the program
        });
    }

    
    @Override
    public void clear() throws RemoteException {
    	canvasGraphics.setColor(canvasBackgroundColor);
        canvasGraphics.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        panel.repaint();
    }

    

    @Override
    public void loadMessage(CopyOnWriteArrayList<String> messages) throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chattingFrame.setText("");
                for (String message : messages) {
                    chattingFrame.append(message + "\n");
                }
            }
        });
    }
    
    
    // 处理远程连接异常
    private void handleRemoteException(RemoteException e) {
        if (e.getCause() instanceof ConnectException) {
            JOptionPane.showMessageDialog(null, "Connection has been rejected. Please double check the server IP address, the port number.");
        } else {
            JOptionPane.showMessageDialog(null, "Connection failed.");
        }
        e.printStackTrace();
    }
    
    
    private void sendText(String text, int x, int y) {
        try {
            int color = drawColor.getRGB();
            server.drawText(text, x, y, color);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void setColor(Color color) throws RemoteException {
    	drawColor = color;
    }


}
