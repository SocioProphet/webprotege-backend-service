package edu.stanford.protege.webprotege.frame;

import edu.stanford.protege.webprotege.api.ActionExecutor;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import javax.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-21
 */
@WebProtegeHandler
public class UpdateObjectPropertyFrameCommandHandler implements CommandHandler<UpdateObjectPropertyFrameAction, UpdateObjectPropertyFrameResult> {

    private final ActionExecutor executor;

    public UpdateObjectPropertyFrameCommandHandler(ActionExecutor executor) {
        this.executor = executor;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return UpdateObjectPropertyFrameAction.CHANNEL;
    }

    @Override
    public Class<UpdateObjectPropertyFrameAction> getRequestClass() {
        return UpdateObjectPropertyFrameAction.class;
    }

    @Override
    public Mono<UpdateObjectPropertyFrameResult> handleRequest(UpdateObjectPropertyFrameAction request,
                                                               ExecutionContext executionContext) {
        return executor.executeRequest(request, executionContext);
    }
}