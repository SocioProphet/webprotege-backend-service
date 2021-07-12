package edu.stanford.protege.webprotege.crud.supplied;

import edu.stanford.protege.webprotege.crud.ChangeSetEntityCrudSession;
import edu.stanford.protege.webprotege.crud.EntityCrudKitHandler;
import edu.stanford.protege.webprotege.crud.EntityCrudKitPlugin;
import edu.stanford.protege.webprotege.crud.EntityCrudKit;
import edu.stanford.protege.webprotege.crud.EntityCrudKitPrefixSettings;
import edu.stanford.protege.webprotege.crud.EntityCrudKitSettings;
import edu.stanford.protege.webprotege.crud.gen.GeneratedAnnotationsSettings;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class SuppliedNameSuffixEntityCrudKitPlugin implements EntityCrudKitPlugin<SuppliedNameSuffixEntityCrudKitHandler, SuppliedNameSuffixSettings, ChangeSetEntityCrudSession> {

    @Nonnull
    private final SuppliedNameSuffixKit kit;

    @Nonnull
    private final SuppliedNameSuffixEntityCrudKitHandlerFactory factory;

    @Inject
    public SuppliedNameSuffixEntityCrudKitPlugin(@Nonnull SuppliedNameSuffixKit kit,
                                                 @Nonnull SuppliedNameSuffixEntityCrudKitHandlerFactory factory) {
        this.kit = checkNotNull(kit);
        this.factory = checkNotNull(factory);
    }

    @Override
    public EntityCrudKit<SuppliedNameSuffixSettings> getEntityCrudKit() {
        return kit;
    }

    @Override
    public EntityCrudKitHandler<SuppliedNameSuffixSettings, ChangeSetEntityCrudSession> getEntityCrudKitHandler() {
        return factory.create(EntityCrudKitPrefixSettings.get(), SuppliedNameSuffixSettings.get(), GeneratedAnnotationsSettings
                .empty());
    }

    @Override
    public EntityCrudKitHandler<SuppliedNameSuffixSettings, ChangeSetEntityCrudSession> getEntityCrudKitHandler(EntityCrudKitSettings<SuppliedNameSuffixSettings> settings) {
        return factory.create(settings.getPrefixSettings(), settings.getSuffixSettings(),
                              settings.getGeneratedAnnotationsSettings());
    }

    @Override
    public SuppliedNameSuffixSettings getDefaultSettings() {
        return SuppliedNameSuffixSettings.get();
    }
}