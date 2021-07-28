package Unused;


import Client.Client;
import Server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by rafid on 28/9/2017.
 */
public class SendFileServer implements Runnable {
    /*private Socket socket;
    private Thread thread;
    private String receiver;
    public static boolean fileSendAllowed;
    public static boolean checkingForPermission;
    public static boolean checkingForNextChunkPermission;
    public static boolean chunkSendAllowed;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;*/

    public SendFileServer(ObjectInputStream ois, ObjectOutputStream oos, String receiverID) {
        /*receiver = receiverID;
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
        thread = new Thread(this);
        thread.start();*/
    }

    @Override
    public void run() {
        /*while (true) {
            try {
                if (!Server.receivers.containsKey(receiver)) {
                    thread.sleep(20000);
                    continue;
                }
                ArrayList<String> fileIDs = Server.receivers.get(receiver);
                for (String fileID : fileIDs) {
                    checkingForPermission = true;
                    int secondUnderscore = fileID.lastIndexOf('_');
                    String sender = fileID.substring(secondUnderscore + 1);
                    oos.writeObject(sender + " wants to send a file named "
                            + Server.fileBuffer.get(fileID).getFileName() + ", File ID " + fileID);
                    while (checkingForPermission) {
                        //wait for response
                    }
                    if (!fileSendAllowed) {
                        Server.receivers.get(receiver).remove(fileID);
                        Server.fileBuffer.remove(fileID);
                    }
                    ArrayList<byte[]> chunks = Server.fileBuffer.get(fileID).getChunk();
                    int size = chunks.size();
                    for (int i = 0; i < size; ++i) {
                        checkingForNextChunkPermission = true;
                        synchronized (this) {
                            oos.writeObject("Sending Chunk with File ID " + fileID);
                            oos.writeObject(chunks.get(i));
                        }
                        while (checkingForNextChunkPermission) {
                            //wait for response
                        }
                        if (!chunkSendAllowed) break;
                    }
                    oos.writeObject("All chunks sent");
                    Server.receivers.get(receiver).remove(fileID);
                    Server.fileBuffer.remove(fileID);
                    if (Server.receivers.get(receiver).size() == 0) Server.receivers.remove(receiver);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
}
