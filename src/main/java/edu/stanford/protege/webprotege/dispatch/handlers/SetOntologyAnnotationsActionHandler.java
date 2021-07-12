package edu.stanford.protege.webprotege.dispatch.handlers;

import edu.stanford.protege.webprotege.access.AccessManager;
import edu.stanford.protege.webprotege.change.*;
import edu.stanford.protege.webprotege.dispatch.AbstractProjectChangeHandler;
import edu.stanford.protege.webprotege.dispatch.ExecutionContext;
import edu.stanford.protege.webprotege.events.EventManager;
import edu.stanford.protege.webprotege.index.OntologyAnnotationsIndex;
import edu.stanford.protege.webprotege.access.BuiltInAction;
import edu.stanford.protege.webprotege.ontology.SetOntologyAnnotationsAction;
import edu.stanford.protege.webprotege.ontology.SetOntologyAnnotationsResult;
import edu.stanford.protege.webprotege.event.EventList;
import edu.stanford.protege.webprotege.event.ProjectEvent;
import edu.stanford.protege.webprotege.frame.PropertyAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.protege.webprotege.access.BuiltInAction.EDIT_ONTOLOGY;
import static edu.stanford.protege.webprotege.access.BuiltInAction.EDIT_ONTOLOGY_ANNOTATIONS;
import static java.util.Arrays.asList;

/**
 * Author: Matthew Horridge<br> Stanford University<br> Bio-Medical Informatics Research Group<br> Date: 01/08/2013
 */
public class SetOntologyAnnotationsActionHandler extends AbstractProjectChangeHandler<Set<OWLAnnotation>, SetOntologyAnnotationsAction, SetOntologyAnnotationsResult> {

    @Nonnull
    private final OWLDataFactory dataFactory;

    @Nonnull
    private final OntologyAnnotationsIndex ontologyAnnotationsIndex;

    @Inject
    public SetOntologyAnnotationsActionHandler(@Nonnull AccessManager accessManager,
                                               @Nonnull EventManager<ProjectEvent<?>> eventManager,
                                               @Nonnull HasApplyChanges applyChanges,
                                               @Nonnull OWLDataFactory dataFactory,
                                               @Nonnull OntologyAnnotationsIndex ontologyAnnotationsIndex) {
        super(accessManager, eventManager, applyChanges);
        this.dataFactory = checkNotNull(dataFactory);
        this.ontologyAnnotationsIndex = checkNotNull(ontologyAnnotationsIndex);
    }

    @Nonnull
    @Override
    public Class<SetOntologyAnnotationsAction> getActionClass() {
        return SetOntologyAnnotationsAction.class;
    }

    @Nonnull
    @Override
    protected Iterable<BuiltInAction> getRequiredExecutableBuiltInActions(SetOntologyAnnotationsAction action) {
        return asList(EDIT_ONTOLOGY, EDIT_ONTOLOGY_ANNOTATIONS);
    }

    @Override
    protected ChangeListGenerator<Set<OWLAnnotation>> getChangeListGenerator(SetOntologyAnnotationsAction action,
                                                                             ExecutionContext executionContext) {
        final var fromAnnotations = action.getFromAnnotations();
        final var toAnnotations = action.getToAnnotations();
        var ontologyId = action.getOntologyId();

        var changeList = new ArrayList<OntologyChange>();

        for(PropertyAnnotationValue annotation : fromAnnotations) {
            if(!toAnnotations.contains(annotation)) {
                annotation.getValue()
                          .asAnnotationValue()
                          .ifPresent(av -> {
                              var property = annotation.getProperty()
                                                       .getEntity()
                                                       .asOWLAnnotationProperty();
                              var oldAnnotation = dataFactory.getOWLAnnotation(
                                      property,
                                      av);
                              var chg = RemoveOntologyAnnotationChange.of(
                                      ontologyId,
                                      oldAnnotation);
                              changeList.add(chg);
                          });
            }
        }
        for(PropertyAnnotationValue annotation : toAnnotations) {
            if(!fromAnnotations.contains(annotation)) {
                annotation.getValue()
                          .asAnnotationValue()
                          .ifPresent(av -> {
                              var property = annotation.getProperty()
                                                       .getEntity()
                                                       .asOWLAnnotationProperty();
                              var newAnnotation = dataFactory.getOWLAnnotation(
                                      property,
                                      av
                              );
                              changeList.add(AddOntologyAnnotationChange.of(ontologyId,
                                                                                       newAnnotation));
                          });
            }
        }
        return new FixedChangeListGenerator<>(changeList, Collections.emptySet(), "Edited ontology annotations");
    }

    @Override
    protected SetOntologyAnnotationsResult createActionResult(ChangeApplicationResult<Set<OWLAnnotation>> changeApplicationResult,
                                                              SetOntologyAnnotationsAction action,
                                                              ExecutionContext executionContext,
                                                              EventList<ProjectEvent<?>> eventList) {
        var ontologyId = action.getOntologyId();
        Set<OWLAnnotation> annotations = ontologyAnnotationsIndex
                .getOntologyAnnotations(ontologyId)
                .collect(Collectors.toSet());
        return SetOntologyAnnotationsResult.create(annotations, eventList);
    }
}