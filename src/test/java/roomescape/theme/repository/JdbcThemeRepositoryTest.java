package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.testutil.JdbcRepositoryTest;
import roomescape.theme.model.Theme;

@JdbcRepositoryTest
class JdbcThemeRepositoryTest {

    private final ThemeRepository themeRepository;

    private final ReservationRepository reservationRepository;

    @Autowired
    JdbcThemeRepositoryTest(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.themeRepository = new JdbcThemeRepository(jdbcTemplate, dataSource);
        this.reservationRepository = new JdbcReservationRepository(jdbcTemplate, dataSource);
    }

    @Test
    @DisplayName("Theme 저장한 후 저장한 row의 id값을 반환한다.")
    void save() {
        Theme savedTheme = themeRepository.save(ThemeFixture.getOne());
        assertThat(savedTheme.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Theme 테이블의 있는 모든 데이터를 조회한다.")
    void findAll() {
        Theme savedTheme = themeRepository.save(ThemeFixture.getOne());
        assertThat(themeRepository.findAll()).isEqualTo(List.of(savedTheme));
    }

    @Test
    @DisplayName("Theme 테이블의 주어진 id와 동일한 데이터를 조회한다.")
    void findById() {
        Theme savedTheme = themeRepository.save(ThemeFixture.getOne());

        assertThat(themeRepository.findById(savedTheme.getId())).isEqualTo(Optional.of(savedTheme));
    }

    @Test
    @DisplayName("Theme 테이블에 주어진 id와 없는 경우 빈 옵셔널을 반환한다.")
    void findById_Return_EmptyOptional() {
        assertThat(themeRepository.findById(20L)).isNotPresent();
    }

    @Test
    @DisplayName("Theme 테이블에서 많이 예약된 테마 10개를 내림차순으로 가져온다.")
    void findOrderByReservation() {
        // given
        List<Theme> savedThemes = ThemeFixture.get(10).stream()
                .map(themeRepository::save)
                .toList();

        Theme topTheme = savedThemes.get(3);
        reservationRepository.save(ReservationFixture.getOneWithTheme(topTheme));
        reservationRepository.save(ReservationFixture.getOneWithTheme(topTheme));

        Theme secondTheme = savedThemes.get(0);
        reservationRepository.save(ReservationFixture.getOneWithTheme(secondTheme));

        // when
        List<Theme> orderByReservations = themeRepository.findOrderByReservation(10);

        // then
        assertAll(
                () -> assertThat(orderByReservations).hasSize(10),
                () -> assertThat(orderByReservations.get(0)).isEqualTo(topTheme),
                () -> assertThat(orderByReservations.get(1)).isEqualTo(secondTheme)
        );
    }

    @Test
    @DisplayName("해당 테마 id값과 일치하는 예약이 존재하는 경우 참을 반환한다.")
    void existsById() {
        Theme savedTheme = themeRepository.save(ThemeFixture.getOne());

        assertTrue(themeRepository.existsById(savedTheme.getId()));
    }

    @Test
    @DisplayName("해당 테마 id값과 일치하는 예약이 존재하지 않은 경우 거짓을 반환한다.")
    void existsById_WhenNotExist() {
        assertFalse(themeRepository.existsById(1L));
    }

    @Test
    @DisplayName("ReservationTime 테이블에 주어진 id와 동일한 데이터를 삭제한다.")
    void deleteById() {
        Theme savedTheme = themeRepository.save(ThemeFixture.getOne());

        themeRepository.deleteById(savedTheme.getId());
        assertThat(themeRepository.findById(savedTheme.getId())).isNotPresent();
    }
}
