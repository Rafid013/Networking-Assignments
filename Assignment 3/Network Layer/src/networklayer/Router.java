/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author samsung
 */
public class Router implements Serializable{
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddrs;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private Map<Integer, RoutingTableEntry> routingTableMap;
    private ArrayList<Integer> neighborRouterIds;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state

    private boolean tableChanged;

    public Router() {
        interfaceAddrs = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIds = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.80) state = true;
        else state = false;
        
        numberOfInterfaces = 0;
    }
    
    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddrs)
    {
        this.routerId = routerId;
        this.interfaceAddrs = interfaceAddrs;
        this.neighborRouterIds = neighborRouters;
        routingTable = new ArrayList<>();
        routingTableMap = new HashMap<>();
        
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.80) state = true;
        else state = false;
        
        numberOfInterfaces = this.interfaceAddrs.size();
        tableChanged = false;
    }

    @Override
    public String toString() {
        String temp = "";
        temp+="Router ID: "+routerId+"\n";
        temp+="Interfaces: \n";
        for(int i=0;i<numberOfInterfaces;i++)
        {
            temp+=interfaceAddrs.get(i).getString()+"\t";
        }
        temp+="\n";
        temp+="Neighbors: \n";
        for(int i=0;i<neighborRouterIds.size();i++)
        {
            temp+=neighborRouterIds.get(i)+"\t";
        }
        return temp;
    }


    void printRoutingTable() {
        for(RoutingTableEntry rte : routingTable) {
            System.out.println("Destination: " + rte.getRouterId() + ", Distance: " + rte.getDistance()
                               + ", Next Hop: " + rte.getGatewayRouterId());
        }
    }

    public boolean isTableChanged() {
        return tableChanged;
    }

    public void setTableChanged(boolean tableChanged) {
        this.tableChanged = tableChanged;
    }

    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable()
    {
        tableChanged = false;
        for(Router router : NetworkLayerServer.routers) {
            RoutingTableEntry rte;
            if(router.getRouterId() == this.routerId) {
                rte = new RoutingTableEntry(routerId, 0, routerId);
            }
            else if(neighborRouterIds.contains(router.getRouterId()) && router.getState()) {
                rte = new RoutingTableEntry(router.getRouterId(), 1, router.getRouterId());
            }
            else {
                rte = new RoutingTableEntry(router.getRouterId(), Constants.INFTY, -1);
            }
            routingTable.add(rte);
            routingTableMap.put(router.getRouterId(), rte);
        }
    }
    
    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable()
    {
        tableChanged = false;
        routingTable.clear();
        routingTableMap.clear();
    }


    public void updateRoutingTable_SimpleDVR(Router neighbor) {
        ArrayList<RoutingTableEntry> neighborTable = neighbor.getRoutingTable();
        for (RoutingTableEntry rte2 : neighborTable) {
            RoutingTableEntry rte1 = routingTableMap.get(rte2.getRouterId());
            double distance_src_dest = rte1.getDistance();
            double distance_int_dest = rte2.getDistance();
            double d = 1 + distance_int_dest;
            if(d > Constants.INFTY) d = Constants.INFTY;
            if(d < distance_src_dest) {
                rte1.setDistance(d);
                if(d < Constants.INFTY) rte1.setGatewayRouterId(neighbor.getRouterId());
                else rte1.setGatewayRouterId(-1);
                tableChanged = true;
            }
        }
    }


    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public void updateRoutingTable(Router neighbor)
    {
        ArrayList<RoutingTableEntry> neighborTable = neighbor.getRoutingTable();
        for (RoutingTableEntry rte2 : neighborTable) {
            RoutingTableEntry rte1 = routingTableMap.get(rte2.getRouterId());
            double distance_src_dest = rte1.getDistance();
            double distance_int_dest = rte2.getDistance();
            double d = 1 + distance_int_dest;
            if(d > Constants.INFTY) d = Constants.INFTY;
            boolean cond1 = rte1.getGatewayRouterId() == neighbor.getRouterId() && d != distance_src_dest;
            boolean cond2 = d < distance_src_dest && routerId != rte2.getGatewayRouterId();
            if(cond1 || cond2) {
                rte1.setDistance(d);
                if(d < Constants.INFTY) rte1.setGatewayRouterId(neighbor.getRouterId());
                else rte1.setGatewayRouterId(-1);
                tableChanged = true;
            }
        }
    }
    
    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState()
    {
        state=!state;
        if(state) this.initiateRoutingTable();
        else this.clearRoutingTable();
    }
    
    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddrs() {
        return interfaceAddrs;
    }

    public void setInterfaceAddrs(ArrayList<IPAddress> interfaceAddrs) {
        this.interfaceAddrs = interfaceAddrs;
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIds() {
        return neighborRouterIds;
    }

    public void setNeighborRouterIds(ArrayList<Integer> neighborRouterIds) {
        this.neighborRouterIds = neighborRouterIds;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, RoutingTableEntry> getRoutingTableMap() {
        return routingTableMap;
    }

    public void setRoutingTableMap(Map<Integer, RoutingTableEntry> routingTableMap) {
        this.routingTableMap = routingTableMap;
    }
}
