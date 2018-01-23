package edu.stanford.bmir.protege.web.client.dispatch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.web.bindery.event.shared.EventBus;
import edu.stanford.bmir.protege.web.client.dispatch.cache.ResultCache;
import edu.stanford.bmir.protege.web.client.library.msgbox.MessageBox;
import edu.stanford.bmir.protege.web.client.progress.HasBusy;
import edu.stanford.bmir.protege.web.client.user.LoggedInUser;
import edu.stanford.bmir.protege.web.shared.HasProjectId;
import edu.stanford.bmir.protege.web.shared.TimeUtil;
import edu.stanford.bmir.protege.web.shared.app.UserInSession;
import edu.stanford.bmir.protege.web.shared.dispatch.Action;
import edu.stanford.bmir.protege.web.shared.dispatch.DispatchServiceResultContainer;
import edu.stanford.bmir.protege.web.shared.dispatch.InvocationExceptionTolerantAction;
import edu.stanford.bmir.protege.web.shared.dispatch.Result;
import edu.stanford.bmir.protege.web.shared.event.HasEventList;
import edu.stanford.bmir.protege.web.shared.event.WebProtegeEvent;
import edu.stanford.bmir.protege.web.shared.events.EventList;
import edu.stanford.bmir.protege.web.shared.inject.ApplicationSingleton;
import edu.stanford.bmir.protege.web.shared.permissions.PermissionDeniedException;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 20/01/2013
 */
@ApplicationSingleton
public class DispatchServiceManager {

    @Nonnull
    private final DispatchServiceAsync async;

    @Nonnull
    private final EventBus eventBus;

    @Nonnull
    private final SignInRequiredHandler signInRequiredHandler;

    @Nonnull
    private final LoggedInUser loggedInUser;

    @Nonnull
    private final PlaceController placeController;

    @Inject
    public DispatchServiceManager(@Nonnull EventBus eventBus,
                                  @Nonnull SignInRequiredHandler signInRequiredHandler,
                                  @Nonnull LoggedInUser loggedInUser,
                                  @Nonnull PlaceController placeController) {
        this.loggedInUser = checkNotNull(loggedInUser);
        this.placeController = placeController;
        async = GWT.create(DispatchService.class);
        this.eventBus = checkNotNull(eventBus);
        this.signInRequiredHandler = checkNotNull(signInRequiredHandler);
    }

    private int requestCount;

    private Map<ProjectId, ResultCache> resultCacheMap = new HashMap<>();



    private ResultCache getResultCache(ProjectId projectId, EventBus eventBus) {
        ResultCache resultCache = resultCacheMap.get(projectId);
        if(resultCache == null) {
            resultCache = new ResultCache(projectId, eventBus);
            resultCacheMap.put(projectId, resultCache);
        }
        return resultCache;
    }

    @SuppressWarnings("unchecked")
    public <A extends Action<R>, R extends Result> void execute(A action, final DispatchServiceCallback<R> callback) {
        callback.handleSubmittedForExecution();
        if(action instanceof HasProjectId) {
            ProjectId projectId = ((HasProjectId) action).getProjectId();
            ResultCache resultCache = getResultCache(projectId, eventBus);
            Optional<R> result = resultCache.getCachedResult(action);
            if (result.isPresent()) {
                callback.onSuccess(result.get());
                return;
            }
        }
        requestCount++;
        GWT.log("[Dispatch] Executing action " + requestCount + "    " + action.getClass().getSimpleName());
        async.executeAction(action, new AsyncCallbackProxy(action, callback));
    }


    public <A extends Action<R>, R extends Result> void execute(A action, final Consumer<R> successConsumer) {
        execute(action, new DispatchServiceCallback<R>() {
            @Override
            public void handleSuccess(R r) {
                successConsumer.accept(r);
            }
        });
    }

    public <A extends Action<R>, R extends Result> void execute(A action, HasBusy hasBusy, final Consumer<R> successConsumer) {
        execute(action, new DispatchServiceCallback<R>() {

            private Timer timer = new Timer() {
                @Override
                public void run() {
                    hasBusy.setBusy(true);
                }
            };

            @Override
            public void handleFinally() {
                hasBusy.setBusy(false);
            }

            @Override
            public void handleSuccess(R r) {
                timer.cancel();
                successConsumer.accept(r);
            }

            @Override
            public void handleSubmittedForExecution() {
                timer.schedule(1000);
            }
        });
    }




    private class AsyncCallbackProxy<R extends Result> implements AsyncCallback<DispatchServiceResultContainer> {

        private Action<?> action;

        private DispatchServiceCallback<Result> delegate;

        public AsyncCallbackProxy(Action<?> action, DispatchServiceCallback<Result> delegate) {
            this.delegate = delegate;
            this.action = action;
        }

        @Override
        public void onFailure(Throwable caught) {
            handleError(caught, action, delegate);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onSuccess(DispatchServiceResultContainer result) {
            if(action instanceof HasProjectId) {
                ResultCache resultCache = getResultCache(((HasProjectId) action).getProjectId(), eventBus);
                resultCache.cacheResult((Action<R>) action, (R) result.getResult());
            }
            dispatchEvents(result.getResult());
            delegate.onSuccess(result.getResult());
        }
    }

    private void dispatchEvents(Object result) {
        if(result instanceof HasEventList<?>) {
            EventList<? extends WebProtegeEvent<?>> eventList = ((HasEventList<? extends WebProtegeEvent<?>>) result).getEventList();

            List<? extends WebProtegeEvent<?>> events = eventList.getEvents();
            // TODO: FIX - Should be dispatched by the project event manager otherwise we will get events from the
            // TODO: more than once!
            GWT.log("[Dispatch] Dispatching " + events.size() + " events");
            long t0 = TimeUtil.getCurrentTime();
            for(WebProtegeEvent<?> event : events) {
                GWT.log("[Dispatch] Dispatching event (" + event + ")");
                if(event.getSource() != null) {
                    eventBus.fireEventFromSource(event.asGWTEvent(), event.getSource());
                }
                else {
                    eventBus.fireEvent(event.asGWTEvent());
                }
            }
            long t1 = TimeUtil.getCurrentTime();
            GWT.log("[Dispatch] Dispatched events in " + (t1 - t0) + " ms");
        }
    }

    private void handleError(final Throwable throwable, final Action<?> action, final DispatchServiceCallback<?> callback) {
        if (throwable instanceof PermissionDeniedException) {
            // Try to determine if the user is logged in.  The session might have expired.
            UserInSession userInSession = ((PermissionDeniedException) throwable).getUserInSession();
            if(userInSession.isGuest()) {
                // Set up next place
                Place continueTo = placeController.getWhere();
                loggedInUser.setLoggedInUser(userInSession);
                GWT.log("[Dispatch] Permission denied.  User is the guest user so redirecting to login.");
                signInRequiredHandler.handleSignInRequired(continueTo);
            }
        }
        // Skip handling for actions that do not care about errors
        if(action instanceof InvocationExceptionTolerantAction) {
            Optional<String> errorMessage = ((InvocationExceptionTolerantAction) action).handleInvocationException((InvocationException) throwable);
            errorMessage.ifPresent(this::displayAlert);
            return;
        }
        callback.onFailure(throwable);
    }

    private void displayAlert(String alert) {
        MessageBox.showAlert(alert);
    }
}