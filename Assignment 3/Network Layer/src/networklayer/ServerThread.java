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
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samsung
 */
public class ServerThread implements Runnable {
    private Thread t;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private EndDevice endDevice;
    private String path;
    private int hop_count;
    private int clientNo;
    
    public ServerThread(Socket socket, EndDevice endDevice, int clientNo){
        
        this.socket = socket;
        this.endDevice = endDevice;
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.clientNo = clientNo;
        System.out.println("Server Ready for client "+NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;
        t=new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */

        /*
        Tasks:
        1. Upon receiving a packet server will assign a recipient.
        [Also modify packet to add destination]
        2. call deliverPacket(packet)
        3. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        4. Either send acknowledgement with number of hops or send failure message back to client
        */
        try {
            output.writeObject(endDevice);
        } catch (Exception e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
        while(true) {
            try {
                String msg = (String)input.readObject();
                if(msg.compareTo("send receiver") == 0) {
                    IPAddress receiver = assignReceiver();
                    output.writeObject(receiver);
                    Packet packet = (Packet) input.readObject();
                    Boolean delivered = deliverPacket(packet);
                    if (delivered) {
                        output.writeObject("success");
                        if (packet.getSpecialMessage().compareTo("SHOW_ROUTE") == 0) {
                            output.writeObject(path);
                            output.writeObject(hop_count);
                            output.writeObject(NetworkLayerServer.routers.size());
                            for (Router router : NetworkLayerServer.routers) output.writeObject(router);
                        }
                        else {
                            output.writeObject(hop_count);
                        }
                    }
                    else {
                        output.writeObject("failure");
                        if(packet.getSpecialMessage().compareTo("SHOW_ROUTE") == 0) {
                            output.writeObject(path);
                            output.writeObject(NetworkLayerServer.routers.size());
                            for (Router router : NetworkLayerServer.routers) output.writeObject(router);
                        }
                    }
                }
                else if(msg.compareTo("Disconnecting") == 0) {
                    NetworkLayerServer.activeClients.remove(endDevice);
                    NetworkLayerServer.clientCount--;
                    NetworkLayerServer.clientInterfaces.replace(endDevice.getGateway(),
                            NetworkLayerServer.clientInterfaces.get(endDevice.getGateway()) - 1);
                    System.out.println("Client " + clientNo + " disconnected");
                    break;
                }
            } catch (Exception e) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
                NetworkLayerServer.activeClients.remove(endDevice);
                NetworkLayerServer.clientCount--;
                NetworkLayerServer.clientInterfaces.replace(endDevice.getGateway(),
                        NetworkLayerServer.clientInterfaces.get(endDevice.getGateway()) - 1);
                System.out.println("Client " + clientNo + " disconnected");
                break;
            }
        }
    }
    
    /**
     * Returns true if successfully delivered
     * Returns false if packet is dropped
     * @param p
     * @return 
     */
    public Boolean deliverPacket(Packet p)
    {
        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination, 
                and eventually the packet reaches to destination router d.
                
            3(a) If, while forwarding, any gateway x, found from routingTable of router x is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t
                            
            3(b) If, while forwarding, a router x receives the packet from router y, 
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t
                            
        4. If 3(a) occurs at any stage, packet will be dropped, 
            otherwise successfully sent to the destination router
        */
        Router src = findRouter(p.getSourceIP());
        Router dest = findRouter(p.getDestinationIP());
        int srcID = -1, destID = -1;
        if(src != null) srcID = src.getRouterId();
        if(dest != null) destID = dest.getRouterId();
        return forwardPacket(srcID, destID);
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

    private IPAddress assignReceiver(){
        Random random = new Random();
        int r = Math.abs(random.nextInt(NetworkLayerServer.activeClients.size()));
        return NetworkLayerServer.activeClients.get(r).getIp();
    }


    private Router findRouter(IPAddress ipAddress) {
        for(Router router : NetworkLayerServer.routers) {
            ArrayList<IPAddress> interfaces = router.getInterfaceAddrs();
            if(matchNetwork(ipAddress, interfaces.get(0))) return router;
        }
        return null;
    }

    private boolean matchNetwork(IPAddress ip1, IPAddress ip2) {
        short[] bytes1 = ip1.getBytes();
        short[] bytes2 = ip2.getBytes();
        for(int i = 0; i < 3; ++i) {
            if(bytes1[i] != bytes2[i]) return false;
        }
        return true;
    }


    private boolean forwardPacket(int srcID, int destID) {
        hop_count = 0;
        path = "Source: " + srcID + "; Destination: " + destID + "; Route: ";
        if(srcID == -1 && destID == -1) return false;
        if(!NetworkLayerServer.routerMap.get(srcID).getState()) {
            path += "[DROPPED AT SOURCE]";
            return false;
        }
        if(srcID == destID) {
            path += (srcID + "->" + destID);
            return true;
        }
        int currentRouterID = srcID;
        path += srcID;
        boolean destReached = false;
        while (!destReached) {
            Router currentRouter = NetworkLayerServer.routerMap.get(currentRouterID);
            if(!currentRouter.getState()) {
                path += "[DROPPED]";
                return false;
            }
            Map<Integer, RoutingTableEntry> routingTableMap = currentRouter.getRoutingTableMap();
            RoutingTableEntry rte = routingTableMap.get(destID);
            if(rte.getDistance() == Constants.INFTY) {
                path += "[DROPPED]";
                return false;
            }
            Router gatewayRouter = NetworkLayerServer.routerMap.get(rte.getGatewayRouterId());
            if(gatewayRouter.getState()) {
                if(reverseInfty(gatewayRouter, currentRouterID)){
                    RouterStateChanger.lock.lock();
                    //NetworkLayerServer.DVR(gatewayRouter.getRouterId());
                    NetworkLayerServer.simpleDVR(gatewayRouter.getRouterId());
                    RouterStateChanger.lock.unlock();
                }
                hop_count++;
                currentRouterID = gatewayRouter.getRouterId();
                path += ("->" + currentRouterID);
                if(currentRouterID == destID) destReached = true;
            }
            else {
                rte.setDistance(Constants.INFTY);
                rte.setGatewayRouterId(-1);
                RouterStateChanger.lock.lock();
                //NetworkLayerServer.DVR(currentRouterID);
                NetworkLayerServer.simpleDVR(currentRouterID);
                RouterStateChanger.lock.unlock();
                path += "[DROPPED]";
                return false;
            }
        }
        return true;
    }


    private boolean reverseInfty(Router gateway, int src) {
        Map<Integer, RoutingTableEntry> routingTableMap = gateway.getRoutingTableMap();
        RoutingTableEntry rte = routingTableMap.get(src);
        if(rte.getDistance() == Constants.INFTY) {
            rte.setDistance(1);
            rte.setGatewayRouterId(src);
            return true;
        }
        return false;
    }
}
