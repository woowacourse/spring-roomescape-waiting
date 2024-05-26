package roomescape.domain.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RepositoryTest;
import roomescape.domain.reservation.domain.Status;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRepositoryTest extends RepositoryTest {

    static Stream<Arguments> statusProvider() {
        return Stream.of(
                Arguments.of(List.of(Status.RESERVATION), 8),
                Arguments.of(List.of(Status.RESERVATION_WAIT), 12),
                Arguments.of(List.of(Status.RESERVATION, Status.RESERVATION_WAIT), 20)
        );
    }

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("특정 예약상태들을 갖는 예약 목록을 불러올 수 있다.")
    @MethodSource("statusProvider")
    @ParameterizedTest
    void findByStatusInTest(List<Status> statuses, int size) {
        assertThat(reservationRepository.findByStatusIn(statuses)).hasSize(size);
    }
}
