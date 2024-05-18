package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.domain.ReservationMapping;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("날짜와 테마 번호를 기준으로 예약의 시간 번호를 조회한다.")
    @Test
    void findByDateAndThemeId() {
        LocalDate date = LocalDate.parse("2024-12-12");

        List<ReservationMapping> result = reservationRepository.findByDateAndThemeId(date, 1);

        assertThat(result)
                .hasSize(2)
                .extracting(ReservationMapping::getTimeId)
                .containsExactly(1, 4);
    }
}
