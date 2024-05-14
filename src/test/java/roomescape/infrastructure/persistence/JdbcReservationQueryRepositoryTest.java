package roomescape.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationQueryRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
class JdbcReservationQueryRepositoryTest extends JdbcReservationTest {
    private final ReservationQueryRepository reservationQueryRepository;

    @Autowired
    public JdbcReservationQueryRepositoryTest(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.reservationQueryRepository = new JdbcReservationQueryRepository(jdbcTemplate);
    }

    @DisplayName("id로 예약을 조회시 존재하지 않으면 빈 객체를 반환한다.")
    @Test
    void shouldEmptyReservationWhenReservationIdNotExist() {
        Optional<Reservation> reservation = reservationQueryRepository.findById(99L);
        assertThat(reservation).isEmpty();
    }

    @DisplayName("날짜, 시간으로 저장된 예약이 있는지 확인한다.")
    @Test
    void shouldReturnIsExistReservationWhenReservationsNameAndDateAndTimeIsSame() {
        Reservation reservation = createReservation();
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();

        boolean isExist = reservationQueryRepository.existBy(reservation.getDate(), time.getId(), theme.getId());
        assertThat(isExist).isTrue();
    }
}
