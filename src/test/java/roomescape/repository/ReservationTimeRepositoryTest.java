package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        reservationTimeRepository.deleteAllInBatch();
    }

    @DisplayName("해당 시간이 존재하는지 확인한다.")
    @Test
    void existByStartAt() {
        // given
        reservationTimeRepository.save(RESERVATION_TIME_10AM);

        // when
        boolean exist = reservationTimeRepository.existsByStartAt(RESERVATION_TIME_10AM.getStartAt());

        // then
        assertThat(exist).isTrue();
    }
}
