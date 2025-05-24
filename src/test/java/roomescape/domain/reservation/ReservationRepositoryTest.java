package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.TestRepositoryHelper;
import roomescape.exception.NotFoundException;

@DataJpaTest
@Import(TestRepositoryHelper.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TestRepositoryHelper repositoryHelper;

    private Reservation savedReservation;

    @BeforeEach
    void setUp() {
        var user = repositoryHelper.saveAnyUser();
        var date = LocalDate.of(3000, 1, 1);
        var timeSlot = repositoryHelper.saveAnyTimeSlot();
        var theme = repositoryHelper.saveAnyTheme();

        var reservation = new Reservation(user, ReservationSlot.forReserve(date, timeSlot, theme));
        savedReservation = reservationRepository.save(reservation);
        repositoryHelper.flushAndClear();
    }

    @Test
    @DisplayName("아이디에 해당하는 예약을 삭제한다.")
    void deleteByIdWhenFound() {
        var id = savedReservation.id();

        assertAll(
            () -> assertDoesNotThrow(() -> reservationRepository.deleteByIdOrElseThrow(id)),
            () -> assertThat(reservationRepository.findById(id)).isEmpty()
        );
    }

    @Test
    @DisplayName("예약 삭제 시 해당 아이디의 예약이 없으면 예외가 발생한다.")
    void deleteByIdWhenNotFound() {
        var id = savedReservation.id();

        assertAll(
            () -> assertThrows(NotFoundException.class, () -> reservationRepository.deleteByIdOrElseThrow(1234L)),
            () -> assertThat(reservationRepository.findById(id)).hasValue(savedReservation)
        );
    }

    @Test
    @DisplayName("아이디에 해당하는 예약을 조회한다.")
    void getById() {
        var id = savedReservation.id();

        var found = reservationRepository.getById(id);

        assertThat(found).isEqualTo(savedReservation);
    }

    @Test
    @DisplayName("예약 조회 시 해당 아이디의 예약이 없으면 예외가 발생한다.")
    void getByIdWhenNotFound() {
        assertThatThrownBy(() -> reservationRepository.getById(1234L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 슬롯 별 대기열로 이루어진 대기열들을 불러온다.")
    void findQueuesBySlots() {
        var queues = reservationRepository.findQueuesBySlots(List.of(savedReservation.slot()));
        assertThat(queues).isInstanceOf(ReservationQueues.class);
    }
}
