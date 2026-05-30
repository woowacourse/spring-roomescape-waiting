package roomescape.reservationWaiting.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservationWaiting.exception.ReservationWaitingErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationWaitingTest {

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void of() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        assertThatCode(() -> ReservationWaiting.of("브라운", LocalDate.now(), time, theme))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기 시간이 현재 시간보다 이전이면 예외가 발생한다.")
    void validateExpiry_throwsException_whenPast() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        ReservationWaiting waiting = ReservationWaiting.of("브라운", LocalDate.now().minusDays(1), time, theme);

        // when & then
        assertThatThrownBy(waiting::validateExpiry)
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 대기 시간이 현재 시간 이후면 예외가 발생하지 않는다.")
    void validateExpiry_doesNotThrow_whenFuture() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        ReservationWaiting waiting = ReservationWaiting.of("브라운", LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatCode(waiting::validateExpiry)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기의 소유자가 맞는지 검증한다.")
    void validateOwner_doesNotThrow_whenOwnerMatches() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        ReservationWaiting waiting = ReservationWaiting.of("브라운", LocalDate.now(), time, theme);

        // when & then
        assertThatCode(() -> waiting.validateOwner("브라운"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기의 소유자가 아니면 예외가 발생한다.")
    void validateOwner_throwsException_whenOwnerMismatches() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        ReservationWaiting waiting = ReservationWaiting.of("브라운", LocalDate.now(), time, theme);

        // when & then
        assertThatThrownBy(() -> waiting.validateOwner("포비"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationWaitingErrorCode.AUTHORIZATION_FAIL.getMessage());
    }
}
