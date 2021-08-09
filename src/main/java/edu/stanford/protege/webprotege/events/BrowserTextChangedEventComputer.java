package edu.stanford.protege.webprotege.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import edu.stanford.protege.webprotege.change.ChangeApplicationResult;
import edu.stanford.protege.webprotege.change.HasGetChangeSubjects;
import edu.stanford.protege.webprotege.change.OntologyChange;
import edu.stanford.protege.webprotege.event.BrowserTextChangedEvent;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.revision.Revision;
import edu.stanford.protege.webprotege.shortform.DictionaryLanguage;
import edu.stanford.protege.webprotege.shortform.DictionaryManager;
import org.semanticweb.owlapi.model.HasContainsEntityInSignature;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 18/02/2014
 */
public class BrowserTextChangedEventComputer implements EventTranslator {

    private final Map<OWLEntity, ImmutableMap<DictionaryLanguage, String>> shortFormMap = Maps.newHashMap();

    private final ProjectId projectId;

    @Nonnull
    private final DictionaryManager dictionaryManager;

    private final HasGetChangeSubjects changeSubjectsProvider;

    private final HasContainsEntityInSignature signature;

    @Inject
    public BrowserTextChangedEventComputer(@Nonnull ProjectId projectId,
                                           @Nonnull DictionaryManager dictionaryManager,
                                           @Nonnull HasGetChangeSubjects changeSubjectsProvider,
                                           @Nonnull HasContainsEntityInSignature signature) {
        this.projectId = checkNotNull(projectId);
        this.dictionaryManager = checkNotNull(dictionaryManager);
        this.changeSubjectsProvider = checkNotNull(changeSubjectsProvider);
        this.signature = checkNotNull(signature);
    }

    @Override
    public void prepareForOntologyChanges(List<OntologyChange> submittedChanges) {
        shortFormMap.clear();
        submittedChanges.stream()
                        // All display names are based on annotations, or paths of annotations
                        .filter(ax -> ax instanceof OWLAnnotationAssertionAxiom)
                        .flatMap(change -> changeSubjectsProvider.getChangeSubjects(change).stream())
                        // The changes might only be extending the signature, so check for this.
                        // If this is the case, then there will not be any existing short form.
                        .filter(signature::containsEntityInSignature)
                        .forEach(entity -> {
                            var shortForms = dictionaryManager.getShortForms(entity);
                            shortFormMap.put(entity, shortForms);
                        });
    }

    @Override
    public void translateOntologyChanges(Revision revision, ChangeApplicationResult<?> changes, List<HighLevelProjectEventProxy> projectEventList) {
        Set<OWLEntity> processedEntities = new HashSet<>();
        changes.getChangeList().stream()
               .flatMap(change -> changeSubjectsProvider.getChangeSubjects(change).stream())
               .distinct()
               .forEach(entity -> {
                   ImmutableMap<DictionaryLanguage, String> oldShortForms = shortFormMap.get(entity);
                   ImmutableMap<DictionaryLanguage, String> shortForms = dictionaryManager.getShortForms(entity);
                   if(oldShortForms == null || !shortForms.equals(oldShortForms)) {
                       var browserTextChangedEvent = new BrowserTextChangedEvent(entity,
                                                                                                     dictionaryManager.getShortForm(
                                                                                                             entity),
                                                                                                     projectId,
                                                                                                     dictionaryManager.getShortForms(
                                                                                                             entity));
                       projectEventList.add(SimpleHighLevelProjectEventProxy.wrap(browserTextChangedEvent));
                   }
               });
    }
}
