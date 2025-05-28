package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private static Stream<Arguments> past_time_reservation_validate_test() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 5, 5), LocalTime.of(10, 0)),
                Arguments.of(LocalDate.of(2025, 4, 5), LocalTime.of(10, 0)),
                Arguments.of(LocalDate.of(2025, 5, 4), LocalTime.of(10, 0)),
                Arguments.of(LocalDate.of(2025, 5, 5), LocalTime.of(9, 0))
        );
    }

    @ParameterizedTest
    @DisplayName("같은 시간 확인 테스트")
    @CsvSource({"20:10,true", "20:20,false", "21:10,false", "19:09,false"})
    void isSameTime_Test(LocalTime localTime, boolean expected) {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithId(1L, LocalTime.of(20, 10));
        Theme theme = Theme.createWithId(1L, "a", "a", "a");
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        Reservation reservation = Reservation.createWithId(1L, member, LocalDate.of(2000, 11, 2), reservationTime1,
                theme, RESERVED, LocalDateTime.now());
        // when
        ReservationTime reservationTime2 = ReservationTime.createWithId(2L, localTime);
        // then
        assertThat(reservation.isSameTime(reservationTime2)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Id 할당 테스트")
    void assignGetId_Test() {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithId(1L, LocalTime.of(20, 10));
        Theme theme = Theme.createWithId(1L, "a", "a", "a");
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        Reservation withoutId = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime1, theme, RESERVED);
        // when
        Reservation reservation = withoutId.assignId(1L);
        // then
        assertAll(
                () -> assertThat(reservation.getId()).isEqualTo(1L),
                () -> assertThat(reservation.name()).isEqualTo("a")
        );
    }

    @ParameterizedTest
    @DisplayName("과거 시간 예약 검증 테스트")
    @MethodSource
    void past_time_reservation_validate_test(LocalDate date, LocalTime time) {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 5, 5, 10, 0);
        ReservationTime reservationTime = ReservationTime.createWithId(1L, time);
        Theme theme = Theme.createWithId(1L, "a", "a", "a");
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(now, member, date, reservationTime, theme, RESERVED))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
