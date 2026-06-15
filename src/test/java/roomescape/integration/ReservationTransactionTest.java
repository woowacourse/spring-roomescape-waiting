package roomescape.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.ReservationService;

@SpringBootTest
public class ReservationTransactionTest {
    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM waiting");
        jdbcTemplate.execute("DELETE FROM reservation");
        jdbcTemplate.execute("DELETE FROM reservation_time");
        jdbcTemplate.execute("DELETE FROM theme");
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("DELETE FROM waiting");
        jdbcTemplate.execute("DELETE FROM reservation");
        jdbcTemplate.execute("DELETE FROM reservation_time");
        jdbcTemplate.execute("DELETE FROM theme");
    }

    @MockitoSpyBean
    ReservationRepository reservationRepository;

    @MockitoSpyBean
    WaitingRepository waitingRepository;

    @Test
    void 대기_삭제_실패_시_예약_삭제도_롤백된다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(
                new ReservationTime(null, LocalTime.of(10, 0)));

        Theme theme = themeRepository.save(new Theme(null, "공포방", "무서운방입니다.", "image-url"));

        Reservation reservation = reservationRepository.save(new Reservation(
                null,
                "브라운",
                LocalDate.of(2026, 5, 10),
                reservationTime,
                theme
        ));

        Waiting waiting = waitingRepository.save(new Waiting(
                null,
                "어셔",
                LocalDate.of(2026, 5, 10),
                reservationTime,
                theme
        ));

        doThrow(new RuntimeException("대기 삭제 실패"))
                .when(waitingRepository).delete(waiting);

        //when
        assertThatThrownBy(
                () -> reservationService.deleteUserReservation(reservation.getId(), reservation.getName()))
                .isInstanceOf(RuntimeException.class);

        //then
        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(waitingRepository.findById(waiting.getId())).isPresent();
    }

    @Test
    void 예약_생성_실패_시_예약_삭제와_대기_삭제_모두_롤백된다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));

        Theme theme = themeRepository.save(new Theme(1L, "공포방", "무서운방입니다.", "image-url"));

        Reservation reservation = reservationRepository.save(new Reservation(
                null,
                "브라운",
                LocalDate.of(2026, 5, 10),
                reservationTime,
                theme
        ));

        Waiting waiting = waitingRepository.save(new Waiting(
                null,
                "어셔",
                LocalDate.of(2026, 5, 10),
                reservationTime,
                theme
        ));

        doThrow(new RuntimeException("예약 생성 실패"))
                .when(reservationRepository).save(any(Reservation.class));

        //when
        assertThatThrownBy(
                () -> reservationService.deleteUserReservation(reservation.getId(), reservation.getName()))
                .isInstanceOf(RuntimeException.class);

        //then
        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(waitingRepository.findById(waiting.getId())).isPresent();
    }
}
