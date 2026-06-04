package roomescape.reservationwaiting.service;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ConflictException;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationWaitingServiceTest {

    @Mock
    ReservationWaitingRepository reservationWaitingRepository;

    @InjectMocks
    ReservationWaitingService reservationWaitingService;

    LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @Test
    void 중복으로_대기할_수_없다() {
        // given
        when(reservationWaitingRepository.existsByDateAndThemeIdAndTimeIdAndName(TOMORROW, 1L, 1L, "도우너"))
                .thenReturn(true);

        // when & then
        assertThrows(ConflictException.class, () -> reservationWaitingService.save("도우너", TOMORROW, 1L, 1L));
    }
}
