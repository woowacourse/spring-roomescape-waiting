package roomescape.domain;

import static roomescape.exception.ExceptionType.EMPTY_DATE;
import static roomescape.exception.ExceptionType.EMPTY_MEMBER;
import static roomescape.exception.ExceptionType.EMPTY_THEME;
import static roomescape.exception.ExceptionType.EMPTY_TIME;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;

class ReservationTest {
    @Test
    @DisplayName("예약 날짜가 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyDate() {
        Assertions.assertThatThrownBy(() -> new Reservation(DEFAULT_MEMBER, null, DEFAULT_TIME, DEFAULT_THEME))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 시간이 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyTime() {
        Assertions.assertThatThrownBy(() -> new Reservation(DEFAULT_MEMBER, LocalDate.now(), null, DEFAULT_THEME))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 테마가 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyTheme() {
        Assertions.assertThatThrownBy(() -> new Reservation(DEFAULT_MEMBER, LocalDate.now(), DEFAULT_TIME, null))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_THEME.getMessage());
    }

    @Test
    @DisplayName("예약 테마가 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyMember() {
        Assertions.assertThatThrownBy(() -> new Reservation(null, LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_MEMBER.getMessage());
    }

    @Test
    @DisplayName("날짜를 기준으로 비교를 잘 하는지 확인.")
    void compareTo() {
        Member robin = new Member(1L, "name", "email@email.com", new Sha256Encryptor().encrypt("1234"));
        Member polla = new Member(2L, "name", "email@email.com", new Sha256Encryptor().encrypt("1234"));

        Reservation first = new Reservation(1L, robin, LocalDate.of(1999, 12, 1), new ReservationTime(
                LocalTime.of(16, 30)), DEFAULT_THEME);
        Reservation second = new Reservation(2L, polla, LocalDate.of(1998, 1, 8), new ReservationTime(
                LocalTime.of(16, 30)), DEFAULT_THEME);

        int compareTo = first.compareTo(second);
        Assertions.assertThat(compareTo)
                .isGreaterThan(0);
    }
}
