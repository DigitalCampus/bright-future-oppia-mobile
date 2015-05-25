package org.digitalcampus.oppia.model;

import java.util.ArrayList;

public class ClientDTO {
	private ArrayList<Client> clients = new ArrayList<Client>();
	private long previousSyncTime;

    public ArrayList<Client> getClients() {
        return clients;
    }

    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }

    public long getPreviousSyncTime() {
        return previousSyncTime;
    }

    public void setPreviousSyncTime(long previousSyncTime) {
        this.previousSyncTime = previousSyncTime;
    }
}
