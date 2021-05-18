package edu.stanford.bmir.protege.web.server.form;

import com.google.common.collect.ImmutableList;
import edu.stanford.bmir.protege.web.server.form.data.FormControlDataDto;
import edu.stanford.bmir.protege.web.server.form.data.FormEntitySubject;
import edu.stanford.bmir.protege.web.server.form.data.MultiChoiceControlDataDto;
import edu.stanford.bmir.protege.web.server.form.field.MultiChoiceControlDescriptor;
import edu.stanford.bmir.protege.web.server.form.field.OwlBinding;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@FormDataBuilderSession
public class MultiChoiceControlValueBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final PrimitiveFormControlDataDtoRenderer renderer;

    @Inject
    public MultiChoiceControlValueBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor,
                                          @Nonnull PrimitiveFormControlDataDtoRenderer renderer) {
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.renderer = checkNotNull(renderer);
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getMultiChoiceControlDataDtoValues(@Nonnull MultiChoiceControlDescriptor multiChoiceControlDescriptor,
                                                                         @Nonnull Optional<FormEntitySubject> subject,
                                                                         @Nonnull OwlBinding theBinding,
                                                                                int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject, theBinding);
        var vals = values.stream()
                         .flatMap(renderer::toFormControlDataDto)
                         .collect(ImmutableList.toImmutableList());
        return ImmutableList.of(MultiChoiceControlDataDto.get(multiChoiceControlDescriptor,
                                                              vals,
                                                              depth));
    }
}