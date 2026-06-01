package integration.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import integration.BaseIntegrationTest;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.ReservationTime;
import roomescape.exception.DuplicateEntityException;
import roomescape.persistence.ReservationTimeRepository;

class ReservationTimeRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationTimeDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
    }

    @Test
    void 시간을_저장하고_ID로_조회한다() {
        // given
        LocalTime reservationStartTime = LocalTime.of(10, 0);
        ReservationTime time = new ReservationTime(reservationStartTime);

        // when
        ReservationTime saved = reservationTimeRepository.save(time);

        // then
        Optional<ReservationTime> found = reservationTimeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStartAt()).isEqualTo(reservationStartTime);
    }

    @Test
    void 같은_시간으로_저장하면_참조_무결성_예외가_발생한다() {
        // given
        LocalTime reservationStartTime = LocalTime.of(10, 0);
        ReservationTime time = new ReservationTime(reservationStartTime);
        reservationTimeRepository.save(time);

        // when & then: 무결성 위반 예외를 비즈니스 예외로 변경
        assertThatThrownBy(() -> reservationTimeRepository.save(time))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 존재하는 시간 정보입니다.");
    }

    @Test
    void 특정_시간이_존재하는지_확인한다() {
        // given
        LocalTime targetTime = LocalTime.of(10, 0);
        reservationTimeRepository.save(new ReservationTime(targetTime));

        // when & then
        LocalTime otherTime = LocalTime.of(11, 0);
        assertThat(reservationTimeRepository.existsByStartAt(targetTime)).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(otherTime)).isFalse();
    }

}
