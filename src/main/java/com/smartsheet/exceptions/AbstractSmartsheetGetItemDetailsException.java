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

    private final String itemName;
    private final long itemId;
    private final String parentItemName;

    protected AbstractSmartsheetGetItemDetailsException(Exception cause, String itemName, long itemId, String parentItemName) {
        super(cause);
        this.itemName = itemName;
        this.itemId = itemId;
        this.parentItemName = parentItemName;
    }

    public String getItemName() {
        return itemName;
    }

    public long getItemId() {
        return itemId;
    }

    public String getParentItemName() {
        return parentItemName;
    }

    @Override
    public String getMessage() {
        Throwable cause = getCause();
        String errorType = cause.getClass().getSimpleName();
        String errorMessage = cause.getMessage();

        if (getParentItemType() == null) // no parent
            return String.format("Failed to get %s with name [%s] and id [%d] due to %s: %s",
                getItemType(), itemName, itemId, errorType, errorMessage);

        // else have parent
        return String.format("Failed to get %s with name [%s] and id [%d] belonging to %s [%s] due to %s: %s",
            getItemType(), itemName, itemId, getParentItemType(), parentItemName, errorType, errorMessage);
    }

    protected abstract String getItemType();

    protected String getParentItemType() {
        return null; // this item has no parent
    }
}
