package edu.stanford.bmir.protege.web.client.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import edu.stanford.bmir.protege.web.client.Messages;
import edu.stanford.bmir.protege.web.client.form.FormPresenter;
import edu.stanford.bmir.protege.web.client.form.FormStackPresenter;
import edu.stanford.bmir.protege.web.client.form.LanguageMapCurrentLocaleMapper;
import edu.stanford.bmir.protege.web.client.library.dlg.DialogButton;
import edu.stanford.bmir.protege.web.client.library.modal.ModalManager;
import edu.stanford.bmir.protege.web.client.library.modal.ModalPresenter;
import edu.stanford.bmir.protege.web.client.uuid.UuidV4Provider;
import edu.stanford.bmir.protege.web.shared.DataFactory;
import edu.stanford.bmir.protege.web.shared.entity.FreshEntityIri;
import edu.stanford.bmir.protege.web.shared.form.FormDescriptorDto;
import edu.stanford.bmir.protege.web.shared.form.FormId;
import edu.stanford.bmir.protege.web.shared.form.data.FormData;
import edu.stanford.bmir.protege.web.shared.form.data.FormDataDto;
import edu.stanford.bmir.protege.web.shared.form.data.FormFieldDataDto;
import edu.stanford.bmir.protege.web.shared.form.data.FormSubjectDto;
import edu.stanford.bmir.protege.web.shared.lang.LanguageMap;
import edu.stanford.bmir.protege.web.shared.pagination.Page;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-09-29
 */
public class CreateEntityFormPresenter {

    @Nonnull
    private final FormPresenter formStackPresenter;

    @Nonnull
    private final CreateEntityFormView view;

    @Nonnull
    private UuidV4Provider uuidV4Provider;

    @Nonnull
    private final ModalManager modalManager;

    @Nonnull
    private final Messages messages;

    @Nonnull
    private final LanguageMapCurrentLocaleMapper localeMapper;

    @Inject
    public CreateEntityFormPresenter(@Nonnull FormPresenter formStackPresenter,
                                     @Nonnull CreateEntityFormView view,
                                     @Nonnull UuidV4Provider uuidV4Provider,
                                     @Nonnull ModalManager modalManager,
                                     @Nonnull Messages messages,
                                     @Nonnull LanguageMapCurrentLocaleMapper localeMapper) {
        this.formStackPresenter = checkNotNull(formStackPresenter);
        this.view = checkNotNull(view);
        this.uuidV4Provider = uuidV4Provider;
        this.modalManager = checkNotNull(modalManager);
        this.messages = messages;
        this.localeMapper = localeMapper;
    }

    public void createEntities(@Nonnull EntityType<?> entityType,
                               @Nonnull Optional<? extends OWLEntity> parentEntity,
                               @Nonnull CreateEntityPresenter.EntitiesCreatedHandler entitiesCreatedHandler,
                               @Nonnull ImmutableList<FormDescriptorDto> formDescriptorDtos) {
        if(formDescriptorDtos.isEmpty()) {
            throw new RuntimeException("No form descriptors have been specified");
        }

        ImmutableSet<IRI> parentEntityIris = parentEntity.map(owlEntity -> ImmutableSet.of(owlEntity.getIRI()))
                                       .orElseGet(ImmutableSet::of);
        FreshEntityIri freshEntityIri = FreshEntityIri.get("", "", uuidV4Provider.get(), parentEntityIris);
        OWLEntity entity = DataFactory.getOWLEntity(entityType, freshEntityIri.getIri());
        FormSubjectDto subject = FormSubjectDto.getFormSubject(
            DataFactory.getOWLEntityData(entity, ImmutableMap.of())
        );
        formStackPresenter.start(view.getFormsContainer());
        ImmutableList<FormDataDto> formData = formDescriptorDtos.stream()
                                                               .map(descriptor -> {
                                                                   ImmutableList<FormFieldDataDto> fields = descriptor.getFields()
                                                                                                                       .stream()
                                                                                                                       .map(fd -> FormFieldDataDto
                                                                                                                               .get(fd,
                                                                                                                                    Page.emptyPage()))
                                                                                                                       .collect(
                                                                                                                               toImmutableList());
                                                                   return FormDataDto.get(subject,
                                                                                   descriptor,
                                                                           fields,
                                                                                   0);
                                                               })
                                                               .collect(toImmutableList());
        formStackPresenter.displayForm(formData.get(0));

        LanguageMap label = formDescriptorDtos.get(0).getLabel();
        String title = localeMapper.getValueForCurrentLocale(label);
        ModalPresenter modalPresenter = modalManager.createPresenter();
        modalPresenter.setTitle(title);
        modalPresenter.setView(view);
        modalPresenter.setEscapeButton(DialogButton.CANCEL);
        modalPresenter.setPrimaryButton(DialogButton.CREATE);
        modalPresenter.setButtonHandler(DialogButton.CREATE, closer -> {
            Optional<FormData> formData1 = formStackPresenter.getFormData();
            GWT.log(formData1.toString());
            closer.closeModal();
        });
        modalManager.showModal(modalPresenter);
        Scheduler.get().scheduleDeferred(formStackPresenter::requestFocus);
    }
}
