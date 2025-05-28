package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.repository.JpaReservationSlots;
import roomescape.infrastructure.repository.JpaReservationTimes;
import roomescape.infrastructure.repository.JpaReservations;
import roomescape.infrastructure.repository.JpaThemes;
import roomescape.infrastructure.repository.JpaUsers;
import roomescape.test_util.JpaTestUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaReservations.class, JpaTestUtil.class, JpaReservationTimes.class, JpaThemes.class, JpaUsers.class})
class ReservationsTest {

    private static final LocalDate DATE1 = LocalDate.now().plusDays(3);
    private static final LocalDate DATE2 = LocalDate.now().plusDays(4);
    private static final LocalDate DATE3 = LocalDate.now().plusDays(5);

    @Autowired
    private Reservations sut;
    @Autowired
    private JpaTestUtil testUtil;
    @Autowired
    private ReservationTimes reservationTimes;
    @Autowired
    private Themes themes;
    @Autowired
    private Users users;
    @Autowired
    private JpaReservationSlots reservationSlots;

    @AfterEach
    void tearDown() {
        testUtil.deleteAll();
    }

    @Test
    void 예약을_저장할_수_있다() {
        // given
        final ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("스릴러", "", "");
        final User user = User.member("돔푸", "dompoo@email.com", "password");
        final ReservationSlot slot = new ReservationSlot(time, DATE1, theme);
        reservationTimes.save(time);
        themes.save(theme);
        users.save(user);
        reservationSlots.save(slot);

        // when, then
        assertThatCode(() -> sut.save(new Reservation(user, slot)))
                .doesNotThrowAnyException();
    }

    @Test
    void ID_기준으로_예약을_찾을_수_있다() {
        // given
        String themeId = testUtil.insertTheme();
        String timeId = testUtil.insertReservationTime();
        String userId = testUtil.insertUser();
        String slotId = testUtil.insertSlot(DATE1, timeId, themeId);
        String reservationId = testUtil.insertReservation(slotId, userId);

        // when
        final Optional<Reservation> result = sut.findById(Id.create(reservationId));

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId().value()).isEqualTo(reservationId);
    }

    @Test
    void 시간_ID_기준으로_존재하는지_확인할_수_있다() {
        // given
        String themeId = testUtil.insertTheme();
        String timeId = testUtil.insertReservationTime();
        String userId = testUtil.insertUser();
        String slotId = testUtil.insertSlot(DATE1, timeId, themeId);
        String reservationId = testUtil.insertReservation(slotId, userId);

        // when
        final boolean result = sut.existByTimeId(Id.create(timeId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 테마_ID_기준으로_존재하는지_확인할_수_있다() {
        // given
        String themeId = testUtil.insertTheme();
        String timeId = testUtil.insertReservationTime();
        String userId = testUtil.insertUser();
        String slotId = testUtil.insertSlot(DATE1, timeId, themeId);
        String reservationId = testUtil.insertReservation(slotId, userId);

        // when
        final boolean result = sut.existByThemeId(Id.create(themeId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void ID를_통해_예약을_삭제할_수_있다() {
        // given
        String themeId = testUtil.insertTheme();
        String timeId = testUtil.insertReservationTime();
        String userId = testUtil.insertUser();
        String slotId = testUtil.insertSlot(DATE1, timeId, themeId);
        String reservationId = testUtil.insertReservation(slotId, userId);

        // when
        sut.deleteById(Id.create(reservationId));

        // then
        assertThat(testUtil.countReservation()).isZero();
    }
}
