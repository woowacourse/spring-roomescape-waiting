package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.ThemeRepository;
import roomescape.business.model.vo.Id;
import roomescape.test_util.JpaTestUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@Import({JpaThemeRepository.class, JpaTestUtil.class})
class ThemeRepositoryTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(5);

    @Autowired
    private ThemeRepository sut;
    @Autowired
    private JpaTestUtil testUtil;

    @AfterEach
    void tearDown() {
        testUtil.deleteAll();
    }

    @Test
    void 테마를_저장할_수_있다() {
        assertThatCode(() -> sut.save(Theme.create("주홍색 연구", "", "")))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_테마를_찾을_수_있다() {
        testUtil.insertTheme("1", "주홍색 연구");
        testUtil.insertTheme("2", "아라비아해의 비밀");

        final List<Theme> result = sut.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId().value()).isEqualTo("1");
        assertThat(result.get(1).getId().value()).isEqualTo("2");
    }

    @Test
    void 예약이_많은_순으로_테마를_찾을_수_있다() {
        testUtil.insertReservationTime("1", LocalTime.of(10, 0));
        testUtil.insertTheme("2", "주홍색 연구");
        testUtil.insertTheme("3", "아라비아해의 비밀");
        testUtil.insertTheme("4", "범위_외부_테마");
        testUtil.insertUser("5", "돔푸");
        testUtil.insertUser("6", "레몬");
        testUtil.insertReservation("A", DATE, "1", "2", "5");
        testUtil.insertReservation("B", DATE, "1", "3", "5");
        testUtil.insertReservation("C", DATE, "1", "3", "5");
        testUtil.insertReservation("D", DATE.minusDays(10), "1", "4", "6");
        testUtil.insertReservation("E", DATE.minusDays(10), "1", "4", "6");
        testUtil.insertReservation("F", DATE.plusDays(10), "1", "4", "6");

        final List<Theme> result = sut.findPopularThemes(DATE.minusDays(5), DATE.plusDays(5), 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId().value()).isEqualTo("3");
        assertThat(result.get(1).getId().value()).isEqualTo("2");
    }

    @Test
    void ID를_기준으로_테마를_찾을_수_있다() {
        testUtil.insertTheme("1", "주홍색 연구");

        final Optional<Theme> result = sut.findById(Id.create("1"));

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId().value()).isEqualTo("1");
        assertThat(result.get().getName().value()).isEqualTo("주홍색 연구");
    }

    @Test
    void ID를_기준으로_존재하는지_확인할_수_있다() {
        testUtil.insertTheme("1", "주홍색 연구");

        final boolean result = sut.existById(Id.create("1"));

        assertThat(result).isTrue();
    }

    @Test
    void ID를_기준으로_삭제할_수_있다() {
        testUtil.insertTheme("1", "주홍색 연구");

        sut.deleteById(Id.create("1"));

        assertThat(testUtil.countTheme()).isEqualTo(0);
    }
}
