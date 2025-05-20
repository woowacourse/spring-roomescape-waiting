package roomescape.repository;


import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    EntityManager entityManager;

    @DisplayName("인기 테마를 조회한다")
    @Test
    void test1() {
        //given
        Theme theme = new Theme("테마", "테마 설명", "섬네일");
        entityManager.persist(theme);

        Theme theme1 = new Theme("테마1", "테마 설명", "섬네일");
        entityManager.persist(theme1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 0));
        entityManager.persist(reservationTime);

        ReservationTime reservationTime1 = new ReservationTime(LocalTime.of(13, 0));
        entityManager.persist(reservationTime1);

        Member member = new Member("도기", "ff@gmail.com", "password", Role.ADMIN);
        entityManager.persist(member);

        Reservation reservation = new Reservation(LocalDate.now().minusDays(1), reservationTime, theme, member,
                LocalDate.now().minusDays(3
                ));
        entityManager.persist(reservation);

        Reservation reservation1 = new Reservation(LocalDate.now().minusDays(2), reservationTime, theme, member,
                LocalDate.now().minusDays(3
                ));
        entityManager.persist(reservation1);

        Reservation reservation2 = new Reservation(LocalDate.now().minusDays(2), reservationTime, theme1, member,
                LocalDate.now().minusDays(3));
        entityManager.persist(reservation2);

        Reservation reservation3 = new Reservation(LocalDate.now().minusDays(2), reservationTime1, theme, member,
                LocalDate.now().minusDays(3));
        entityManager.persist(reservation3);

        entityManager.flush();
        entityManager.clear();

        //when
        List<String> actual = themeRepository.findTopReservedThemesSince(
                        LocalDate.now().minusDays(2),
                        LocalDate.now(),
                        2
                ).stream()
                .map(Theme::getName)
                .toList();

        //then
        List<String> comparedNames = List.of(theme.getName(), theme1.getName());

        assertThat(actual).isEqualTo(comparedNames);
    }

    @Test
    @DisplayName("이름으로 존재 여부를 확인한다")
    void test2() {
        // given
        String name = "새로운 테마";
        Theme theme = new Theme(name, "설명", "썸네일");
        Theme savedTheme = themeRepository.save(theme);

        // when
        boolean actual = themeRepository.existsByName(name);

        // then
        assertThat(actual).isTrue();
    }
}
