package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

class ReservationTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

    @Test
    @DisplayName("예약자 이름이 비어있으면 도메인 예외가 발생한다.")
    void create_fail_when_name_is_blank() {
        assertDomainException(
                () -> Reservation.create(" ", LocalDate.of(2023, 8, 5), time, theme),
                INVALID_RESERVATION_GUEST_NAME
        );
    }

    @Test
    @DisplayName("예약 날짜가 null이면 도메인 예외가 발생한다.")
    void create_fail_when_date_is_null() {
        assertDomainException(
                () -> Reservation.create("브라운", null, time, theme),
                INVALID_RESERVATION_DATE
        );
    }

    @Test
    @DisplayName("예약 시간이 null이면 도메인 예외가 발생한다.")
    void create_fail_when_time_is_null() {
        assertDomainException(
                () -> Reservation.create("브라운", LocalDate.of(2023, 8, 5), null, theme),
                INVALID_RESERVATION_TIME
        );
    }

    @Test
    @DisplayName("예약 테마가 null이면 도메인 예외가 발생한다.")
    void create_fail_when_theme_is_null() {
        assertDomainException(
                () -> Reservation.create("브라운", LocalDate.of(2023, 8, 5), time, null),
                INVALID_THEME
        );
    }

    @Test
    @DisplayName("이미 id가 있는 예약에 id를 부여하면 도메인 예외가 발생한다.")
    void withId_fail_when_reservation_already_has_id() {
        Reservation reservation = Reservation.of(1L, "브라운", LocalDate.of(2023, 8, 5), time, theme);

        assertDomainException(
                () -> reservation.withId(2L),
                RESERVATION_ALREADY_HAS_ID
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2025-05-11T09:59:59,false",
            "2025-05-11T10:00:00,false",
            "2025-05-11T10:00:01,true",
    })
    @DisplayName("예약의 날짜 및 시간이 이미 지났는지 여부를 반환한다.")
    public void isPassed(LocalDateTime now, boolean expected) {
        // given
        // 2025-05-11T10:00:00
        LocalDate date = LocalDate.of(2025, 5, 11);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Reservation reservation = Reservation.of(1L, "브라운", date, time, theme);

        // when
        boolean result = reservation.isPassed(now);

        // then
        assertThat(result).isEqualTo(expected);
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
                1L, "브라운", LocalDate.of(2025, 5, 11), time, theme);

        // when
        boolean result = reservation.isSameGuest(targetName);

        // then
        assertThat(result).isEqualTo(expected);
    }
    private void assertDomainException(Runnable runnable, ErrorPolicy errorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getErrorPolicy()).isEqualTo(errorCode)
                )
                .hasMessage(errorCode.message());
    }
}
