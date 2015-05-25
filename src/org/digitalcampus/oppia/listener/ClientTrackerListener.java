package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.Payload;

public interface ClientTrackerListener {
    void clientTrackerComplete(Payload response);
}
