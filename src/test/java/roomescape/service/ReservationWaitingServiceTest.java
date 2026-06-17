package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.response.ReservationWaitingResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationWaitingService waitingService;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 예약_대기를_추가한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.insert(Reservation.createWithoutId("로지",
                new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme)));
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        );

        // when
        ReservationWaitingResponse response = waitingService.createReservationWaiting(command, LocalDateTime.now());

        // then
        assertThat(response)
                .extracting(ReservationWaitingResponse::name, ReservationWaitingResponse::reservationDate,
                        r -> r.time().id(), r -> r.theme().id())
                .containsExactly("맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId());
    }

    @Test
    void 예약이_없는_슬롯에_대기를_신청하면_404를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.createReservationWaiting(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 동일한_슬롯에_동일한_사용자의_예약이_존재하면_409를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.insert(Reservation.createWithoutId("로지",
                new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme)));
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "로지", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.createReservationWaiting(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void 동일한_슬롯에_중복_대기를_신청하면_409를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.insert(Reservation.createWithoutId("로지",
                new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme)));
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        );
        waitingService.createReservationWaiting(command, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> waitingService.createReservationWaiting(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void 대기_순번이_올바르게_계산된다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.insert(Reservation.createWithoutId("로지",
                new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme)));

        waitingService.createReservationWaiting(new CreateReservationWaitingCommand(
                "브라운", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        ), LocalDateTime.now());

        // when
        ReservationWaitingResponse response = waitingService.createReservationWaiting(
                new CreateReservationWaitingCommand(
                        "맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
                ), LocalDateTime.now());

        // then
        assertThat(response.order()).isEqualTo(2);
    }

    @Test
    void 예약_대기를_취소한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationWaiting saved = reservationWaitingRepository.insert(
                ReservationWaiting.createWithoutId("브라운", LocalDateTime.now(),
                        new ReservationSlot(LocalDate.of(2026, 5, 5), time, theme))
        );

        // when & then
        assertThatNoException().isThrownBy(() -> waitingService.delete(saved.getId()));
    }

    @Test
    void 존재하지_않는_대기를_취소하면_404를_반환한다() {
        assertThatThrownBy(() -> waitingService.delete(999L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.insert(Theme.createWithoutId(name, description, thumbnail));
    }
}
