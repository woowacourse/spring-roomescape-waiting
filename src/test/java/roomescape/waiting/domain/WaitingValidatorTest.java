package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.fake.FakeWaitingRepository;

class WaitingValidatorTest {

    private FakeWaitingRepository waitingRepository;
    private WaitingValidator waitingValidator;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        waitingValidator = new WaitingValidator(waitingRepository);
    }

    @Test
    @DisplayName("같은 슬롯에 대기가 없으면 예외가 발생하지 않는다")
    void 같은_슬롯에_대기가_없으면_예외가_발생하지_않는다() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                LocalDate.now().plusDays(1),
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatCode(() -> waitingValidator.validateAlreadyReservation(command))
                .doesNotThrowAnyException();
    }

}
