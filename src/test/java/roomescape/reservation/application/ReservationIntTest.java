package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.fixture.ReservationFixture;
import roomescape.support.datasource.ReservationDataSource;

@SpringBootTest
@ActiveProfiles("test")
class ReservationIntTest {

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();

        LocalDate reservationDate = LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1);

        dataSource.insertTheme("공포테마", "https://image.com/image.png", "무서운 테마입니다.");
        dataSource.insertReservationTime(LocalTime.of(10, 0));
        dataSource.insertReservationTime(LocalTime.of(11, 0));

        dataSource.insertReservation(
                "바니", reservationDate, 1L, 1L, Status.RESERVED.name(),
                LocalDateTime.of(2026, 5, 2, 10, 0)
        );
        dataSource.insertReservation(
                "포비", reservationDate, 1L, 1L, Status.WAITING.name(),
                LocalDateTime.of(2026, 5, 2, 10, 1)
        );
    }

    @Test
    void 예약_수정_후_대기_승격에_실패하면_기존_예약으로_복구된다() {
        // given
        doThrow(new RuntimeException())
                .when(reservationRepository)
                .update(argThat(reservation -> 2L == reservation.getId() && reservation.isReserved()));

        ReservationChangeCommand command = new ReservationChangeCommand(
                "바니",
                2L,
                1L,
                LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1)
        );

        // when
        assertThatThrownBy(() -> reservationService.modify(1L, command))
                .isInstanceOf(RuntimeException.class);

        // then
        Reservation reserved = reservationRepository.getById(1L);
        Reservation waiting = reservationRepository.getById(2L);

        assertThat(reserved.getStatus()).isEqualTo(Status.RESERVED);
        assertThat(reserved.getTime().getId()).isEqualTo(1L);
        assertThat(waiting.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    void 예약_취소_후_대기_승격에_실패하면_기존_예약으로_복구된다() {
        // given
        doThrow(new RuntimeException())
                .when(reservationRepository)
                .update(argThat(reservation -> 2L == reservation.getId() && reservation.isReserved()));

        // when
        assertThatThrownBy(() -> reservationService.cancel(1L, "바니"))
                .isInstanceOf(RuntimeException.class);

        // then
        Reservation reserved = reservationRepository.getById(1L);
        Reservation waiting = reservationRepository.getById(2L);

        assertThat(reserved.getStatus()).isEqualTo(Status.RESERVED);
        assertThat(waiting.getStatus()).isEqualTo(Status.WAITING);
    }
}
