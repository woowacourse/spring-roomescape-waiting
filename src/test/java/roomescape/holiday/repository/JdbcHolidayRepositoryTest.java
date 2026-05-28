package roomescape.holiday.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import roomescape.holiday.domain.Holiday;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcHolidayRepositoryTest {

    @Autowired
    private JdbcHolidayRepository holidayRepository;

    @DisplayName("JdbcHolidayRepository가 null이 아니다.")
    @Test
    public void JdbcHolidayIsNotNull() {
        assertThat(holidayRepository).isNotNull();
    }

    @DisplayName("휴일을 저장한다.")
    @Test
    void save() {
        Holiday holiday = new Holiday(1L, LocalDate.of(2026, 5, 6));
        Holiday saved = holidayRepository.save(holiday);

        assertThat(saved.id()).isEqualTo(1L);
        assertThat(saved.date()).isEqualTo(LocalDate.of(2026, 5, 6));
    }

    @DisplayName("전체 휴일 목록을 조회한다.")
    @Test
    void findAll() {
        List<Holiday> holidays = List.of(new Holiday(1L, LocalDate.of(2026, 5, 6)),
                new Holiday(2L, LocalDate.of(2026, 6, 6)),
                new Holiday(3L, LocalDate.of(2026, 7, 6)));

        for (Holiday each: holidays) {
            holidayRepository.save(each);
        }

        List<Holiday> results = holidayRepository.findAll();

        assertThat(results).hasSize(3);
        assertThat(results.getFirst().id()).isEqualTo(1L);
        assertThat(results.getFirst().date()).isEqualTo(LocalDate.of(2026, 5, 6));
    }

    @DisplayName("id로 휴일을 삭제한다.")
    @Test
    void delete() {
        Holiday holiday = new Holiday(1L, LocalDate.of(2026, 5, 6));
        holidayRepository.save(holiday);
        assertThat(holidayRepository.deleteById(1L)).isTrue();
        assertThat(holidayRepository.findAll()).isEmpty();
    }

    @DisplayName("날짜로 휴일 존재 여부를 확인한다.")
    @Test
    void existsByDate() {
        LocalDate date = LocalDate.of(2026, 5, 6);
        assertThat(holidayRepository.existsByDate(date)).isFalse();

        holidayRepository.save(new Holiday(date));
        assertThat(holidayRepository.existsByDate(date)).isTrue();
        assertThat(holidayRepository.existsByDate(date.plusDays(1))).isFalse();
    }
}
