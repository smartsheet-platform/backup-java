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
package com.smartsheet.exceptions;

public abstract class AbstractSmartsheetGetItemDetailsException extends AbstractSmartsheetServiceException {

    private static final long serialVersionUID = 1L;

    private final long itemId;

    protected AbstractSmartsheetGetItemDetailsException(Exception cause, long itemId) {
        super(cause);
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
    }

    @Override
    public String getMessage() {
        Throwable cause = getCause();
        return String.format("Failed to get %s with id [%d] due to %s: %s",
            getItemType(), itemId, cause.getClass().getSimpleName(), cause.getMessage());
    }

    protected abstract String getItemType();
}
