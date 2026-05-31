package roomescape.holiday.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.holiday.domain.Holiday;

@JdbcTest
@Import(JdbcHolidayRepository.class)
class JdbcHolidayRepositoryTest {

    @Autowired
    private JdbcHolidayRepository holidayRepository;

    @DisplayName("휴일을 저장한다.")
    @Test
    void save() {
        // when
        Holiday saved = holidayRepository.save(new Holiday(LocalDate.of(2026, 5, 6)));

        // then
        assertThat(saved.id()).isNotNull();
        assertThat(saved.date()).isEqualTo(LocalDate.of(2026, 5, 6));
    }

    @DisplayName("전체 휴일 목록을 조회한다.")
    @Test
    void findAll() {
        // given
        holidayRepository.save(new Holiday(LocalDate.of(2026, 5, 6)));
        holidayRepository.save(new Holiday(LocalDate.of(2026, 6, 6)));
        holidayRepository.save(new Holiday(LocalDate.of(2026, 7, 6)));

        // when
        List<Holiday> results = holidayRepository.findAll();

        // then
        assertThat(results).hasSize(3);
        assertThat(results.getFirst().date()).isEqualTo(LocalDate.of(2026, 5, 6));
    }

    @DisplayName("id로 휴일을 삭제한다.")
    @Test
    void delete() {
        // given
        Holiday saved = holidayRepository.save(new Holiday(LocalDate.of(2026, 5, 6)));

        // when & then
        assertThat(holidayRepository.deleteById(saved.id())).isTrue();
        assertThat(holidayRepository.findAll()).isEmpty();
        assertThat(holidayRepository.deleteById(saved.id())).isFalse();
    }

    @DisplayName("날짜로 휴일 존재 여부를 확인한다.")
    @Test
    void existsByDate() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 6);
        holidayRepository.save(new Holiday(date));

        // when & then
        assertThat(holidayRepository.existsByDate(date)).isTrue();
        assertThat(holidayRepository.existsByDate(date.plusDays(1))).isFalse();
    }
}