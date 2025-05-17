package roomescape.theme.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
class ThemeRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(ThemeRepositoryTest.class);
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("테마를 저장한다")
    @Test
    void save() {
        // given
        String name = "무서운방";
        String description = "덜덜";
        String thumbnail = "무서운 사진";
        Theme theme = new Theme(name, description, thumbnail);

        // when
        themeRepository.save(theme);
        Iterable<Theme> themes = themeRepository.findAll();

        // then
        assertThat(themes).extracting(Theme::getName, Theme::getDescription, Theme::getThumbnail)
                .containsExactlyInAnyOrder(tuple(name, description, thumbnail));
    }

    @DisplayName("특정 기간 내의 상위권 테마를 조회한다")
    @Test
    void findRankedByPeriod() {
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
        LocalDate day1 = today.minusDays(8);
        LocalDate day2 = today.minusDays(5);
        LocalDate day3 = today.minusDays(2);

        Reservation r1 = new Reservation(member1, day1, time1, theme1);
        Reservation r2 = new Reservation(member1, day2, time1, theme1);
        Reservation r3 = new Reservation(member1, day3, time1, theme1);

        Reservation r4 = new Reservation(member1, day3, time1, theme2);
        Reservation r5 = new Reservation(member1, day3, time1, theme2);
        Reservation r6 = new Reservation(member1, day3, time1, theme2);

        Reservation r7 = new Reservation(member2, day2, time2, theme2);
        Reservation r8 = new Reservation(member2, day2, time2, theme2);
        Reservation r9 = new Reservation(member2, day2, time2, theme2);

        Reservation r10 = new Reservation(member2, day1, time2, theme3);
        Reservation r11 = new Reservation(member2, day1, time2, theme3);
        Reservation r12 = new Reservation(member2, day1, time2, theme3);

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

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(1);
        int limit = 10;

        // when
        Iterable<Theme> themes = themeRepository.findRankedByPeriod(startDate, endDate, limit);

        // then
        assertThat(themes).hasSize(2);
    }
}
