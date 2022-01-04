package edu.stanford.protege.webprotege.project;

import edu.stanford.protege.webprotege.api.ActionExecutor;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import javax.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-19
 */
@WebProtegeHandler
public class MoveProjectToTrashCommandHandler implements CommandHandler<MoveProjectToTrashAction, MoveProjectToTrashResult> {

    private final ActionExecutor executor;

    public MoveProjectToTrashCommandHandler(ActionExecutor executor) {
        this.executor = executor;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return MoveProjectToTrashAction.CHANNEL;
    }

    @Override
    public Class<MoveProjectToTrashAction> getRequestClass() {
        return MoveProjectToTrashAction.class;
    }

    @Override
    public Mono<MoveProjectToTrashResult> handleRequest(MoveProjectToTrashAction request,
                                                        ExecutionContext executionContext) {
        return executor.executeRequest(request, executionContext);
    }
}