package roomescape.reservationTime.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservationTime.domain.ReservationTime;

@DataJpaTest
public class JpaReservationTimeRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository repository;

    @Test
    @DisplayName("저장 후 아이디 반환 테스트")
    void save_test() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        // when
        ReservationTime save = repository.save(reservationTime);
        // then
        assertThat(save).isNotNull();
    }

    @Test
    @DisplayName("삭제 성공 관련 테스트")
    void delete_test() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime save = repository.save(reservationTime);
        // when & them
        assertDoesNotThrow(() -> repository.deleteById(save.getId()));
    }

    @Test
    @DisplayName("전체 조회 테스트")
    void find_all_test() {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(10, 11));
        ReservationTime reservationTime3 = ReservationTime.createWithoutId(LocalTime.of(10, 12));
        repository.save(reservationTime1);
        repository.save(reservationTime2);
        repository.save(reservationTime3);
        // when
        List<ReservationTime> reservations = repository.findAll();
        // then
        List<LocalTime> names = reservations.stream()
                .map(ReservationTime::getStartAt)
                .toList();
        assertAll(
                () -> assertThat(reservations).hasSize(3),
                () -> assertThat(names).contains(LocalTime.of(10, 10), LocalTime.of(10, 11), LocalTime.of(10, 12))
        );
    }

    @Test
    @DisplayName("아이디로 조회 테스트")
    void find_by_id() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime save = repository.save(reservationTime);
        // when
        Optional<ReservationTime> findTime = repository.findById(save.getId());
        // then
        assertThat(findTime).isPresent();
        assertThat(reservationTime.getStartAt()).isEqualTo(findTime.get().getStartAt());
    }
}
