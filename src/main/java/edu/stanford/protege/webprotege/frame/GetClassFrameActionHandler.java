package edu.stanford.protege.webprotege.frame;

import edu.stanford.protege.webprotege.access.AccessManager;
import edu.stanford.protege.webprotege.dispatch.AbstractProjectActionHandler;
import edu.stanford.protege.webprotege.dispatch.Action;
import edu.stanford.protege.webprotege.dispatch.ExecutionContext;
import edu.stanford.protege.webprotege.access.BuiltInAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.Comparator;

import static edu.stanford.protege.webprotege.logging.Markers.BROWSING;
import static edu.stanford.protege.webprotege.frame.ClassFrameTranslationOptions.AncestorsTreatment.EXCLUDE_ANCESTORS;
import static edu.stanford.protege.webprotege.frame.RelationshipTranslationOptions.*;
import static edu.stanford.protege.webprotege.frame.RelationshipTranslationOptions.RelationshipMinification.NON_MINIMIZED_RELATIONSHIPS;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 20/02/2013
 */
public class GetClassFrameActionHandler extends AbstractProjectActionHandler<GetClassFrameAction, GetClassFrameResult> {

    private static final Logger logger = LoggerFactory.getLogger(GetClassFrameActionHandler.class);

    @Nonnull
    private final ClassFrameProvider classFrameProvider;

    @Nonnull
    private final FrameComponentSessionRendererFactory rendererFactory;

    @Nonnull
    private final Comparator<PropertyValue> propertyValueComparator;

    @Inject
    public GetClassFrameActionHandler(@Nonnull AccessManager accessManager,
                                      @Nonnull ClassFrameProvider classFrameProvider,
                                      @Nonnull FrameComponentSessionRendererFactory rendererFactory,
                                      @Nonnull Comparator<PropertyValue> propertyValueComparator) {
        super(accessManager);
        this.classFrameProvider = classFrameProvider;
        this.rendererFactory = rendererFactory;
        this.propertyValueComparator = propertyValueComparator;
    }

    /**
     * Gets the class of {@link Action} handled by this handler.
     * @return The class of {@link Action}.  Not {@code null}.
     */
    @Nonnull
    @Override
    public Class<GetClassFrameAction> getActionClass() {
        return GetClassFrameAction.class;
    }

    @Nullable
    @Override
    protected BuiltInAction getRequiredExecutableBuiltInAction(GetClassFrameAction action) {
        return BuiltInAction.VIEW_PROJECT;
    }

    @Nonnull
    @Override
    public GetClassFrameResult execute(@Nonnull GetClassFrameAction action,
                                       @Nonnull ExecutionContext executionContext) {
        var subject = action.getSubject();
        var options = ClassFrameTranslationOptions.get(
                EXCLUDE_ANCESTORS,
                RelationshipTranslationOptions.get(allOutgoingRelationships(),
                                                   noIncomingRelationships(),
                                                   NON_MINIMIZED_RELATIONSHIPS));
        var classFrame = classFrameProvider.getFrame(subject, options);
        var renderedFrame = classFrame.toEntityFrame(rendererFactory.create(), propertyValueComparator);
        logger.info(BROWSING,
                    "{} {} retrieved Class frame for {}",
                    action.getProjectId(),
                    executionContext.getUserId(),
                    subject);
        return GetClassFrameResult.get(renderedFrame);
    }
}