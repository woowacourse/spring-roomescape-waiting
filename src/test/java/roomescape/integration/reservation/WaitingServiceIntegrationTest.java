package roomescape.integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.support.util.TestCurrentDateTime;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.application.dto.WaitingInfo;
import roomescape.waiting.application.service.WaitingService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
public class WaitingServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    private WaitingService waitingService;
    private TestCurrentDateTime currentDateTime;

    @BeforeEach
    void init() {
        final LocalDateTime now = LocalDateTime.of(2025, 4, 29, 10, 0);
        currentDateTime = new TestCurrentDateTime(now);
        waitingService = new WaitingService(reservationRepository, waitingRepository, memberRepository, currentDateTime);
    }

    @DisplayName("새로운 예약 대기를 추가할 수 있다")
    @Test
    void createWaiting() {
        // given
        final LocalDate date = currentDateTime.getDate().plusDays(1);
        final WaitingCreateCommand request = new WaitingCreateCommand(date, 2L, 10L, 1L);
        // when
        final WaitingInfo result = waitingService.createWaiting(request);
        // then
        assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.member().name()).isEqualTo("리버"),
                () -> assertThat(result.reservationInfo().date()).isEqualTo(date),
                () -> assertThat(result.reservationInfo().time().id()).isNotNull(),
                () -> assertThat(result.reservationInfo().time().startAt()).isEqualTo(LocalTime.of(15, 0))
        );
    }

    @DisplayName("이미 예약한 슬롯에 예약 대기를 하는 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenOwnedReservation() {
        // given
        final LocalDate date = currentDateTime.getDate().plusDays(1);
        final WaitingCreateCommand request = new WaitingCreateCommand(date, 1L, 11L, 1L);
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("이미 예약한 슬롯에 예약 대기를 할 수 없습니다.");
    }


    @DisplayName("이미 예약 대기한 슬롯에 예약 대기를 하는 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateWaiting() {
        // given
        final LocalDate date = currentDateTime.getDate().plusDays(1);
        final WaitingCreateCommand request = new WaitingCreateCommand(date, 2L, 11L, 1L);
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("이미 예약 대기한 슬롯에 예약 대기를 할 수 없습니다.");
    }


    @DisplayName("이미 지난 슬롯에 예약 대기를 하는 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenPastWaiting() {
        // given
        final LocalDate date = currentDateTime.getDate().minusDays(1);
        final WaitingCreateCommand request = new WaitingCreateCommand(date, 1L, 8L, 2L);
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("이미 지난 슬롯에 예약 대기를 할 수 없습니다.");
    }

    @DisplayName("id를 기반으로 예약 대기를 취소할 수 있다")
    @Test
    void cancelWaitingById() {
        // when
        final List<Waiting> cancelBeforeWaitings = waitingRepository.findAll();
        waitingService.cancelWaitingById(1L);
        // then
        final List<Waiting> cancelAfterWaitings = waitingRepository.findAll();
        assertThat(cancelAfterWaitings).hasSize(cancelBeforeWaitings.size() - 1);
    }
}
