package edu.stanford.bmir.protege.web.server.project;

import com.google.common.collect.ImmutableSet;
import edu.stanford.bmir.protege.web.server.projectsettings.*;
import edu.stanford.bmir.protege.web.server.webhook.SlackWebhookRepository;
import edu.stanford.bmir.protege.web.server.webhook.WebhookRepository;
import edu.stanford.bmir.protege.web.server.lang.DefaultDisplayNameSettingsFactory;
import edu.stanford.bmir.protege.web.server.shortform.DictionaryLanguage;
import edu.stanford.bmir.protege.web.server.user.UserId;
import edu.stanford.bmir.protege.web.server.webhook.ProjectWebhook;
import edu.stanford.bmir.protege.web.server.webhook.ProjectWebhookEventType;
import edu.stanford.bmir.protege.web.server.webhook.SlackWebhook;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 06/02/15
 */
public class ProjectDetailsManagerImpl implements ProjectDetailsManager {

    @Nonnull
    private final ProjectDetailsRepository repository;

    @Nonnull
    private final SlackWebhookRepository slackWebhookRepository;

    @Nonnull
    private final WebhookRepository webhookRepository;

    @Nonnull
    private final DefaultDisplayNameSettingsFactory displayNameSettingsFactory;

    @Inject
    public ProjectDetailsManagerImpl(@Nonnull ProjectDetailsRepository repository,
                                     @Nonnull SlackWebhookRepository slackWebhookRepository,
                                     @Nonnull WebhookRepository webhookRepository,
                                     @Nonnull DefaultDisplayNameSettingsFactory displayNameSettingsFactory) {
        this.repository = checkNotNull(repository);
        this.webhookRepository = checkNotNull(webhookRepository);
        this.slackWebhookRepository = checkNotNull(slackWebhookRepository);
        this.displayNameSettingsFactory = checkNotNull(displayNameSettingsFactory);
    }

    @Override
    public void registerProject(ProjectId projectId, NewProjectSettings settings) {
        long now = System.currentTimeMillis();
        ProjectDetails record = ProjectDetails.get(
                projectId,
                settings.getDisplayName(),
                settings.getProjectDescription(),
                settings.getProjectOwner(),
                false,
                DictionaryLanguage.rdfsLabel(settings.getLangTag()),
                displayNameSettingsFactory.getDefaultDisplayNameSettings(settings.getLangTag()),
                now,
                settings.getProjectOwner(),
                now,
                settings.getProjectOwner(),
                EntityDeprecationSettings.empty());
        repository.save(record);
    }

    @Override
    public ProjectDetails getProjectDetails(ProjectId projectId) throws UnknownProjectException {
        Optional<ProjectDetails> record = repository.findOne(projectId);
        if (!record.isPresent()) {
            throw new UnknownProjectException(projectId);
        }
        return record.get();
    }

    @Override
    public boolean isExistingProject(ProjectId projectId) {
        return repository.containsProject(projectId);
    }

    @Override
    public boolean isProjectOwner(UserId userId, ProjectId projectId) {
        return !userId.isGuest() && repository.containsProjectWithOwner(projectId, userId);
    }

    @Override
    public void setInTrash(ProjectId projectId, boolean b) {
        repository.setInTrash(projectId, b);
    }

    @Override
    public void setProjectSettings(ProjectSettings projectSettings) {
        ProjectId projectId = projectSettings.getProjectId();
        Optional<ProjectDetails> record = repository.findOne(projectId);
        record.ifPresent(rec -> {
            ProjectDetails updatedRecord = rec.withDisplayName(projectSettings.getProjectDisplayName())
                                              .withDescription(projectSettings.getProjectDescription())
                                              .withDefaultLanguage(projectSettings.getDefaultLanguage())
                                              .withDefaultDisplayNameSettings(projectSettings.getDefaultDisplayNameSettings())
                    .withEntityDeprecationSettings(projectSettings.getEntityDeprecationSettings());
            repository.save(updatedRecord);

        });
        slackWebhookRepository.clearWebhooks(projectId);
        String payloadUrl = projectSettings.getSlackIntegrationSettings().getPayloadUrl();
        if (!payloadUrl.isEmpty()) {
            slackWebhookRepository.addWebhooks(Collections.singletonList(new SlackWebhook(projectId, payloadUrl)));
        }
        webhookRepository.clearProjectWebhooks(projectId);
        List<ProjectWebhook> projectWebhooks = projectSettings.getWebhookSettings().getWebhookSettings().stream()
                                                              .map(s -> new ProjectWebhook(projectId,
                                                                                           s.getPayloadUrl(),
                                                                                           new ArrayList<ProjectWebhookEventType>(
                                                                                                   s.getEventTypes())))
                                                              .collect(toList());
        webhookRepository.addProjectWebhooks(projectWebhooks);
    }

    @Override
    public ProjectSettings getProjectSettings(ProjectId projectId) throws UnknownProjectException {
        List<SlackWebhook> projectWebhooks = slackWebhookRepository.getWebhooks(projectId);
        String slackPayloadUrl = projectWebhooks.stream()
                                                .findFirst()
                                                .map(SlackWebhook::getPayloadUrl).orElse("");
        List<WebhookSetting> webhookSettings = webhookRepository.getProjectWebhooks(projectId).stream()
                                                                .map(wh -> WebhookSetting.get(wh.getPayloadUrl(),
                                                                                              ImmutableSet.copyOf(wh.getSubscribedToEvents())))
                                                                .collect(toList());
        ProjectDetails projectDetails = getProjectDetails(projectId);
        return ProjectSettings.get(projectId,
                                   projectDetails.getDisplayName(),
                                   projectDetails.getDescription(),
                                   projectDetails.getDefaultDictionaryLanguage(),
                                   projectDetails.getDefaultDisplayNameSettings(),
                                   SlackIntegrationSettings.get(slackPayloadUrl),
                                   WebhookSettings.get(webhookSettings),
                                   projectDetails.getEntityDeprecationSettings());
    }

}
