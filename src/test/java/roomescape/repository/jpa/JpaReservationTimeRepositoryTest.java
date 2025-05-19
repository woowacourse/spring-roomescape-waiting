package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.entity.ReservationTime;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JpaReservationTimeRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Test
    @DisplayName("해당 시간이 있다면 true를 반환한다.")
    void existTimeByStartAt() {
        LocalTime time = LocalTime.of(10, 0);

        jpaReservationTimeRepository.save(new ReservationTime(time));

        assertThat(jpaReservationTimeRepository.existsByStartAt(time)).isTrue();
    }

    @Test
    @DisplayName("해당 시간이 없다면 false를 반환한다.")
    void notExistTimeByStartAt() {
        LocalTime time = LocalTime.of(11, 0);

        assertThat(jpaReservationTimeRepository.existsByStartAt(time)).isFalse();
    }
}
