package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private Member member;
    private Theme theme1;
    private Theme theme2;
    private ReservationTime time1;
    private ReservationTime time2;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        member = Member.createWithoutId("사용자", "user@example.com", Role.USER, "password");
        memberRepository.save(member);

        theme1 = new Theme(null, "테마1", "테마1 설명", "thumbnail1.jpg");
        theme2 = new Theme(null, "테마2", "테마2 설명", "thumbnail2.jpg");
        themeRepository.saveAll(List.of(theme1, theme2));

        time1 = new ReservationTime(null, LocalTime.of(10, 0));
        time2 = new ReservationTime(null, LocalTime.of(12, 0));
        reservationTimeRepository.saveAll(List.of(time1, time2));

        today = LocalDate.now();

        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("예약을 저장하고 ID로 조회할 수 있다")
    void saveAndFindById() {
        // given
        Reservation reservation = Reservation.createWithoutId(member, today, time1, theme1);

        // when
        Reservation savedReservation = reservationRepository.save(reservation);
        Optional<Reservation> foundReservation = reservationRepository.findById(savedReservation.getId());

        // then
        assertThat(foundReservation).isPresent();
        assertThat(foundReservation.get().getMember().getId()).isEqualTo(member.getId());
        assertThat(foundReservation.get().getDate()).isEqualTo(today);
        assertThat(foundReservation.get().getTime().getId()).isEqualTo(time1.getId());
        assertThat(foundReservation.get().getTheme().getId()).isEqualTo(theme1.getId());
    }

    @Test
    @DisplayName("모든 예약을 조회할 수 있다")
    void findAll() {
        // given
        Reservation reservation1 = Reservation.createWithoutId(member, today, time1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(member, today.plusDays(1), time2, theme2);
        reservationRepository.saveAll(List.of(reservation1, reservation2));

        // when
        List<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void delete() {
        // given
        Reservation reservation = Reservation.createWithoutId(member, today, time1, theme1);
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        reservationRepository.delete(savedReservation);
        Optional<Reservation> foundReservation = reservationRepository.findById(savedReservation.getId());

        // then
        assertThat(foundReservation).isEmpty();
    }

    @Test
    @DisplayName("테마 ID로 예약 존재 여부를 확인할 수 있다")
    void existsByThemeId() {
        // given
        Reservation reservation = Reservation.createWithoutId(member, today, time1, theme1);
        reservationRepository.save(reservation);

        // when
        boolean exists = reservationRepository.existsByThemeId(theme1.getId());
        boolean notExists = reservationRepository.existsByThemeId(999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("날짜와 테마 ID로 예약을 조회할 수 있다")
    void findByDateAndThemeId() {
        // given
        Reservation reservation1 = Reservation.createWithoutId(member, today, time1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(member, today, time2, theme1);
        Reservation reservation3 = Reservation.createWithoutId(member, today.plusDays(1), time1, theme1);
        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));

        // when
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(today, theme1.getId());

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).extracting(r -> r.getTime().getId())
                .containsExactlyInAnyOrder(time1.getId(), time2.getId());
    }

    @Test
    @DisplayName("날짜, 시간 ID, 테마 ID로 예약을 조회할 수 있다")
    void findReservationsByDateAndTimeIdAndThemeId() {
        // given
        Reservation reservation1 = Reservation.createWithoutId(member, today, time1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(member, today, time2, theme1);
        Reservation reservation3 = Reservation.createWithoutId(member, today, time1, theme2);
        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));

        // when
        List<Reservation> reservations = reservationRepository.findReservationsByDateAndTimeIdAndThemeId(
                today, time1.getId(), theme1.getId());

        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getTime().getId()).isEqualTo(time1.getId());
        assertThat(reservations.get(0).getTheme().getId()).isEqualTo(theme1.getId());
    }

    @Test
    @DisplayName("날짜 범위, 테마 ID, 회원 ID로 예약을 조회할 수 있다")
    void findReservationsByDateBetweenAndThemeIdAndMemberId() {
        // given
        LocalDate startDate = today.minusDays(5);
        LocalDate endDate = today.plusDays(5);

        Reservation reservation1 = Reservation.createWithoutId(member, today.minusDays(2), time1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(member, today, time1, theme1);
        Reservation reservation3 = Reservation.createWithoutId(member, today.plusDays(2), time1, theme1);
        Reservation reservation4 = Reservation.createWithoutId(member, today, time1, theme2);
        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3, reservation4));

        // when
        List<Reservation> reservations = reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberId(
                startDate, endDate, theme1.getId(), member.getId());

        // then
        assertThat(reservations).hasSize(3);
        assertThat(reservations).extracting("date")
                .containsExactlyInAnyOrder(today.minusDays(2), today, today.plusDays(2));
    }

    @Test
    @DisplayName("시간 ID로 예약 존재 여부를 확인할 수 있다")
    void existsByTimeId() {
        // given
        Reservation reservation = Reservation.createWithoutId(member, today, time1, theme1);
        reservationRepository.save(reservation);

        // when
        boolean exists = reservationRepository.existsByTimeId(time1.getId());
        boolean notExists = reservationRepository.existsByTimeId(999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("회원 ID로 예약을 조회할 수 있다")
    void findReservationsByMemberId() {
        // given
        Member anotherMember = Member.createWithoutId("다른사용자", "another@example.com", Role.USER, "password");
        memberRepository.save(anotherMember);

        Reservation reservation1 = Reservation.createWithoutId(member, today, time1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(member, today.plusDays(1), time2, theme2);
        Reservation reservation3 = Reservation.createWithoutId(anotherMember, today, time1, theme1);
        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));

        // when
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(member.getId());

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).extracting(r -> r.getMember().getId())
                .containsOnly(member.getId());
    }
}
