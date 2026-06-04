package roomescape.waiting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.ReservationTimeJdbcTemplateRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeJdbcTemplateRepository;
import roomescape.waiting.domain.Waiting;

@JdbcTest
@Import({
        WaitingJdbcTemplateRepository.class,
        ReservationTimeJdbcTemplateRepository.class,
        ThemeJdbcTemplateRepository.class
})
class WaitingJdbcTemplateRepositoryTest {

    private final WaitingJdbcTemplateRepository waitingRepository;
    private final ReservationTimeJdbcTemplateRepository timeRepository;
    private final ThemeJdbcTemplateRepository themeRepository;
    private final JdbcTemplate jdbcTemplate;

    private ReservationTime savedTime;
    private Theme savedTheme;

    @Autowired
    WaitingJdbcTemplateRepositoryTest(
            WaitingJdbcTemplateRepository waitingRepository,
            ReservationTimeJdbcTemplateRepository timeRepository,
            ThemeJdbcTemplateRepository themeRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.waitingRepository = waitingRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        savedTime = timeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(
                Theme.create("테스트 테마", "테스트 테마 설명", "https://good.com/thumb-nail/1")
        );
    }

    @Test
    @DisplayName("예약 대기를 저장하면 생성된 ID를 포함한 대기 객체를 반환한다")
    void save_success() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Waiting waiting = Waiting.create("브라운", date, savedTime, savedTheme);

        // when
        Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getName()).isEqualTo("브라운");
        assertThat(savedWaiting.getDate()).isEqualTo(date);
        assertThat(savedWaiting.getTime()).isEqualTo(savedTime);
        assertThat(savedWaiting.getTheme()).isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("예약 대기를 저장하면 같은 슬롯의 대기 순번을 포함한 대기 객체를 반환한다")
    void save_success_with_rank() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("리오", date, savedTime, savedTheme));
        Waiting waiting = Waiting.create("브라운", date, savedTime, savedTheme);

        // when
        Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        assertThat(savedWaiting.getRank()).isEqualTo(2L);
    }

    @Test
    @DisplayName("예약 대기 저장 시 waiting 테이블에 값이 저장된다")
    void save_persist_waiting_row() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Waiting waiting = Waiting.create("브라운", date, savedTime, savedTheme);

        // when
        Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT name, date, time_id, theme_id FROM waiting WHERE id = ?",
                savedWaiting.getId()
        );
        assertThat(row.get("NAME")).isEqualTo("브라운");
        assertThat(row.get("DATE").toString()).isEqualTo(date.toString());
        assertThat(((Number) row.get("TIME_ID")).longValue()).isEqualTo(savedTime.getId());
        assertThat(((Number) row.get("THEME_ID")).longValue()).isEqualTo(savedTheme.getId());
    }

    @Test
    @DisplayName("ID로 예약 대기를 조회한다")
    void findById_success() {
        // given
        waitingRepository.save(
                Waiting.create("리오", LocalDate.now().plusDays(1), savedTime, savedTheme)
        );
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", LocalDate.now().plusDays(1), savedTime, savedTheme)
        );

        // when
        Waiting foundWaiting = waitingRepository.findById(savedWaiting.getId()).orElseThrow();

        // then
        assertThat(foundWaiting).isEqualTo(savedWaiting);
        assertThat(foundWaiting.getRank()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 예약 대기를 조회하면 빈 Optional을 반환한다")
    void findById_returns_empty_with_not_found_id() {
        // when & then
        assertThat(waitingRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("ID와 이름이 일치하는 예약 대기를 삭제한다")
    void deleteByIdAndName_success() {
        // given
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", LocalDate.now().plusDays(1), savedTime, savedTheme)
        );

        // when
        waitingRepository.deleteByIdAndName(savedWaiting.getId(), "브라운");

        // then
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("이름이 일치하지 않으면 예약 대기를 삭제하지 않는다")
    void deleteByIdAndName_does_not_delete_with_different_name() {
        // given
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", LocalDate.now().plusDays(1), savedTime, savedTheme)
        );

        // when
        waitingRepository.deleteByIdAndName(savedWaiting.getId(), "다른사람");

        // then
        assertThat(waitingRepository.findById(savedWaiting.getId())).contains(savedWaiting);
    }

    @Test
    @DisplayName("이름으로 예약 대기를 조회할 때 같은 슬롯의 대기 순번을 함께 반환한다")
    void findByName_success_with_rank() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("리오", date, savedTime, savedTheme));
        Waiting savedWaiting = waitingRepository.save(Waiting.create("브라운", date, savedTime, savedTheme));

        // when
        List<Waiting> waitings = waitingRepository.findByName("브라운");

        // then
        assertThat(waitings).hasSize(1);
        assertThat(waitings.getFirst()).isEqualTo(savedWaiting);
        assertThat(waitings.getFirst().getRank()).isEqualTo(2L);
    }

    @Test
    @DisplayName("이름과 날짜와 시간과 테마가 모두 일치하는 예약 대기를 조회한다")
    void findByNameAndDateAndTimeIdAndThemeId_success() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("리오", date, savedTime, savedTheme));
        Waiting savedWaiting = waitingRepository.save(Waiting.create("브라운", date, savedTime, savedTheme));

        // when
        Waiting foundWaiting = waitingRepository.findByNameAndDateAndTimeIdAndThemeId(
                "브라운",
                date,
                savedTime.getId(),
                savedTheme.getId()
        ).orElseThrow();

        // then
        assertThat(foundWaiting).isEqualTo(savedWaiting);
        assertThat(foundWaiting.getRank()).isEqualTo(2L);
    }

    @Test
    @DisplayName("같은 슬롯에 다른 사용자의 예약 대기만 있으면 빈 Optional을 반환한다")
    void findByNameAndDateAndTimeIdAndThemeId_returns_empty_with_other_user_waiting() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("리오", date, savedTime, savedTheme));

        // when & then
        assertThat(waitingRepository.findByNameAndDateAndTimeIdAndThemeId(
                "브라운",
                date,
                savedTime.getId(),
                savedTheme.getId()
        )).isEmpty();
    }

    @Test
    @DisplayName("날짜와 시간과 테마가 일치하는 예약 대기 중 1순위 대기를 조회한다")
    void findFirstByDateAndTimeIdAndThemeId_success() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "늦은대기",
                date,
                savedTime.getId(),
                savedTheme.getId()
        );
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, DATEADD('SECOND', -1, CURRENT_TIMESTAMP))",
                "빠른대기",
                date,
                savedTime.getId(),
                savedTheme.getId()
        );

        // when
        Waiting foundWaiting = waitingRepository.findFirstByDateAndTimeIdAndThemeId(
                date,
                savedTime.getId(),
                savedTheme.getId()
        ).orElseThrow();

        // then
        assertThat(foundWaiting.getName()).isEqualTo("빠른대기");
        assertThat(foundWaiting.getRank()).isEqualTo(1L);
    }
}
