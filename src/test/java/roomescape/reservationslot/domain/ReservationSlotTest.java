package roomescape.reservationslot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.TestFixture.FUTURE_DATE;
import static roomescape.fixture.TestFixture.NOW_DATE;
import static roomescape.fixture.TestFixture.NOW_DATETIME;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationDuplicatedException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationslot.exception.InvalidReservationSlotException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationSlotTest {

    private final ReservationTime reservationTime = TestFixture.makeReservationTime(LocalTime.of(10, 0));
    private final Theme theme = TestFixture.makeTheme();
    private final ReservationSlot reservationSlot = new ReservationSlot(FUTURE_DATE, reservationTime, theme);
    private Member member;

    @BeforeEach
    void setUp() {
        member = TestFixture.makeMember();
        ReflectionTestUtils.setField(member, "id", 4L);
    }

    @Test
    void addReservation_whenValidRequest_returnReservations() {
        // given
        reservationSlot.addReservation(member, NOW_DATETIME);

        // when & then
        assertThat(reservationSlot.getReservations()).hasSize(1);
    }

    @Test
    void addReservation_whenPastDate_throwsException() {
        assertThatThrownBy(() -> reservationSlot.addReservation(member,
                LocalDateTime.of(FUTURE_DATE.plusDays(1), LocalTime.of(11, 0))))
                .isInstanceOf(InvalidReservationSlotException.class)
                .hasMessageContaining("예약 시간이 현재 시간보다 이전일 수 없습니다.");
    }

    @Test
    void addReservation_whenAlreadyReserved_throwsException() {
        reservationSlot.addReservation(member,
                LocalDateTime.of(NOW_DATE, LocalTime.of(11, 0)));

        assertThatThrownBy(() -> reservationSlot.addReservation(member,
                LocalDateTime.of(NOW_DATE, LocalTime.of(11, 0))))
                .isInstanceOf(ReservationDuplicatedException.class)
                .hasMessageContaining("해당 멤버는 이미 예약 중입니다.");
    }

    @Test
    void findConfirmedMember_whenReservationsExist_returnsConfirmedMember() {
        // given
        LocalDateTime reservationTime = LocalDateTime.of(NOW_DATE, LocalTime.of(10, 0));
        Member member2 = new Member("Free", "free@gmail.com", "password", MemberRole.REGULAR);
        ReflectionTestUtils.setField(member2, "id", 4L);
        reservationSlot.addReservation(member2, reservationTime);

        // when & then
        assertThat(reservationSlot.findConfirmedMember()).isEqualTo(member);
    }

    @Test
    void findRank_whenReservationsExist_returnsRank() {
        // given
        Member member1 = new Member("Free", "free@gmail.com", "password", MemberRole.REGULAR);
        ReflectionTestUtils.setField(member1, "id", 4L);
        LocalDateTime reservationTime = LocalDateTime.of(NOW_DATE, LocalTime.of(11, 0));
        Reservation reservation1 = reservationSlot.addReservation(member1, reservationTime);
        ReflectionTestUtils.setField(reservation1, "id", 1L);
        Member member2 = new Member("Free", "free@gmail.com", "password", MemberRole.REGULAR);
        ReflectionTestUtils.setField(member1, "id", 5L);
        Reservation reservation2 = reservationSlot.addReservation(member2, reservationTime);
        ReflectionTestUtils.setField(reservation2, "id", 2L);

        // when & then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(reservationSlot.findRank(reservation1)).isEqualTo(0);
            softAssertions.assertThat(reservationSlot.findRank(reservation2)).isEqualTo(1);
        });
    }

    @Test
    void findRank_whenReservationNotExist_throwsException() {
        // given
        Member member1 = new Member("Free", "free@gmail.com", "password", MemberRole.REGULAR);
        ReflectionTestUtils.setField(member1, "id", 4L);
        Reservation reservation = new Reservation(member1, reservationSlot);

        // when & then
        assertThatThrownBy(() -> reservationSlot.findRank(reservation))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("해당 예약을 찾을 수 없습니다.");
    }

    @Test
    void findConfirmedReservation_whenValidParameters_returnReservation() {
        // given
        Reservation reservation = reservationSlot.addReservation(member, NOW_DATETIME);

        // when & then
        assertThat(reservationSlot.findConfirmedReservation()).isEqualTo(reservation);
    }

    @Test
    void findConfirmedReservation_whenReservationsNotExist_throwsException() {
        // given

        // when & then
        assertThatThrownBy(reservationSlot::findConfirmedReservation)
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("예약이 존재하지 않습니다.");
    }
}
