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
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;

/**
 * JdbcReservationRepository 슬라이스 테스트 (@JdbcTest).
 *
 * <p>검증 대상: 직접 작성한 비단순 SQL의 정확성.
 * <ul>
 *   <li>findByNameOrderByDateAscTimeAsc — 날짜·시간 다중 정렬 + 이름 필터 + 조인 매핑</li>
 *   <li>existsByDateAndTimeAndThemeExcludingId — 변경 시 "자기 자신은 충돌에서 제외"하는 id != ? 조건</li>
 *   <li>existsBySlotAndName / existsByTimeId / existsByThemeId — 존재 여부 쿼리의 참/거짓 경계</li>
 *   <li>updateDateAndTime — 부분 갱신이 조회에 반영되는가</li>
 * </ul>
 *
 * <p>save/findById의 해피 패스는 서비스 통합·인수에서 자연히 거쳐가므로 여기서 두껍게 검증하지 않는다.
 * "id != ?"의 자기 제외 같은 미묘한 조건은 SQL을 실제 실행해야만 잡혀서, 이 슬라이스의 고유 가치다.
 */
@JdbcTest
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    private Long timeId10;
    private Long timeId11;
    private Long themeId;

    @BeforeEach
    void setUp() {
        timeId10 = insertTime(LocalTime.of(10, 0));
        timeId11 = insertTime(LocalTime.of(11, 0));
        themeId = insertTheme("테마A");
    }

    @Nested
    @DisplayName("findByNameOrderByDateAscTimeAsc — 내 예약을 날짜·시간 순으로")
    class FindByName {

        @Test
        @DisplayName("해당 이름의 예약만, 날짜 ASC·시간 ASC로 반환한다")
        void 이름_조회_정렬() {
            // 같은 이름의 예약을 날짜·시간 뒤섞어 insert
            insertReservation("브라운", DATE.plusDays(1), timeId10);
            insertReservation("브라운", DATE, timeId11);
            insertReservation("브라운", DATE, timeId10);
            insertReservation("콘", DATE, timeId10, insertTheme("테마B")); // 다른 사람 (필터 검증)

            List<Reservation> result = reservationRepository.findByNameOrderByDateAscTimeAsc("브라운");

            assertThat(result).hasSize(3);
            // DATE 10:00 → DATE 11:00 → DATE+1 10:00
            assertThat(result.get(0).getDate()).isEqualTo(DATE);
            assertThat(result.get(0).getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
            assertThat(result.get(1).getDate()).isEqualTo(DATE);
            assertThat(result.get(1).getTime().getStartAt()).isEqualTo(LocalTime.of(11, 0));
            assertThat(result.get(2).getDate()).isEqualTo(DATE.plusDays(1));
        }

        @Test
        @DisplayName("해당 이름의 예약이 없으면 빈 리스트")
        void 빈_결과() {
            assertThat(reservationRepository.findByNameOrderByDateAscTimeAsc("없는사람")).isEmpty();
        }
    }

    @Nested
    @DisplayName("중복 검사 쿼리")
    class ExistsQueries {

        @Test
        @DisplayName("existsByDateAndTimeAndTheme — 슬롯에 예약이 있으면 true")
        void 슬롯_존재() {
            insertReservation("브라운", DATE, timeId10);

            assertThat(reservationRepository.existsByDateAndTimeAndTheme(DATE, timeId10, themeId)).isTrue();
            assertThat(reservationRepository.existsByDateAndTimeAndTheme(DATE, timeId11, themeId)).isFalse();
        }

        @Test
        @DisplayName("existsBySlotAndName — 그 슬롯에 그 이름의 예약이 있으면 true")
        void 슬롯_이름_존재() {
            insertReservation("브라운", DATE, timeId10);

            assertThat(reservationRepository.existsBySlotAndName(DATE, timeId10, themeId, "브라운")).isTrue();
            assertThat(reservationRepository.existsBySlotAndName(DATE, timeId10, themeId, "콘")).isFalse();
        }

        @Test
        @DisplayName("existsByDateAndTimeAndThemeExcludingId — 자기 자신은 충돌에서 제외한다")
        void 자기_제외() {
            Long myId = insertReservation("브라운", DATE, timeId10);

            // 자기 id를 제외하면 그 슬롯에 다른 예약이 없으므로 false
            assertThat(reservationRepository
                    .existsByDateAndTimeAndThemeExcludingId(DATE, timeId10, themeId, myId)).isFalse();

            // 같은 테마·날짜의 다른 시간(11:00)에 콘의 예약을 넣으면,
            // 11:00 슬롯 기준으로 내 id(10:00 예약)를 제외해도 콘이 남아 true
            insertReservation("콘", DATE, timeId11);
            assertThat(reservationRepository
                    .existsByDateAndTimeAndThemeExcludingId(DATE, timeId11, themeId, myId)).isTrue();
        }

        @Test
        @DisplayName("existsByTimeId / existsByThemeId — 참조 존재 여부")
        void 참조_존재() {
            insertReservation("브라운", DATE, timeId10);

            assertThat(reservationRepository.existsByTimeId(timeId10)).isTrue();
            assertThat(reservationRepository.existsByTimeId(timeId11)).isFalse();
            assertThat(reservationRepository.existsByThemeId(themeId)).isTrue();
        }
    }

    @Nested
    @DisplayName("updateDateAndTime")
    class Update {

        @Test
        @DisplayName("날짜·시간을 갱신하면 조회 시 새 값이 반영된다")
        void 부분_갱신_반영() {
            Long id = insertReservation("브라운", DATE, timeId10);

            reservationRepository.updateDateAndTime(id, DATE.plusDays(1), timeId11);

            Reservation updated = reservationRepository.findById(id).orElseThrow();
            assertThat(updated.getDate()).isEqualTo(DATE.plusDays(1));
            assertThat(updated.getTime().getStartAt()).isEqualTo(LocalTime.of(11, 0));
        }
    }

    // --- given 헬퍼 (이 슬라이스는 ReservationTestHelper 빈을 로드하지 않으므로 로컬 헬퍼) ---

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

    private Long insertReservation(String name, LocalDate date, Long timeId) {
        return insertReservation(name, date, timeId, themeId);
    }

    private Long insertReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?",
                Long.class, name, date, timeId, themeId);
    }
}
