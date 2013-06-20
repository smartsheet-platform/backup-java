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

public class SmartsheetRow extends SmartsheetEntity {

    private List<SmartsheetAttachment> attachments = Collections.emptyList();

    private List<SmartsheetDiscussion> discussions = Collections.emptyList();

    public SmartsheetRow() {
    }

    public List<SmartsheetAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<SmartsheetAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<SmartsheetDiscussion> getDiscussions() {
        return discussions;
    }

    public void setDiscussions(List<SmartsheetDiscussion> discussions) {
        this.discussions = discussions;
    }

    @Override
    public String toString() {
        return "SmartsheetRow [id=" + id + ", attachments=" + attachments + ", discussions=" + discussions + "]";
    }
}
