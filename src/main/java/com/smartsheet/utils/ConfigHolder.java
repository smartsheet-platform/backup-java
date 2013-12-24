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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A holder of specific configuration properties for the current application.
 * Any such properties are injected into this holder.
 */
public class ConfigHolder {

    private static final ConfigHolder singleton = new ConfigHolder();
    private static final String DEFAULT_PROPERTY_FILE = "/smartsheet-backup.properties";
    private Properties properties;

    public static ConfigHolder getInstance() {
        return singleton;
    }

    // config properties
    private boolean continueOnError;

    private ConfigHolder() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
        try{
          loadDefaultPropertyFile();
        } catch(IOException e){
          throw new RuntimeException(e);
        }
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    private void loadDefaultPropertyFile() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(DEFAULT_PROPERTY_FILE);
        properties = new Properties();
        properties.load(inputStream);
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
}
