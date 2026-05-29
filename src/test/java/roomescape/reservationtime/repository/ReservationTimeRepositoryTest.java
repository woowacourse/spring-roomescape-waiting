package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ReservationTimeFactory reservationTimeFactory;

    @Test
    @DisplayName("시간 저장 성공")
    void 시간_저장_성공() {
        ReservationTime saved = timeRepository.save(reservationTimeFactory.create(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("전체 시간 조회")
    void 전체_시간_조회() {
        List<ReservationTime> times = timeRepository.findAll();
        assertThat(times).hasSize(3);
    }

    @Test
    @DisplayName("id로 시간 조회 성공")
    void id로_시간_조회_성공() {
        assertThat(timeRepository.findById(1L)).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 id 조회 시 빈 Optional 반환")
    void 존재하지_않는_id_조회() {
        assertThat(timeRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("시간 삭제 성공")
    void 시간_삭제_성공() {
        ReservationTime saved = timeRepository.save(reservationTimeFactory.create(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        timeRepository.deleteById(saved.getId());

        assertThat(timeRepository.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("예약 가능 시간 조회")
    void 예약_가능_시간_조회() {
        List<ReservationTime> available = timeRepository.findAvailableByDateAndThemeId(LocalDate.now().minusDays(1), 1L);
        assertThat(available).hasSize(2);
    }
}
