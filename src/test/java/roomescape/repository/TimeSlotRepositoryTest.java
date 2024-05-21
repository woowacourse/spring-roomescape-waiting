package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TimeSlotRepositoryTest {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @DisplayName("해당 startAt과 동일한 시간대가 존재한다면 true를 반환한다.")
    @Test
    void existsByStartAt_isTrue() {
        //given
        LocalTime alreadyExistingTime = LocalTime.of(0, 0);

        //when
        boolean isTimeSlotExists_true = timeSlotRepository.existsByStartAt(alreadyExistingTime);

        //then
        assertThat(isTimeSlotExists_true).isTrue();
    }

    @DisplayName("해당 startAt과 동일한 시간대가 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByStartAt_isFalse() {
        //given
        LocalTime notExistingTime = LocalTime.of(23, 59);

        //when
        boolean isTimeSlotExists_false = timeSlotRepository.existsByStartAt(notExistingTime);

        //then
        assertThat(isTimeSlotExists_false).isFalse();
    }
}
