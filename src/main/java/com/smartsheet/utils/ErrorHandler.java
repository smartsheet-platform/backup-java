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
package com.smartsheet.utils;

/**
 * An application-level error handler.
 */
public class ErrorHandler {

    private ErrorHandler() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
    }

    /**
     * Handles the given {@link Exception} in the context of the specified user,
     * either returning to the caller if the {@link Exception} was handled, or
     * rethrowing the {@link Exception} if not.
     */
    public static void handle(Exception exception, String currentUser) throws Exception {
        if (!ConfigHolder.getInstance().isContinueOnError())
            throw exception;

        // else isContinueOnError, so handle the error and return

        String exceptionType = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getLocalizedMessage();
        if (exceptionMessage == null)
            exceptionMessage = "no more details";

        String error = String.format("backing up user [%s] - %s - %s",
            currentUser, exceptionType, exceptionMessage);

        ProgressWatcher.getInstance().notifyError(error);
    }
}
