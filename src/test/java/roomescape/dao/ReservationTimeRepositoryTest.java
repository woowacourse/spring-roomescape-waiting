package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.ReservationTime;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void findAll_전체_시간_조회() {
        assertThat(reservationTimeRepository.findAll()).isNotEmpty();
    }

    @Test
    void findById_존재하는_id이면_반환() {
        Optional<ReservationTime> result = reservationTimeRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void findById_존재하지_않는_id이면_empty() {
        Optional<ReservationTime> result = reservationTimeRepository.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByStartAt_존재하면_true() {
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
    }

    @Test
    void existsByStartAt_없으면_false() {
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(23, 0))).isFalse();
    }

    @Test
    void save_시간_저장() {
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(23, 0)));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(23, 0));
    }

    @Test
    void delete_시간_삭제() {
        reservationTimeRepository.deleteById(12L);

        assertThat(reservationTimeRepository.findById(12L)).isEmpty();
    }
}
