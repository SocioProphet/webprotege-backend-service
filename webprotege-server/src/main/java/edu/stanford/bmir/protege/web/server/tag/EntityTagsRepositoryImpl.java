package edu.stanford.bmir.protege.web.server.tag;

import edu.stanford.bmir.protege.web.server.persistence.Repository;
import edu.stanford.bmir.protege.web.server.inject.ProjectSingleton;
import edu.stanford.bmir.protege.web.server.project.ProjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.server.tag.EntityTags.*;
import static java.util.stream.Collectors.toMap;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 15 Mar 2018
 */
@ProjectSingleton
public class EntityTagsRepositoryImpl implements EntityTagsRepository, Repository {

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final Datastore datastore;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    private boolean empty = false;

    @Inject
    public EntityTagsRepositoryImpl(@Nonnull ProjectId projectId,
                                    @Nonnull Datastore datastore) {
        this.projectId = checkNotNull(projectId);
        this.datastore = checkNotNull(datastore);
    }

    @Override
    public void ensureIndexes() {
        datastore.ensureIndexes(EntityTags.class);
        empty = countTaggedEntities() == 0;
    }

    @Override
    public void save(@Nonnull EntityTags tag) {
        try {
            writeLock.lock();
            datastore.delete(tagWithProjectIdAndEntity(tag.getProjectId(), tag.getEntity()));
            datastore.save(tag);
            empty = false;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addTag(@Nonnull OWLEntity entity, @Nonnull TagId tagId) {
        try {
            writeLock.lock();
            Query<EntityTags> query = tagWithProjectIdAndEntity(projectId, entity);
            UpdateOperations<EntityTags> updateOps = datastore.createUpdateOperations(EntityTags.class);
            updateOps.addToSet(TAGS, tagId);
            datastore.update(query, updateOps);
            empty = false;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeTag(@Nonnull OWLEntity entity, @Nonnull TagId tagId) {
        try {
            writeLock.lock();
            Query<EntityTags> query = tagWithProjectIdAndEntity(projectId, entity);
            UpdateOperations<EntityTags> updateOps = datastore.createUpdateOperations(EntityTags.class);
            updateOps.removeAll(TAGS, tagId);
            datastore.update(query, updateOps);
            empty = countTaggedEntities() == 0;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeTag(@Nonnull TagId tagId) {
        try {
            writeLock.lock();
            Query<EntityTags> query = datastore.createQuery(EntityTags.class)
                                               .field(PROJECT_ID).equal(projectId);
            UpdateOperations<EntityTags> updateOps = datastore.createUpdateOperations(EntityTags.class);
            updateOps.removeAll(TAGS, tagId);
            datastore.update(query, updateOps);
            empty = countTaggedEntities() == 0;
        } finally {
            writeLock.unlock();
        }
    }

    private Query<EntityTags> tagWithProjectIdAndEntity(ProjectId projectId, OWLEntity entity) {
        return datastore.createQuery(EntityTags.class)
                        .field(PROJECT_ID).equal(projectId)
                        .field(ENTITY).equal(entity);
    }

    @Nonnull
    public Map<OWLEntity, EntityTags> findAll() {
        try {
            readLock.lock();
            if(empty) {
                return Collections.emptyMap();
            }
            return datastore.createQuery(EntityTags.class)
                            .field(PROJECT_ID).equal(projectId)
                            .asList()
                            .stream()
                            .collect(toMap(EntityTags::getEntity, tags -> tags));
        } finally {
            readLock.unlock();
        }
    }

    @Nonnull
    @Override
    public Optional<EntityTags> findByEntity(@Nonnull OWLEntity entity) {
        try {
            readLock.lock();
            if(empty) {
                return Optional.empty();
            }
            return Optional.ofNullable(tagWithProjectIdAndEntity(projectId, entity).get());
        } finally {
            readLock.unlock();
        }
    }

    @Nonnull
    @Override
    public Collection<EntityTags> findByTagId(@Nonnull TagId tagId) {
        try {
            readLock.lock();
            if(empty) {
                return Collections.emptySet();
            }
            return datastore.find(EntityTags.class)
                            .field(TAGS)
                            .equal(tagId)
                            .asList();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long countTaggedEntities() {
        return datastore.createQuery(EntityTags.class)
                .field(PROJECT_ID).equal(projectId)
                .count();
    }

}
