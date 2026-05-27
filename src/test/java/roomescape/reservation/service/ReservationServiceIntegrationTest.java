package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorInformation;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.util.List;

@Import(ReservationService.class)
class ReservationServiceIntegrationTest extends ServiceSupport {

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("나의 예약 목록을 조회하면, 대기 순번을 조회한다.")
    void getMyReservations() {
        // given
        String themeName = "테마1";
        String name1 = "사람1";
        String name2 = "사람2";
        String myName = "사람3";

        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        Theme theme = saveTheme(themeName);

        reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));
        reservationRepository.save(ReservationFixture.waitReservation(name2, date, time, theme));
        reservationRepository.save(ReservationFixture.waitReservation(myName, date, time, theme));

        // when
        List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(myName);

        // then
        Assertions.assertThat(actual.getFirst().waitingTurn())
            .isEqualTo(2);
    }

    @Test
    @DisplayName("나의 예약 목록을 조회할때, 예약상태면 대기 순번이 없다.")
    void getMyReservations_no_waiting_turn() {
        // given
        String themeName = "테마1";
        String name1 = "사람1";

        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        Theme theme = saveTheme(themeName);

        reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));

        // when
        List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name1);

        // then
        Assertions.assertThat(actual.getFirst().waitingTurn())
            .isNull();
    }

    @Test
    @DisplayName("이미 슬롯에 내 예약이 있으면 예약할 수 없다.")
    void reserve_duplicated() {
        // given
        String themeName = "테마1";
        String name1 = "사람1";

        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        Theme theme = saveTheme(themeName);

        reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));
        ReservationSaveCommand command = new ReservationSaveCommand(date.getId(), time.getId(),
            theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(name1, command))
            .isInstanceOf(ReservationException.class)
            .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_BOOKED.getMessage());
    }

    @Test
    @DisplayName("대기 상태인 예약은 변경할 수 없다.")
    void waiting_reserve_not_changeable() {
        // given
        String themeName = "테마1";
        String name1 = "사람1";

        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        Theme theme = saveTheme(themeName);

        Reservation saved = reservationRepository.save(ReservationFixture.waitReservation(name1, date, time, theme));
        ReservationChangeCommand command = new ReservationChangeCommand(
                saved.getId(), saved.getName(), saved.getDate().getId(), saved.getTime().getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.changeSchedule(command))
            .isInstanceOf(ReservationException.class)
            .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_WAITING.getMessage());
    }

}
