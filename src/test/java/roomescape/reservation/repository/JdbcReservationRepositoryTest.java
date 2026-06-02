package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Time;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@JdbcTest
class JdbcReservationRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationRepository reservationRepository;

    @Autowired
    public JdbcReservationRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationRepository = new JdbcReservationRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약을 저장하고 반환된 객체의 ID를 확인한다.")
    void save_validReservation_returnsWithId() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        // when
        Reservation saved = reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        new ReservationSlot(LocalDate.of(2024, 5, 1), time, theme)
                )
        );

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("기존에 이미 동일한 예약이 있으면 예외가 발생한다.")
    void save_duplicateReservation_throwsDataIntegrityViolation() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        new ReservationSlot(LocalDate.of(2024, 5, 1), time, theme)
                )
        );

        // when & then
        assertThatThrownBy(() -> reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        new ReservationSlot(LocalDate.of(2024, 5, 1), time, theme)
                )
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("ID를 통해 예약을 삭제한다.")
    void deleteById_existingId_removesReservation() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        Reservation saved = saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);

        // when
        reservationRepository.delete(saved);

        // then
        List<Reservation> reservations = reservationRepository.findAllByName("브라운");
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("예약 데이터를 삭제해도 시간/테마 데이터는 함께 삭제되지 않는다.")
    void deleteById_existingId_doesNotDeleteTimeAndTheme() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");
        Reservation saved = saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);

        // when
        reservationRepository.delete(saved);

        // then
        Integer reservationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class,
                saved.getId()
        );
        Integer timeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_time WHERE id = ?",
                Integer.class,
                time.getId()
        );
        Integer themeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM theme WHERE id = ?",
                Integer.class,
                theme.getId()
        );

        assertThat(reservationCount).isEqualTo(0);
        assertThat(timeCount).isEqualTo(1);
        assertThat(themeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("예약을 수정한다.")
    void update_validReservation_updatesDate() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        Reservation saved = reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        new ReservationSlot(LocalDate.now().plusDays(1), time, theme)
                )
        );

        Reservation updated = saved.update(LocalDate.now().plusDays(5), null, "브라운");

        // when
        reservationRepository.update(updated);

        // then
        assertThat(updated.getId()).isNotNull();
        assertThat(updated.getDate()).isEqualTo(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("저장돼 있지 않은 예약을 수정하면 예외가 발생한다.")
    void update_nonExistentReservation_throwsNotFoundException() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        // when & then
        assertThatThrownBy(
                () -> reservationRepository.update(
                        new Reservation(
                                999L,
                                "브라운",
                                new ReservationSlot(LocalDate.of(2024, 5, 1), time, theme)
                        )
                )
        ).isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("수정하려는 예약이 이미 존재하면 예외가 발생한다.")
    void update_duplicateReservation_throwsDataIntegrityViolation() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        new ReservationSlot(LocalDate.now().plusDays(5), time, theme)
                )
        );

        Reservation saved = reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        new ReservationSlot(LocalDate.now().plusDays(1), time, theme)
                )
        );

        Reservation updated = saved.update(LocalDate.now().plusDays(5), time, "브라운");

        // when & then
        assertThatThrownBy(
                () -> reservationRepository.update(updated)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("id에 해당하는 예외를 조회한다.")
    void findById() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        Reservation saved = saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);

        // when & then
        assertThat(reservationRepository.findById(saved.getId())).isPresent();
        assertThat(reservationRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        Reservation saved1 = saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);
        Reservation saved2 = saveReservation("브라운", LocalDate.of(2024, 5, 2), time, theme);
        Reservation saved3 = saveReservation("포피", LocalDate.of(2024, 5, 3), time, theme);

        // when
        List<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations).hasSize(3);
        assertThat(reservations).containsExactly(saved1, saved2, saved3);
    }

    @Test
    @DisplayName("이름에 해당하는 모든 예약 목록을 조회한다.")
    void findAllByName() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        Reservation saved1 = saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);
        Reservation saved2 = saveReservation("브라운", LocalDate.of(2024, 5, 2), time, theme);
        saveReservation("포피", LocalDate.of(2024, 5, 3), time, theme);

        // when
        List<Reservation> reservations = reservationRepository.findAllByName("브라운");

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).containsExactly(saved1, saved2);
    }

    @Test
    @DisplayName("이름에 해당하는 예약과 대기를 상태·순번과 함께 조회한다.")
    void findAllByNameWithStatus_reservedAndWaiting() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);
        saveWaiting("코니", LocalDate.of(2024, 5, 1), time, theme);   // 1번째 대기
        saveWaiting("브라운", LocalDate.of(2024, 5, 1), time, theme); // 2번째 대기 ← 브라운

        // when
        List<ReservationWithStatusResult> result =
                reservationRepository.findAllByNameWithStatus("브라운");

        // then
        assertThat(result).hasSize(2);

        ReservationWithStatusResult reserved = result.stream()
                .filter(r -> r.status().equals("reserved"))
                .findFirst().orElseThrow();
        ReservationWithStatusResult waiting = result.stream()
                .filter(r -> r.status().equals("waiting"))
                .findFirst().orElseThrow();

        assertThat(reserved.waitingOrder()).isEqualTo(0L);
        assertThat(waiting.waitingOrder()).isEqualTo(2L); // 코니(1)보다 늦게 등록했으므로 2번째
    }

    @Test
    @DisplayName("대기가 1번째인 경우 waitingOrder는 1이다.")
    void findAllByNameWithStatus_firstWaiting() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);
        saveWaiting("코니", LocalDate.of(2024, 5, 1), time, theme); // 1번째 대기

        // when
        List<ReservationWithStatusResult> result =
                reservationRepository.findAllByNameWithStatus("코니");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("waiting");
        assertThat(result.getFirst().waitingOrder()).isEqualTo(1L);
    }

    @Test
    @DisplayName("from과 to 사이 일정의 예약들에 대해, 상위 limit 개의 테마들을 조회한다.")
    void findPopularThemes_dateRangeAndLimit_returnsOrderedThemes() {
        // given
        Theme woowaTheme = createTheme("우테코", "우테코 전용 테마", "https://example.com");
        Theme pairTheme = createTheme("페어", "페어 전용 테마", "https://pair.com");
        Theme carrotTheme = createTheme("당근", "당근 전용 테마", "https://carrot.com");

        ReservationTime time = createTime(LocalTime.of(10, 0));

        LocalDate today = LocalDate.now(
                Clock.fixed(
                        Instant.parse("2026-05-06T00:00:00Z"),
                        ZoneId.of("Asia/Seoul")
                )
        );

        saveReservation("브라운", today.minusDays(1), time, woowaTheme);
        saveReservation("포비", today.minusDays(2), time, woowaTheme);
        saveReservation("제이슨", today.minusDays(3), time, woowaTheme);
        saveReservation("이든", today.minusDays(1), time, pairTheme);
        saveReservation("레아", today.minusDays(2), time, pairTheme);
        saveReservation("웨지", today.minusDays(1), time, carrotTheme);
        saveReservation("오늘예약", today, time, carrotTheme);
        saveReservation("범위밖예약", today.minusDays(8), time, carrotTheme);

        // when
        List<PopularThemeQueryResult> popularThemes = reservationRepository.findPopularThemes(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                2
        );

        // then
        assertThat(popularThemes)
                .extracting(PopularThemeQueryResult::name)
                .containsExactly("우테코", "페어");
    }

    private ReservationTime createTime(LocalTime time) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(time)
        );

        long timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class,
                Time.valueOf(time)
        );

        return new ReservationTime(timeId, time);
    }

    private Theme createTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, description, thumbnailUrl
        );

        Long themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                name
        );

        return new Theme(themeId, name, description, thumbnailUrl);
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(
                new Reservation(null, name, new ReservationSlot(date, time, theme))
        );
    }

    @Test
    @DisplayName("date, themeId, timeId를 기준으로 예약을 조회한다.")
    void findByDateAndTimeIdAndThemeId_existingReservation_returnsPresent() {
        //given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        Reservation saved = saveReservation("브라운", LocalDate.of(2024, 5, 1), time, theme);

        //when
        Optional<Reservation> result = reservationRepository.findByDateAndTimeIdAndThemeId(
                saved.getDate(),
                saved.getTime().getId(),
                saved.getTheme().getId()
        );

        //then
        assertThat(result).isPresent();
    }

    private void saveWaiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name,
                java.sql.Date.valueOf(date),
                time.getId(),
                theme.getId()
        );
    }
}
