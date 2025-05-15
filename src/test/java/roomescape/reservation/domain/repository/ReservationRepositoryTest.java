package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@ActiveProfiles("test")
@DataJpaTest
class ReservationRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("예약을 저장한다")
    @Test
    void save() {
        // given
        Member member = new Member("user", "user@example.com", "password");
        entityManager.persist(member);
        ReservationTime reservationTime = new ReservationTime(LocalTime.parse("10:00"));
        entityManager.persist(reservationTime);
        Theme theme = new Theme("roomescape", "timeAttack", "timeAttack.jpg");
        entityManager.persist(theme);
        LocalDate date = LocalDate.parse("2025-05-05");
        entityManager.flush();
        Reservation reservation = new Reservation(member, date, reservationTime, theme);

        // when
        reservationRepository.save(reservation);
        Iterable<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations).extracting(Reservation::getDate, Reservation::getMember, Reservation::getTime,
                        Reservation::getTheme)
                .containsExactlyInAnyOrder(tuple(date, member, reservationTime, theme));
    }


    @Test
    @DisplayName("회원 ID만으로 예약 목록을 조회한다")
    void findAllByMemberId() {
        // given
        ReservationTime time1 = new ReservationTime(LocalTime.now());
        ReservationTime time2 = new ReservationTime(LocalTime.now().plusHours(1));
        entityManager.persist(time1);
        entityManager.persist(time2);

        Theme theme1 = new Theme("name1", "description1", "thumbnail1");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        Theme theme3 = new Theme("name3", "description3", "thumbnail3");
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(theme3);

        Member member1 = new Member("name1", "email1", "password1");
        Member member2 = new Member("name2", "email2", "password2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        LocalDate today = LocalDate.now();
        LocalDate day1 = today;
        LocalDate day2 = today.plusDays(1);
        LocalDate day3 = today.plusDays(2);

        Reservation r1 = new Reservation(member1, day1, time1, theme1);
        Reservation r2 = new Reservation(member1, day2, time1, theme1);
        Reservation r3 = new Reservation(member1, day3, time1, theme1);

        Reservation r4 = new Reservation(member1, day1, time1, theme2);
        Reservation r5 = new Reservation(member1, day2, time1, theme2);
        Reservation r6 = new Reservation(member1, day3, time1, theme2);

        Reservation r7 = new Reservation(member2, day1, time2, theme2);
        Reservation r8 = new Reservation(member2, day2, time2, theme2);
        Reservation r9 = new Reservation(member2, day3, time2, theme2);

        Reservation r10 = new Reservation(member2, day1, time2, theme3);
        Reservation r11 = new Reservation(member2, day2, time2, theme3);
        Reservation r12 = new Reservation(member2, day3, time2, theme3);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.persist(r4);
        entityManager.persist(r5);
        entityManager.persist(r6);
        entityManager.persist(r7);
        entityManager.persist(r8);
        entityManager.persist(r9);
        entityManager.persist(r10);
        entityManager.persist(r11);
        entityManager.persist(r12);

        // when
        Collection<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                member1.getId(), null, null, null);

        // then
        assertThat(reservations).hasSize(6);
    }

    @Test
    @DisplayName("회원 ID와 테마 ID로 예약 목록을 조회한다")
    void findAllByMemberIdAndThemeId() {
        // given
        ReservationTime time1 = new ReservationTime(LocalTime.now());
        ReservationTime time2 = new ReservationTime(LocalTime.now().plusHours(1));
        entityManager.persist(time1);
        entityManager.persist(time2);

        Theme theme1 = new Theme("name1", "description1", "thumbnail1");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        Theme theme3 = new Theme("name3", "description3", "thumbnail3");
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(theme3);

        Member member1 = new Member("name1", "email1", "password1");
        Member member2 = new Member("name2", "email2", "password2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        LocalDate today = LocalDate.now();
        LocalDate day1 = today;
        LocalDate day2 = today.plusDays(1);
        LocalDate day3 = today.plusDays(2);

        Reservation r1 = new Reservation(member1, day1, time1, theme1);
        Reservation r2 = new Reservation(member1, day2, time1, theme1);
        Reservation r3 = new Reservation(member1, day3, time1, theme1);

        Reservation r4 = new Reservation(member1, day1, time1, theme2);
        Reservation r5 = new Reservation(member1, day2, time1, theme2);
        Reservation r6 = new Reservation(member1, day3, time1, theme2);

        Reservation r7 = new Reservation(member2, day1, time2, theme2);
        Reservation r8 = new Reservation(member2, day2, time2, theme2);
        Reservation r9 = new Reservation(member2, day3, time2, theme2);

        Reservation r10 = new Reservation(member2, day1, time2, theme3);
        Reservation r11 = new Reservation(member2, day2, time2, theme3);
        Reservation r12 = new Reservation(member2, day3, time2, theme3);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.persist(r4);
        entityManager.persist(r5);
        entityManager.persist(r6);
        entityManager.persist(r7);
        entityManager.persist(r8);
        entityManager.persist(r9);
        entityManager.persist(r10);
        entityManager.persist(r11);
        entityManager.persist(r12);

        // when
        Collection<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                member1.getId(), theme2.getId(), null, null);

        // then
        assertThat(reservations).hasSize(3);
    }

    @Test
    @DisplayName("회원 ID, 테마 ID, 날짜 기간으로 예약 목록을 조회한다")
    void findAllByMemberIdAndThemeIdAndDateBetween() {
        // given
        ReservationTime time1 = new ReservationTime(LocalTime.now());
        ReservationTime time2 = new ReservationTime(LocalTime.now().plusHours(1));
        entityManager.persist(time1);
        entityManager.persist(time2);

        Theme theme1 = new Theme("name1", "description1", "thumbnail1");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        Theme theme3 = new Theme("name3", "description3", "thumbnail3");
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(theme3);

        Member member1 = new Member("name1", "email1", "password1");
        Member member2 = new Member("name2", "email2", "password2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        LocalDate today = LocalDate.now();
        LocalDate day1 = today;
        LocalDate day2 = today.plusDays(1);
        LocalDate day3 = today.plusDays(2);

        Reservation r1 = new Reservation(member1, day1, time1, theme1);
        Reservation r2 = new Reservation(member1, day2, time1, theme1);
        Reservation r3 = new Reservation(member1, day3, time1, theme1);

        Reservation r4 = new Reservation(member1, day1, time1, theme2);
        Reservation r5 = new Reservation(member1, day2, time1, theme2);
        Reservation r6 = new Reservation(member1, day3, time1, theme2);

        Reservation r7 = new Reservation(member2, day1, time2, theme2);
        Reservation r8 = new Reservation(member2, day2, time2, theme2);
        Reservation r9 = new Reservation(member2, day3, time2, theme2);

        Reservation r10 = new Reservation(member2, day1, time2, theme3);
        Reservation r11 = new Reservation(member2, day2, time2, theme3);
        Reservation r12 = new Reservation(member2, day3, time2, theme3);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.persist(r4);
        entityManager.persist(r5);
        entityManager.persist(r6);
        entityManager.persist(r7);
        entityManager.persist(r8);
        entityManager.persist(r9);
        entityManager.persist(r10);
        entityManager.persist(r11);
        entityManager.persist(r12);

        // when
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(3);
        Collection<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                member2.getId(), theme2.getId(), from, to);

        // then
        assertThat(reservations).hasSize(2);
    }

    private void dataSetUp() {
        ReservationTime time1 = new ReservationTime(LocalTime.now());
        ReservationTime time2 = new ReservationTime(LocalTime.now().plusHours(1));
        entityManager.persist(time1);
        entityManager.persist(time2);

        Theme theme1 = new Theme("name1", "description1", "thumbnail1");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        Theme theme3 = new Theme("name3", "description3", "thumbnail3");
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(theme3);

        Member member1 = new Member("name1", "email1", "password1");
        Member member2 = new Member("name2", "email2", "password2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        LocalDate today = LocalDate.now();
        LocalDate day1 = today;
        LocalDate day2 = today.plusDays(1);
        LocalDate day3 = today.plusDays(2);

        Reservation r1 = new Reservation(member1, day1, time1, theme1);
        Reservation r2 = new Reservation(member1, day2, time1, theme1);
        Reservation r3 = new Reservation(member1, day3, time1, theme1);

        Reservation r4 = new Reservation(member1, day1, time1, theme2);
        Reservation r5 = new Reservation(member1, day2, time1, theme2);
        Reservation r6 = new Reservation(member1, day3, time1, theme2);

        Reservation r7 = new Reservation(member2, day1, time2, theme2);
        Reservation r8 = new Reservation(member2, day2, time2, theme2);
        Reservation r9 = new Reservation(member2, day3, time2, theme2);

        Reservation r10 = new Reservation(member2, day1, time2, theme3);
        Reservation r11 = new Reservation(member2, day2, time2, theme3);
        Reservation r12 = new Reservation(member2, day3, time2, theme3);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.persist(r4);
        entityManager.persist(r5);
        entityManager.persist(r6);
        entityManager.persist(r7);
        entityManager.persist(r8);
        entityManager.persist(r9);
        entityManager.persist(r10);
        entityManager.persist(r11);
        entityManager.persist(r12);
    }
}
