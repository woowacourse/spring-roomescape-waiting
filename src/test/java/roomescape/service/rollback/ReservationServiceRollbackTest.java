package roomescape.service.rollback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.common.config.FixedClockConfig.FUTURE_DATE;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.service.ReservationService;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.event.ReservationChangeEvent;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationServiceRollbackTest {
    private final String name = "브라운";
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);

    private final Long timeId = 1L;
    private final ReservationTime time = new ReservationTime(timeId, LocalTime.parse("10:00"));

    private final Long themeId = 1L;
    private final Theme theme = new Theme(themeId, ThemeName.parse("테마1"), Description.parse("설명1"),
            ThumbnailUrl.parse("/images/thumbnail"));

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationDao reservationDao;

    @TestConfiguration
    static class ExplodingListenerConfig {
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void explode(ReservationChangeEvent event) {
            try {
                throw new RuntimeException("의도된 에러 발생");
            } catch (Exception e) {
            }
        }
    }

    @Test
    @DisplayName("예약 변경 시 이벤트 발행 중 에러가 발생해도, 예약 변경은 커밋된다.")
    void changeDateTimeReservation_RollbackTest() {
        Long reservationId = 25L;
        LocalDate newDate = futureDate.plusDays(5);
        ReservationCommand command = new ReservationCommand(name, newDate, timeId, themeId);

        assertDoesNotThrow(() -> reservationService.changeDateTime(reservationId, command));

        Reservation rollback = reservationDao.findById(reservationId).orElseThrow();
        assertThat(rollback.getEventSlot().date()).isEqualTo(newDate);
    }
}
