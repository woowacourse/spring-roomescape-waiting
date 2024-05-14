package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.TimeSlot;

@DataJpaTest
class JpaReservationTimeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Test
    @DisplayName("특정 시간이 저장소에 존재하면 true를 반환한다.")
    void shouldReturnTrueWhenReservationTimeAlreadyExist() {
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);
        boolean exists = timeRepository.existsByStartAt(time.getStartAt());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 시간이 저장소에 존재하지 않으면 false를 반환한다.")
    void shouldReturnFalseWhenReservationTimeAlreadyExist() {
        LocalTime startAt = LocalTime.of(10, 0);
        boolean exists = timeRepository.existsByStartAt(startAt);
        assertThat(exists).isFalse();
    }

    @Test
    @Sql("/insert-reservations.sql")
    @DisplayName("날짜와 테마 id가 주어지면, 예약 가능한 시간을 반환한다.")
    void shouldReturnAvailableTimes() {
        LocalDate date = LocalDate.of(2024, 12, 25);
        List<TimeSlot> times = timeRepository.getReservationTimeAvailabilities(date, 1L)
                .stream()
                .filter(time -> !time.isBooked())
                .toList();
        assertThat(times).hasSize(4);
    }
}
