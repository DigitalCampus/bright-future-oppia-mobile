package org.digitalcampus.oppia.model;

public class ClientSession {
    private String healthWorker;
    private long startDateTime, endDateTime, clientId, id;
    private Boolean isSynced; // check if the client has been synced

    // if client has been synced, clientId will be the server ID and isSynced will be true else
    // clientId will be the local ID and isSynced is set to false
    public String getHealthWorker() {
        return healthWorker;
    }

    public Boolean getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(Boolean isSynced) {
        this.isSynced = isSynced;
    }

    public void setHealthWorker(String healthWorker) {
        this.healthWorker = healthWorker;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = endDateTime;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }
}
