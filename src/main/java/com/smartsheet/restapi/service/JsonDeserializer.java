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
package com.smartsheet.restapi.service;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

public class JsonDeserializer<T> {

    /**
     * {@link ObjectMapper} is thread-safe as long as it's not reconfigured,
     * therefore can make this mapper static final for best performance.
     */
    private static final ObjectMapper mapper = newMapper();

    /**
     * Deserialize a JSON object.
     */
    public T deserialize(String json, Class<T> type)
            throws JsonMappingException, JsonParseException, IOException {

        return mapper.readValue(json, type);
    }

    /**
     * Deserialize a JSON array.
     */
    public List<T> deserializeArray(String json, Class<T> type)
            throws JsonMappingException, JsonParseException, IOException {

        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        return mapper.readValue(json, listType);
    }

    private static ObjectMapper newMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
