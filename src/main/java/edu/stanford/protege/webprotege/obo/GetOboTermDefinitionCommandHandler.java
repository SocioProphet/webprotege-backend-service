package edu.stanford.protege.webprotege.obo;

import edu.stanford.protege.webprotege.api.ActionExecutor;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import javax.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-20
 */
@WebProtegeHandler
public class GetOboTermDefinitionCommandHandler implements CommandHandler<GetOboTermDefinitionAction, GetOboTermDefinitionResult> {

    private final ActionExecutor executor;

    public GetOboTermDefinitionCommandHandler(ActionExecutor executor) {
        this.executor = executor;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetOboTermDefinitionAction.CHANNEL;
    }

    @Override
    public Class<GetOboTermDefinitionAction> getRequestClass() {
        return GetOboTermDefinitionAction.class;
    }

    @Override
    public Mono<GetOboTermDefinitionResult> handleRequest(GetOboTermDefinitionAction request,
                                                          ExecutionContext executionContext) {
        return executor.executeRequest(request, executionContext);
    }
}