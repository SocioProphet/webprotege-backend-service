package edu.stanford.protege.webprotege.axioms;

import edu.stanford.protege.webprotege.access.AccessManager;
import edu.stanford.protege.webprotege.change.FixedChangeListGenerator;
import edu.stanford.protege.webprotege.change.OntologyChange;
import edu.stanford.protege.webprotege.change.RemoveAxiomChange;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.dispatch.AbstractProjectActionHandler;
import edu.stanford.protege.webprotege.dispatch.ExecutionContext;
import edu.stanford.protege.webprotege.project.DefaultOntologyIdManager;
import edu.stanford.protege.webprotege.project.chg.ChangeManager;
import org.jetbrains.annotations.NotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-09-01
 */
public class RemoveAxiomsDelegateHandler extends AbstractProjectActionHandler<RemoveAxiomsRequest, RemoveAxiomsResponse> {

    private final ChangeManager changeManager;

    private final DefaultOntologyIdManager defaultOntologyIdManager;

    public RemoveAxiomsDelegateHandler(@NotNull AccessManager accessManager,
                                       ChangeManager changeManager,
                                       DefaultOntologyIdManager defaultOntologyIdManager) {
        super(accessManager);
        this.changeManager = changeManager;
        this.defaultOntologyIdManager = defaultOntologyIdManager;
    }

    @NotNull
    @Override
    public Class<RemoveAxiomsRequest> getActionClass() {
        return RemoveAxiomsRequest.class;
    }

    @NotNull
    @Override
    public RemoveAxiomsResponse execute(@NotNull RemoveAxiomsRequest action,
                                        @NotNull ExecutionContext executionContext) {
        var projectId = action.projectId();
        var loader = new AxiomsDocumentLoader(projectId,
                                              action.ontologyDocument(),
                                              action.mimeType(),
                                              defaultOntologyIdManager.getDefaultOntologyId());
        var changes = loader.<OntologyChange>loadAxioms((ax, ontologyId) -> new RemoveAxiomChange(ontologyId, ax));
        var result = changeManager.applyChanges(executionContext.getUserId(),
                                   new FixedChangeListGenerator<>(changes, "", action.commitMessage()));
        var appliedChangesCount = result.getChangeList().size();
        return new RemoveAxiomsResponse(projectId,
                                        appliedChangesCount);
    }
}
