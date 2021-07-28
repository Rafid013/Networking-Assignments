package Unused;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import Client.Client;
import HybridChunk.HybridChunk;

/**
 * Created by rafid on 28/9/2017.
 */
public class ClientReadThread implements Runnable {

    /*Client client;

    ClientReadThread(Client client) {
        this.client = client;
        Thread thread = new Thread(this);
        thread.start();
    }*/

    @Override
    public void run() {
        /*try {
            while (true) {
                String msg = (String) client.getObjectInputStream().readObject();
                System.out.println("Server: " + msg);
                if(msg.compareTo("Recipient is logged in. Proceed.") == 0){
                    synchronized (client) {
                        client.setCheckingLogIn(false);
                        client.setRecipientLoggedIn(true);
                    }
                }
                else if(msg.compareTo("Recipient is not logged in. Stop.") == 0) {
                    synchronized (client) {
                        client.setCheckingLogIn(false);
                        client.setRecipientLoggedIn(false);
                    }
                }
                else if(msg.compareTo("No available space in server.") == 0) {
                    synchronized (client) {
                        client.setCheckingOverflow(false);
                        client.setServerOverflow(true);
                    }
                }
                else if(msg.compareTo("Enough space available in server.") == 0) {
                    synchronized (client) {
                        client.setCheckingOverflow(false);
                        client.setServerOverflow(true);
                    }
                }
                else if(msg.startsWith("Random Chunk")) {
                    client.setChunkSize(Integer.parseInt(msg.substring(13)));
                }
                else if(msg.compareTo("Start Sending File") == 0) {
                    client.setSendFileAllowed(true);
                }
                else if(msg.startsWith("File ID")) {
                    synchronized (client) {
                        client.setFileID(msg.substring(8));
                        client.setFileID_Received(true);
                    }
                }
                else if(msg.compareTo("Send next chunk") == 0) {
                    client.setChunkReceived(true);
                }
                else if(msg.compareTo("File received successfully.") == 0) {
                    synchronized (client) {
                        client.setWaitingFinalCompletion(false);
                        client.setFileSendSuccess(true);
                    }
                }
                else if(msg.compareTo("File size don't match.") == 0) {
                    synchronized (client) {
                        client.setWaitingFinalCompletion(false);
                        client.setFileSendSuccess(false);
                    }
                }
                else if(msg.contains("wants to send a file named")) {
                    String ans;
                    System.out.println("Do you want to receive?");
                    Scanner scanner = new Scanner(System.in);
                    ans = scanner.nextLine();
                    if(ans.compareTo("y") == 0) {
                        client.getObjectOutputStream().writeObject("Start sending file");
                        HybridChunk hybridChunk = new HybridChunk();
                        hybridChunk.setFileName(msg.substring(msg.lastIndexOf('d') + 2));
                        hybridChunk.setAllChunksPresent(false);
                        client.getFileChunks().put(
                                msg.substring(msg.lastIndexOf('D') + 2),
                                new HybridChunk());
                    }
                    else {
                        client.getObjectOutputStream().writeObject("Do not send file");
                    }
                }
                else if(msg.contains("Sending Chunk")) {
                    byte[] chunk = (byte[])client.getObjectInputStream().readObject();
                    String fileID = msg.substring(msg.lastIndexOf('D') + 2);
                    client.getFileChunks().get(fileID).getChunk().add(chunk);
                    client.getObjectOutputStream().writeObject("Send Next");
                }
                else if(msg.compareTo("All chunks sent") == 0) {
                    //start adding chunks
                }
            }
        }
        catch (IOException e) {
                e.printStackTrace();
        } catch (ClassNotFoundException e) {
                e.printStackTrace();
        }*/
    }
}
