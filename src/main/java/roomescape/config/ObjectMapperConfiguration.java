package roomescape.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.config.deserializer.LocalDateDeserializerWithValidation;
import roomescape.config.deserializer.LocalTimeDeserializerWithValidation;
import roomescape.config.deserializer.LongDeserializerWithValidation;

@Configuration
public class ObjectMapperConfiguration {
    @Bean
    public ObjectMapper getObjectMapper() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializerWithValidation());
        simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializerWithValidation());
        simpleModule.addDeserializer(Long.class, new LongDeserializerWithValidation());
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        simpleModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm")));
        mapper.registerModule(simpleModule);
        return mapper;
    }
}
