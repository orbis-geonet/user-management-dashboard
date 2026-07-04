package to.orbis.dashboard.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;
    public <T> T readFromString(final String context, final TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(context, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T cleanContextAndReadFromString(final String context, final TypeReference<T> typeReference) {
        String newContext = context.replace("\\\"", "\"")
                .replace("\"{", "{")
                .replace("}\"", "}")
                .replace("\\n\"{", "{")
                .replace("}\\n\"", "}")
                .replace("\\\\\"Orbis Rede Geo-Social\\\\\"", "");
        return readFromString(newContext, typeReference);
    }
}
