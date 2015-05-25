package org.digitalcampus.oppia.model;

import java.util.ArrayList;

public class ClientSessionDTO {
    private ArrayList<ClientSession> sessions = new ArrayList<ClientSession>();

    public ArrayList<ClientSession> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<ClientSession> sessions) {
        this.sessions = sessions;
    }
}
