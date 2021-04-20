package edu.stanford.bmir.protege.web.shared.issues;



import edu.stanford.bmir.protege.web.shared.event.EventHandler;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Oct 2016
 */
public interface CommentUpdatedHandler extends EventHandler {


    void handleCommentUpdated(@Nonnull CommentUpdatedEvent commentUpdatedEvent);
}
