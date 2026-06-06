package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.application.dto.ReservationApplicationResult.Status;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
class ReservationCommandServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private TestDataHelper testHelper;

    @MockitoSpyBean
    private WaitingRepository waitingRepository;

    @DisplayName("사용자의 방탈출 예약 생성을 테스트합니다.")
    @Test
    void save_user_reservation_successfully() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        ReservationApplicationCreateCommand request = ReservationFixture.futureStarkCreateCommand(themeId, timeId, NOW);
        ReservationApplicationResult result = reservationCommandService.save(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isPositive();
            softly.assertThat(result.name()).isEqualTo(request.name());
            softly.assertThat(result.date()).isEqualTo(request.date());
            softly.assertThat(result.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(result.time()).isEqualTo(new ReservationTimeResult(timeId, LocalTime.of(10, 0)));
            softly.assertThat(result.status()).isEqualTo(Status.CONFIRM);
        });
    }

    @DisplayName("현재 시간보다 이전 시간의 예약 생성 예외를 테스트합니다.")
    @Test
    void save_past_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationCommandService.save(
                ReservationFixture.pastStarkCreateCommand(themeId, timeId, NOW)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("예약 삭제를 테스트합니다.")
    @Test
    void cancel_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatNoException().isThrownBy(() -> reservationCommandService.cancel(reservationId, NOW));
    }

    @DisplayName("삭제할 예약이 없을 시 예외 발생을 테스트합니다.")
    @Test
    void cancel_not_found_reservation_exception() {
        assertThatThrownBy(() -> reservationCommandService.cancel(1L, NOW))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @DisplayName("삭제할 예약이 현재 시간보다 이전 시간일 경우 예외 발생을 테스트합니다.")
    @Test
    void cancel_past_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() -> reservationCommandService.cancel(reservationId, NOW))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 삭제할 수 없습니다.");
    }

    @DisplayName("사용자의 방탈출 예약 날짜/시간 변경을 테스트합니다.")
    @Test
    void update_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(0, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        ReservationApplicationResult result = reservationCommandService.update(
                reservationId,
                new ReservationUpdateCommand(ReservationFixture.futureReservationDate(), updateTimeId, NOW)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.date()).isEqualTo(ReservationFixture.futureReservationDate());
            softly.assertThat(result.time().id()).isEqualTo(updateTimeId);
        });
    }

    @DisplayName("동일한 날짜와 시간으로 예약 변경 시 예외 발생을 테스트합니다.")
    @Test
    void update_same_date_and_time_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() -> reservationCommandService.update(
                reservationId,
                new ReservationUpdateCommand(ReservationFixture.futureReservationDate(), timeId, NOW)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("동일한 날짜와 시간으로 변경할 수 없습니다.");
    }

    @DisplayName("존재하지 않는 예약 업데이트 시도 시 예외 발생을 테스트합니다.")
    @Test
    void update_not_found_reservation_exception() {
        Long newTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));

        assertThatThrownBy(
                () -> reservationCommandService.update(1L, ReservationFixture.futureStarkUpdateCommand(newTimeId, NOW)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @DisplayName("변경하려는 예약 날짜에 이미 예약이 존재할 시 예외 발생을 테스트합니다.")
    @Test
    void update_already_reserved_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long starkReservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                tenTimeId
        );

        Long elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        testHelper.insertReservation(
                "카야",
                ReservationFixture.futureReservationDate(),
                themeId,
                elevenTimeId
        );

        assertThatThrownBy(() -> reservationCommandService.update(
                starkReservationId,
                ReservationFixture.futureStarkUpdateCommand(elevenTimeId, NOW))
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage("변경하려는 날짜와 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("변경하려는 날짜가 현재 시각보다 이전일 경우 예외 발생을 테스트합니다.")
    @Test
    void update_past_date_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(0, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() ->
                reservationCommandService.update(
                        reservationId,
                        new ReservationUpdateCommand(ReservationFixture.pastReservationDate(), timeId, NOW)
                ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("이미 확정된 예약이 존재할 경우, 예약 생성 예외를 테스트합니다.")
    @Test
    void save_duplicated_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        ReservationApplicationCreateCommand request = ReservationFixture.futureKayaCreateCommand(themeId, timeId, NOW);

        assertThatThrownBy(() -> reservationCommandService.save(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 해당 날짜와 시간에 예약이 존재합니다.");
    }

    @DisplayName("확정 예약 삭제 시 예약 대기의 확정 예약으로의 승격을 테스트합니다.")
    @Test
    void cancel_confirmed_reservation_and_waiting_to_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "피노",
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

        reservationCommandService.cancel(reservationId, NOW);

        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();
        Reservation promoteReservation = testHelper.findReservationBySlot(slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(promoteReservation.getMemberName().name()).isEqualTo("스타크");
            softly.assertThatThrownBy(() -> reservationCommandService.cancel(reservationId, NOW))
                    .isInstanceOf(NotFoundException.class);
        });
    }

    @DisplayName("확정 예약의 예약 변경 시 대기 중인 예약이 자동 승격 됨을 테스트합니다.")
    @Test
    void update_reservation_and_waiting_to_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(0, 0));
        Long reservationId = testHelper.insertReservation(
                "피노",
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

        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        reservationCommandService.update(
                reservationId,
                new ReservationUpdateCommand(ReservationFixture.futureReservationDate(), updateTimeId, NOW)
        );

        ReservationSlot originalSlot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(0, 0))
                .build();
        Reservation promoteReservation = testHelper.findReservationBySlot(originalSlot);

        ReservationSlot updatedSlot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(updateTimeId)
                .startAt(LocalTime.of(11, 0))
                .build();
        Reservation changeReservation = testHelper.findReservationBySlot(updatedSlot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(changeReservation.getMemberName().name()).isEqualTo("피노");
            softly.assertThat(promoteReservation.getMemberName().name()).isEqualTo("스타크");
        });
    }

    @DisplayName("예약 취소 중 대기 승격이 실패하면, 기존 예약 취소도 롤백되어 DB에 남아있어야 한다.")
    @Test
    void delete_rollback_when_waiting_promotion_fails() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        Long reservationId = testHelper.insertReservation("피노", ReservationFixture.futureReservationDate(), themeId,
                timeId);
        testHelper.insertWaiting("스타크", ReservationFixture.futureReservationDate(), themeId, timeId);

        doThrow(new RuntimeException("대기 승격 중 에러 발생!"))
                .when(waitingRepository)
                .delete(anyLong());

        assertThatThrownBy(() -> reservationCommandService.cancel(reservationId, NOW))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("에러 발생!");

        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        Reservation survivedReservation = testHelper.findReservationBySlot(slot);

        assertThat(survivedReservation).isNotNull();
        assertThat(survivedReservation.getMemberName().name()).isEqualTo("피노");
    }

    @DisplayName("예약을 취소할 때, 해당 슬롯에 대기자가 아무도 없어도 정상적으로 취소되어야 한다.")
    @Test
    void cancel_reservation_when_no_waiting_exists_successfully() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        Long reservationId = testHelper.insertReservation("피노", ReservationFixture.futureReservationDate(), themeId,
                timeId);

        assertThatNoException().isThrownBy(() -> reservationCommandService.cancel(reservationId, NOW));

        assertThatThrownBy(() -> reservationCommandService.cancel(reservationId, NOW))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 예약");
    }
}
