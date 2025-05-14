package roomescape.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.model.exception.ReservationException.ReservationThemeInUseException;
import roomescape.domain.reservation.model.repository.ReservationRepository;
import roomescape.domain.reservation.model.service.ReservationThemeValidator;

@ExtendWith(MockitoExtension.class)
class ReservationThemeValidatorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationThemeValidator reservationThemeValidator;

    @DisplayName("해당 테마를 사용중인 예약이 있다면 예외를 발생시킨다")
    @Test
    void validateNotInUse() {
        // given
        Long themeId = 1L;
        given(reservationRepository.existsByThemeId(themeId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationThemeValidator.validateNotInUse(themeId))
                .isInstanceOf(ReservationThemeInUseException.class);
    }
}
