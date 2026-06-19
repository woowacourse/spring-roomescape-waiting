package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.exception.ReservationWaitingErrorCode;

@JdbcTest
class ReservationWaitingRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    public ReservationWaitingRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationWaitingRepository = new ReservationWaitingRepository(
                new ReservationWaitingDao(jdbcTemplate)
        );
    }

    @Test
    @DisplayName("예약 대기를 정상적으로 저장하고 반환된 객체의 ID를 확인한다.")
    void save_validWaiting_returnsWithId() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        // when
        ReservationWaiting saved1 = reservationWaitingRepository.save(
                new ReservationWaiting(null, "브라운", new ReservationSlot(LocalDate.of(2026, 5, 1), time, theme),
                        LocalDate.of(2026, 5, 1).atStartOfDay())
        );

        ReservationWaiting saved2 = reservationWaitingRepository.save(
                new ReservationWaiting(null, "포비", new ReservationSlot(LocalDate.of(2026, 5, 1), time, theme),
                        LocalDate.of(2026, 5, 1).atStartOfDay())
        );

        // then
        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
    }

    @Test
    @DisplayName("이미 동일한 예약 대기(동일 날짜, 시간, 테마, 이름, 삭제여부)가 있으면 ConflictException이 발생한다.")
    void save_duplicateWaiting_throwsConflictException() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        reservationWaitingRepository.save(
                new ReservationWaiting(null, "브라운", new ReservationSlot(LocalDate.of(2026, 5, 1), time, theme),
                        LocalDate.of(2026, 5, 1).atStartOfDay())
        );

        // when & then
        assertThatThrownBy(() -> reservationWaitingRepository.save(
                new ReservationWaiting(null, "브라운", new ReservationSlot(LocalDate.of(2026, 5, 1), time, theme),
                        LocalDate.of(2026, 5, 1).atStartOfDay())
        )).isInstanceOf(ConflictException.class)
                .hasMessage(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 시간 ID를 참조하여 예약 대기를 저장하면 외래키 제약조건 위반으로 예외가 발생한다.")
    void save_nonExistentTimeId_throwsConflictException() {
        // given
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");
        ReservationTime nonExistentTime = new ReservationTime(9999L, LocalTime.of(10, 0));

        // when & then
        assertThatThrownBy(() -> reservationWaitingRepository.save(
                new ReservationWaiting(null, "브라운",
                        new ReservationSlot(LocalDate.of(2026, 5, 1), nonExistentTime, theme),
                        LocalDate.of(2026, 5, 1).atStartOfDay())
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID를 참조하여 예약 대기를 저장하면 외래키 제약조건 위반으로 예외가 발생한다.")
    void save_nonExistentThemeId_throwsConflictException() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme nonExistentTheme = new Theme(9999L, "없는테마", "설명", "url");

        // when & then
        assertThatThrownBy(() -> reservationWaitingRepository.save(
                new ReservationWaiting(null, "브라운",
                        new ReservationSlot(LocalDate.of(2026, 5, 1), time, nonExistentTheme),
                        LocalDate.of(2026, 5, 1).atStartOfDay())
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("아이디를 기반으로 저장된 예약 대기 건을 정상적으로 찾아온다.")
    void findById_existingWaiting_returnsWaiting() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");
        ReservationWaiting saved = saveReservationWaiting("브라운", LocalDate.of(2026, 5, 1), time, theme);

        // when
        Optional<ReservationWaiting> found = reservationWaitingRepository.findById(saved.getId());

        // then
        assertTrue(found.isPresent());
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기 ID로 조회하면 Optional.empty()를 반환한다.")
    void findById_nonExistentId_returnsEmpty() {
        // when
        Optional<ReservationWaiting> found = reservationWaitingRepository.findById(9999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이름을 기반으로 해당 사용자의 예약 대기 내역을 모두 조회한다.")
    void findAllByName_returnsWaitingsForGivenName() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        ReservationWaiting saved1 = saveReservationWaiting("브라운", LocalDate.of(2026, 5, 1), time, theme);
        saveReservationWaiting("검프", LocalDate.of(2026, 5, 1), time, theme);

        // when
        List<ReservationWaiting> result = reservationWaitingRepository.findAllByName("브라운");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(saved1.getId());
    }

    @Test
    @DisplayName("예약 대기 엔티티를 전달하여 삭제한다.")
    void delete_existingWaiting_removesWaiting() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");
        ReservationWaiting saved = saveReservationWaiting("브라운", LocalDate.of(2026, 5, 1), time, theme);

        // when
        reservationWaitingRepository.delete(saved);

        // then
        Optional<ReservationWaiting> found = reservationWaitingRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("동일 날짜, 시간, 예약자 이름에 해당하는 예약 대기가 존재하는지 여부를 확인한다.")
    void hasWaitingAtSameTime_returnsTrueIfSameUserHasWaiting() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");
        saveReservationWaiting("브라운", LocalDate.of(2026, 5, 1), time, theme);

        ReservationWaiting target1 = new ReservationWaiting(null, "브라운",
                new ReservationSlot(LocalDate.of(2026, 5, 1), time, theme), LocalDate.of(2026, 5, 1).atStartOfDay());
        ReservationWaiting target2 = new ReservationWaiting(null, "포비",
                new ReservationSlot(LocalDate.of(2026, 5, 1), time, theme), LocalDate.of(2026, 5, 1).atStartOfDay());

        // when & then
        assertThat(reservationWaitingRepository.hasWaitingAtSameTime(target1.getName(), target1.getSlot())).isTrue();
        assertThat(reservationWaitingRepository.hasWaitingAtSameTime(target2.getName(), target2.getSlot())).isFalse();
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마의 모든 대기 리스트를 ID 순으로 정렬하여 조회한다.")
    void queryAllBySlotForUpdate_returnsWaitingsInOrder() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        ReservationWaiting saved1 = saveReservationWaiting("브라운", LocalDate.of(2026, 5, 1), time, theme);
        ReservationWaiting saved2 = saveReservationWaiting("포비", LocalDate.of(2026, 5, 1), time, theme);

        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 5, 1), time, theme);

        // when
        List<ReservationWaiting> result = reservationWaitingRepository.queryAllBySlotForUpdate(slot);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(result.get(1).getId()).isEqualTo(saved2.getId());
    }

    // Helper Methods
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

    private ReservationWaiting saveReservationWaiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationWaitingRepository.save(
                new ReservationWaiting(null, name, new ReservationSlot(date, time, theme), date.atStartOfDay())
        );
    }
}
