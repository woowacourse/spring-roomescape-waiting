package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {

    @Test
    @DisplayName("예약 하나를 생성한다")
    void 객체_생성_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        Theme theme = new Theme("라이의 공포 방", "라이라이차라차", "test-url");

        // when
        Reservation reservation = new Reservation("라이",
                time,
                theme,
                Status.RESERVED,
                LocalDateTime.now()).withId(1L);

        // then
        assertThat(reservation.getName()).isEqualTo("라이");
        assertThat(reservation.getTheme().getName()).isEqualTo("라이의 공포 방");
        assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
    }
}
