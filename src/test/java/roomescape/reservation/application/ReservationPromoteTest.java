package roomescape.reservation.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.PendingReservationRepository;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.domain.Theme;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.domain.ReservationTime;

@SpringBootTest
public class ReservationPromoteTest {

    @Autowired
    private Clock clock;

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private PendingReservationRepository pendingReservationRepository;

    @MockitoSpyBean
    private ActiveReservationService activeReservationService;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        ReservationTimeInfo reservationTimeInfo = reservationTimeService.addReservationTime(
                ReservationTimeCommand.builder()
                        .startAt(LocalTime.now(clock))
                        .build()
        );

        time = ReservationTime.builder()
                .id(reservationTimeInfo.id())
                .startAt(reservationTimeInfo.startAt())
                .build();

        ThemeInfo themeInfo = themeService.addTheme(ThemeCommand.builder()
                .name("추리")
                .description("추리하기")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build()
        );

        theme = Theme.builder()
                .id(themeInfo.id())
                .name(themeInfo.name())
                .description(themeInfo.description())
                .durationTime(themeInfo.durationTime())
                .thumbnailImageUrl(themeInfo.thumbnailImageUrl())
                .build();
    }

    @Test
    @DisplayName("대기자를 확정으로 승격할 때 삽입이 실패하면, 삭제되었던 대기자 데이터는 롤백되어 유지되어야 한다.")
    void promotionFailRollbackTest() {
        ReservationCreateCommand activeCommand = createCommand("확정자", time.getId());
        ReservationInfo activeReservation = reservationFacade.addReservation(activeCommand);

        ReservationCreateCommand pendingCommand = createCommand("대기자", time.getId());
        reservationFacade.addReservation(pendingCommand);

        doThrow(new RuntimeException("DB 삽입 중 알 수 없는 에러 발생!"))
                .when(activeReservationService).savePromoted(any());

        ReservationCancelCommand cancelCommand = new ReservationCancelCommand(activeCommand.name());

        assertThatThrownBy(() -> reservationFacade.cancelReservation(activeReservation.id(), cancelCommand))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 삽입 중 알 수 없는 에러 발생!");

        long pendingCount = pendingReservationRepository.findAll().size();
        assertThat(pendingCount).isEqualTo(1);
    }

    private ReservationCreateCommand createCommand(String name, Long timeId) {
        return ReservationCreateCommand.builder()
                .name(name)
                .date(LocalDate.now(clock))
                .timeId(timeId)
                .themeId(theme.getId())
                .build();
    }
}
