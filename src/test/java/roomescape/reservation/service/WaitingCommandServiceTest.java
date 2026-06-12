package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.application.dto.ReservationApplicationResult.Status;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
class WaitingCommandServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    private WaitingCommandService waitingCommandService;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("이미 확정된 예약이 존재할 경우, 예약이 대기로 생성되는 것을 테스트합니다.")
    @Test
    void save_waiting_if_reservation_exists() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        ReservationApplicationCreateCommand request = ReservationFixture.futureKayaCreateCommand(themeId, timeId, NOW);
        ReservationApplicationResult savedWaiting = waitingCommandService.save(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedWaiting.id()).isPositive();
            softly.assertThat(savedWaiting.name()).isEqualTo(request.name());
            softly.assertThat(savedWaiting.date()).isEqualTo(request.date());
            softly.assertThat(savedWaiting.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(savedWaiting.time()).isEqualTo(new ReservationTimeResult(timeId, LocalTime.of(10, 0)));
            softly.assertThat(savedWaiting.status()).isEqualTo(Status.WAITING);
            softly.assertThat(savedWaiting.rank()).isEqualTo(1L);
        });
    }

    @DisplayName("예약 대기의 순번을 테스트합니다.")
    @Test
    void check_waiting_rank() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피노",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        ReservationApplicationCreateCommand request = ReservationFixture.futureKayaCreateCommand(themeId, timeId, NOW);
        ReservationApplicationResult savedWaiting = waitingCommandService.save(request);

        assertThat(savedWaiting.rank()).isEqualTo(3L);
    }

    @DisplayName("확정된 예약이 존재하지 않을 경우, 예약 대기 생성 예외를 테스트합니다.")
    @Test
    void save_waiting_without_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        ReservationApplicationCreateCommand request = ReservationFixture.futureStarkCreateCommand(themeId, timeId, NOW);

        assertThatThrownBy(() -> waitingCommandService.save(request))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("예약이 존재하지 않는 경우, 대기를 신청할 수 없습니다.");
    }

    @DisplayName("동일한 예약 대기 생성 시 예외를 테스트합니다.")
    @Test
    void save_duplicated_waiting_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        ReservationApplicationCreateCommand request = ReservationFixture.futureStarkCreateCommand(themeId, timeId, NOW);

        assertThatThrownBy(() -> waitingCommandService.save(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 해당 테마의 날짜와 시간에 대기를 신청했습니다.");
    }

    @DisplayName("예약 대기 삭제를 테스트합니다.")
    @Test
    void cancel_waiting_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatNoException().isThrownBy(() -> waitingCommandService.cancel(waitingId, NOW));
    }

    @DisplayName("삭제할 예약 대기가 없을 시 예외 발생을 테스트합니다.")
    @Test
    void cancel_not_found_waiting_exception() {
        assertThatThrownBy(() -> waitingCommandService.cancel(1L, NOW))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 대기입니다.");
    }

    @DisplayName("삭제할 예약 대기가 현재 시간보다 이전 시간일 경우 예외 발생을 테스트합니다.")
    @Test
    void cancel_past_waiting_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() -> waitingCommandService.cancel(waitingId, NOW))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 삭제할 수 없습니다.");
    }
}
