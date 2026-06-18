package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;

@SpringBootTest
class ReservationAutoPromotionTransactionTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @MockitoSpyBean
    private JpaReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 예약_취소_후_대기_승격_중_실패하면_전체_변경이_롤백된다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Reservation reservation = reservationRepository.save(new Reservation(null, "브라운", date, time, theme));
        ReservationWaiting waiting = reservationWaitingRepository.save(
                new ReservationWaiting(null, "구구", date, time, theme));

        doThrow(new RuntimeException("대기 삭제 실패"))
                .when(reservationWaitingRepository)
                .delete(any(ReservationWaiting.class));

        // when & then
        assertThatThrownBy(() -> reservationService.delete(reservation.getId(), "브라운"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("대기 삭제 실패");

        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(reservationRepository.findByName("구구")).isEmpty();
        assertThat(reservationWaitingRepository.findById(waiting.getId())).isPresent();
    }
}
