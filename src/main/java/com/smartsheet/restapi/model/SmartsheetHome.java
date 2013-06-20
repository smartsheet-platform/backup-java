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

public class SmartsheetHome {

    private List<SmartsheetSheet> sheets = Collections.emptyList();

    private List<SmartsheetFolder> folders = Collections.emptyList();

    private List<SmartsheetWorkspace> workspaces = Collections.emptyList();

    public SmartsheetHome() {
    }

    public List<SmartsheetSheet> getSheets() {
        return sheets;
    }

    public void setSheets(List<SmartsheetSheet> sheets) {
        this.sheets = sheets;
    }

    public List<SmartsheetFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<SmartsheetFolder> folders) {
        this.folders = folders;
    }

    public List<SmartsheetWorkspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<SmartsheetWorkspace> workspaces) {
        this.workspaces = workspaces;
    }

    @Override
    public String toString() {
        return "SmartsheetHome [sheets=" + sheets + ", folders=" + folders + ", workspaces=" + workspaces + "]";
    }
}
