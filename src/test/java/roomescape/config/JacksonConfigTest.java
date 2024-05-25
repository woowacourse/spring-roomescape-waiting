package roomescape.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class JacksonConfigTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        JacksonConfig jacksonConfig = new JacksonConfig();
        objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(jacksonConfig.javaTimeModule())
                .build();
    }

    @DisplayName("날짜는 yyyy-MM-dd 형식으로 직렬화된다.")
    @Test
    void dateSerialize() throws JsonProcessingException {
        // given
        LocalDate date = LocalDate.of(2021, 7, 1);

        // when
        String json = objectMapper.writeValueAsString(date);
        LocalDate actual = objectMapper.readValue(json, LocalDate.class);

        // then
        assertThat(actual.toString()).isEqualTo("2021-07-01");
    }

    @DisplayName("시간은 HH:mm 형식으로 직렬화된다.")
    @Test
    void timeSerialize() throws JsonProcessingException {
        // given
        LocalTime time = LocalTime.of(12, 30, 0);

        // when
        String json = objectMapper.writeValueAsString(time);
        LocalTime actual = objectMapper.readValue(json, LocalTime.class);

        // then
        assertThat(actual.toString()).isEqualTo("12:30");
    }
}
