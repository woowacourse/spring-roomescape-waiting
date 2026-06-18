package roomescape.reservation.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest(showSql = false)
public class MissionStep3ReservationRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약 목록 조회 후 LAZY 연관객체에 접근하면 N + 1이 발생한다")
    @Disabled
    void test1() {
        // given
        Member member = saveMember("member");

        saveReservation(
            member,
            saveDate(LocalDate.of(2099, 1, 1)),
            saveTime(LocalTime.of(10, 0)),
            saveTheme("theme1")
        );

        saveReservation(
            member,
            saveDate(LocalDate.of(2099, 1, 2)),
            saveTime(LocalTime.of(11, 0)),
            saveTheme("theme2")
        );

        saveReservation(
            member,
            saveDate(LocalDate.of(2099, 1, 3)),
            saveTime(LocalTime.of(12, 0)),
            saveTheme("theme3")
        );

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory
            .unwrap(SessionFactory.class)
            .getStatistics();

        statistics.clear();

        // when
        List<Reservation> reservations =
            reservationRepository.findAllByMemberIdOrderByIdDesc(member.getId());

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        reservations.forEach(reservation -> {
            reservation.getTime().getStartAt();
            reservation.getTheme().getName();
            reservation.getDate().getDate();
        });

        // then
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("fetch join이 적용된 경우: 예약 목록 조회 후 LAZY 연관객체에 접근해도 1번의 쿼리만 발생한다")
    @Disabled
    void test2() {
        // given
        Member member = saveMember("member");

        saveReservation(
            member,
            saveDate(LocalDate.of(2099, 1, 1)),
            saveTime(LocalTime.of(10, 0)),
            saveTheme("theme1")
        );

        saveReservation(
            member,
            saveDate(LocalDate.of(2099, 1, 2)),
            saveTime(LocalTime.of(11, 0)),
            saveTheme("theme2")
        );

        saveReservation(
            member,
            saveDate(LocalDate.of(2099, 1, 3)),
            saveTime(LocalTime.of(12, 0)),
            saveTheme("theme3")
        );

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory
            .unwrap(SessionFactory.class)
            .getStatistics();

        statistics.clear();

        // when
        List<Reservation> reservations =
            reservationRepository.findAllByMemberIdOrderByIdDesc(member.getId());

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        reservations.forEach(reservation -> {
            reservation.getTime().getStartAt();
            reservation.getTheme().getName();
            reservation.getDate().getDate();
        });

        // then
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    private Member saveMember(String name) {
        return memberRepository.save(Member.register(name, "password"));
    }

    private ReservationDate saveDate(LocalDate date) {
        return reservationDateRepository.save(ReservationDate.create(date));
    }

    private ReservationTime saveTime(LocalTime time) {
        return reservationTimeRepository.save(ReservationTime.create(time));
    }

    private Theme saveTheme(String name) {
        return themeRepository.save(Theme.create(name, "description", "thumbnail-url"));
    }

    private Reservation saveReservation(Member member, ReservationDate reservationDate,
        ReservationTime reservationTime, Theme theme) {
        return reservationRepository.save(
            Reservation.reserved(member, reservationDate, reservationTime, theme));
    }
}
