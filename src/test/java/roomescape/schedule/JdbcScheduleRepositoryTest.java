package roomescape.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.schedule.infrastructure.JdbcScheduleRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Transactional
@ActiveProfiles("test")
@Import(JdbcScheduleRepository.class)
class JdbcScheduleRepositoryTest {

    @Autowired
    private JdbcScheduleRepository repository;

    @Test
    void 스케줄_저장_레포지토리_테스트() {
        Schedule schedule = new Schedule(null, LocalDate.of(2026, 5, 7), 1L, 2L);

        Schedule savedSchedule = repository.save(schedule);

        assertThat(savedSchedule.getId()).isNotNull();
        assertThat(savedSchedule.getDate()).isEqualTo(LocalDate.of(2026, 5, 7));
        assertThat(savedSchedule.getTimeId()).isEqualTo(1L);
        assertThat(savedSchedule.getThemeId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("같은 날짜, 시간, 테마의 스케줄은 중복 저장할 수 없다.")
    void save_중복스케줄_DB제약_테스트() {
        Schedule duplicatedSchedule = new Schedule(null, LocalDate.of(2026, 5, 5), 1L, 1L);

        assertThatThrownBy(() -> repository.save(duplicatedSchedule))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 스케줄_조회_레포지토리_테스트() {
        Optional<Schedule> schedule = repository.findById(1L);

        assertThat(schedule).isPresent();
        assertThat(schedule.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(schedule.get().getTimeId()).isEqualTo(1L);
        assertThat(schedule.get().getThemeId()).isEqualTo(1L);
    }

    @Test
    void 스케줄_전체_조회_레포지토리_테스트() {
        List<Schedule> schedules = repository.findAll();

        assertThat(schedules).hasSize(6);
    }

    @Test
    void 스케줄_삭제_레포지토리_테스트() {
        Schedule savedSchedule = repository.save(new Schedule(null, LocalDate.of(2026, 5, 8), 3L, 4L));

        repository.deleteById(savedSchedule.getId());

        assertThat(repository.findById(savedSchedule.getId())).isEmpty();
    }

    @Test
    @DisplayName("날짜, 시간id, 테마id을 가진 스케줄id를 찾을 수 있다.")
    void findScheduleIdByDateAndTimeIdAndThemeId_레포지토리_테스트() {
        long scheduleId = repository.findScheduleIdByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 5), 1L, 1L)
                .orElseThrow();

        assertThat(repository.findById(scheduleId)).isPresent();
    }

    @Test
    @DisplayName("이미 존재하는 스케줄이면 true를 반환한다.")
    void existsAlreadySchedule_테스트_1() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        long themeId = 1L;
        long timeId = 1L;

        // when
        boolean result = repository.existsAlreadySchedule(date, themeId, timeId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 스케줄이면 false를 반환한다.")
    void existsAlreadySchedule_테스트_2() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        long themeId = 1L;
        long timeId = 99L;

        // when
        boolean result = repository.existsAlreadySchedule(date, themeId, timeId);

        // then
        assertThat(result).isFalse();
    }
}
