package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFactory;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationFactory reservationFactory;

    @Test
    @DisplayName("예약 저장 성공")
    void 예약_저장_성공() {
        Reservation saved = reservationRepository.save(
                reservationFactory.create("현미밥", LocalDate.now().plusDays(1),
                        timeRepository.findById(1L).get(),
                        themeRepository.findById(1L).get()));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("예약 삭제 성공")
    void 예약_삭제_성공() {
        reservationRepository.deleteById(11L);

        assertThat(timeRepository.findAvailableByDateAndThemeId(LocalDate.of(2099, 12, 1), 1L)).hasSize(2);
    }
}