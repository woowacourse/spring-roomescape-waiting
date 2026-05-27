package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.SlotDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingWithRankResponse;
import roomescape.exception.code.WaitingErrorCode;
import roomescape.exception.domain.WaitingException;

class WaitingServiceTest extends ServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private SlotDao slotDao;

    @Autowired
    private Clock clock;

    @Test
    void 이미_동일한_날짜와_테마에_대기를_신청한_경우_예외가_발생한다() {
        // given
        Theme theme = saveTheme("테마1");
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(10, 0));
        saveReservation("기존예약자", theme, reservationTime);

        WaitingRequest request = new WaitingRequest(LocalDate.now(clock), reservationTime.getId(), theme.getId(), "대기신청자");
        waitingService.create(request);

        // when & then
        assertThatThrownBy(() -> waitingService.create(request))
                .isInstanceOf(WaitingException.class)
                .hasMessage(WaitingErrorCode.WAITING_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 본인의_예약에_대기를_신청할_경우_예외가_발생한다() {
        // given
        Theme theme = saveTheme("테마1");
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(10, 0));
        saveReservation("기존예약자", theme, reservationTime);

        WaitingRequest request = new WaitingRequest(LocalDate.now(clock), reservationTime.getId(), theme.getId(), "기존예약자");

        // when & // then
        assertThatThrownBy(() -> waitingService.create(request))
                .isInstanceOf(WaitingException.class)
                .hasMessage(WaitingErrorCode.CANNOT_WAIT_OWN_RESERVATION.getMessage());
    }

    @Test
    void 이름에_해당하는_대기순번_목록을_조회한다() {
        // given
        Theme theme = saveTheme("테마1");
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(10, 0));
        saveReservation("예약자", theme, reservationTime);

        String testUser = "첫번째대기신청자";
        saveWaiting(reservationTime, theme, testUser);
        saveWaiting(reservationTime, theme, "두번째대기신청자");
        saveWaiting(reservationTime, theme, "세번째대기신청자");

        // when
        List<WaitingWithRankResponse> response = waitingService.getWaitingsByName(testUser);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getFirst().rank()).isEqualTo(1);
            softly.assertThat(response.getFirst().name()).isEqualTo(testUser);
        });

    }

    private Theme saveTheme(String name) {
        Theme theme = new Theme(
                name,
                "설명",
                "https://dsf.sdaf"
        );
        return themeDao.save(theme);
    }

    private ReservationTime saveReservationTime(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeDao.save(reservationTime);
    }

    private Slot saveSlot(LocalDate date, ReservationTime time, Theme theme) {
        Slot slot = new Slot(date, time, theme);
        return slotDao.save(slot);
    }

    private Reservation saveReservation(
            String name,
            Theme theme,
            ReservationTime reservationTime
    ) {
        Slot savedSlot = saveSlot(LocalDate.now(clock), reservationTime, theme);
        Reservation reservation = new Reservation(
                savedSlot,
                name
        );
        return reservationDao.save(reservation);
    }

    private WaitingRequest saveWaiting(ReservationTime reservationTime, Theme theme, String name) {
        WaitingRequest request1 = new WaitingRequest(LocalDate.now(clock), reservationTime.getId(), theme.getId(), name);
        waitingService.create(request1);
        return request1;
    }
}
