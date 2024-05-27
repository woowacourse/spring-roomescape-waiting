package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.util.JpaRepositoryTest;

@JpaRepositoryTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("동일한 시간 존재: 참")
    void existsByStartAt() {
        LocalTime time = LocalTime.parse("10:00");
        reservationTimeRepository.save(new ReservationTime(time));

        assertThat(reservationTimeRepository.existsByStartAt(time)).isTrue();
    }

    @Test
    @DisplayName("동일한 시간 존재하지 않음: 거짓")
    void existsByStartAt_WhenNotExists() {
        LocalTime time = LocalTime.parse("10:00");

        assertThat(reservationTimeRepository.existsByStartAt(time)).isFalse();
    }
}
