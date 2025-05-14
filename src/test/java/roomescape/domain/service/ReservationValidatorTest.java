package roomescape.domain.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.domain.reservation.model.repository.ReservationRepository;
import roomescape.domain.reservation.model.service.ReservationValidator;

@ExtendWith(MockitoExtension.class)
class ReservationValidatorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationValidator reservationValidator;

    @DisplayName("중복 예약이면 예외를 발생시킨다")
    @Test
    void existDuplicatedGetDateGetTime() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;

        given(reservationRepository.existDuplicatedDateTime(date, timeId, themeId)).willReturn(true);

        // when & then
        assertThrows(InvalidReservationTimeException.class, () ->
                reservationValidator.validateNoDuplication(date, timeId, themeId)
        );
    }
}
