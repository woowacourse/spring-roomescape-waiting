package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("해당 시간에 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByTimeId_returnsTrueIfExists() {
        // given
        ReservationTime time = em.persist(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = em.persist(new Theme(null, "Theme", "desc", "url"));
        Member member = em.persist(new Member(null, "User", MemberRole.USER, "user@email.com", "Password1!"));
        LocalDate date = LocalDate.now();
        em.persist(new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED));
        em.flush();

        // when
        boolean exists = reservationRepository.existsByTimeId(time.getId());

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("해당 테마에 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByThemeId_returnsTrueIfExists() {
        // given
        ReservationTime time = em.persist(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = em.persist(new Theme(null, "Theme", "desc", "url"));
        Member member = em.persist(new Member(null, "User", MemberRole.USER, "user@email.com", "Password1!"));
        LocalDate date = LocalDate.now();
        em.persist(new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED));
        em.flush();

        // when
        boolean exists = reservationRepository.existsByThemeId(theme.getId());

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("날짜, 시간, 테마에 해당하는 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByDateAndTimeIdAndThemeId_returnsTrueIfExists() {
        // given
        ReservationTime time = em.persist(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = em.persist(new Theme(null, "Theme", "desc", "url"));
        Member member = em.persist(new Member(null, "User", MemberRole.USER, "user@email.com", "Password1!"));
        LocalDate date = LocalDate.now();
        em.persist(new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED));
        em.flush();

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId());

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("테마와 날짜로 예약 목록을 조회한다.")
    @Test
    void findByThemeIdAndDate_returnsReservations() {
        // given
        ReservationTime time = em.persist(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = em.persist(new Theme(null, "Theme", "desc", "url"));
        Member member = em.persist(new Member(null, "User", MemberRole.USER, "user@email.com", "Password1!"));
        LocalDate date = LocalDate.now();
        Reservation reservation = em.persist(
                new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED));
        em.flush();

        // when
        List<Reservation> found = reservationRepository.findByThemeIdAndDate(theme.getId(), date);

        // then
        assertThat(found).contains(reservation);
    }

    @DisplayName("회원 ID로 예약 목록을 조회한다.")
    @Test
    void findByMemberId_returnsReservations() {
        // given
        ReservationTime time = em.persist(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = em.persist(new Theme(null, "Theme", "desc", "url"));
        Member member = em.persist(new Member(null, "User", MemberRole.USER, "user@email.com", "Password1!"));
        LocalDate date = LocalDate.now();
        Reservation reservation = em.persist(
                new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED));
        em.flush();

        // when
        List<Reservation> found = reservationRepository.findByMemberId(member.getId());

        // then
        assertThat(found).contains(reservation);
    }

    @DisplayName("조건에 맞는 예약 목록을 조회한다.")
    @Test
    void findReservationsInConditions_returnsFilteredReservations() {
        // given
        ReservationTime time = em.persist(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = em.persist(new Theme(null, "Theme", "desc", "url"));
        Member member = em.persist(new Member(null, "User", MemberRole.USER, "user@email.com", "Password1!"));
        LocalDate date = LocalDate.now();
        Reservation reservation = em.persist(
                new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED));
        em.flush();

        // when
        List<Reservation> found = reservationRepository.findReservationsInConditions(
                member.getId(), theme.getId(), date.minusDays(1), date.plusDays(1));

        // then
        assertThat(found).contains(reservation);
    }
}
