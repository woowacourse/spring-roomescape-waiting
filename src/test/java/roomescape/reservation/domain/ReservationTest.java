package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.exception.ForbiddenRequestException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {

    @DisplayName("예약 하나를 생성한다")
    @Test
    void 객체_생성_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        Theme theme = new Theme("라이의 공포 방", "라이라이차라차", "test-url");

        // when
        Reservation reservation = new Reservation("라이",
                time,
                theme,
                Status.RESERVED,
                null, null,
                LocalDateTime.now()).withId(1L);

        // then
        assertThat(reservation.getName()).isEqualTo("라이");
        assertThat(reservation.getTheme().getName()).isEqualTo("라이의 공포 방");
        assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
    }

    @DisplayName("이름이 같지 않다면 예외를 발생한다")
    @Test
    void validateOwnedBy_이름이_같지_않으면_예외_발생_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        Theme theme = new Theme("라이의 공포 방", "라이라이차라차", "test-url");
        Reservation reservation = new Reservation("라이",
                time,
                theme,
                Status.RESERVED,
                null, null,
                LocalDateTime.now()).withId(1L);

        // when & then
        assertThatThrownBy(() -> reservation.validateOwnedBy("어셔"))
                .isInstanceOf(ForbiddenRequestException.class);
    }

    @DisplayName("예약 상태가 RESERVED이면 true를 반환한다")
    @Test
    void isReserved_true_반환_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        Theme theme = new Theme("라이의 공포 방", "라이라이차라차", "test-url");
        Reservation reservation = new Reservation("라이",
                time,
                theme,
                Status.RESERVED,
                null, null,
                LocalDateTime.now()).withId(1L);

        // when
        boolean result = reservation.isReserved();

        // then
        assertThat(result).isTrue();
    }
}
