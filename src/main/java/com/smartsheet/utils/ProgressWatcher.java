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
 * A Progress Watcher which receives status and error notifications, publishing
 * them to the console.
 */
public class ProgressWatcher {

    private ProgressWatcher() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
    }

    public static void notify(String status) {
        System.out.println(status);
    }

    public static void notifyError(String error) {
        notify("***ERROR*** " + error);
    }

    public static void notifyError(Throwable error) {
        Throwable cause = error.getCause();
        if (cause != null)
            error = cause;
        notifyError(String.format("%s - %s", error.getClass().getSimpleName(), error.getLocalizedMessage()));
        System.out.flush(); // flush before printing stack trace to avoid overlapping logs from multiple threads
        error.printStackTrace();
    }
}
