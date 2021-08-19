package edu.stanford.protege.webprotege;

import edu.stanford.protege.webprotege.download.DownloadGeneratorExecutor;
import edu.stanford.protege.webprotege.download.FileTransferExecutor;
import edu.stanford.protege.webprotege.index.IndexUpdatingService;
import edu.stanford.protege.webprotege.inject.ApplicationExecutorsRegistry;
import edu.stanford.protege.webprotege.upload.UploadedOntologiesCacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-13
 */
@Configuration
public class ExecutorsConfiguration {

    private static final int MAX_FILE_DOWNLOAD_THREADS = 5;

    private static final int INDEX_UPDATING_THREADS = 10;

    @Bean
    @DownloadGeneratorExecutor
    public ExecutorService provideDownloadGeneratorExecutorService(ApplicationExecutorsRegistry executorsRegistry) {
        // Might prove to be too much of a bottle neck.  For now, this limits the memory we need
        // to generate downloads
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName(thread.getName().replace("thread", "Download-Generator"));
            return thread;
        });
        executorsRegistry.registerService(executor, "Download-Generator-Service");
        return executor;
    }

    @Bean
    @FileTransferExecutor
    public ExecutorService provideFileTransferExecutorService(ApplicationExecutorsRegistry executorsRegistry) {
        var executor = Executors.newFixedThreadPool(MAX_FILE_DOWNLOAD_THREADS, r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName(thread.getName().replace("thread", "Download-Streamer"));
            return thread;
        });
        executorsRegistry.registerService(executor, "Download-Streaming-Service");
        return executor;
    }

    @Bean
    @IndexUpdatingService
    public ExecutorService provideIndexUpdatingExecutorService(ApplicationExecutorsRegistry executorsRegistry) {
        var executor = Executors.newFixedThreadPool(INDEX_UPDATING_THREADS, r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName(thread.getName().replace("thread", "Index-Updater"));
            return thread;
        });
        executorsRegistry.registerService(executor, "Index-Updater");
        return executor;
    }

    @Bean
    @UploadedOntologiesCacheService
    public ScheduledExecutorService provideUploadedOntologiesCacheService(ApplicationExecutorsRegistry executorsRegistry) {
        var executor = Executors.newSingleThreadScheduledExecutor();
        executorsRegistry.registerService(executor, "Uploaded-Ontologies-Cache-Service");
        return executor;
    }
}