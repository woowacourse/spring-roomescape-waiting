package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import roomescape.domain.TimeSlot;
import roomescape.fixture.TimeSlotFixtures;

@DataJpaTest
class TimeSlotRepositoryTest {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @DisplayName("시작 시간이 존재하는지 판별한다.")
    @Test
    void existsByStartAt() {
        LocalTime startAt = LocalTime.now();
        TimeSlot time = TimeSlotFixtures.createTimeSlot(startAt);
        timeSlotRepository.save(time);

        boolean result = timeSlotRepository.existsByStartAt(startAt);

        assertThat(result).isTrue();
    }

    @DisplayName("id로 시간 정보를 조회한다.")
    @Test
    void getTimeById() {
        LocalTime startAt = LocalTime.now();
        TimeSlot time = TimeSlotFixtures.createTimeSlot(startAt);
        TimeSlot savedTime = timeSlotRepository.save(time);

        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(savedTime.getId());
        assertThat(timeSlot.getStartAt()).isEqualTo(startAt);
    }

    @DisplayName("id가 존재하지 않는다면 예외가 발생한다.")
    @Test
    void getTimeByIdWhenNotExist() {
        assertThatThrownBy(() -> timeSlotRepository.getTimeSlotById(1L))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 시간 입니다");
    }
}
