package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.dto.MyWaitingResult;

@JdbcTest
@Import(WaitingRepository.class)
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00:00', '11:00:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES ('테마1', '설명', 'https://example.com/image.jpg')");

        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);

        time = ReservationTime.of(timeId, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.of(themeId, "테마1", "설명", "https://example.com/image.jpg");
    }

    @Nested
    @DisplayName("대기 저장")
    class Save {

        @Test
        void 저장하면_id가_부여된다() {
            Waiting waiting = Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme);

            Waiting saved = waitingRepository.save(waiting);

            assertAll(
                    () -> assertThat(saved.getId()).isNotNull(),
                    () -> assertThat(saved.getName()).isEqualTo("유저1"),
                    () -> assertThat(saved.getDate()).isEqualTo(LocalDate.of(2099, 12, 31))
            );
        }
    }

    @Nested
    @DisplayName("날짜/시간/테마/이름으로 존재 여부 조회")
    class ExistsByDateAndTimeIdAndThemeIdAndName {

        @Test
        void 대기가_존재하면_true를_반환한다() {
            Waiting waiting = Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme);
            waitingRepository.save(waiting);

            boolean result = waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(
                    LocalDate.of(2099, 12, 31), time.getId(), theme.getId(), "유저1"
            );

            assertThat(result).isTrue();
        }

        @Test
        void 대기가_없으면_false를_반환한다() {
            boolean result = waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(
                    LocalDate.of(2099, 12, 31), time.getId(), theme.getId(), "유저1"
            );

            assertThat(result).isFalse();
        }

        @Test
        void 이름이_다르면_false를_반환한다() {
            Waiting waiting = Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme);
            waitingRepository.save(waiting);

            boolean result = waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(
                    LocalDate.of(2099, 12, 31), time.getId(), theme.getId(), "유저2"
            );

            assertThat(result).isFalse();
        }

        @Test
        void 날짜가_다르면_false를_반환한다() {
            Waiting waiting = Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme);
            waitingRepository.save(waiting);

            boolean result = waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(
                    LocalDate.of(2099, 12, 30), time.getId(), theme.getId(), "유저1"
            );

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("id로 존재 여부 조회")
    class ExistsById {

        @Test
        void 존재하는_id면_true를_반환한다() {
            Waiting waiting = Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme);
            Waiting saved = waitingRepository.save(waiting);

            boolean result = waitingRepository.existsById(saved.getId());

            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_id면_false를_반환한다() {
            boolean result = waitingRepository.existsById(999L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("id로 대기 삭제")
    class DeleteById {

        @Test
        void 삭제하면_해당_대기가_조회되지_않는다() {
            Waiting waiting = Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme);
            Waiting saved = waitingRepository.save(waiting);

            waitingRepository.deleteById(saved.getId());

            assertThat(waitingRepository.existsById(saved.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("이름으로 대기 조회")
    class FindByName {

        @Test
        void 이름에_해당하는_대기와_대기_순번을_반환한다() {
            waitingRepository.save(Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme));
            waitingRepository.save(Waiting.of("유저2", LocalDate.of(2099, 12, 31), time, theme));
            waitingRepository.save(Waiting.of("유저1", LocalDate.of(2099, 12, 30), time, theme));

            List<MyWaitingResult> result = waitingRepository.findByName("유저1");

            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result).extracting(MyWaitingResult::name)
                            .containsExactly("유저1", "유저1"),
                    () -> assertThat(result).extracting(MyWaitingResult::themeName)
                            .containsExactly("테마1", "테마1"),
                    () -> assertThat(result).extracting(MyWaitingResult::waitingNumber)
                            .containsExactly(1, 1)
            );
        }

        @Test
        void 이름에_해당하는_대기가_없으면_빈_리스트를_반환한다() {
            waitingRepository.save(Waiting.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            List<MyWaitingResult> result = waitingRepository.findByName("없는유저");

            assertThat(result).isEmpty();
        }
    }
}
