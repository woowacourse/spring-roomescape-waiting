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
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.repository.ReservationWaitingRepository;
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
        jdbcTemplate.update("DELETE FROM payment");
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void delete_rollback() {
        // given
        ReservationTimeResult reservationTimeResult = saveReservationTime(LocalTime.of(1, 11));
        ThemeResult themeResult = saveTheme();
        ReservationCommand reservationCommand = saveReservation(reservationTimeResult, themeResult);
        ReservationResult reservationResult = reservationService.save(reservationCommand,
                java.time.LocalDateTime.now());
        saveReservationWaiting(reservationResult);

        Long testTargetId = reservationResult.id();
        String testTargetOwnerName = reservationResult.name();

        doThrow(new RuntimeException("에러 발생"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        // when
        assertThatThrownBy(
                () -> reservationService.deleteByUser(testTargetId, testTargetOwnerName, java.time.LocalDateTime.now()))
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
        ReservationTimeResult reservationTimeResult = saveReservationTime(LocalTime.of(2, 22));
        ThemeResult themeResult = saveTheme();
        ReservationCommand reservationCommand = saveReservation(reservationTimeResult, themeResult);
        ReservationResult reservationResult = reservationService.save(reservationCommand,
                java.time.LocalDateTime.now());
        saveReservationWaiting(reservationResult);

        Long testTargetId = reservationResult.id();
        String testTargetOwnerName = reservationResult.name();

        doThrow(new RuntimeException("대기 예약 삭제 중 에러 발생"))
                .when(reservationWaitingRepository)
                .delete(any(ReservationWaiting.class));

        // when
        assertThatThrownBy(
                () -> reservationService.deleteByUser(testTargetId, testTargetOwnerName, java.time.LocalDateTime.now()))
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
        reservationWaitingService.save(reservationWaitingCommand, java.time.LocalDateTime.now());
    }


    private static ReservationCommand saveReservation(
            ReservationTimeResult reservationTimeResult, ThemeResult themeResult
    ) {
        return new ReservationCommand(
                "홍길동",
                LocalDate.now().plusDays(1),
                reservationTimeResult.id(),
                themeResult.id()
        );
    }

    private ThemeResult saveTheme() {
        ThemeCommand testThemeCommand = new ThemeCommand("테스트 테마", "테마 설명", "http://www.thumbnail.kr");
        return themeService.save(testThemeCommand);
    }

    private ReservationTimeResult saveReservationTime(LocalTime startAt) {
        ReservationTimeCommand testReservationTimeCommand = new ReservationTimeCommand(startAt);
        return reservationTimeService.save(testReservationTimeCommand);
    }
}
