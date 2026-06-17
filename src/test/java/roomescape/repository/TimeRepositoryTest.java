package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Time;

@DataJpaTest
class TimeRepositoryTest {

    @Autowired
    private TimeRepository timeRepository;

    @Test
    @DisplayName("예약 시간을 저장하고 영속화된 객체를 반환한다.")
    void save() {
        Time time = Time.of(LocalTime.of(10, 0));
        Time savedTime = timeRepository.save(time);
        assertThat(savedTime.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 시간 객체를 조회한다.")
    void findById() {
        Time savedTime = timeRepository.save(Time.of(LocalTime.of(10, 0)));
        Time foundTime = timeRepository.findById(savedTime.getId()).get();
        assertThat(foundTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("모든 예약 시간 객체 목록을 조회한다.")
    void findAll() {
        timeRepository.save(Time.of(LocalTime.of(10, 0)));
        List<Time> times = timeRepository.findAll();
        assertThat(times).hasSize(1);
    }

    @Test
    @DisplayName("식별자로 예약 시간을 삭제한다.")
    void deleteById() {
        Time savedTime = timeRepository.save(Time.of(LocalTime.of(10, 0)));
        timeRepository.deleteById(savedTime.getId());
        assertThat(timeRepository.findAll()).isEmpty();
    }
}
