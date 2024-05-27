package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import roomescape.DBTest;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.domain.Status;
import roomescape.domain.Theme;

class ReservationRepositoryTest extends DBTest {

    @DisplayName("테마 ID로 예약을 조회한다.")
    @Test
    void findByThemeId() {
        // given
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());
        Member member = memberRepository.save(TestFixture.getMember1());
        reservationRepository.save(new Reservation(member, LocalDate.now(), time, theme, Status.CONFIRMED));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), time, theme, Status.CONFIRMED));

        // when
        List<Reservation> reservations = reservationRepository.findByThemeId(theme.getId());

        // then
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("멤버의 모든 예약 및 대기를 조회한다.")
    @Test
    void findMemberReservationAndWaiting() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());
        reservationRepository.save(new Reservation(member, LocalDate.now(), time, theme, Status.WAITING));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), time, theme, Status.CONFIRMED));

        // when
        List<ReservationWithWaitingOrder> myReservations = reservationRepository.findMyReservations(member.getId());

        // then
        assertThat(myReservations).hasSize(2);
        assertThat(myReservations).extracting("reservation.status").contains(Status.WAITING, Status.CONFIRMED);
        assertThat(myReservations).extracting("reservation.member.id").containsOnly(member.getId());
    }

    @DisplayName("예약 상태를 이용하여 해당되는 모든 예약을 조회한다.")
    @ParameterizedTest
    @EnumSource(Status.class)
    void findAllConfirmed(Status status) {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());
        reservationRepository.save(new Reservation(member, LocalDate.now(), time, theme, Status.WAITING));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), time, theme, Status.CONFIRMED));

        // when
        List<Reservation> reservations = reservationRepository.findAllByStatus(status);

        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations).extracting("status").containsOnly(status);
    }
}
