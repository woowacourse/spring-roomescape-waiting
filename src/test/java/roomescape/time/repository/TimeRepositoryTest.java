package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import roomescape.time.domain.Time;

@DataJpaTest
class TimeRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TimeRepository timeRepository;

    @Test
    @DisplayName("성공 : 시간 정보가 DB에 저장할 수 있다.")
    void save() {
        Time time = new Time(LocalTime.of(12, 0));
        Time actualSavedTime = timeRepository.save(time);

        List<Time> expected = timeRepository.findAll();
        assertThat(actualSavedTime).isEqualTo(expected.get(0));
    }

    @Test
    @DisplayName("성공 : 시간 정보들을 조회할 수 있다.")
    void findAllByOrderByStartAtAsc() {
        entityManager.persist(new Time(LocalTime.of(13, 0)));
        entityManager.persist(new Time(LocalTime.of(14, 0)));
        entityManager.persist(new Time(LocalTime.of(15, 0)));

        List<Time> times = timeRepository.findAllByOrderByStartAtAsc();

        assertThat(times).hasSize(3);
    }

    @Test
    @DisplayName("성공 : 해당 시간이 존재할 경우 true를 반환한다.")
    void countByStartAt_true() {
        entityManager.persist(new Time(LocalTime.of(13, 0)));

        boolean actual = timeRepository.existsByStartAt(LocalTime.of(13, 0));

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("성공 : 해당 시간이 존재할 경우 false를 반환한다.")
    void countByStartAt_false() {
        entityManager.persist(new Time(LocalTime.of(13, 0)));

        boolean actual = timeRepository.existsByStartAt(LocalTime.of(12, 0));

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("성공 : 시간 정보를 삭제할 수 있다.")
    void deleteTime() {
        entityManager.persist(new Time(LocalTime.of(10, 0)));

        timeRepository.deleteById(1L);

        assertThat(timeRepository.findById(1L)).isEmpty();
    }
}
