package edu.stanford.bmir.protege.web.server.search;

import com.google.common.collect.ImmutableList;
import edu.stanford.bmir.protege.web.server.persistence.Repository;
import edu.stanford.bmir.protege.web.server.project.ProjectId;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-15
 */
public interface EntitySearchFilterRepository extends Repository {

    @Nonnull
    ImmutableList<EntitySearchFilter> getSearchFilters(@Nonnull ProjectId projectId);

    void saveSearchFilters(@Nonnull ImmutableList<EntitySearchFilter> filters);

}
