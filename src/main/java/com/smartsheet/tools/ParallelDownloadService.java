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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.smartsheet.exceptions.ServiceUnavailableException;
import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.service.SmartsheetService;
import com.smartsheet.utils.ProgressWatcher;
import com.smartsheet.utils.PropertyUtils;

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
    public ParallelDownloadService(final int numberOfThreads, final long allJobsDoneTimeoutMinutes)
            throws IllegalArgumentException {

        executor = Executors.newFixedThreadPool(numberOfThreads);
        this.allJobsDoneTimeoutMinutes = allJobsDoneTimeoutMinutes;
    }

    /**
     * Posts an asynchronous ("parallel") download job. 
     *
     * @param sourceId
     *          The id of the attachment source file . 
     *          Use this sourceId to get the attachment details like source file URL on the Internet to download.
     *
     * @param targetFile
     *          The local file to download the source to. The file will be
     *          created if it doesn't exist, and overwritten if it does.

     * @param apiService
     * 			The interfacing service to get the attachment details.
     * @param sheetName 
     * @throws Exception 
     * 
     * 			 
     */
    public void postAsynchronousDownloadJob(
            final long sourceId, final File targetFile,
            final SmartsheetService apiService,final String sheetName) throws Exception {

        // Submit a new job, returning immediately. The job will be queued until
        // a thread in the pool becomes available to handle it.
        final Future<File> futureResponse = executor.submit(new Callable<File>() {

            // The logic which is executed asynchronously when a thread becomes
            // available to handle the job.
            @Override
			public File call() throws Exception {

            	//Get the attachment details from the sourceFileId using apiService
            	final SmartsheetAttachment attachment = apiService.getAttachmentDetails(sourceId);
            	
            	final String attachmentName = attachment.getName();
            	
            	//Get the source Url of the attachment
            	final String sourceUrl = attachment.getUrl();

            	final String postedMessage = String.format("[UserId : %s] :  Download request for sheet[%s] Attachment[%s]", apiService.getAssumedUser(), sheetName, attachmentName);
            	final String completedMessage = String.format("[UserId : %s] : ..Sheet[%s] Attachment [%s] downloaded", apiService.getAssumedUser(), sheetName, targetFile.getAbsolutePath());
            	try {

                    ProgressWatcher.notify(postedMessage);
					saveUrlToFile(sourceUrl, targetFile);
                    ProgressWatcher.notify(completedMessage);

                    completions.incrementAndGet();
                    return targetFile;

                } catch (final Exception e) {
                    failures.incrementAndGet();
					ProgressWatcher.notifyError(String.format("[UserId : %s] : Error while downloading sheet[%s] attachment[%s]",
	                        apiService.getAssumedUser(), sheetName, attachmentName));
                    if(!PropertyUtils.isContinueOnError()){
         				throw e;
         			}
                    return null;
                }
            }});
        try {
			futureResponse.get();
		} catch (final InterruptedException e) {
			ProgressWatcher.notifyError(String.format("[UserId : %s] : Error while downloading sheet[%s] attachments ",
                    apiService.getAssumedUser(), sheetName));
            if(!PropertyUtils.isContinueOnError()){
 				throw new ServiceUnavailableException(e.getMessage());
 			}
		} catch (final ExecutionException e) {
			ProgressWatcher.notifyError(String.format("[UserId : %s] : Error while downloading sheet[%s] attachments ",
                    apiService.getAssumedUser(), sheetName));
            if(!PropertyUtils.isContinueOnError()){
            	throw new ServiceUnavailableException(e.getMessage());
 			}
		}
       
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
            ProgressWatcher.notifyError("All " + posts + " parallel download jobs failed (see previous logs)");
            return false; // all jobs failed, also no need to wait
        }

        // initiate shutdown
        executor.shutdown();

        // prepare to wait
        final String timeUnits = allJobsDoneTimeoutMinutes <= 1 ? "minute" : "minutes";
        ProgressWatcher.notify("Wait up to " + allJobsDoneTimeoutMinutes + " " + timeUnits + " for any outstanding parallel download jobs...");

        // wait...
        boolean allDone = false;
        try {
            allDone = executor.awaitTermination(allJobsDoneTimeoutMinutes, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            // user or system interrupted the wait
        }

        if (!allDone)
            ProgressWatcher.notifyError("Not all parallel download jobs completed. Please retry with a longer wait.");

        // force shutdown
        executor.shutdownNow();

        // check if all completed (completions equal posts)
        allDone = completions.intValue() == posts.intValue();
        if (!allDone)
            ProgressWatcher.notifyError((posts.intValue() - completions.intValue()) + " parallel download jobs didn't complete");

        return allDone;
    }
}
