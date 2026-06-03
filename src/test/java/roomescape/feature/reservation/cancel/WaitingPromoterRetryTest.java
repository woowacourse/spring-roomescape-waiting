package roomescape.feature.reservation.cancel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;

@SpringBootTest
class WaitingPromoterRetryTest {

    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final LocalDate DATE = LocalDate.now().plusYears(1);

    @Autowired
    private WaitingPromoter waitingPromoter;

    @MockitoBean
    private ReservationRepository reservationRepository;

    private Logger promoterLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        promoterLogger = (Logger) LoggerFactory.getLogger(WaitingPromoter.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        promoterLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        promoterLogger.detachAppender(logAppender);
    }

    private Reservation waiting() {
        Time time = Time.reconstruct(TIME_ID, LocalTime.of(10, 0), EntityStatus.ACTIVE);
        Theme theme = Theme.reconstruct(THEME_ID, "테마", "설명", "https://example.com/theme.png", EntityStatus.ACTIVE);
        return Reservation.reconstruct(1L, new ReserverName("예약자"), DATE, time, theme, ReservationStatus.WAITING);
    }

    @Test
    void DB_예외가_발생하면_재시도하여_승격에_성공한다() {
        // given: 첫 시도에는 DB 예외, 두 번째 시도에는 정상 조회
        when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                .thenThrow(new CannotAcquireLockException("일시적 락 획득 실패"))
                .thenReturn(Optional.of(waiting()));
        when(reservationRepository.changeStatus(1L, ReservationStatus.WAITING, ReservationStatus.ACTIVE))
                .thenReturn(1);

        // when
        waitingPromoter.promoteFastestWaiting(new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE));

        // then: 재시도되어 총 2회 조회, 승격 1회 수행
        verify(reservationRepository, times(2)).findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID);
        verify(reservationRepository).changeStatus(1L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);
    }

    @Test
    void DB_예외가_재시도_횟수만큼_지속되도_예외를_전파하지_않는다() {
        // given: 매 시도마다 DB 예외
        when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                .thenThrow(new CannotAcquireLockException("지속적인 락 획득 실패"));

        // when & then: 최대 시도(2회) 후 @Recover 가 예외를 전파하지 않는다
        assertThatNoException().isThrownBy(() ->
                waitingPromoter.promoteFastestWaiting(new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE)));
        verify(reservationRepository, times(2)).findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID);
        verify(reservationRepository, times(0)).changeStatus(any(), any(), any());
    }

    @Test
    void DB_예외가_재시도_횟수만큼_지속되면_에러_로그를_남긴다() {
        // given: 매 시도마다 DB 예외
        when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                .thenThrow(new CannotAcquireLockException("지속적인 락 획득 실패"));

        // when: 최대 시도 후 @Recover(recoverPromotion) 진입
        waitingPromoter.promoteFastestWaiting(new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE));

        // then: recoverPromotion 이 ERROR 로그를 한 번 남긴다
        List<ILoggingEvent> errorLogs = logAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .toList();
        assertThat(errorLogs).hasSize(1);
        assertThat(errorLogs.get(0).getFormattedMessage())
                .contains("대기 예약 자동 승격에 재시도 후에도 실패했습니다");
    }
}
