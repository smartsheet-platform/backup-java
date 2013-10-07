package com.smartsheet.tools;

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.service.SmartsheetService;

/**
 * An {@link InternetContentSource} which is an ethereal Smartsheet attachment.
 */
public class SmartsheetAttachmentContentSource implements InternetContentSource {

    private final SmartsheetService apiService;
    private final SmartsheetAttachment attachment;
    private final String sheetName;

    public SmartsheetAttachmentContentSource(SmartsheetService apiService,
            SmartsheetAttachment attachment, String sheetName) {
        this.apiService = apiService;
        this.attachment = attachment;
        this.sheetName = sheetName;
    }

    @Override
    public String getURL() throws Exception {
        SmartsheetAttachment attachmentDetails = apiService.getAttachmentDetails(
            attachment.getName(), attachment.getId(), sheetName);
        return attachmentDetails.getUrl(); // ethereal - valid for only 2 minutes
    }
}
