package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import roomescape.domain.Schedule;
import roomescape.repository.ScheduleDao;

@JdbcTest
@Import(ScheduleDao.class)
class ScheduleDaoTest {

    @Autowired
    private ScheduleDao scheduleDao;

    @DisplayName("스케줄을 저장하면 생성된 ID를 반환한다.")
    @Test
    void 스케줄_저장() {
        Long id = scheduleDao.save(LocalDate.of(2026, 7, 1), 1L, 1L);

        assertThat(id).isNotNull().isPositive();
    }

    @DisplayName("ID로 스케줄을 조회하면 날짜·시간·테마 정보가 포함된다.")
    @Test
    void ID로_스케줄_조회() {
        // data.sql: schedule ID 1 = 2026-04-29 / time_id=3(12:00) / theme_id=1
        Schedule schedule = scheduleDao.findById(1L);

        assertThat(schedule.getId()).isEqualTo(1L);
        assertThat(schedule.getDate()).isEqualTo(LocalDate.of(2026, 4, 29));
        assertThat(schedule.getTime().getStartAt()).isEqualTo(LocalTime.of(12, 0));
        assertThat(schedule.getTheme().getId()).isEqualTo(1L);
    }

    @DisplayName("날짜·시간·테마로 스케줄 ID를 조회한다.")
    @Test
    void 날짜_시간_테마로_스케줄_ID_조회() {
        Optional<Long> scheduleId = scheduleDao.findIdByDateAndTimeIdAndThemeId(
                LocalDate.of(2026, 4, 29), 3L, 1L);

        assertThat(scheduleId).isPresent().hasValue(1L);
    }

    @DisplayName("존재하지 않는 조건이면 빈 Optional을 반환한다.")
    @Test
    void 없는_스케줄_조회_시_빈_Optional() {
        Optional<Long> scheduleId = scheduleDao.findIdByDateAndTimeIdAndThemeId(
                LocalDate.of(2099, 12, 31), 1L, 1L);

        assertThat(scheduleId).isEmpty();
    }

    @DisplayName("전체 스케줄 목록을 조회하면 초기 데이터 22건이 반환된다.")
    @Test
    void 전체_스케줄_조회() {
        List<Schedule> schedules = scheduleDao.findAll();

        assertThat(schedules).hasSize(22);
    }
}
