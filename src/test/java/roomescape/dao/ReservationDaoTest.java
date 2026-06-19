package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.repository.ReservationDao;
import roomescape.service.dto.ReservationWithWaitingOrder;

@JdbcTest
@Import(ReservationDao.class)
class ReservationDaoTest {

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("예약을 저장하고 ID로 조회하면 예약자, 스케줄, 상태를 함께 매핑한다.")
    @Test
    void saveAndFindById() {
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long themeId = insertTheme("잠긴 방");
        Long scheduleId = insertSchedule(LocalDate.of(2026, 7, 1), timeId, themeId);
        Schedule schedule = new Schedule(
                scheduleId,
                new Theme(themeId, "잠긴 방", "닫힌 문을 여는 테마", "https://example.com/theme.jpg", 20000),
                LocalDate.of(2026, 7, 1),
                new ReservationTime(timeId, LocalTime.of(10, 0))
        );
        Member member = insertMember("러로");

        Long reservationId = reservationDao.save(new Reservation(
                null,
                member,
                schedule,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 12, 0)
        ));

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();

        assertThat(reservation.getId()).isEqualTo(reservationId);
        assertThat(reservation.getMember().getName()).isEqualTo("러로");
        assertThat(reservation.getSchedule().getId()).isEqualTo(scheduleId);
        assertThat(reservation.getSchedule().getTheme().getName()).isEqualTo("잠긴 방");
        assertThat(reservation.getSchedule().getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @DisplayName("예약 ID로 스케줄 ID를 조회한다.")
    @Test
    void findScheduleIdById() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(11, 0), "우주선");
        Long reservationId = insertReservation("러로", scheduleId, ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0));

        assertThat(reservationDao.findScheduleIdById(reservationId)).contains(scheduleId);
        assertThat(reservationDao.findScheduleIdById(999L)).isEmpty();
    }

    @DisplayName("상태만 변경하면 신청 순서 기준 시각은 유지된다.")
    @Test
    void changeStatusOnly() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(12, 0), "마법 학교");
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
        Long reservationId = insertReservation("러로", scheduleId, ReservationStatus.WAITING, originalUpdatedAt);

        reservationDao.promoteToReserved(reservationId);

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getUpdateAt()).isEqualTo(originalUpdatedAt);
    }

    @DisplayName("WAITING 상태가 아니면 상태만 변경하지 않는다.")
    @Test
    void changeStatusOnlyWhenWaiting() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(12, 0), "마법 학교");
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
        Long reservationId = insertReservation("러로", scheduleId, ReservationStatus.CANCELED, originalUpdatedAt);

        reservationDao.promoteToReserved(reservationId);

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(reservation.getUpdateAt()).isEqualTo(originalUpdatedAt);
    }

    @DisplayName("상태와 수정 시각을 함께 변경할 수 있다.")
    @Test
    void changeStatusWithUpdateAt() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(13, 0), "탐정 사무소");
        Long reservationId = insertReservation("러로", scheduleId, ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0));
        LocalDateTime canceledAt = LocalDateTime.of(2026, 6, 1, 11, 0);

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();
        reservationDao.changeStatusWithUpdateAt(new Reservation(
                reservation.getId(),
                reservation.getMember(),
                reservation.getSchedule(),
                ReservationStatus.CANCELED,
                canceledAt
        ));

        Reservation updated = reservationDao.findById(reservationId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(updated.getUpdateAt()).isEqualTo(canceledAt);
    }

    @DisplayName("이미 같은 상태이면 수정 시각을 덮어쓰지 않는다.")
    @Test
    void changeStatusWithUpdateAtSameStatus() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(13, 0), "탐정 사무소");
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
        Long reservationId = insertReservation("러로", scheduleId, ReservationStatus.CANCELED, originalUpdatedAt);

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();
        reservationDao.changeStatusWithUpdateAt(new Reservation(
                reservation.getId(),
                reservation.getMember(),
                reservation.getSchedule(),
                ReservationStatus.CANCELED,
                LocalDateTime.of(2026, 6, 1, 11, 0)
        ));

        Reservation updated = reservationDao.findById(reservationId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(updated.getUpdateAt()).isEqualTo(originalUpdatedAt);
    }

    @DisplayName("특정 스케줄에서 가장 먼저 신청한 대기 예약을 조회한다.")
    @Test
    void findFirstByScheduleIdAndStatus() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(14, 0), "고대 유적");
        insertReservation("확정자", scheduleId, ReservationStatus.RESERVED, LocalDateTime.of(2026, 6, 1, 10, 0));
        Long firstWaitingId = insertReservation("첫대기", scheduleId, ReservationStatus.WAITING,
                LocalDateTime.of(2026, 6, 1, 10, 1));
        insertReservation("둘째대기", scheduleId, ReservationStatus.WAITING,
                LocalDateTime.of(2026, 6, 1, 10, 2));

        Reservation waiting = reservationDao.findFirstByScheduleIdAndStatus(scheduleId, ReservationStatus.WAITING)
                .orElseThrow();

        assertThat(waiting.getId()).isEqualTo(firstWaitingId);
        assertThat(waiting.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @DisplayName("회원 ID로 예약을 조회하면 예약 정보와 대기 순번을 함께 반환한다.")
    @Test
    void findByMemberIdWithOrder() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(15, 0), "비밀 연구소");
        Member reservedMember = insertMember("확정자");
        Member waitingMember = insertMember("대기자");
        insertReservation(reservedMember, scheduleId, ReservationStatus.RESERVED, LocalDateTime.of(2026, 6, 1, 10, 0));
        Long waitingId = insertReservation(waitingMember, scheduleId, ReservationStatus.WAITING,
                LocalDateTime.of(2026, 6, 1, 10, 1));

        List<ReservationWithWaitingOrder> results = reservationDao.findByMemberId(waitingMember.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).reservation().getId()).isEqualTo(waitingId);
        assertThat(results.get(0).reservation().getMember().getName()).isEqualTo("대기자");
        assertThat(results.get(0).order()).isEqualTo(1);
    }

    @DisplayName("전체 예약을 조회하면 취소 예약을 제외한 순번을 함께 반환한다.")
    @Test
    void findAllWithOrder() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(15, 0), "비밀 연구소");
        insertReservation("확정자", scheduleId, ReservationStatus.RESERVED, LocalDateTime.of(2026, 6, 1, 10, 0));
        insertReservation("취소자", scheduleId, ReservationStatus.CANCELED, LocalDateTime.of(2026, 6, 1, 10, 1));
        Long waitingId = insertReservation("대기자", scheduleId, ReservationStatus.WAITING,
                LocalDateTime.of(2026, 6, 1, 10, 2));

        List<ReservationWithWaitingOrder> results = reservationDao.findAll();

        assertThat(results)
                .filteredOn(result -> result.reservation().getId().equals(waitingId))
                .singleElement()
                .extracting(ReservationWithWaitingOrder::order)
                .isEqualTo(1);
    }

    @DisplayName("같은 회원과 스케줄의 취소되지 않은 예약 존재 여부를 조회한다.")
    @Test
    void existByMemberIdAndScheduleId() {
        Long scheduleId = insertScheduleWithDependencies(LocalDate.of(2026, 7, 1), LocalTime.of(16, 0), "잠수함");
        Member member = insertMember("러로");
        insertReservation(member, scheduleId, ReservationStatus.CANCELED, LocalDateTime.of(2026, 6, 1, 10, 0));

        assertThat(reservationDao.existByMemberIdAndScheduleId(member.getId(), scheduleId)).isFalse();

        insertReservation(member, scheduleId, ReservationStatus.WAITING, LocalDateTime.of(2026, 6, 1, 10, 1));

        assertThat(reservationDao.existByMemberIdAndScheduleId(member.getId(), scheduleId)).isTrue();
    }

    private Long insertScheduleWithDependencies(LocalDate date, LocalTime startAt, String themeName) {
        Long timeId = insertReservationTime(startAt);
        Long themeId = insertTheme(themeName);
        return insertSchedule(date, timeId, themeId);
    }

    private Long insertReservationTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                name,
                name + " 설명",
                "https://example.com/" + name + ".jpg",
                20000
        );
        return jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private Long insertSchedule(LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update("INSERT INTO schedule (date, time_id, theme_id) VALUES (?, ?, ?)", date, timeId, themeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM schedule WHERE date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                timeId,
                themeId
        );
    }

    private Member insertMember(String name) {
        String loginId = name + "-" + System.nanoTime();
        jdbcTemplate.update(
                "INSERT INTO users (login_id, name, password, role) VALUES (?, ?, ?, ?)",
                loginId,
                name,
                "password",
                Role.USER.name()
        );
        Long id = jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Long.class, loginId);
        return new Member(id, loginId, name, "password", Role.USER);
    }

    private Long insertReservation(
            String name,
            Long scheduleId,
            ReservationStatus status,
            LocalDateTime updatedAt
    ) {
        return insertReservation(insertMember(name), scheduleId, status, updatedAt);
    }

    private Long insertReservation(
            Member member,
            Long scheduleId,
            ReservationStatus status,
            LocalDateTime updatedAt
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation (user_id, schedule_id, status, updated_at) VALUES (?, ?, ?, ?)",
                member.getId(),
                scheduleId,
                status.name(),
                updatedAt
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE user_id = ? AND schedule_id = ? AND updated_at = ?",
                Long.class,
                member.getId(),
                scheduleId,
                updatedAt
        );
    }
}
