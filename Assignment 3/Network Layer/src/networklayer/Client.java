/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samsung
 */
public class Client {
    private static String generateRandomString() {
        Random rand = new Random();
        int size = 1 + Math.abs(rand.nextInt(10));
        StringBuilder tmp = new StringBuilder();
        for(int i = 0; i < size; ++i) {
            int ascii = 32 + Math.abs(rand.nextInt(91));
            tmp.append(Character.toString((char)ascii));
        }
        return tmp.toString();
    }


    public static void main(String[] args)
    {
        Socket socket;
        ObjectInputStream input;
        ObjectOutputStream output;
        double drop_count = 0;
        double total_hops = 0;
        double sentSuccessfully = 0;
        try {
            socket = new Socket("localhost", 1234);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        System.out.println("Connected to server");
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
            handles a list of active clients.]        
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive 
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.   
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
        try {
            EndDevice endDevice = (EndDevice)input.readObject();
            IPAddress source = endDevice.getIp();
            Thread.sleep(20000);
            for(int i = 0; i < 100; ++i) {
                String msg = generateRandomString();
                System.out.println("MSG: " + msg);
                System.out.println("Source: " + source.getString());
                output.writeObject("send receiver");
                IPAddress receiver = (IPAddress)input.readObject();
                System.out.println("Receiver: " + receiver.getString());
                if(i == 20) {
                    Packet packet = new Packet(msg, "SHOW_ROUTE", source, receiver);
                    output.writeObject(packet);
                    String msgServer = (String)input.readObject();
                    if(msgServer.compareTo("failure") != 0) {
                        String path = (String) input.readObject();
                        Integer hop_count = (Integer)input.readObject();
                        Integer routerCount = (Integer)input.readObject();
                        total_hops += hop_count;
                        System.out.println("Path:\n" + path);
                        System.out.println("Hop Count: " + hop_count);
                        for (int j = 0; j < routerCount; ++j) {
                            Router temp = (Router) input.readObject();
                            System.out.println("Router ID: " + temp.getRouterId());
                            temp.printRoutingTable();
                        }
                        sentSuccessfully++;
                    }
                    else {
                        String path = (String) input.readObject();
                        Integer routerCount = (Integer)input.readObject();
                        System.out.println("Path:\n" + path);
                        for (int j = 0; j < routerCount; ++j) {
                            Router temp = (Router) input.readObject();
                            System.out.println("Router ID: " + temp.getRouterId());
                            temp.printRoutingTable();
                        }
                        System.out.println("Packet dropped");
                        drop_count++;
                    }
                }
                else {
                    Packet packet = new Packet(msg, "", source, receiver);
                    output.writeObject(packet);
                    String msgServer = (String)input.readObject();
                    if (msgServer.compareTo("failure") != 0) {
                        Integer hop_count = (Integer)input.readObject();
                        total_hops += hop_count;
                        System.out.println("Hop Count: " + hop_count);
                        sentSuccessfully++;
                    }
                    else {
                        System.out.println("Packet dropped");
                        drop_count++;
                    }
                }
                System.out.println("\n");
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
        try {
            output.writeObject("Disconnecting");
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Average number of hops: " + total_hops/sentSuccessfully);
        System.out.println("Success rate: " + sentSuccessfully);
        System.out.println("Drop rate: " + drop_count);
    }
}
