package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.JpaConfig;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.repository.impl.ReservationTimeRepositoryImpl;
import roomescape.repository.jpa.ReservationTimeJpaRepository;

@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaConfig.class)
@DataJpaTest
class ReservationTimeRepositoryTest {

    ReservationTimeRepository timeRepository;

    @Autowired
    ReservationTimeJpaRepository reservationTimeJpaRepository;

    private Long timeId;
    private LocalTime now;

    @BeforeEach
    void setUp() {
        timeRepository = new ReservationTimeRepositoryImpl(reservationTimeJpaRepository);

        now = LocalTime.now();
        timeId = timeRepository.save(new ReservationTime(now)).getId();
    }

    @DisplayName("id로 예약 시간을 조회한다.")
    @Test
    void findById() {
        // when
        Optional<ReservationTime> time = timeRepository.findById(timeId);

        // then
        assertAll(
                () -> assertThat(time).isPresent(),
                () -> assertThat(time.get().getStartAt()).isEqualTo(now)
        );
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void findAll() {
        // when
        List<ReservationTime> times = timeRepository.findAll();
        ReservationTime time = times.getFirst();

        // then
        assertAll(
                () -> assertThat(times).hasSize(1),
                () -> assertThat(time.getId()).isEqualTo(timeId),
                () -> assertThat(time.getStartAt()).isEqualTo(now)
        );
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void save() {
        // given
        LocalTime newTime = LocalTime.of(16, 30);
        ReservationTime reservationTime = new ReservationTime(newTime);

        // when
        ReservationTime saved = timeRepository.save(reservationTime);


        // then
        Optional<ReservationTime> found = timeRepository.findById(saved.getId());
        assertAll(
                () -> assertThat(saved.getStartAt()).isEqualTo(newTime),
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getStartAt()).isEqualTo(newTime)
        );
    }

    @DisplayName("id로 예약시간을 삭제한다.")
    @Test
    void deleteById() {
        // when
        timeRepository.deleteById(timeId);

        // then
        assertThat(timeRepository.findAll()).isEmpty();
    }

    @DisplayName("이미 존재하는 예약 시간이므로 true를 반환한다.")
    @Test
    void existByStartAt() {
        // when
        final boolean expected = timeRepository.existsByStartAt(now);

        // then
        assertThat(expected).isTrue();
    }
}
