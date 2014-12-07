package com.plexobject.fsm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.util.IOUtils;

/**
 * This class provides helper methods to serialize and deserialize transition
 * mapings in JSON format
 * 
 * @author shahzad bhatti
 *
 */
public class TransitionMappingsCodec {
    public static TransitionMappings decode(File file) throws IOException {
        final String mappingJson = IOUtils.toString(new FileInputStream(file));
        return decode(mappingJson);
    }

    public static TransitionMappings decode(String mappingJson) {
        Collection<TransitionMapping> list = new JsonObjectCodec().decode(
                mappingJson, new TypeReference<List<TransitionMapping>>() {
                });
        TransitionMappings transitionMappings = new TransitionMappings();
        for (TransitionMapping m : list) {
            transitionMappings.register(m);
        }
        return transitionMappings;
    }

    public static String encode(TransitionMappings mapping) {
        return new JsonObjectCodec().encode(mapping.getTransitions());
    }

}
