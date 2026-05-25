package roomescape.global.exception.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Type;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import roomescape.global.exception.InvalidRequestFormatException;

public class BusinessExceptionMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public BusinessExceptionMappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        try {
            return super.readInternal(clazz, inputMessage);
        } catch (HttpMessageNotReadableException e) {
            throw new InvalidRequestFormatException();
        }
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        try {
            return super.read(type, contextClass, inputMessage);
        } catch (HttpMessageNotReadableException e) {
            throw new InvalidRequestFormatException();
        }
    }
}
