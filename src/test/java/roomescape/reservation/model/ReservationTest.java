package roomescape.reservation.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.reservationtime.model.ReservationTime;

class ReservationTest {

    @Nested
    class createReservation {

        @Test
        @DisplayName("예약 객체 생성 실패: 예약자가 없음")
        void createReservation_WhenNameIsBlank() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약자는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 실패: 예약자 명이 공백")
        void createReservation_WhenNameOverLength() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약자는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 실패: 예약 날짜가 공백")
        void createReservation_WhenReservationDateIsNull() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithDateTimeTheme(null, ReservationTimeFixture.getOne(),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약 날짜는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 실패: 예약 시간이 공백")
        void createReservation_WhenReservationTimeIsNull() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithTimeTheme(null, ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약 시간은 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 실패: 예약 테마가 공백")
        void createReservation_WhenReservationThemeIsNull() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithTheme(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약 테마는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 실패: 예약하려는 날짜가 과거")
        void createReservation_WhenReservationDateInPast() {
            assertThatThrownBy(
                    () -> Reservation.create(
                            MemberFixture.getOne(),
                            LocalDate.parse("2024-01-01"),
                            ReservationTimeFixture.getOne(),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("2024-01-01는 지나간 시간임으로 예약 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
        }

        @Test
        @DisplayName("예약 객체 생성 실패: 예약하려는 시간이 과거")
        void createReservation_WhenReservationTimeInPast() {
            LocalDateTime now = LocalDateTime.now();
            assertThatThrownBy(
                    () -> Reservation.create(
                            MemberFixture.getOne(),
                            now.toLocalDate(),
                            new ReservationTime(now.toLocalTime().minusHours(1)),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(now.minusHours(1) + "는 현재보다 동일하거나 지나간 시간임으로 예약 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
        }
    }

    @Test
    @DisplayName("주어진 시간과 예약의 시간이 동일: 참")
    void isSameTime() {
        ReservationTime sameTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        Reservation reservation = new Reservation(MemberFixture.getOne(), LocalDate.parse("2099-10-11"),
                sameTime, ThemeFixture.getOne());
        assertTrue(reservation.isSameTime(sameTime));
    }

    @Test
    @DisplayName("주어진 시간과 예약의 시간이 동일하지 않음: 거짓")
    void isSameTime_WhenNotSame() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        ReservationTime otherTime = new ReservationTime(2L, LocalTime.parse("20:00"));
        Reservation reservation = new Reservation(MemberFixture.getOne(), LocalDate.parse("2099-10-11"),
                reservationTime, ThemeFixture.getOne());
        assertFalse(reservation.isSameTime(otherTime));
    }

    @Test
    @DisplayName("주어진 회원이 예약의 예약자가 동일: 참")
    void isOwnedBy() {
        Member member = MemberFixture.getOneWithId(1L);
        Reservation reservation = new Reservation(member, LocalDate.parse("2099-10-11"),
                ReservationTimeFixture.getOne(), ThemeFixture.getOne());
        assertTrue(reservation.isOwnedBy(member));
    }

    @Test
    @DisplayName("주어진 회원이 예약의 예약자가 동일하지 않음: 참")
    void isOwnedBy_WhenNotSame() {
        Member member = MemberFixture.getOneWithId(1L);
        Member otherMember = MemberFixture.getOneWithId(2L);
        Reservation reservation = new Reservation(member, LocalDate.parse("2099-10-11"),
                ReservationTimeFixture.getOne(), ThemeFixture.getOne());
        assertFalse(reservation.isOwnedBy(otherMember));
    }
}
