package roomescape.theme.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DataJpaTest
class ThemeCustomRepositoryImplTest {

    @Autowired
    private JpaThemeRepository repository;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("저장 후 테마 객체 반환 테스트")
    void save_test() {
        Theme theme = Theme.createWithoutId("a", "a", "a");

        Theme savedTheme = repository.save(theme);

        assertThat(savedTheme).isNotNull();
    }

    @Test
    @DisplayName("삭제 성공 관련 테스트")
    void delete_test() {
        // given
        Theme theme = Theme.createWithoutId("a", "a", "a");
        Theme savedTheme = repository.save(theme);
        // when & then
        assertDoesNotThrow(() -> repository.deleteById(savedTheme.getId()));
    }

    @Test
    @DisplayName("전체 조회 테스트")
    void find_all_test() {
        // given
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");
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
                .map(Theme::getName)
                .toList();
        List<String> thumbnails = reservations.stream()
                .map(Theme::getName)
                .toList();
        assertAll(
                () -> assertThat(reservations).hasSize(3),
                () -> assertThat(names).contains("a", "b", "c"),
                () -> assertThat(descriptions).contains("a", "b", "c"),
                () -> assertThat(thumbnails).contains("a", "b", "c")
        );
    }

    @Test
    @DisplayName("아이디로 조회 테스트")
    void find_by_id() {
        // given
        Theme theme = Theme.createWithoutId("a", "a", "a");
        Theme saveTheme = repository.save(theme);
        // when
        Optional<Theme> findTheme = repository.findById(saveTheme.getId());
        // then
        assertAll(
                () -> assertThat(findTheme).isPresent(),
                () -> assertThat(findTheme.get().getName()).isEqualTo(theme.getName()),
                () -> assertThat(findTheme.get().getDescription()).isEqualTo(theme.getDescription()),
                () -> assertThat(findTheme.get().getThumbnail()).isEqualTo(theme.getThumbnail())
        );
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 미포함, 개수 조건 미포함)")
    void find_popular_theme_no_time_and_count_condition() {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(10, 11));
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");
        em.persist(reservationTime1);
        em.persist(reservationTime2);

        Member member = Member.createWithoutId("a", "a", "a", Role.USER);
        em.persist(member);
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);
        repository.save(theme3);

        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2),
                reservationTime1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2, theme2);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1, theme2);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 3, 1);
        em.flush();
        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 3);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(2);
        assertThat(names).containsExactly("b", "a");
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 포함, 개수 조건 미포함)")
    void find_popular_theme_no_count_condition() {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(10, 11));
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");

        em.persist(reservationTime1);
        em.persist(reservationTime2);
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);

        Member member = Member.createWithoutId("a", "a", "a", Role.USER);
        em.persist(member);

        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2),
                reservationTime1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2, theme1);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1, theme2);
        Reservation reservation4 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 4),
                reservationTime1, theme2);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        em.persist(reservation4);
        em.flush();
        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 2, 1);
        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 3);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(2);
        assertThat(names).containsExactly("b", "a");
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 포함, 개수 조건 포함)")
    void find_popular_theme() {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(10, 11));
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");

        em.persist(reservationTime1);
        em.persist(reservationTime2);
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);

        Member member = Member.createWithoutId("a", "a", "a", Role.USER);
        em.persist(member);

        Reservation reservation1 = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2000, 11, 2),
                reservationTime1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2, theme1);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1, theme2);
        Reservation reservation4 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 4),
                reservationTime1, theme2);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        em.persist(reservation4);
        em.flush();

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 2, 1);
        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 1);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(1);
        assertThat(names).containsExactly("b");
    }
}
