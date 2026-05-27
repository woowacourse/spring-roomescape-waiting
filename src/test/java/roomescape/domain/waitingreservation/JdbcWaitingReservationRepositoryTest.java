package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
@Import(JdbcWaitingReservationRepository.class)
class JdbcWaitingReservationRepositoryTest {

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Test
    void 같은_이름_날짜_테마_시간으로_예약_대기를_생성할_수_없다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 5, 10));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 9, 10, 0);
        WaitingReservation waitingReservation = WaitingReservation.createWithoutId("이산", date, time, theme, createdAt);
        waitingReservationRepository.save(waitingReservation);

        assertThatThrownBy(() -> waitingReservationRepository.save(waitingReservation))
                .isInstanceOf(DuplicateKeyException.class);
    }

}
