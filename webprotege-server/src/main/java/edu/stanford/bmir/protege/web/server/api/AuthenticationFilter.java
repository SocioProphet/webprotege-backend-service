package edu.stanford.bmir.protege.web.server.api;

import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.logging.DefaultLogger;
import edu.stanford.bmir.protege.web.server.session.WebProtegeSession;
import edu.stanford.bmir.protege.web.server.session.WebProtegeSessionImpl;
import edu.stanford.bmir.protege.web.shared.api.ApiKey;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.server.api.AuthenticationConstants.*;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16 Apr 2018
 *
 * An filter that provides api-key based or session based authentication.
 */
public class AuthenticationFilter implements ContainerRequestFilter {

    @Nonnull
    private final ApiKeyChecker apiKeyChecker;

    /**
     * Note that a proxy will be injected here as {@link HttpServletRequest}
     * is request scoped but AuthenticationFilter is application (singleton) scope
     */
    @Context
    private HttpServletRequest servletRequest;

    private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);


    @Inject
    public AuthenticationFilter(@Nonnull ApiKeyChecker apiKeyChecker) {
        this.apiKeyChecker = checkNotNull(apiKeyChecker);
    }

    /**
     * Filters the request by determining the user who made the request.  The user
     * is obtained from either from an API key, that is specified as a request header
     * (Authorization: ApiKey your-api-key-here), or from a session token, that is
     * present when a user logs into WebProtege using a Web browser.
     *
     * If the user cannot be determined then the request is aborted with an UNAUTHORIZED (HTTP 401)
     * response code.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        logger.info("Received Rpc Request with cookies: {}", requestContext.getCookies());


        Optional<ApiKey> apiKey = parseApiKeyFromHeaders(requestContext);
        final UserId userId;
        if(apiKey.isPresent()) {
            userId = getUserIdFromApiKey(apiKey.get());
        }
        else {
            userId = getUserIdFromSession();
        }
        if(userId.isGuest()) {
            requestContext.abortWith(Response.status(UNAUTHORIZED).build());
        }
        else {
            requestContext.setProperty(AUTHENTICATED_USER_ID, userId);
            requestContext.setProperty(EXECUTION_CONTEXT, getExecutionContext());
            apiKey.ifPresent(key -> requestContext.setProperty(AUTHENTICATED_USER_API_KEY, key));
        }
    }

    @Nonnull
    private ExecutionContext getExecutionContext() {
        HttpSession session = servletRequest.getSession(false);
        return new ExecutionContext(new WebProtegeSessionImpl(session));
    }

    private Optional<ApiKey> parseApiKeyFromHeaders(ContainerRequestContext requestContext) {
        return ApiKeyParser.parseApiKey(requestContext.getHeaderString(ApiKeyParser.AUTHORIZATION_HEADER));
    }

    private UserId getUserIdFromApiKey(@Nonnull ApiKey apiKey) {
        Optional<UserId> userIdForApiKey = apiKeyChecker.getUserIdForApiKey(checkNotNull(apiKey));
        return userIdForApiKey.orElse(UserId.getGuest());
    }

    private UserId getUserIdFromSession() {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            logger.info("HttpSession: {}", session.getId());
            // Get the user from session
            WebProtegeSession webProtegeSession = new WebProtegeSessionImpl(session);
            logger.info("Session is assigned to " + webProtegeSession.getUserInSession());
            return webProtegeSession.getUserInSession();
        }
        else {
            return UserId.getGuest();
        }
    }
}
