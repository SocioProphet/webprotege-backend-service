package edu.stanford.bmir.protege.web.server.form;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.access.BuiltInAction;
import edu.stanford.bmir.protege.web.server.project.ProjectId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-16
 */
public class GetEntityFormDescriptorActionHandler extends AbstractProjectActionHandler<GetEntityFormDescriptorAction, GetEntityFormDescriptorResult> {

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final EntityFormSelectorRepository selectorRepository;

    @Nonnull
    private final EntityFormRepository entityFormRepository;

    @Inject
    public GetEntityFormDescriptorActionHandler(@Nonnull AccessManager accessManager,
                                                @Nonnull ProjectId projectId,
                                                @Nonnull EntityFormSelectorRepository selectorRepository,
                                                @Nonnull EntityFormRepository entityFormRepository) {
        super(accessManager);
        this.projectId = checkNotNull(projectId);
        this.selectorRepository = checkNotNull(selectorRepository);
        this.entityFormRepository = checkNotNull(entityFormRepository);
    }

    @Nonnull
    @Override
    public GetEntityFormDescriptorResult execute(@Nonnull GetEntityFormDescriptorAction action,
                                                 @Nonnull ExecutionContext executionContext) {

        var formId = action.getFormId();
        var formDescriptor = entityFormRepository.findFormDescriptor(projectId, formId)
                                                 .orElse(null);
        Optional<EntityFormSelector> firstSelector = selectorRepository.findFormSelectors(projectId)
                                                               .filter(selector -> selector.getFormId().equals(formId))
                                                               .findFirst();
        var formSelector = firstSelector.map(EntityFormSelector::getCriteria)
                                        .orElse(null);
        var purpose = firstSelector.map(EntityFormSelector::getPurpose)
                .orElse(FormPurpose.ENTITY_EDITING);
        return GetEntityFormDescriptorResult.get(projectId,
                                                 action.getFormId(),
                                                 formDescriptor,
                                                 purpose,
                                                 formSelector);
    }

    @Nonnull
    @Override
    public Class<GetEntityFormDescriptorAction> getActionClass() {
        return GetEntityFormDescriptorAction.class;
    }

    @Nullable
    @Override
    protected BuiltInAction getRequiredExecutableBuiltInAction(GetEntityFormDescriptorAction action) {
        return BuiltInAction.VIEW_PROJECT;
    }
}
