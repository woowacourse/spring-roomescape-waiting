package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.exception.ReservationWaitingErrorCode;

class ReservationWaitingTest {

    @Test
    @DisplayName("ReservationWaiting 객체를 성공적으로 생성하고 필드를 검증한다.")
    void create_Success() {
        // given
        String name = "브라운";
        LocalDate date = LocalDate.of(2026, 5, 5);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");

        // when
        ReservationWaiting waiting = ReservationWaiting.of(name, date, time, theme);

        // then
        assertThat(waiting.getId()).isNull();
        assertThat(waiting.getName()).isEqualTo(name);
        assertThat(waiting.getDate()).isEqualTo(date);
        assertThat(waiting.getTime()).isEqualTo(time);
        assertThat(waiting.getTheme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("예약 대기 시간이 현재보다 미래인 경우 유효성 검증을 통과한다.")
    void validateExpiry_Future_Success() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = ReservationWaiting.of("브라운", futureDate, time, theme);

        // when & then
        assertThatCode(waiting::validateExpiry).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기 시간이 현재보다 과거인 경우 예외를 발생시킨다.")
    void validateExpiry_Past_ThrowsException() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = ReservationWaiting.of("브라운", pastDate, time, theme);

        // when & then
        assertThatThrownBy(waiting::validateExpiry)
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 대기 작성자 본인 검증 시 일치하면 예외가 발생하지 않는다.")
    void validateOwner_Match_Success() {
        // given
        roomescape.reservation.domain.ReservationSlot slot = new roomescape.reservation.domain.ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot);

        // when & then
        assertThatCode(() -> waiting.validateOwner("브라운")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기 작성자 본인 검증 시 일치하지 않으면 예외를 발생시킨다.")
    void validateOwner_Mismatch_ThrowsException() {
        // given
        roomescape.reservation.domain.ReservationSlot slot = new roomescape.reservation.domain.ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot);

        // when & then
        assertThatThrownBy(() -> waiting.validateOwner("네오"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationWaitingErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("식별자(ID)가 같으면 동등한 객체로 판단한다.")
    void equalsAndHashCode_SameId_Success() {
        // given
        roomescape.reservation.domain.ReservationSlot slot = new roomescape.reservation.domain.ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting1 = new ReservationWaiting(1L, "브라운", slot);
        ReservationWaiting waiting2 = new ReservationWaiting(1L, "네오", slot);

        // when & then
        assertThat(waiting1).isEqualTo(waiting2);
        assertThat(waiting1.hashCode()).isEqualTo(waiting2.hashCode());
    }
}
