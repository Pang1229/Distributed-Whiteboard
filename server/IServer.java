package server;
import java.awt.geom.Rectangle2D;
import java.rmi.Remote;
import java.rmi.RemoteException;
import func.WhiteBoardShape;
import client.IClient;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;

public interface IServer extends Remote {
	Object clients = null;

	// handle user function
    void addClient(IClient client, String UName) throws RemoteException;
    
    void removeClient(IClient client) throws RemoteException;
    
    boolean handleRequest(String clientName) throws RemoteException;
    
    // handle all functions according to project2 doc
//    void handleClose(String clientName, JFrame serverFrame);
//
//	void handleSaveAs(String clientName, JFrame serverFrame);
//
//	void handleSave(String clientName, JFrame serverFrame);
//
//	void handleOpen(String clientName, JFrame serverFrame);
//
//	void handleNew(String clientName, JFrame serverFrame);
	
	void drawFreeLine(int x1, int y1, int x2, int y2, int color) throws RemoteException;
		
    void drawLine(int x1, int y1, int x2, int y2, int color) throws RemoteException;
    
    void drawCircle(int x, int y, int diameter, int color) throws RemoteException;

    void drawEclipse(int x, int y, int width, int height, int color) throws RemoteException;

    void drawRectangle(int x, int y, int width, int height, int color) throws RemoteException;
    
    void drawText(String text, int x, int y, int color) throws RemoteException;
    
    void message(String message) throws RemoteException;

    void loadMessage(String clientName, String message) throws RemoteException;
    
    // ensure it's a shared board
    CopyOnWriteArrayList<WhiteBoardShape> loadDiagram() throws RemoteException;

    CopyOnWriteArrayList<String> loadChat() throws RemoteException;
    
    CopyOnWriteArrayList<IClient> getClients() throws RemoteException;
    
    void clear() throws RemoteException;
    
    void requestCanvasColorChange(int rgb, String clientName) throws RemoteException;

	void eraseShape(Rectangle2D eraser)throws RemoteException;

	boolean isAdmin(String clientName) throws RemoteException;
	
	void triggerAdminUI(boolean isAdmin) throws RemoteException;

}
