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
import org.springframework.transaction.annotation.Transactional;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservation.application.dto.WaitingCreateCommand;
import roomescape.reservation.application.dto.WaitingPostponeResult;
import roomescape.reservation.application.dto.WaitingResult;
import roomescape.reservation.application.dto.WaitingResult.Status;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
@Transactional
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

        WaitingCreateCommand request = ReservationFixture.futureKayaWaitingCreateCommand(themeId, timeId, NOW);
        WaitingResult savedWaiting = waitingCommandService.save(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedWaiting.id()).isPositive();
            softly.assertThat(savedWaiting.name()).isEqualTo(request.name());
            softly.assertThat(savedWaiting.date()).isEqualTo(request.date());
            softly.assertThat(savedWaiting.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(savedWaiting.time()).isEqualTo(new ReservationTimeResult(timeId, LocalTime.of(10, 0)));
            softly.assertThat(savedWaiting.status()).isEqualTo(Status.WAITING);
            softly.assertThat(savedWaiting.rank()).isEqualTo(1);
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

        WaitingCreateCommand request = ReservationFixture.futureKayaWaitingCreateCommand(themeId, timeId, NOW);
        WaitingResult savedWaiting = waitingCommandService.save(request);

        assertThat(savedWaiting.rank()).isEqualTo(3);
    }

    @DisplayName("확정된 예약이 존재하지 않을 경우, 예약 대기 생성 예외를 테스트합니다.")
    @Test
    void save_waiting_without_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        WaitingCreateCommand request = ReservationFixture.futureStarkWaitingCreateCommand(themeId, timeId, NOW);

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

        WaitingCreateCommand request = ReservationFixture.futureStarkWaitingCreateCommand(themeId, timeId, NOW);

        assertThatThrownBy(() -> waitingCommandService.save(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 해당 테마의 날짜와 시간에 대기를 신청했습니다.");
    }

    @DisplayName("동일한 사용자가 이미 예약한 날짜와 시간으로 대기 예약 생성 시 예외를 테스트합니다.")
    @Test
    void save_waiting_with_confirmed_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        WaitingCreateCommand request = ReservationFixture.futureStarkWaitingCreateCommand(themeId, timeId, NOW);

        assertThatThrownBy(() -> waitingCommandService.save(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약한 날짜와 시간에는 대기를 신청할 수 없습니다.");
    }

    @DisplayName("예약 대기 삭제를 테스트합니다.")
    @Test
    void delete_waiting_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatNoException().isThrownBy(() -> waitingCommandService.delete(waitingId, NOW));
    }

    @DisplayName("예약 대기 삭제 시 뒤 순번을 당기는 것을 테스트합니다.")
    @Test
    void delete_waiting_and_decrement_next_ranks() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        Long waitingId = testHelper.insertWaiting(
                "피노",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        waitingCommandService.delete(waitingId, NOW);
        Integer starkRank = testHelper.findWaitingRank("스타크", slot);
        Integer neoRank = testHelper.findWaitingRank("네오", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(starkRank).isEqualTo(1);
            softly.assertThat(neoRank).isEqualTo(2);
        });
    }

    @DisplayName("삭제할 예약 대기가 없을 시 예외 발생을 테스트합니다.")
    @Test
    void delete_not_found_waiting_exception() {
        assertThatThrownBy(() -> waitingCommandService.delete(1L, NOW))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 대기입니다.");
    }

    @DisplayName("삭제할 예약 대기가 현재 시간보다 이전 시간일 경우 예외 발생을 테스트합니다.")
    @Test
    void delete_past_waiting_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() -> waitingCommandService.delete(waitingId, NOW))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 삭제할 수 없습니다.");
    }

    @DisplayName("예약 대기 순번 미루기를 테스트합니다.")
    @Test
    void postpone_waiting_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피케이",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        WaitingPostponeResult result = waitingCommandService.postpone(waitingId, 2, NOW);
        Integer starkRank = testHelper.findWaitingRank("스타크", slot);
        Integer pkRank = testHelper.findWaitingRank("피케이", slot);
        Integer neoRank = testHelper.findWaitingRank("네오", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(waitingId);
            softly.assertThat(result.rank()).isEqualTo(3);
            softly.assertThat(starkRank).isEqualTo(3);
            softly.assertThat(pkRank).isEqualTo(1);
            softly.assertThat(neoRank).isEqualTo(2);
        });
    }

    @DisplayName("예약 대기를 남은 순번보다 많이 미루면 마지막 순번으로 이동하는 것을 테스트합니다.")
    @Test
    void postpone_waiting_reservation_to_last_rank() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피케이",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        WaitingPostponeResult result = waitingCommandService.postpone(waitingId, 99, NOW);
        Integer starkRank = testHelper.findWaitingRank("스타크", slot);
        Integer pkRank = testHelper.findWaitingRank("피케이", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(waitingId);
            softly.assertThat(result.rank()).isEqualTo(2);
            softly.assertThat(starkRank).isEqualTo(2);
            softly.assertThat(pkRank).isEqualTo(1);
        });
    }

    @DisplayName("이미 지나간 시간의 예약 대기 순번 미루기 시 예외 발생을 테스트합니다.")
    @Test
    void postpone_past_waiting_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() -> waitingCommandService.postpone(waitingId, 1, NOW))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 미룰 수 없습니다.");
    }
}
