package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalTime;
import java.util.List;

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
    @DisplayName("시간 데이터들이 잘 저장되는지 확인.")
    void saveTime() {
        Time time = new Time(LocalTime.of(12, 0));
        Time actualSavedTime = timeRepository.save(time);

        Iterable<Time> expected = timeRepository.findAll();
        assertThat(actualSavedTime).isEqualTo(expected.iterator()
                .next());
    }

    @Test
    @DisplayName("시간 데이터들을 잘 가져오는지 확인.")
    void getTimes() {
        entityManager.merge(new Time(LocalTime.of(13, 0)));
        entityManager.merge(new Time(LocalTime.of(14, 0)));
        entityManager.merge(new Time(LocalTime.of(15, 0)));

        List<Time> times = timeRepository.findAllByOrderByStartAtAsc();

        assertThat(times).hasSize(3);
    }

//    @Test
//    @DisplayName("시간 데이터들의 연관관계가 없다면 잘 지우는지 확인")
//    void deleteTime() {
//        timeRepository.deleteById(3L);
//
//        Assertions.assertThat(timeRepository.findAllByOrderByStartAtAsc()
//                                      .size())
//                .isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("시간 데이터들의 연관관계가 있다면 에러가 나는지 확인")
//    void canNotDeleteTime() {
//        Assertions.assertThatThrownBy(() -> timeRepository.deleteById(1L))
//                .isInstanceOf(DataAccessException.class);
//    }

}
