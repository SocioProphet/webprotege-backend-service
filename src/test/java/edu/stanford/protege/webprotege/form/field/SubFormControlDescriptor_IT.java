package edu.stanford.protege.webprotege.form.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.jackson.ObjectMapperProvider;
import edu.stanford.protege.webprotege.form.ExpansionState;
import edu.stanford.protege.webprotege.form.FormDescriptor;
import edu.stanford.protege.webprotege.form.FormId;
import edu.stanford.protege.webprotege.lang.LanguageMap;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-09
 */
public class SubFormControlDescriptor_IT {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapperProvider().get();
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        var formDescriptor = new FormDescriptor(FormId.get("12345678-1234-1234-1234-123456789abc"),
                                                LanguageMap.of("en", "The sub form"),
                                                singletonList(
                                                        FormFieldDescriptor.get(
                                                                FormFieldId.get(UUID.randomUUID().toString()),
                                                                OwlPropertyBinding.get(new OWLObjectPropertyImpl(
                                                                                               OWLRDFVocabulary.RDFS_LABEL.getIRI()),
                                                                                       null),
                                                                LanguageMap.of("en", "The Label"),
                                                                FieldRun.START,
                                                                FormFieldDeprecationStrategy.LEAVE_VALUES_INTACT,
                                                                new TextControlDescriptor(
                                                                        LanguageMap.empty(),
                                                                        StringType.SIMPLE_STRING,
                                                                        LineMode.SINGLE_LINE,
                                                                        "Pattern",
                                                                        LanguageMap.empty()
                                                                ),
                                                                Repeatability.NON_REPEATABLE,
                                                                Optionality.REQUIRED,
                                                                true,
                                                                ExpansionState.COLLAPSED,
                                                                LanguageMap.empty()
                                                        )
                                                ), Optional.empty());
        SubFormControlDescriptor descriptor = new SubFormControlDescriptor(formDescriptor);
        var serialized = objectMapper.writeValueAsString(descriptor);
        System.out.println(serialized);
        var deserialized = objectMapper.readerFor(SubFormControlDescriptor.class)
                .readValue(serialized);
        assertThat(deserialized, is(descriptor));
    }
}