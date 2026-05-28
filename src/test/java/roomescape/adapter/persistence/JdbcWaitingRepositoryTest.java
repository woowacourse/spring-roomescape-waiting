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
import roomescape.domain.Waiting;
import roomescape.domain.repository.WaitingRepository;

/**
 * JdbcWaitingRepository 슬라이스 테스트 (@JdbcTest).
 *
 * <p>검증 대상: "우리가 직접 작성한 SQL"의 정확성. 이건 Service 통합 테스트의 해피 패스로는
 * 충분히 검증되지 않는다(통합은 save·findBySlot을 한 경로로만 스쳐 간다). 컬럼명 오타,
 * 파라미터 바인딩 순서, ORDER BY 정렬, 조인 매핑 같은 건 SQL을 실제로 실행해야 잡힌다.
 *
 * <p>왜 @SpringBootTest가 아니라 @JdbcTest인가: 이 검증에 서비스·웹 계층은 필요 없다.
 * JdbcTemplate과 DataSource만 있으면 된다. 슬라이스로 좁히면 컨텍스트가 가볍고 빠르며,
 * 실패 시 "쿼리 문제"로 원인이 바로 좁혀진다.
 *
 * <p>주의: @JdbcTest는 @Repository 빈을 자동 스캔하지 않으므로 @Import로 명시한다.
 * schema.sql은 클래스패스에 있으면 슬라이스가 자동 적용한다(임베디드 H2).
 *
 * <p>@JdbcTest는 기본적으로 각 테스트를 트랜잭션으로 감싸 자동 롤백한다.
 * 여기서는 단일 쿼리의 정확성만 보고 트랜잭션 경계를 검증하지 않으므로 롤백 격리로 충분하다.
 * (트랜잭션 경계 검증이 필요한 건 Service 통합 테스트 쪽이고, 거기선 롤백을 쓰지 않는다.)
 */
@JdbcTest
@Import(JdbcWaitingRepository.class)
class JdbcWaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaitingRepository waitingRepository;

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.of(10, 0));
        timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, LocalTime.of(10, 0));

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마A", "설명", "url");
        themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?", Long.class, "테마A");
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("저장하면 발급된 id를 가진 대기를 반환한다")
        void 저장_후_id_발급() {
            Waiting saved = waitingRepository.save(
                    Waiting.create("콘", DATE,
                            roomescape.domain.ReservationTime.withId(timeId, LocalTime.of(10, 0)),
                            roomescape.domain.Theme.withId(themeId, "테마A", "설명", "url"),
                            1));

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("콘");
            assertThat(saved.getOrderIndex()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findBySlot — 같은 슬롯의 대기를 순번 오름차순으로")
    class FindBySlot {

        @Test
        @DisplayName("같은 슬롯의 대기만, order_index 오름차순으로 반환한다")
        void 슬롯_조회_정렬() {
            // 일부러 순번을 뒤섞어 insert한다 — ORDER BY가 진짜 동작하는지 보기 위함
            insertWaiting("핀", DATE, 3);
            insertWaiting("콘", DATE, 1);
            insertWaiting("모카", DATE, 2);

            List<Waiting> result = waitingRepository.findBySlot(DATE, timeId, themeId);

            assertThat(result).extracting(Waiting::getName)
                    .containsExactly("콘", "모카", "핀");  // 순번 오름차순
            assertThat(result).extracting(Waiting::getOrderIndex)
                    .containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("다른 날짜의 대기는 포함되지 않는다 (필터링 경계)")
        void 다른_슬롯_제외() {
            insertWaiting("콘", DATE, 1);
            insertWaiting("모카", DATE.plusDays(1), 1);  // 다른 날짜

            List<Waiting> result = waitingRepository.findBySlot(DATE, timeId, themeId);

            assertThat(result).extracting(Waiting::getName).containsExactly("콘");
        }

        @Test
        @DisplayName("대기가 없으면 빈 리스트를 반환한다 (빈 결과 경계)")
        void 빈_결과() {
            assertThat(waitingRepository.findBySlot(DATE, timeId, themeId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByName — 내 대기를 날짜·시간 순으로")
    class FindByName {

        @Test
        @DisplayName("해당 이름의 대기만, 날짜·시간 오름차순으로 반환한다")
        void 이름_조회_정렬() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.of(11, 0));
            Long timeId11 = jdbcTemplate.queryForObject(
                    "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, LocalTime.of(11, 0));

            // 같은 이름, 날짜·시간을 뒤섞어 insert
            insertWaitingWithTime("콘", DATE.plusDays(1), timeId, 1);
            insertWaitingWithTime("콘", DATE, timeId11, 1);
            insertWaitingWithTime("콘", DATE, timeId, 1);
            insertWaiting("모카", DATE, 2);  // 다른 사람 (필터 검증)

            List<Waiting> result = waitingRepository.findByName("콘");

            assertThat(result).hasSize(3);
            // DATE 10:00 → DATE 11:00 → DATE+1 10:00 순
            assertThat(result.get(0).getDate()).isEqualTo(DATE);
            assertThat(result.get(0).getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
            assertThat(result.get(1).getDate()).isEqualTo(DATE);
            assertThat(result.get(1).getTime().getStartAt()).isEqualTo(LocalTime.of(11, 0));
            assertThat(result.get(2).getDate()).isEqualTo(DATE.plusDays(1));
        }
    }

    @Nested
    @DisplayName("updateOrderIndex / deleteById / findById")
    class UpdateAndDelete {

        @Test
        @DisplayName("순번을 갱신하면 조회 시 새 순번이 반영된다")
        void 순번_갱신() {
            insertWaiting("콘", DATE, 3);
            Long id = waitingRepository.findBySlot(DATE, timeId, themeId).get(0).getId();

            waitingRepository.updateOrderIndex(id, 1);

            assertThat(waitingRepository.findById(id)).isPresent();
            assertThat(waitingRepository.findById(id).get().getOrderIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("삭제하면 findById가 비어 있다")
        void 삭제() {
            insertWaiting("콘", DATE, 1);
            Long id = waitingRepository.findBySlot(DATE, timeId, themeId).get(0).getId();

            waitingRepository.deleteById(id);

            assertThat(waitingRepository.findById(id)).isEmpty();
        }
    }

    // --- given 헬퍼 (이 슬라이스는 ReservationTestHelper 빈을 로드하지 않으므로 로컬 헬퍼 사용) ---

    private void insertWaiting(String name, LocalDate date, int order) {
        insertWaitingWithTime(name, date, timeId, order);
    }

    private void insertWaitingWithTime(String name, LocalDate date, Long timeId, int order) {
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, order_index) VALUES (?, ?, ?, ?, ?)",
                name, date, timeId, themeId, order);
    }
}
