package edu.stanford.protege.webprotege.download;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Apr 2017
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectDownloadCache_TestCase {

    private static final String THE_PROJECT_ID = "TheProjectId";

    private static final long REVISION_NUMBER = 33L;

    private ProjectDownloadCache cache;

    @Mock
    private ProjectDownloadCacheDirectorySupplier directorySupplier;

    private ProjectId projectId = ProjectId.generate();

    @Mock
    private RevisionNumber revisionNumber;

    private DownloadFormat downloadFormat;
    
    private Path root;

    @Before
    public void setUp() throws Exception {
        when(revisionNumber.getValue()).thenReturn(REVISION_NUMBER);
        downloadFormat = DownloadFormat.RDF_XML;
        root = Paths.get("tmp");
        when(directorySupplier.get()).thenReturn(root);
        cache = new ProjectDownloadCache(directorySupplier);
    }

    @Test
    public void shouldResolvePath() {
        Path path = cache.getCachedDownloadPath(projectId, revisionNumber, downloadFormat);
        Path expectedPath = root.resolve(THE_PROJECT_ID).resolve(THE_PROJECT_ID + "-R" + REVISION_NUMBER + "." + downloadFormat.getExtension() + ".zip");
        assertThat(path, is(expectedPath));
    }
}
