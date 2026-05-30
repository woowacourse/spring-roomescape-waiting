package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationWaitingDao;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.response.ReservationWaitingResponse;
import roomescape.dao.ReservationDao;
import roomescape.domain.Reservation;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationTimeDao timeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationWaitingService reservationWaitingService;
    @Autowired
    private ReservationWaitingDao reservationWaitingDao;
    @Autowired
    private ReservationDao reservationDao;

    @Test
    void 예약_대기를_추가한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationDao.insert(Reservation.createWithoutId(
                "브라운", LocalDate.of(2026, 6, 10), time, theme
        ));
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        );

        // when
        ReservationWaitingResponse response = reservationWaitingService.addReservationWaiting(command, LocalDateTime.now());

        // then
        assertThat(response)
                .extracting(ReservationWaitingResponse::name, ReservationWaitingResponse::reservationDate, waitingResponse -> waitingResponse.time().id(), waitingResponse -> waitingResponse.theme().id())
                .containsExactly("맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId());
    }

    @Test
    void 예약_대기를_취소한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId("브라운", LocalDateTime.now(), LocalDate.of(2026, 5, 5), time, theme);
        ReservationWaiting saved = reservationWaitingDao.insert(reservationWaiting);

        assertThatNoException().isThrownBy(() -> reservationWaitingService.delete(saved.getId()));
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail));
    }
}
