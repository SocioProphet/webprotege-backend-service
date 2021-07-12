package edu.stanford.protege.webprotege.tag;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 18 Mar 2018
 */
public interface TagRepository {

    void saveTag(@Nonnull Tag tag);

    void saveTags(@Nonnull Iterable<Tag> tags);

    void deleteTag(@Nonnull TagId tagId);

    @Nonnull
    List<Tag> findTags();

    @Nonnull
    Optional<Tag> findTagByTagId(@Nonnull TagId tagId);
}