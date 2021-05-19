package edu.stanford.protege.webprotege.webhook;

import edu.stanford.protege.webprotege.project.ProjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.protege.webprotege.webhook.ProjectWebhook.PROJECT_ID;
import static edu.stanford.protege.webprotege.webhook.ProjectWebhook.SUBSCRIBED_TO_EVENTS;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19 May 2017
 */
public class WebhookRepositoryImpl implements WebhookRepository {

    private final Datastore datastore;

    @Inject
    public WebhookRepositoryImpl(@Nonnull Datastore datastore) {
        this.datastore = checkNotNull(datastore);
    }

    @Override
    public void clearProjectWebhooks(ProjectId projectId) {
        Query<ProjectWebhook> query = queryByProjectId(projectId);
        datastore.delete(query);
    }

    private Query<ProjectWebhook> queryByProjectId(ProjectId projectId) {
        Query<ProjectWebhook> query = datastore.createQuery(ProjectWebhook.class);
        query.field(PROJECT_ID).equal(projectId);
        return query;
    }

    @Override
    public void addProjectWebhooks(List<ProjectWebhook> projectWebhooks) {
        datastore.save(projectWebhooks);
    }

    @Override
    public List<ProjectWebhook> getProjectWebhooks(@Nonnull ProjectId projectId) {
        return datastore.find(ProjectWebhook.class)
                        .field(PROJECT_ID).equal(projectId)
                        .asList();
    }

    @Override
    public List<ProjectWebhook> getProjectWebhooks(@Nonnull ProjectId projectId, ProjectWebhookEventType event) {
        return datastore.find(ProjectWebhook.class)
                .field(PROJECT_ID).equal(projectId)
                .field(SUBSCRIBED_TO_EVENTS).equal(event)
                .asList();
    }
}
