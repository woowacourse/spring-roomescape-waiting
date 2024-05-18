package roomescape.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class SpringConfig {
    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(LocalDate.class, new CustomLocalDateDeserializer()))
                .addModule(new SimpleModule().addDeserializer(LocalTime.class, new CustomLocalTimeDeserializer()))
                .addModule(new SimpleModule().addSerializer(LocalDate.class, new CustomLocalDateSerializer()))
                .addModule(new SimpleModule().addSerializer(LocalTime.class, new CustomLocalTimeSerializer()))
                .build();
    }
}
