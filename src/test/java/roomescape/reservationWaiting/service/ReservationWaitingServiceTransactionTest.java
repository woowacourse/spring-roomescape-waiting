package roomescape.reservationWaiting.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ReservationWaitingServiceTransactionTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @MockitoSpyBean
    ReservationWaitingRepository reservationWaitingRepository;

    private static final String RESERVATION_NAME = "홍길동";
    private static final String WAITING_NAME = "임꺽정";
    private static final String THEME_NAME = "웨이팅 테마";
    private static final String THEME_DESC = "테마 설명";
    private static final String THEME_THUMBNAIL = "http://www.thumbnail.kr";

    @Test
    void save_rollback() {
        // given
        ReservationTimeCommand testReservationTimeCommand = new ReservationTimeCommand(LocalTime.now().plusHours(2));
        ReservationTimeResult reservationTimeResult = reservationTimeService.save(testReservationTimeCommand);

        ThemeCommand testThemeCommand = new ThemeCommand(THEME_NAME + "1", THEME_DESC, THEME_THUMBNAIL);
        ThemeResult themeResult = themeService.save(testThemeCommand);

        ReservationCommand reservationCommand = new ReservationCommand(
                RESERVATION_NAME + "1",
                LocalDate.now(),
                reservationTimeResult.id(),
                themeResult.id()
        );
        ReservationResult reservationResult = reservationService.save(reservationCommand);

        ReservationWaitingCommand command = new ReservationWaitingCommand(
                WAITING_NAME + "1",
                reservationResult.date(),
                reservationResult.time().id(),
                reservationResult.theme().id()
        );

        doThrow(new RuntimeException("대기 예약 저장 중 에러 발생"))
                .when(reservationWaitingRepository)
                .save(any(ReservationWaiting.class));

        // when
        assertThatThrownBy(() -> reservationWaitingService.save(command))
                .isInstanceOf(RuntimeException.class);

        // then
        boolean exists = reservationWaitingRepository.existsByDateAndTimeIdAndName(
                command.date(), command.timeId(), WAITING_NAME + "1"
        );
        Assertions.assertFalse(exists);
    }
}
