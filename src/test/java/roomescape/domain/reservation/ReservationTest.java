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

        assertThatCode(() -> new Reservation(member, theme, date, time, createdAt, BookStatus.BOOKED))
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
                TEST_THEME.create(),
                LocalDate.now(),
                TEN_AM.create(),
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
                TEST_THEME.create(),
                LocalDate.now(),
                TEN_AM.create(),
                LocalDateTime.now().minusDays(1),
                BookStatus.BOOKED
        );

        Member other = MEMBER_ARU.createWithId(2L);
        boolean hasNoPermission = reservation.isNotModifiableBy(other);
        assertThat(hasNoPermission).isTrue();
    }

    @Test
    @DisplayName("확정되지 않은 예약을 취소할 수 없다.")
    void cancelOnNotBookedReservation() {
        Reservation reservation = new Reservation(
                MEMBER_ARU.create(),
                TEST_THEME.create(),
                LocalDate.now(),
                TEN_AM.create(),
                LocalDateTime.now().minusDays(1),
                BookStatus.WAITING
        );

        assertThatCode(reservation::cancelBooking)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("확정된 예약이 아닙니다.");
    }


    @Test
    @DisplayName("예약 대기중이 아닌 예약을 대기취소할 수 없다.")
    void cancelOnNotWaitingReservation() {
        Reservation reservation = new Reservation(
                MEMBER_ARU.create(),
                TEST_THEME.create(),
                LocalDate.now(),
                TEN_AM.create(),
                LocalDateTime.now().minusDays(1),
                BookStatus.BOOKED
        );

        assertThatCode(reservation::cancelWaiting)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 중인 예약이 아닙니다.");
    }

    @Test
    @DisplayName("대기 중이 아닌 예약을 확정할 수 없다.")
    void bookOnNotWaitingReservation() {
        Reservation reservation = new Reservation(
                MEMBER_ARU.create(),
                TEST_THEME.create(),
                LocalDate.now(),
                TEN_AM.create(),
                LocalDateTime.now().minusDays(1),
                BookStatus.BOOKED
        );

        assertThatCode(reservation::book)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 중인 예약이 아닙니다.");
    }
}
