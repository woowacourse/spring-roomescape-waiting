package roomescape.reservation.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;
import roomescape.waiting.service.ReservationWaitingService;
import roomescape.waiting.service.dto.ReservationWaitingCommand;

@SpringBootTest(webEnvironment = NONE)
public class ReservationServiceTransactionTest {

    @MockitoSpyBean
    ReservationRepository reservationRepository;

    @MockitoSpyBean
    ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void delete_rollback() {
        // given
        ReservationTimeResult reservationTimeResult = saveReservationTime();
        ThemeResult themeResult = saveTheme();
        ReservationCommand reservationCommand = saveReservation(reservationTimeResult, themeResult);
        ReservationResult reservationResult = reservationService.save(reservationCommand);
        saveReservationWaiting(reservationResult);

        Long testTargetId = reservationResult.id();
        String testTargetOwnerName = reservationResult.name();

        doThrow(new RuntimeException("에러 발생"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        // when
        assertThatThrownBy(() -> reservationService.deleteById(testTargetId, testTargetOwnerName))
                .isInstanceOf(RuntimeException.class);

        // then
        Optional<Reservation> result = reservationRepository.findById(testTargetId);
        Assertions.assertTrue(result.isPresent());
        List<ReservationWaiting> waitings = reservationWaitingRepository.findAllByName("임꺽정");
        Assertions.assertFalse(waitings.isEmpty());
    }

    @Test
    void delete_rollbackWhenWaitingDeleteFails() {
        // given
        ReservationTimeResult reservationTimeResult = saveReservationTime();
        ThemeResult themeResult = saveTheme();
        ReservationCommand reservationCommand = saveReservation(reservationTimeResult, themeResult);
        ReservationResult reservationResult = reservationService.save(reservationCommand);
        saveReservationWaiting(reservationResult);

        Long testTargetId = reservationResult.id();
        String testTargetOwnerName = reservationResult.name();

        doThrow(new RuntimeException("대기 예약 삭제 중 에러 발생"))
                .when(reservationWaitingRepository)
                .delete(any(ReservationWaiting.class));

        // when
        assertThatThrownBy(() -> reservationService.deleteById(testTargetId, testTargetOwnerName))
                .isInstanceOf(RuntimeException.class);

        // then
        Optional<Reservation> result = reservationRepository.findById(testTargetId);
        Assertions.assertTrue(result.isPresent());
        List<ReservationWaiting> waitings = reservationWaitingRepository.findAllByName("임꺽정");
        Assertions.assertFalse(waitings.isEmpty());
    }

    private void saveReservationWaiting(ReservationResult reservationResult) {
        ReservationWaitingCommand reservationWaitingCommand = new ReservationWaitingCommand(
                "임꺽정",
                reservationResult.date(),
                reservationResult.time().id(),
                reservationResult.theme().id()
        );
        reservationWaitingService.save(reservationWaitingCommand);
    }

    @Test
    void save_rollback() {
        // given
        ReservationTimeResult reservationTimeResult = saveReservationTime();
        ThemeResult themeResult = saveTheme();
        ReservationCommand command = new ReservationCommand(
                "홍길동",
                LocalDate.now().plusDays(1),
                reservationTimeResult.id(),
                themeResult.id()
        );

        doThrow(new RuntimeException("저장 중 에러 발생"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        // when
        assertThatThrownBy(() -> reservationService.save(command))
                .isInstanceOf(RuntimeException.class);

        // then
        List<Reservation> reservations = reservationRepository.findAllByName("홍길동");
        Assertions.assertTrue(reservations.isEmpty());
    }

    @Test
    void update_rollback() {
        // given
        ReservationTimeResult reservationTimeResult = saveReservationTime();
        ThemeResult themeResult = saveTheme();

        // 시간 하나 더 생성 (수정용)
        ReservationTimeCommand command2 = new ReservationTimeCommand(LocalTime.now().plusHours(2));
        ReservationTimeResult time2 = reservationTimeService.save(command2);

        ReservationCommand reservationCommand = saveReservation(reservationTimeResult, themeResult);
        ReservationResult reservationResult = reservationService.save(reservationCommand);

        Long testTargetId = reservationResult.id();
        String testTargetOwnerName = reservationResult.name();

        ReservationUpdateCommand updateCommand = new ReservationUpdateCommand(LocalDate.now().plusDays(5), time2.id());

        doThrow(new RuntimeException("수정 중 에러 발생"))
                .when(reservationRepository)
                .update(any(Reservation.class));

        // when
        assertThatThrownBy(() -> reservationService.update(updateCommand, testTargetId, testTargetOwnerName))
                .isInstanceOf(RuntimeException.class);

        // then
        Optional<Reservation> result = reservationRepository.findById(testTargetId);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(reservationResult.date(), result.get().getDate());
        Assertions.assertEquals(reservationResult.time().id(), result.get().getTimeId());
    }

    private static ReservationCommand saveReservation(
            ReservationTimeResult reservationTimeResult, ThemeResult themeResult
    ) {
        return new ReservationCommand(
                "홍길동",
                LocalDate.now(),
                reservationTimeResult.id(),
                themeResult.id()
        );
    }

    private ThemeResult saveTheme() {
        ThemeCommand testThemeCommand = new ThemeCommand("테스트 테마", "테마 설명", "http://www.thumbnail.kr");
        return themeService.save(testThemeCommand);
    }

    private ReservationTimeResult saveReservationTime() {
        ReservationTimeCommand testReservationTimeCommand = new ReservationTimeCommand(LocalTime.now().plusHours(1));
        return reservationTimeService.save(testReservationTimeCommand);
    }
}
