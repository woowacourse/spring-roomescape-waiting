package roomescape.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

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
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.projection.PopularThemeProjection;

/**
 * JdbcThemeRepository 슬라이스 테스트 (@JdbcTest).
 *
 * <p>검증 대상: findPopularBetween의 집계 SQL. 이건 단순 CRUD가 아니라
 * INNER JOIN + COUNT + GROUP BY + ORDER BY DESC + LIMIT + 날짜 범위(>= from, < to)가 얽힌 비단순 쿼리라, 그 자체로 검증 가치가 있다. (네
 * "Repository 테스트 필요한가" 정리의 핵심 사례)
 *
 * <p>특히 날짜 경계가 까다롭다: from은 포함(>=), to는 제외(<). 이 경계는 SQL을 실제 실행해야 잡힌다.
 * findAll/save/deleteById 같은 단순 동작은 서비스·인수에서 거쳐가므로 여기서 두껍게 검증하지 않는다.
 */
@JdbcTest
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeRepository themeRepository;

    private static final LocalDate FROM = LocalDate.of(2026, 5, 2);
    private static final LocalDate TO = LocalDate.of(2026, 5, 9);  // 집계 범위: [FROM, TO)

    private Long timeId;

    @BeforeEach
    void setUp() {
        timeId = insertTime(LocalTime.of(10, 0));
    }

    @Nested
    @DisplayName("findPopularBetween — 기간 내 예약 건수 집계")
    class Popular {

        @Test
        @DisplayName("예약 건수 내림차순으로 정렬되고, 건수가 함께 담긴다")
        void 건수_내림차순() {
            Long popular = insertTheme("인기");
            Long less = insertTheme("덜인기");
            // 인기 테마에 2건, 덜인기에 1건 (모두 기간 내)
            insertReservation("a", LocalDate.of(2026, 5, 3), popular);
            insertReservation("b", LocalDate.of(2026, 5, 4), popular);
            insertReservation("c", LocalDate.of(2026, 5, 3), less);

            List<PopularThemeProjection> result = themeRepository.findPopularBetween(FROM, TO, 10);

            assertThat(result).extracting(p -> p.getTheme().getName())
                    .containsExactly("인기", "덜인기");
            assertThat(result.get(0).getReservationCount()).isEqualTo(2L);
            assertThat(result.get(1).getReservationCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[날짜 경계] from은 포함(>=), to는 제외(<)된다")
        void 날짜_경계() {
            Long onFrom = insertTheme("from당일");
            Long onTo = insertTheme("to당일");
            Long before = insertTheme("from이전");

            insertReservation("a", FROM, onFrom);              // 경계 시작 → 포함되어야
            insertReservation("b", TO, onTo);                  // 경계 끝 → 제외되어야
            insertReservation("c", FROM.minusDays(1), before); // 범위 이전 → 제외

            List<PopularThemeProjection> result = themeRepository.findPopularBetween(FROM, TO, 10);

            assertThat(result).extracting(p -> p.getTheme().getName())
                    .containsExactly("from당일");  // onFrom만 집계됨
        }

        @Test
        @DisplayName("[LIMIT] 상위 N개만 반환한다")
        void 상위_N개() {
            // 테마i가 i건의 예약을 갖도록 구성. 같은 슬롯(date,time,theme)엔 UNIQUE 제약이 있고
            // start_at에도 UNIQUE가 있으므로, 예약마다 서로 다른 시간 슬롯을 명시적으로 만들어 충돌을 피한다.
            int hour = 11;
            for (int themeNo = 1; themeNo <= 3; themeNo++) {
                Long theme = insertTheme("테마" + themeNo);
                for (int c = 0; c < themeNo; c++) {  // 테마1=1건, 테마2=2건, 테마3=3건
                    Long uniqueTime = insertTime(LocalTime.of(hour++, 0));  // 매번 다른 시각
                    insertReservation("u" + themeNo + "_" + c, LocalDate.of(2026, 5, 3),
                            uniqueTime, theme);
                }
            }

            List<PopularThemeProjection> result = themeRepository.findPopularBetween(FROM, TO, 2);

            assertThat(result).hasSize(2);  // 3개 중 상위 2개만
            assertThat(result.get(0).getTheme().getName()).isEqualTo("테마3");  // 3건으로 최다
        }
    }

    // --- given 헬퍼 ---

    private Long insertTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, "설명", "url");
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private void insertReservation(String name, LocalDate date, Long themeId) {
        insertReservation(name, date, timeId, themeId);
    }

    private void insertReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId);
    }
}
