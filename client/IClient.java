package client;
import java.awt.Color;
import java.rmi.Remote;
import java.rmi.RemoteException;
import func.WhiteBoardShape;
import client.IClient;
import java.util.concurrent.CopyOnWriteArrayList;
import func.WhiteBoardShape;


public interface IClient extends Remote {
	void draw(WhiteBoardShape shape) throws RemoteException;

    void loadDiagram(CopyOnWriteArrayList<WhiteBoardShape> drawings) throws RemoteException;

    void receiveMessage(String message) throws RemoteException;


    void shutDown() throws RemoteException;

    void setColor(Color color) throws RemoteException;

    void loadMessage(CopyOnWriteArrayList<String> chatMessages) throws RemoteException;
    
    void setCanvasColor(int rgb) throws RemoteException;

	String getclientName() throws RemoteException;

	void clear() throws RemoteException;
    
}

