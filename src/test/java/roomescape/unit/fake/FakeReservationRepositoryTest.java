package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.infrastructure.ReservationRepository;

class FakeReservationRepositoryTest {

    private final ReservationRepository reservationRepository = new FakeReservationRepository();

    @Test
    void 모든_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(timeSlot)
                .theme(theme).build();
        Reservation reservation2 = Reservation.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 2))
                .timeSlot(timeSlot)
                .theme(theme).build();
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
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(timeSlot)
                .theme(theme).build();
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
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(timeSlot)
                .theme(theme).build();
        reservationRepository.save(reservation1);
        // when
        reservationRepository.deleteById(1L);
        // then
        List<Reservation> allReservation = reservationRepository.findAll();
        assertThat(allReservation).hasSize(0);
    }

    @Test
    void 테마id로_예약이_존재하는지_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme = Theme.builder()
                .id(1L)
                .name("theme1")
                .description("desc")
                .thumbnail("thumb").build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(timeSlot)
                .theme(theme).build();
        reservationRepository.save(reservation1);
        // when
        boolean exist = reservationRepository.existsByThemeId(theme.getId());
        // then
        assertThat(exist).isTrue();
    }
}