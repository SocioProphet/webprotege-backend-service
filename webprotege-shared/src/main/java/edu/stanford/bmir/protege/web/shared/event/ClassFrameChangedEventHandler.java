package edu.stanford.bmir.protege.web.shared.event;



/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 18/12/2012
 */
public interface ClassFrameChangedEventHandler extends EventHandler {

    void classFrameChanged(ClassFrameChangedEvent event);
}
