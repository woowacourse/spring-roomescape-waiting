package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.ADMIN_PK;
import static roomescape.fixture.MemberFixture.MEMBER_ARU;
import static roomescape.fixture.MemberFixture.MEMBER_PK;
import static roomescape.fixture.ThemeFixture.TEST_THEME;
import static roomescape.fixture.TimeFixture.TEN_AM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;

class ReservationTest {

    @Test
    @DisplayName("예약 시간이 예약 생성 시간보다 이전이면 예외가 발생한다.")
    void invalidReserveTimeTest() {
        LocalDateTime reservationTime = LocalDateTime.of(2024, 1, 1, 12, 0, 59);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 1, 0);
        LocalDate date = reservationTime.toLocalDate();
        ReservationTime time = new ReservationTime(reservationTime.toLocalTime());
        Theme theme = new Theme("테마명", "설명", "url");
        Member member = MEMBER_ARU.create();

        assertThatCode(() -> new Reservation(member, date, time, theme, createdAt, BookStatus.BOOKED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 시간보다 과거로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자나 예약을 만든 사람은 예약을 수정/삭제할 수 있다.")
    void adminOrCreatorModifiableTest() {
        Member aru = MEMBER_ARU.createWithId(1L);
        Member admin = ADMIN_PK.createWithId(2L);
        Reservation reservation = new Reservation(
                aru,
                LocalDate.now(),
                TEN_AM.create(),
                TEST_THEME.create(),
                LocalDateTime.now().minusDays(1),
                BookStatus.BOOKED
        );

        assertAll(
                () -> assertThat(reservation.isNotModifiableBy(aru)).isFalse(),
                () -> assertThat(reservation.isNotModifiableBy(admin)).isFalse()
        );
    }

    @Test
    @DisplayName("관리자가 아닌 다른 사람은 예약을 수정/삭제할 수 없다.")
    void foreignerModifiableTest() {
        Reservation reservation = new Reservation(
                MEMBER_PK.createWithId(1L),
                LocalDate.now(),
                TEN_AM.create(),
                TEST_THEME.create(),
                LocalDateTime.now().minusDays(1),
                BookStatus.BOOKED
        );

        Member other = MEMBER_ARU.createWithId(2L);
        boolean hasNoPermission = reservation.isNotModifiableBy(other);
        assertThat(hasNoPermission).isTrue();
    }
}
