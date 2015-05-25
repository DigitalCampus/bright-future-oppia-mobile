package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.Payload;

public interface ClientDataSyncListener {
    void clientDataSyncComplete(Payload response);
}
