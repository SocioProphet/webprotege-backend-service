package edu.stanford.bmir.protege.web.server.inject.project;

import edu.stanford.bmir.protege.web.server.events.EventLifeTime;
import edu.stanford.bmir.protege.web.server.events.EventManager;
import edu.stanford.bmir.protege.web.server.project.ProjectDisposablesManager;
import edu.stanford.bmir.protege.web.server.event.ProjectEvent;
import edu.stanford.bmir.protege.web.server.project.ProjectId;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 08/07/15
 */
public class EventManagerProvider implements Provider<EventManager<ProjectEvent<?>>> {

    public static final EventLifeTime PROJECT_EVENT_LIFE_TIME = EventLifeTime.get(60, TimeUnit.SECONDS);

    private final ProjectDisposablesManager projectDisposablesManager;

    private final ProjectId projectId;

    @Inject
    public EventManagerProvider(ProjectDisposablesManager projectDisposablesManager, ProjectId projectId) {
        this.projectDisposablesManager = checkNotNull(projectDisposablesManager);
        this.projectId = projectId;
    }

    @Override
    public EventManager<ProjectEvent<?>> get() {
        EventManager<ProjectEvent<?>> projectEventEventManager = new EventManager<>(PROJECT_EVENT_LIFE_TIME, projectId);
        projectDisposablesManager.register(projectEventEventManager);
        return projectEventEventManager;
    }
}
