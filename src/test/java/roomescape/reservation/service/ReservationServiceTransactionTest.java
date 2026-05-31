package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.global.exception.InfrastructureException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationServiceTransactionTest {

    private static final String NAME = "브라운";
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate NEXT_FUTURE_DATE = LocalDate.now().plusDays(2);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("예약 변경 중 새 예약 저장에 실패하면 기존 예약 취소도 롤백된다.")
    void updateDateTime_rollback_whenNewReservationSaveFails() {
        // given
        ReservationTime time = saveReservationTime(10);
        ReservationTime newTime = saveReservationTime(11);
        Theme theme = saveTheme();
        Reservation reservation = reservationService.create(NAME, FUTURE_DATE, time.getId(), theme.getId());

        doThrow(new InfrastructureException("예약 생성에 실패했습니다."))
                .when(reservationRepository)
                .save(any(Reservation.class));

        // when, then
        assertThatThrownBy(() -> reservationService.updateDateTime(
                reservation.getId(),
                NAME,
                NEXT_FUTURE_DATE,
                newTime.getId()
        ))
                .isInstanceOf(InfrastructureException.class);

        assertThat(countReservationById(reservation.getId())).isEqualTo(1);
        assertThat(countHistoryByReservationId(reservation.getId())).isZero();
        assertThat(countAllReservations()).isEqualTo(1);
    }

    private ReservationTime saveReservationTime(int hour) {
        return reservationTimeRepository.save(new ReservationTime(LocalTime.of(hour, 0)));
    }

    private Theme saveTheme() {
        return themeRepository.save(new Theme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        ));
    }

    private Integer countReservationById(Long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class,
                reservationId
        );
    }

    private Integer countHistoryByReservationId(Long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_history WHERE reservation_id = ?",
                Integer.class,
                reservationId
        );
    }

    private Integer countAllReservations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
    }
}
