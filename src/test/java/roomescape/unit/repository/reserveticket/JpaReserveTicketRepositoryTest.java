package roomescape.unit.repository.reserveticket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.domain.theme.Theme;
import roomescape.repository.member.JpaMemberRepository;
import roomescape.repository.reservation.JpaReservationRepository;
import roomescape.repository.reservationtime.JpaReservationTimeRepository;
import roomescape.repository.reserveticket.JpaReserveTicketRepository;
import roomescape.repository.theme.JpaThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaReserveTicketRepositoryTest {

    @Autowired
    private JpaReserveTicketRepository jpaReserveTicketRepository;
    @Autowired
    private JpaMemberRepository jpaMemberRepository;
    @Autowired
    private JpaReservationRepository jpaReservationRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Test
    void 예약한_멤버의_id로_예약한_정보_데이터를_찾을_수_있다() {
        // Given
        Member member = jpaMemberRepository.save(new Member(null, "user@user.com", "password", "user", Role.USER));
        ReservationTime reservationTime1 = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        ReservationTime reservationTime2 = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "themeName", "themeDescription", "thumbnailUrl"));
        Reservation reservation1 = jpaReservationRepository.save(new Reservation(null, member.getName(), LocalDate.now().plusDays(1), reservationTime1, theme));
        Reservation reservation2 = jpaReservationRepository.save(new Reservation(null, member.getName(), LocalDate.now().plusDays(1), reservationTime2, theme));
        ReserveTicket reserveTicket1 = jpaReserveTicketRepository.save(new ReserveTicket(null, reservation1, member));
        ReserveTicket reserveTicket2 = jpaReserveTicketRepository.save(new ReserveTicket(null, reservation2, member));

        // When & Then
        assertThat(jpaReserveTicketRepository.findAllByMemberId(member.getId())).containsExactlyInAnyOrder(reserveTicket1, reserveTicket2);
    }
}
