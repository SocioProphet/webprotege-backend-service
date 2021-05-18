package edu.stanford.bmir.protege.web.server;

import edu.stanford.bmir.protege.web.server.user.UserId;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 20/03/2013
 */
public interface HasUserId {

    /**
     * Gets the {@link UserId} pertaining to this object.
     * @return The {@link UserId}.  Not {@code null}.
     */
    UserId getUserId();
}
