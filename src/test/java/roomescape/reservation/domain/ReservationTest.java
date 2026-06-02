package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_DATE;
import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_GUEST_NAME;
import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_ALREADY_HAS_ID;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.INVALID_RESERVATION_TIME;
import static roomescape.theme.exception.ThemeErrorCode.INVALID_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private final ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
    private final Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

    @Test
    @DisplayName("예약자 이름이 비어있으면 도메인 예외가 발생한다.")
    void create_fail_when_name_is_blank() {
        assertDomainException(
                () -> Reservation.create(" ", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING),
                INVALID_RESERVATION_GUEST_NAME
        );
    }

    @Test
    @DisplayName("예약 날짜가 null이면 도메인 예외가 발생한다.")
    void create_fail_when_date_is_null() {
        assertDomainException(
                () -> Reservation.create("브라운", null, time, theme, Status.WAITING),
                INVALID_RESERVATION_DATE
        );
    }

    @Test
    @DisplayName("예약 시간이 null이면 도메인 예외가 발생한다.")
    void create_fail_when_time_is_null() {
        assertDomainException(
                () -> Reservation.create("브라운", LocalDate.of(2023, 8, 5), null, theme, Status.WAITING),
                INVALID_RESERVATION_TIME
        );
    }

    @Test
    @DisplayName("예약 테마가 null이면 도메인 예외가 발생한다.")
    void create_fail_when_theme_is_null() {
        assertDomainException(
                () -> Reservation.create("브라운", LocalDate.of(2023, 8, 5), time, null, Status.WAITING),
                INVALID_THEME
        );
    }

    @Test
    @DisplayName("이미 id가 있는 예약에 id를 부여하면 도메인 예외가 발생한다.")
    void withId_fail_when_reservation_already_has_id() {
        Reservation reservation = Reservation.of(1L, "브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CONFIRMED);

        assertDomainException(
                () -> reservation.withId(2L),
                RESERVATION_ALREADY_HAS_ID
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            "브라운,true",
            "포비,false"
    })
    @DisplayName("같은 사람의 예약인지 확인한다.")
    public void isSameGuest(String targetName, boolean expected) {
        // given
        Reservation reservation = Reservation.of(
                1L, "브라운", LocalDate.of(2025, 5, 11), time, theme, Status.CONFIRMED);

        // when
        boolean result = reservation.isSameGuest(targetName);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("날짜, 시간, 테마가 같은 예약은 같은 슬롯으로 판단한다.")
    void hasSameSlotAs_true_when_reservation_slot_is_same() {
        ReservationTime sameTime = ReservationTime.of(time.getId(), LocalTime.of(11, 0));
        Theme sameTheme = Theme.of(theme.getId(), "다른 이름", "다른 설명", "https://example.com/other.png");
        LocalDate date = LocalDate.of(2025, 5, 11);
        Reservation reservation = Reservation.of(1L, "브라운", date, time, theme, Status.CONFIRMED);
        Reservation other = Reservation.of(2L, "포비", date, sameTime, sameTheme, Status.WAITING);

        assertThat(reservation.hasSameSlotAs(other)).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간, 테마 중 하나라도 다르면 다른 슬롯으로 판단한다.")
    void hasSameSlotAs_false_when_reservation_slot_is_different() {
        ReservationTime otherTime = ReservationTime.of(2L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 5, 11);
        Reservation reservation = Reservation.of(1L, "브라운", date, time, theme, Status.CONFIRMED);
        Reservation other = Reservation.of(2L, "포비", date, otherTime, theme, Status.WAITING);

        assertThat(reservation.hasSameSlotAs(other)).isFalse();
    }

    private void assertDomainException(Runnable runnable, ErrorPolicy errorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getErrorPolicy()).isEqualTo(errorCode)
                )
                .hasMessage(errorCode.message());
    }
}
