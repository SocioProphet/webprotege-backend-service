package edu.stanford.bmir.protege.web.shared.issues;


import edu.stanford.bmir.protege.web.shared.event.EventHandler;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Oct 2016
 */
public interface DiscussionThreadCreatedHandler extends EventHandler {

    void handleDiscussionThreadCreated(DiscussionThreadCreatedEvent event);
}
