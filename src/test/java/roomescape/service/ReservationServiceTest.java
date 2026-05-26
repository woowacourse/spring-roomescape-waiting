package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.repository.FakeReservationDao;
import roomescape.repository.FakeThemeDao;
import roomescape.repository.FakeThemeSlotDao;
import roomescape.repository.FakeTimeDao;
import roomescape.service.ReservationService;

class ReservationServiceTest {

    private ReservationService reservationService;
    private Time savedTime;
    private Theme savedTheme;
    private ThemeSlot savedThemeSlot;

    @BeforeEach
    void setUp() {
        FakeTimeDao fakeReservationTimeDao = new FakeTimeDao();
        FakeThemeDao fakeThemeDao = new FakeThemeDao();
        FakeThemeSlotDao fakeThemeSlotDao = new FakeThemeSlotDao();

        reservationService = new ReservationService(
                new FakeReservationDao(),
                fakeThemeSlotDao
        );
        savedTime = fakeReservationTimeDao.save(Time.of(LocalTime.of(10, 0)));
        savedTheme = fakeThemeDao.save(new Theme("이름", "설명", "test.com"));
        savedThemeSlot = fakeThemeSlotDao.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(1), savedTime, false));
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 조립한 뒤 예약을 생성한다.")
    void saveReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        assertThat(reservation.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약을 식별자를 통해 삭제하면 목록에서 사라진다.")
    void removeReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        reservationService.removeReservation(reservation.getId());
        assertThat(reservationService.allReservations()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하여 반환한다.")
    void allReservations() {
        reservationService.saveReservation("브라운", savedThemeSlot.getId());
        List<Reservation> reservations = reservationService.allReservations();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 객체를 조회한다.")
    void findReservation() {
        Reservation savedReservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        Reservation foundReservation = reservationService.findReservation(savedReservation.getId());
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }
}
