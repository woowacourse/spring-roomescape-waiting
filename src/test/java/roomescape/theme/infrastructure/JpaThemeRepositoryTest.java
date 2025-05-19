package roomescape.theme.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DataJpaTest
public class JpaThemeRepositoryTest {

    @Autowired
    private JpaThemeRepository repository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("저장 후 아이디 반환 테스트")
    void save_test() {
        Theme theme = TestFixture.createThemeWithoutId("테마1", "설명1", "썸네일1");

        Theme save = repository.save(theme);

        assertThat(save.getId()).isNotNull();
    }

    @ParameterizedTest
    @DisplayName("삭제 성공 관련 테스트")
    @CsvSource({"0,true", "1,false"})
    void delete_test(Long plus, boolean expected) {
        // given
        Theme theme = TestFixture.createThemeWithoutId("테마1", "설명1", "썸네일1");
        Theme save = repository.save(theme);
        // when & then
        assertDoesNotThrow(() -> repository.deleteById(save.getId() + plus));
    }

    @Test
    @DisplayName("전체 조회 테스트")
    void find_all_test() {
        // given
        Theme theme1 = TestFixture.createThemeWithoutId("테마1", "설명1", "썸네일1");
        Theme theme2 = TestFixture.createThemeWithoutId("테마2", "설명2", "썸네일2");
        Theme theme3 = TestFixture.createThemeWithoutId("테마3", "설명3", "썸네일3");

        repository.save(theme1);
        repository.save(theme2);
        repository.save(theme3);
        // when
        List<Theme> reservations = repository.findAll();
        // then
        List<String> names = reservations.stream()
                .map(Theme::getName)
                .toList();
        List<String> descriptions = reservations.stream()
                .map(Theme::getDescription)
                .toList();
        List<String> thumbnails = reservations.stream()
                .map(Theme::getThumbnail)
                .toList();
        assertAll(
                () -> assertThat(reservations).hasSize(3),
                () -> assertThat(names).contains("테마1", "테마2", "테마3"),
                () -> assertThat(descriptions).contains("설명1", "설명2", "설명3"),
                () -> assertThat(thumbnails).contains("썸네일1", "썸네일2", "썸네일3")
        );
    }

    @Test
    @DisplayName("아이디로 조회 테스트")
    void find_by_id() {
        // given
        Theme theme = TestFixture.createThemeWithoutId("테마1", "설명1", "썸네일1");
        Theme save = repository.save(theme);
        // when
        Theme findTheme = repository.findById(save.getId()).orElseThrow();
        // then
        assertAll(
                () -> assertThat(findTheme.getName()).isEqualTo(theme.getName()),
                () -> assertThat(findTheme.getDescription()).isEqualTo(theme.getDescription()),
                () -> assertThat(findTheme.getThumbnail()).isEqualTo(theme.getThumbnail())
        );
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 미포함, 개수 조건 미포함)")
    void find_popular_theme_no_time_and_count_condition() {
        // given
        ReservationTime reservationTime1 = TestFixture.createTimeWithoutId(10, 00);
        ReservationTime reservationTime2 = TestFixture.createTimeWithoutId(11, 00);

        Theme theme1 = TestFixture.createThemeWithoutId("테마1", "설명1", "썸네일1");
        Theme theme2 = TestFixture.createThemeWithoutId("테마2", "설명2", "썸네일2");
        Theme theme3 = TestFixture.createThemeWithoutId("테마3", "설명3", "썸네일3");

        Member member = TestFixture.createMemberWithoutId("멤버1", "member@com", "password");

        em.persist(reservationTime1);
        em.persist(reservationTime2);

        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);

        em.persist(member);
        em.flush();

        ReservationTime savedTime1 = em.find(ReservationTime.class, reservationTime1.getId());
        ReservationTime savedTime2 = em.find(ReservationTime.class, reservationTime2.getId());
        Theme savedTheme1 = em.find(Theme.class, theme1.getId());
        Theme savedTheme2 = em.find(Theme.class, theme2.getId());

        Reservation reservation1 = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2),
                savedTime1,
                savedTheme2);

        Reservation reservation2 = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                savedTime2,
                savedTheme1);

        Reservation reservation3 = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                savedTime1,
                savedTheme2);

        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        em.flush();

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 3, 1);

        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 3);
        List<String> names = popularThemes.stream().map(Theme::getName).toList();

        // then
        assertThat(popularThemes).hasSize(2);
        assertThat(names).containsExactly("테마2", "테마1");
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 포함, 개수 조건 미포함)")
    void find_popular_theme_no_count_condition() {
        // given
        ReservationTime reservationTime1 = TestFixture.createTimeWithoutId(10, 00);
        ReservationTime reservationTime2 = TestFixture.createTimeWithoutId(11, 00);

        Theme theme1 = TestFixture.createThemeWithoutId("테마1", "설명1", "썸네일1");
        Theme theme2 = TestFixture.createThemeWithoutId("테마2", "설명2", "썸네일2");
        Theme theme3 = TestFixture.createThemeWithoutId("테마3", "설명3", "썸네일3");

        Member member = TestFixture.createMemberWithoutId("멤버1", "member@com", "password");

        em.persist(reservationTime1);
        em.persist(reservationTime2);

        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);

        em.persist(member);

        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2),
                reservationTime1,
                theme1);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2,
                theme1);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1,
                theme2);
        Reservation reservation4 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 4),
                reservationTime1,
                theme2);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        em.persist(reservation4);

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 2, 1);
        em.flush();
        em.clear();

        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 3);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(2);
        assertThat(names).containsExactly("테마2", "테마1");
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 포함, 개수 조건 포함)")
    void find_popular_theme() {
        // given
        ReservationTime reservationTime1 = TestFixture.createTimeWithoutId(10, 00);
        ReservationTime reservationTime2 = TestFixture.createTimeWithoutId(11, 00);

        Theme theme1 = TestFixture.createThemeWithoutId("테마1", "설명1", "썸네일1");
        Theme theme2 = TestFixture.createThemeWithoutId("테마2", "설명2", "썸네일2");
        Theme theme3 = TestFixture.createThemeWithoutId("테마3", "설명3", "썸네일3");

        Member member = TestFixture.createMemberWithoutId("멤버1", "member@com", "password");

        em.persist(reservationTime1);
        em.persist(reservationTime2);
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);

        em.persist(member);

        Reservation reservation1 = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2000, 11, 2),
                reservationTime1,
                theme1);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2,
                theme1);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1,
                theme2);
        Reservation reservation4 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 4),
                reservationTime1,
                theme2);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        em.persist(reservation4);

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 2, 1);
        em.flush();
        em.clear();

        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 1);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(1);
        assertThat(names).containsExactly("테마2");
    }
}
