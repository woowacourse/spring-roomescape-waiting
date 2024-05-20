package roomescape.repository;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location=classpath:/application.properties"})
class TimeSlotRepositoryTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("해당 startAt과 동일한 시간대가 이미 존재하는지 확인한다.")
    @Test
    void existsByStartAt() {
        //given
        LocalTime alreadyExistingTime = LocalTime.of(10, 0);
        LocalTime notExistingTime = LocalTime.of(23, 59);

        // when
        boolean isTimeSlotExists_true = timeSlotRepository.existsByStartAt(alreadyExistingTime);
        boolean isTimeSlotExists_false = timeSlotRepository.existsByStartAt(notExistingTime);

        assertAll(
                ()-> assertThat(isTimeSlotExists_true).isTrue(),
                ()-> assertThat(isTimeSlotExists_false).isFalse()
        );
    }
}
