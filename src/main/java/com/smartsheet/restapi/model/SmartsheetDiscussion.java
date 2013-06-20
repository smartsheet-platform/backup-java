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
package com.smartsheet.restapi.model;

import java.util.Collections;
import java.util.List;

public class SmartsheetDiscussion extends SmartsheetEntity {

    private String title;

    private List<SmartsheetAttachment> commentAttachments = Collections.emptyList();

    public SmartsheetDiscussion() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SmartsheetAttachment> getCommentAttachments() {
        return commentAttachments;
    }

    public void setCommentAttachments(List<SmartsheetAttachment> commentAttachments) {
        this.commentAttachments = commentAttachments;
    }

    @Override
    public String toString() {
        return "SmartsheetDiscussion [title=" + title + ", commentAttachments=" + commentAttachments + ", id=" + id
                + "]";
    }
}
