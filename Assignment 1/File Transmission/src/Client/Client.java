package Client;


import HybridChunk.HybridChunk;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by rafid on 23/9/2017.
 */
public class Client {
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private int chunkSize;
    private String fileID;


    boolean connect(String ID) {
        try {
            socket = new Socket("127.0.0.1", 3001);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(ID);
            String msg = (String)objectInputStream.readObject();
            if(msg.compareTo("Log In successful.") == 0) {
                return true;
            }
            else if (msg.compareTo("User already logged in.") == 0) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    void closeConnection() {
        try {
            objectOutputStream.writeObject("Disconnecting");
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean sendFile() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("The ID of the recipient: ");
            String recipientID = scanner.nextLine();
            objectOutputStream.writeObject("Check LogIn " + recipientID);
            String msg = (String)objectInputStream.readObject();
            System.out.println("Server: " + msg);
            if(msg.compareTo("Recipient is not logged in. Stop.") == 0) return false;
            System.out.println("File Path: ");
            String filePath = scanner.nextLine();
            File file = new File(filePath);
            objectOutputStream.writeObject("File Name " + file.getName());
            System.out.println("File Name " + file.getName());
            objectOutputStream.writeObject("File Size " + file.length());
            System.out.println("File Size " + file.length());
            msg = (String)objectInputStream.readObject();
            System.out.println("Server: " + msg);
            if(msg.compareTo("No available space in server.") == 0) return false;
            msg = (String)objectInputStream.readObject();
            System.out.println("Server: " + msg);
            if(msg.startsWith("Random Chunk ")){
                chunkSize = Integer.parseInt(msg.substring(13));
            }
            msg = (String)objectInputStream.readObject();
            System.out.println("Server: " + msg);
            if(msg.compareTo("Start Sending File") == 0) {
                msg = (String)objectInputStream.readObject();
                System.out.println("Server: " + msg);
                fileID = msg.substring(8);
                return new SendFileClient(file, fileID, chunkSize,
                        objectInputStream, objectOutputStream, socket).send();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    void receiveFile() {
        while (true) {
            try {
                objectOutputStream.writeObject("Want to receive file");
                String msg = (String) objectInputStream.readObject();
                System.out.println("Server: " + msg);
                if (msg.compareTo("No file to receive") == 0) return;
                else if (msg.contains("wants to send a file named")) {
                    String ans;
                    System.out.println("Do you want to receive?");
                    Scanner scanner = new Scanner(System.in);
                    ans = scanner.nextLine();
                    if (ans.compareTo("y") == 0) {
                        objectOutputStream.writeObject("Start Sending");
                        HybridChunk hybridChunk = new HybridChunk();
                        hybridChunk.setFileName(msg.substring(msg.lastIndexOf('d') + 2));
                        hybridChunk.setAllChunksPresent(false);
                        new ReceiveFile(objectInputStream, objectOutputStream, hybridChunk).receive();
                    }
                    else if(ans.compareTo("n") == 0) {
                        objectOutputStream.writeObject("Do not send this file");
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
