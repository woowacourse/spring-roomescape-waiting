package roomescape.theme.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.JdbcTimeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private JdbcTimeRepository timeRepository;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @DisplayName("테마를 저장한다.")
    @Test
    void save() {
        Theme saved = jdbcThemeRepository.save(new Theme("테마", "내용", "https://img.test/b.png"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테마");
        assertThat(saved.getDescription()).isEqualTo("내용");
        assertThat(saved.getImageUrl()).isEqualTo("https://img.test/b.png");
    }

    @DisplayName("저장된 테마를 조회한다.")
    @Test
    void findAll() {
        assertThat(jdbcThemeRepository.findAll()).isEmpty();

        jdbcThemeRepository.save(new Theme("이름", "설명", "https://img.test/a.png"));

        List<Theme> themes = jdbcThemeRepository.findAll();
        assertThat(themes).hasSize(1);
        assertThat(themes.getFirst().getId()).isEqualTo(1L);
        assertThat(themes.getFirst().getName()).isEqualTo("이름");
        assertThat(themes.getFirst().getDescription()).isEqualTo("설명");
        assertThat(themes.getFirst().getImageUrl()).isEqualTo("https://img.test/a.png");
    }

    @DisplayName("id로 테마를 삭제한다.")
    @Test
    void deleteById() {
        jdbcThemeRepository.save(new Theme("x", "y", "https://img.test/c.png"));

        assertThat(jdbcThemeRepository.deleteById(1L)).isTrue();
        assertThat(jdbcThemeRepository.findAll()).isEmpty();

        assertThat(jdbcThemeRepository.deleteById(1L)).isFalse();
    }

    @DisplayName("id로 테마가 존재하는지 판단한다.")
    @Test
    void existsById() {
        assertThat(jdbcThemeRepository.existsById(1L)).isFalse();

        jdbcThemeRepository.save(new Theme("테마", "설명", "https://img.test/a.png"));

        assertThat(jdbcThemeRepository.existsById(1L)).isTrue();
        assertThat(jdbcThemeRepository.existsById(2L)).isFalse();
    }

    @DisplayName("특정 날짜의 인기 테마를 조회한다.")
    @Test
    void findBestThemesByDate_인기_테마_조회() {
        // given
        Theme theme1 = jdbcThemeRepository.save(new Theme("테마1", "설명1", "https://img.test/1.png"));
        Theme theme2 = jdbcThemeRepository.save(new Theme("테마2", "설명2", "https://img.test/2.png"));
        Theme theme3 = jdbcThemeRepository.save(new Theme("테마3", "설명3", "https://img.test/3.png"));

        LocalDate startDate = LocalDate.of(2026, 5, 3);
        LocalDate endDate = LocalDate.of(2026, 5, 10);

        ReservationTime time1 = insertTime(LocalDateTime.of(2026, 5, 2, 10, 0), LocalDateTime.of(2026, 5, 2, 12, 0));
        ReservationTime time2 = insertTime(LocalDateTime.of(2026, 5, 6, 10, 0), LocalDateTime.of(2026, 5, 6, 12, 0));
        ReservationTime time3 = insertTime(LocalDateTime.of(2026, 5, 7, 10, 0), LocalDateTime.of(2026, 5, 7, 12, 0));
        ReservationTime time4 = insertTime(LocalDateTime.of(2026, 5, 8, 10, 0), LocalDateTime.of(2026, 5, 8, 12, 0));
        ReservationTime time5 = insertTime(LocalDateTime.of(2026, 5, 9, 10, 0), LocalDateTime.of(2026, 5, 9, 12, 0));
        ReservationTime time6 = insertTime(LocalDateTime.of(2026, 5, 10, 10, 0), LocalDateTime.of(2026, 5, 10, 12, 0));

        insertReservation("a1", time3, theme1);
        insertReservation("a2", time4, theme1);
        insertReservation("a3", time6, theme1);

        insertReservation("b1", time5, theme2);
        insertReservation("b2", time6, theme2);
        insertReservation("b_in", time2, theme2);

        insertReservation("c_out", time1, theme3);

        // when
        List<Theme> bestThemes = jdbcThemeRepository.findBestThemesByDate(startDate, endDate, 10);

        // then
        assertThat(bestThemes).hasSize(2);
        assertThat(bestThemes.get(0).getId()).isEqualTo(theme1.getId());
        assertThat(bestThemes.get(1).getId()).isEqualTo(theme2.getId());
        assertThat(bestThemes).extracting(Theme::getName)
                .containsExactly("테마1", "테마2");
    }

    private ReservationTime insertTime(LocalDateTime startAt, LocalDateTime endAt) {
        return timeRepository.save(startAt, endAt);
    }

    private Reservation insertReservation(String name, ReservationTime time, Theme theme) {
        return reservationRepository.save(
                new Reservation(name, time, theme, Status.RESERVED, LocalDateTime.now()));
    }
}
