/**
   Copyright 2013 Smartsheet.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

**/
package com.smartsheet.tools;

import static com.smartsheet.utils.HttpUtils.saveUrlToFile;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.smartsheet.utils.ProgressWatcher;

/**
 * A service which downloads files from the Internet in parallel (i.e.,
 * asynchronously). The methods of this class are designed to be thread-safe.
 */
public class ParallelDownloadService {

    private final ExecutorService executor;
    private final long allJobsDoneTimeoutMinutes;

    /** The number of posted jobs */
    private final AtomicInteger posts = new AtomicInteger();
    /** The number of completed jobs */
    private final AtomicInteger completions = new AtomicInteger();
    /** The number of failed jobs */
    private final AtomicInteger failures = new AtomicInteger();

    /**
     * @param numberOfThreads
     *          The number of threads to allocate for parallel downloading.
     *          It is recommended that this number is not more than the number
     *          of cores on the executing machine. At least one thread must be
     *          specified, or else {@link IllegalArgumentException} will be thrown.
     *
     * @param allJobsDoneTimeoutMinutes
     *          The maximum timeout in minutes to wait until all parallel
     *          download jobs are "done" (completed successfully) (applied when
     *          the {@link #waitTillAllDownloadJobsDone} method is called). A
     *          value of one or greater is expected.
     */
    public ParallelDownloadService(int numberOfThreads, long allJobsDoneTimeoutMinutes)
            throws IllegalArgumentException {

        executor = Executors.newFixedThreadPool(numberOfThreads);
        this.allJobsDoneTimeoutMinutes = allJobsDoneTimeoutMinutes;
    }

    /**
     * Posts an asynchronous ("parallel") download job.
     *
     * @param source
     *          The source of the file on the Internet to download.
     *
     * @param targetFile
     *          The local file to download the source to. The file will be
     *          created if it doesn't exist, and overwritten if it does.
     *
     * @param postedMessage
     *          The message to log when the job has been posted.
     *
     * @param completedMessage
     *          The message to log when the job has been completed.
     *
     * @param errorContext
     *          Textual context to add to the error message logged when the job has failed.
     */
    public void postAsynchronousDownloadJob(
            final InternetContentSource source,
            final File targetFile,
            final String postedMessage, 
            final String completedMessage,
            final String errorContext) {

        ProgressWatcher.getInstance().notify(postedMessage);

        // Submit a new job, returning immediately. The job will be queued until
        // a thread in the pool becomes available to handle it.
        executor.execute(new Runnable() {

            // The logic which is executed asynchronously when a thread becomes
            // available to handle the job.
            @Override
            public void run() {
            	String sourceUrl = "";
                try {
                	sourceUrl = source.getURL();
                	
                    saveUrlToFile(sourceUrl, targetFile);

                    ProgressWatcher.getInstance().notify(completedMessage);

                    completions.incrementAndGet();

                } catch (Exception e) {
                    failures.incrementAndGet();

                    ProgressWatcher.getInstance().notifyError(String.format("[%s: %s] downloading from [%s] to [%s] for %s",
                        e.getClass().getSimpleName(), e.getLocalizedMessage(), sourceUrl, targetFile, errorContext));
                }
            }});

        // This is executed immediately after the job is posted.
        posts.incrementAndGet();
    }

    /**
     * @return
     *      {@code true} if all jobs were "done" (completed successfully);
     *      otherwise {@code false}
     */
    public boolean waitTillAllDownloadJobsDone() {
        // first check if we actually need to wait (optimization)
        if (completions.intValue() == posts.intValue())
            return true; // all jobs completed, no need to wait

        if (failures.intValue() == posts.intValue()) {
            ProgressWatcher.getInstance().notify("***WARNING*** All " + posts + " parallel download jobs failed (see previous logs)");
            return false; // all jobs failed, also no need to wait
        }

        // initiate shutdown
        executor.shutdown();

        // prepare to wait
        String timeUnits = allJobsDoneTimeoutMinutes <= 1 ? "minute" : "minutes";
        ProgressWatcher.getInstance().notify("Wait up to " + allJobsDoneTimeoutMinutes + " " + timeUnits + " for any outstanding parallel download jobs...");

        // wait...
        boolean allDone = false;
        try {
            allDone = executor.awaitTermination(allJobsDoneTimeoutMinutes, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // user or system interrupted the wait
        }

        if (!allDone)
            ProgressWatcher.getInstance().notifyError("Not all parallel download jobs completed. Please retry with a longer wait.");

        // force shutdown
        executor.shutdownNow();

        // check if all completed (completions equal posts)
        allDone = completions.intValue() == posts.intValue();
        if (!allDone)
            ProgressWatcher.getInstance().notify("***WARNING*** " + failures.intValue() + " of " + posts.intValue() + " parallel download jobs didn't complete (see previous logs)");

        // since now shutdown, reset counters
        posts.set(0);
        completions.set(0);
        failures.set(0);

        return allDone;
    }
}
