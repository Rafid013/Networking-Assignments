package Server;

import HybridChunk.HybridChunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by rafid on 23/9/2017.
 */
public class ServerReadThread implements Runnable {
    private Socket socket;
    private String ID;
    private String recipientID;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String currentFileName;
    private String currentFileID;
    private long currentFileSize;
    ServerReadThread(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, String ID) {
        this.socket = socket;
        this.objectInputStream = ois;
        this.objectOutputStream = oos;
        this.ID = ID;
        Thread thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        try {
            while (true) {
                String msg = (String) objectInputStream.readObject();
                System.out.println("Client(ID: " + ID + "): " + msg);
                if(msg.compareTo("Disconnecting") == 0) {
                    System.out.println(socket.getInetAddress() + ", " + socket.getPort() + " with ID: "
                    + ID + " disconnected");
                    Server.removeConnection(ID);
                    objectInputStream.close();
                    objectOutputStream.close();
                    socket.close();
                    break;
                }
                else if(msg.startsWith("Check LogIn")) {
                    recipientID = msg.substring(12);
                    System.out.println(recipientID);
                    if(Server.checkLogIn(recipientID)) {
                        objectOutputStream.writeObject("Recipient is logged in. Proceed.");
                    }
                    else {
                        objectOutputStream.writeObject("Recipient is not logged in. Stop.");
                    }
                }
                else if(msg.startsWith("File Name")){
                    currentFileName = msg.substring(10);
                }
                else if(msg.startsWith("File Size")){
                    currentFileSize = Long.parseLong(msg.substring(10));
                    System.out.println("ID: " + ID + " wants to send file " + currentFileName
                            + " with size " + currentFileSize);
                    if(Server.checkOverflow(currentFileSize))
                        objectOutputStream.writeObject("No available space in server.");
                    else {
                        objectOutputStream.writeObject("Enough space available in server.");
                        objectOutputStream.writeObject("Random Chunk " + Server.randomChunkSize());
                        objectOutputStream.writeObject("Start Sending File");
                        currentFileID = recipientID + "_" + Server.getStartFileID() + "_" + ID;
                        Server.setStartFileID(Server.getStartFileID() + 1);
                        objectOutputStream.writeObject("File ID " + currentFileID);
                        HybridChunk hybridChunk = new HybridChunk();
                        hybridChunk.setFileName(currentFileName);
                        Server.addToBuffer(currentFileID, hybridChunk);
                    }
                }
                else if(msg.startsWith("Sending file chunk with File ID")) {
                    String fileID = msg.substring(32);
                    byte[] chunk = (byte[])objectInputStream.readObject();
                    Server.setCurrentStoredSize(Server.getCurrentStoredSize() + chunk.length);
                    System.out.println("Current stored size = " + Server.getCurrentStoredSize());
                    Server.addToBuffer(fileID, chunk);
                    objectOutputStream.writeObject("Send next chunk");
                }
                else if(msg.compareTo("No chunk left. Task complete.") == 0) {
                    ArrayList<byte[]> fileChunks = Server.getChunks(currentFileID);
                    if(currentFileSize == Server.calculateSize(fileChunks)) {
                        Server.setAllChunkPresent(currentFileID, true);
                        Server.addToReceiver(recipientID, currentFileID);
                        objectOutputStream.writeObject("File received successfully.");
                    }
                    else {
                        objectOutputStream.writeObject("File size don't match.");
                        long size = Server.calculateSize(Server.getChunks(currentFileID));
                        Server.removeFromBuffer(currentFileID);
                        Server.setCurrentStoredSize(Server.getCurrentStoredSize() - size);
                    }
                }
                else if(msg.compareTo("Time Out. Terminating Transmission.") == 0) {
                    long size = Server.calculateSize(Server.getChunks(currentFileID));
                    Server.removeFromBuffer(currentFileID);
                    Server.setCurrentStoredSize(Server.getCurrentStoredSize() - size);
                }
                else if(msg.compareTo("Want to receive file") == 0) {
                    if(Server.getReceivers().containsKey(ID)){
                        new SendFile(objectInputStream, objectOutputStream, ID).send();
                    }
                    else {
                        objectOutputStream.writeObject("No file to receive");
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println(socket.getInetAddress() + ", " + socket.getPort() + " with ID: "
                    + ID + " disconnected");
            Server.connections.remove(ID);
            try {
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            if(!Server.checkAllChunkPresent(currentFileID)) {
                Server.removeFromBuffer(currentFileID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


class SendFile {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String receiverID;
    private ArrayList<String> sentAndDeniedList;

    SendFile(ObjectInputStream ois, ObjectOutputStream oos, String receiverID) {
        this.ois = ois;
        this.oos = oos;
        this.receiverID = receiverID;
        sentAndDeniedList = new ArrayList<>();
    }
    void send() {
        ArrayList<String> fileIDs = Server.getFileIDsFromReceiver(receiverID);
        String msg;
        try {
            for (String fileID : fileIDs) {
                int secondUnderscore = fileID.lastIndexOf('_');
                String sender = fileID.substring(secondUnderscore + 1);
                oos.writeObject(sender + " wants to send a file named "
                        + Server.getFileBuffer().get(fileID).getFileName());
                System.out.println(sender + " wants to send a file named "
                        + Server.getFileBuffer().get(fileID).getFileName() + ", File ID " + fileID
                        + " to " + receiverID);
                msg = (String)ois.readObject();
                System.out.println("Client(ID: " + receiverID + "): " + msg);
                if(msg.compareTo("Do not send this file") == 0) {
                    ArrayList<byte[]> chunks = Server.getChunks(fileID);
                    Server.setCurrentStoredSize(Server.getCurrentStoredSize()
                            - Server.calculateSize(chunks));
                    Server.removeFromBuffer(fileID);
                    sentAndDeniedList.add(fileID);
                    continue;
                }
                ArrayList<byte[]> chunks = Server.getChunks(fileID);
                int i = 1;
                for(byte[] chunk : chunks) {
                    oos.writeObject("Sending chunk " + i + " with file ID " + fileID);
                    oos.writeObject(chunk);
                    msg = (String)ois.readObject();
                    System.out.println("Client(ID: " + receiverID + "): " + msg);
                    if(msg.compareTo("Send chunk") == 0) {
                        ++i;
                    }
                }
                oos.writeObject("All chunks sent.");
                Server.setCurrentStoredSize(Server.getCurrentStoredSize()
                        - Server.calculateSize(chunks));
                Server.removeFromBuffer(fileID);
                sentAndDeniedList.add(fileID);
            }
            for(String ID : sentAndDeniedList) {
                Server.removeFromReceivers(receiverID, ID);
            }
            if(Server.getReceivers().get(receiverID).size() == 0)
                Server.removeFromReceivers(receiverID);
        } catch (IOException e) {
            e.printStackTrace();
            for(String ID : sentAndDeniedList) {
                Server.removeFromReceivers(receiverID, ID);
            }
            if(Server.getReceivers().get(receiverID).size() == 0)
                Server.removeFromReceivers(receiverID);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            for(String ID : sentAndDeniedList) {
                Server.removeFromReceivers(receiverID, ID);
            }
            if(Server.getReceivers().get(receiverID).size() == 0)
                Server.removeFromReceivers(receiverID);
        }
    }
}