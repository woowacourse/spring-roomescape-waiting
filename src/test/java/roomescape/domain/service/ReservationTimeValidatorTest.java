package roomescape.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.model.exception.ReservationException.ReservationTimeInUseException;
import roomescape.domain.reservation.model.repository.ReservationRepository;
import roomescape.domain.reservation.model.service.ReservationTimeValidator;

@ExtendWith(MockitoExtension.class)
class ReservationTimeValidatorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationTimeValidator reservationTimeValidator;

    @DisplayName("해당 예약시간을 사용중인 예약이 있다면 예외를 발생시킨다")
    @Test
    void validateNotInUse() {
        // given
        Long timeId = 1L;
        given(reservationRepository.existsByTimeId(timeId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeValidator.validateNotInUse(timeId))
                .isInstanceOf(ReservationTimeInUseException.class);
    }
}
