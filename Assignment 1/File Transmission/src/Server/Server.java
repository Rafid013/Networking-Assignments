package Server;

import HybridChunk.HybridChunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by rafid on 22/9/2017.
 */
public class Server implements Runnable {
    private static long maxSize;
    public static Hashtable<String, Socket> connections;
    private static int startFileID;
    private static Hashtable<String, HybridChunk> fileBuffer;
    private static long currentStoredSize;
    private static Hashtable<String, ArrayList<String>> receivers;
    private ServerSocket serverSocket;
    private static Random random;


    Server() {
        try {

            serverSocket = new ServerSocket(3001);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connections = new Hashtable<>();
        fileBuffer = new Hashtable<>();
        receivers = new Hashtable<>();
        startFileID = 0;
        currentStoredSize = 0;
        random = new Random();
        Thread thread = new Thread(this);
        thread.start();
    }

    synchronized static void addToReceiver(String recipientID, String fileID) {
        if(receivers.containsKey(recipientID))
            receivers.get(recipientID).add(fileID);
        else {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(fileID);
            Server.receivers.put(recipientID, tmp);
        }
    }

    synchronized static void removeFromReceivers(String receiverID, String fileID) {
        receivers.get(receiverID).remove(fileID);
    }

    synchronized static void removeFromReceivers(String receiverID) {
        receivers.remove(receiverID);
    }

    synchronized static ArrayList<String> getFileIDsFromReceiver(String receiverID) {
        return receivers.get(receiverID);
    }

    synchronized static ArrayList<byte[]> getChunks(String fileID) {
        return fileBuffer.get(fileID).getChunk();
    }

    synchronized static boolean checkAllChunkPresent(String fileID) {
        return fileBuffer.get(fileID).isAllChunksPresent();
    }

    synchronized static void setAllChunkPresent(String fileID, boolean bool) {
        fileBuffer.get(fileID).setAllChunksPresent(bool);
    }

    synchronized static void removeConnection(String ID){
        connections.remove(ID);
    }

    synchronized static int getStartFileID() {
        return startFileID;
    }

    synchronized static void setStartFileID(int startFileID) {
        Server.startFileID = startFileID;
    }

    synchronized static void addToBuffer(String fileID, HybridChunk hybridChunk) {
        fileBuffer.put(fileID, hybridChunk);
    }

    synchronized static void addToBuffer(String fileID, byte[] chunk){
        fileBuffer.get(fileID).getChunk().add(chunk);
    }

    synchronized static void removeFromBuffer(String fileID){
        Server.fileBuffer.remove(fileID);
    }

    synchronized static Hashtable<String, HybridChunk> getFileBuffer() {
        return fileBuffer;
    }

    synchronized static long getCurrentStoredSize() {
        return currentStoredSize;
    }

    synchronized static void setCurrentStoredSize(long currentStoredSize) {
        Server.currentStoredSize = currentStoredSize;
    }

    public static Hashtable<String, ArrayList<String>> getReceivers() {
        return receivers;
    }


    private void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
    synchronized static boolean checkLogIn(String ID) {
        if(connections.containsKey(ID)) {
            return true;
        }
        else return false;
    }

    synchronized static boolean checkOverflow(long size) {
        if(size + currentStoredSize > maxSize) return true;
        return false;
    }

    synchronized static long calculateSize(ArrayList<byte[]> fileChunks) {
        long size = 0;
        int arraySize = fileChunks.size();
        for(int i = 0; i < arraySize; ++i) {
            size += fileChunks.get(i).length;
        }
        return size;
    }

    synchronized static int randomChunkSize() {
        return random.nextInt(10000) + 10000;
    }

    @Override
    public void run() {
        System.out.println("Server started.");
        setMaxSize(400000000);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                String ID = (String)objectInputStream.readObject();
                if(checkLogIn(ID)) {
                    objectOutputStream.writeObject("User already logged in.");
                    System.out.println("Connection denied to " + socket.getInetAddress() + ", "
                    + socket.getPort());
                }
                else {
                    objectOutputStream.writeObject("Log In successful.");
                    connections.put(ID, socket);
                    System.out.println(socket.getInetAddress() + ", " + socket.getPort() + " connected" +
                            " with ID: " + ID);
                    new ServerReadThread(socket, objectInputStream, objectOutputStream, ID);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
