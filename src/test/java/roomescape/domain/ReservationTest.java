package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.exception.ExceptionType.EMPTY_DATE;
import static roomescape.exception.ExceptionType.EMPTY_MEMBER;
import static roomescape.exception.ExceptionType.EMPTY_THEME;
import static roomescape.exception.ExceptionType.EMPTY_TIME;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;

class ReservationTest {
    @Test
    @DisplayName("예약 멤버가 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyMember() {
        assertThatThrownBy(() ->
                Reservation.builder()
                        .member(null)
                        .date(LocalDate.now())
                        .time(DEFAULT_TIME)
                        .theme(DEFAULT_THEME)
                        .build())
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_MEMBER.getMessage());
    }

    @Test
    @DisplayName("예약 날짜가 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyDate() {
        assertThatThrownBy(() ->
                Reservation.builder()
                        .member(DEFAULT_MEMBER)
                        .date(null)
                        .time(DEFAULT_TIME)
                        .theme(DEFAULT_THEME)
                        .build())
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 시간이 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyTime() {
        assertThatThrownBy(() ->
                Reservation.builder()
                        .member(DEFAULT_MEMBER)
                        .date(LocalDate.now())
                        .time(null)
                        .theme(DEFAULT_THEME)
                        .build())
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 테마가 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyTheme() {
        assertThatThrownBy(() ->
                Reservation.builder()
                        .member(DEFAULT_MEMBER)
                        .date(LocalDate.now())
                        .time(DEFAULT_TIME)
                        .theme(null)
                        .build())
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_THEME.getMessage());
    }
}
