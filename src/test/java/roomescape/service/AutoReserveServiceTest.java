package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.Waiting;
import roomescape.dto.reservation.AutoReservedFilter;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.ADMIN;
import static roomescape.TestFixture.RESERVATION_TIME_SEVEN;
import static roomescape.TestFixture.THEME_ANIME;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AutoReserveServiceTest {

    @Autowired
    private AutoReserveService autoReserveService;

    @Autowired
    private WaitingRepository waitingRepository;

    private Long waitingId;

    @BeforeEach
    void setUp() {
        Waiting saved = waitingRepository.save(new Waiting(ADMIN(1L), LocalDate.of(2030, 5, 22), RESERVATION_TIME_SEVEN(1L), THEME_ANIME(2L)));
        waitingId = saved.getId();
    }

    @Test
    @DisplayName("대기 1순위가 예약된다.")
    void reserveWaiting() {
        // given
        AutoReservedFilter filter = new AutoReservedFilter(LocalDate.of(2030, 5, 22), 2L, 1L);

        // when
        autoReserveService.reserveWaiting(filter);

        // then
        assertThat(waitingRepository.findById(waitingId)).isEmpty();
    }

    @Test
    @DisplayName("예약이 존재하면 예약으로 변경하지 않는다.")
    void notCreateWithReservation() {
        // given
        Waiting waiting = new Waiting(ADMIN(1L), LocalDate.of(2024, 5, 22), RESERVATION_TIME_SEVEN(2L), THEME_ANIME(10L));
        Waiting saved = waitingRepository.save(waiting);
        AutoReservedFilter filter = new AutoReservedFilter(saved.getDate(), saved.getThemeId(), saved.getTimeId());

        // when
        autoReserveService.reserveWaiting(filter);

        // then
        assertThat(waitingRepository.findTopByDateAndTime_IdAndTheme_IdOrderById(filter.date(), filter.timeId(), filter.themeId()).get()).isEqualTo(waiting);
    }
}
