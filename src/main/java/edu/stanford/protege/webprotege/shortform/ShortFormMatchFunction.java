package edu.stanford.protege.webprotege.shortform;

import com.google.common.primitives.ImmutableIntArray;
import edu.stanford.protege.webprotege.common.ShortFormMatch;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 6 Apr 2018
 */
@FunctionalInterface
public interface ShortFormMatchFunction {

    @Nonnull
    ShortFormMatch createMatch(@Nonnull OWLEntity entity,
                               @Nonnull String shortForm,
                               int matchCount,
                               @Nonnull ImmutableIntArray matchPositions);

}
