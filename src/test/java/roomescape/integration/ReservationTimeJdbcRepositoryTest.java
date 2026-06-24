package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.ConflictException;
import roomescape.infrastructure.repository.ReservationTimeJdbcRepository;

@JdbcTest
@Import(ReservationTimeJdbcRepository.class)
class ReservationTimeJdbcRepositoryTest {

    private static final LocalTime START_AT = LocalTime.of(10, 0);
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 8, 5);
    private static final String THEME_NAME = "공포";
    private static final String THEME_DESCRIPTION = "무서운 테마";
    private static final String THEME_THUMBNAIL_IMAGE_URL = "https://example.com/horror.jpg";

    @Autowired
    private ReservationTimeJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void save는_생성된_id를_부여한_시간을_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, START_AT));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(START_AT);
    }

    @Test
    void 같은_시작_시간으로_저장하면_ConflictException을_던진다() {
        repository.save(new ReservationTime(null, START_AT));

        assertThatThrownBy(() -> repository.save(new ReservationTime(null, START_AT)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 존재하는 예약 시간");
    }

    @Test
    void findAll은_시작_시간_오름차순으로_정렬해_반환한다() {
        repository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        repository.save(new ReservationTime(null, LocalTime.of(9, 0)));

        List<ReservationTime> times = repository.findAll();

        assertThat(times)
                .extracting(ReservationTime::getStartAt)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(11, 0));
    }

    @Test
    void findById는_존재하는_시간을_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, START_AT));

        Optional<ReservationTime> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStartAt()).isEqualTo(START_AT);
    }

    @Test
    void findById는_존재하지_않으면_빈_Optional을_반환한다() {
        assertThat(repository.findById(9999L)).isEmpty();
    }

    @Test
    void existsById는_저장된_시간에_대해_true를_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, START_AT));

        assertThat(repository.existsById(saved.getId())).isTrue();
    }

    @Test
    void deleteById_이후_existsById는_false를_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, START_AT));

        repository.deleteById(saved.getId());

        assertThat(repository.existsById(saved.getId())).isFalse();
    }

    @Test
    void 예약에서_사용_중인_시간을_삭제하면_ConflictException을_던진다() {
        ReservationTime saved = repository.save(new ReservationTime(null, START_AT));
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", RESERVATION_DATE, saved.getId(), themeId
        );

        assertThatThrownBy(() -> repository.deleteById(saved.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("사용 중인 예약");
    }
}
