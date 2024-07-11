### README

# Distributed Shared Whiteboard

## Introduction

This project aims to build a distributed shared whiteboard for multiple clients. The whiteboard supports various drawing operations such as line, circle, rectangle, ellipse, and text, along with update and delete functionalities. It also includes a chat feature for user communication. The architecture utilizes Java RMI for network communication and `CopyOnWriteArrayList` for concurrency. The server manages all connected clients and broadcasts updates to maintain consistency across all clients.

## System Architecture

The system follows a client-server architecture:
- **Server**: Manages all connected clients, handles drawing and chat requests, and broadcasts updates.
- **Client**: Connects to the server, performs drawing operations, and communicates with other clients through chat.

### Main Components:
1. **Server**: 
   - Opens the RMI registry and binds itself to it.
   - Manages connected clients and broadcasts updates.
   - Provides a GUI for the administrator to manage clients and perform file operations.
   
2. **Client**:
   - Connects to the server using RMI.
   - Allows users to perform drawing operations and chat.
   - Receives updates from the server and refreshes the local display.

### Process:
1. The server opens the RMI registry and binds itself, making it accessible to clients.
2. The server accepts client requests and registers them.
3. The server maintains a list of connected clients, whiteboard shapes, and chat messages.
4. The server updates the shared state and broadcasts updates to all clients.
5. Clients connect to the server, perform operations, and receive updates to refresh their local display.

## Design Analysis

### Server Side:
1. **Server.java**:
   - Manages connected clients, handles drawing and chat requests, and broadcasts updates.
   - Uses `CopyOnWriteArrayList` to store clients, shapes, and chat messages for thread safety.

2. **ServerGUI.java**:
   - Provides a GUI for the administrator to manage clients and perform file operations.
   - Implements the GUI using the Swing framework.

3. **IServer.java**:
   - Defines remote methods for client management, drawing operations, chat handling, and request processing.

### Client Side:
1. **Client.java**:
   - Provides the main interface for drawing and chat.
   - Connects to the server via RMI, registers the client, and handles drawing and chat requests.
   - Implements the GUI using the Swing framework.

2. **IClient.java**:
   - Defines remote methods for drawing, loading diagrams, receiving messages, clearing the whiteboard, and shutting down the client.

### Communication Protocols and Message Formats

Communication between clients and the server is handled through Java RMI. Methods are defined in the `IServer` and `IClient` interfaces, facilitating remote method calls. For example:
- **Drawing Methods**: `drawLine`, `drawCircle` (with parameters like coordinates and color).
- **Message Methods**: `receiveMessage` (with a string message).

### GUI and New Innovations

Both the server and client share an identical whiteboard interface. Key features include:
- **Toolbar**: Allows users to choose drawing tools and clear the whiteboard.
- **Color Buttons**: Customize brush and canvas colors (changes are server-approved).
- **User List**: Displays current users; the server admin can manage users.
- **File Operations**: Support for creating, saving, and opening whiteboards.
- **Text Insertion**: Allows users to insert text on the canvas.

### Conclusion

The system employs Java RMI technology to implement a distributed multi-client whiteboard, enabling various drawing and chat functionalities. It ensures consistent state synchronization across clients, handles concurrency, and provides reliable communication. The system also includes error reporting mechanisms to handle operational failures effectively.

---

**Distributed Shared White Board Report** 

This README provides a comprehensive overview of the distributed shared whiteboard project, including system architecture, class design, protocols, and handling of operations and failures.
