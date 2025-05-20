package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.infrastructure.ReservationRepository;

class FakeReservationRepositoryTest {

    private final ReservationRepository reservationRepository = new FakeReservationRepository();

    @Test
    void 모든_예약을_조회한다() {
        // given
        Member member = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        Theme theme = Theme.createWithoutId("theme1", "desc", "thumb");
        Reservation reservation1 = Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 1), reservationTime, theme
        );
        Reservation reservation2 = Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 2), reservationTime, theme
        );
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        // when
        List<Reservation> allReservation = reservationRepository.findAll();
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(allReservation).hasSize(2);
        soft.assertThat(allReservation.getFirst().getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        soft.assertAll();
    }

    @Test
    void 예약을_생성한다() {
        // given
        Member member = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        Theme theme = Theme.createWithoutId("theme1", "desc", "thumb");
        Reservation reservation1 = Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 1), reservationTime, theme
        );
        // when
        reservationRepository.save(reservation1);
        // then
        List<Reservation> allReservation = reservationRepository.findAll();
        assertThat(allReservation).hasSize(1);
        assertThat(allReservation.getFirst().getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void 예약을_삭제한다() {
        // given
        Member member = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        Theme theme = Theme.createWithoutId("theme1", "desc", "thumb");
        Reservation reservation1 = Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 1), reservationTime, theme
        );
        reservationRepository.save(reservation1);
        // when
        reservationRepository.deleteById(1L);
        // then
        List<Reservation> allReservation = reservationRepository.findAll();
        assertThat(allReservation).hasSize(0);
    }

    @Test
    void 테마id로_예약을_조회한다() {
        // given
        Member member = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        Theme theme = new Theme(1L, "theme1", "desc", "thumb");
        Reservation reservation1 = Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 1), reservationTime, theme
        );
        Reservation reservation2 = Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 2), reservationTime, theme
        );
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        // when
        List<Reservation> reservations = reservationRepository.findByThemeId(theme.getId());
        // then
        assertThat(reservations).hasSize(2);
    }
}