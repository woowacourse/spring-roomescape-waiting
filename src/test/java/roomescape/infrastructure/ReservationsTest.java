package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
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
import roomescape.infrastructure.jpa.JpaReservationSlots;
import roomescape.infrastructure.jpa.JpaReservationTimes;
import roomescape.infrastructure.jpa.JpaReservations;
import roomescape.infrastructure.jpa.JpaThemes;
import roomescape.infrastructure.jpa.JpaUsers;
import roomescape.test_util.JpaTestUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    @Nested
    class 필터링_조회_테스트 {

        @Test
        void 테마아이디로_예약을_필터링할_수_있다() {
            // given
            String themeId1 = testUtil.insertTheme();
            String themeId2 = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId1);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId2);
            String reservationId1 = testUtil.insertReservation(slotId1, userId);
            String reservationId2 = testUtil.insertReservation(slotId2, userId);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(Id.create(themeId1), null, null, null);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId1);
        }

        @Test
        void 사용자아이디로_예약을_필터링할_수_있다() {
            // given
            String themeId = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId1 = testUtil.insertUser();
            String userId2 = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId);
            String reservationId1 = testUtil.insertReservation(slotId1, userId1);
            String reservationId2 = testUtil.insertReservation(slotId2, userId2);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(null, Id.create(userId1), null, null);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId1);
        }

        @Test
        void 날짜범위로_예약을_필터링할_수_있다() {
            // given
            String themeId = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId);
            String slotId3 = testUtil.insertSlot(DATE3, timeId, themeId);
            String reservationId1 = testUtil.insertReservation(slotId1, userId);
            String reservationId2 = testUtil.insertReservation(slotId2, userId);
            String reservationId3 = testUtil.insertReservation(slotId3, userId);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(null, null, DATE1, DATE2);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId1, reservationId2);
        }

        @Test
        void 시작_날짜만으로_필터링_할_수_있다() {
            // given
            String themeId = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId);
            String slotId3 = testUtil.insertSlot(DATE3, timeId, themeId);
            String reservationId1 = testUtil.insertReservation(slotId1, userId);
            String reservationId2 = testUtil.insertReservation(slotId2, userId);
            String reservationId3 = testUtil.insertReservation(slotId3, userId);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(null, null, DATE2, null);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId2, reservationId3);
        }

        @Test
        void 종료_날짜만으로_필터링_할_수_있다() {
            // given
            String themeId = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId);
            String slotId3 = testUtil.insertSlot(DATE3, timeId, themeId);
            String reservationId1 = testUtil.insertReservation(slotId1, userId);
            String reservationId2 = testUtil.insertReservation(slotId2, userId);
            String reservationId3 = testUtil.insertReservation(slotId3, userId);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(null, null, null, DATE2);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId1, reservationId2);
        }

        @Test
        void 여러_필터를_조합하여_예약을_검색할_수_있다() {
            // given
            String themeId1 = testUtil.insertTheme();
            String themeId2 = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId1 = testUtil.insertUser();
            String userId2 = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId1);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId1);
            String slotId3 = testUtil.insertSlot(DATE2, timeId, themeId2);
            String slotId4 = testUtil.insertSlot(DATE3, timeId, themeId2);
            String reservationId1 = testUtil.insertReservation(slotId1, userId1);
            String reservationId2 = testUtil.insertReservation(slotId2, userId1);
            String reservationId3 = testUtil.insertReservation(slotId3, userId2);
            String reservationId4 = testUtil.insertReservation(slotId4, userId2);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(Id.create(themeId1), Id.create(userId1), DATE1, DATE2);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId1, reservationId2);
        }

        @Test
        void 필터없이_모든_예약을_찾을_수_있다() {
            // given
            String themeId = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId1 = testUtil.insertUser();
            String userId2 = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId);
            String reservationId1 = testUtil.insertReservation(slotId1, userId1);
            String reservationId2 = testUtil.insertReservation(slotId2, userId2);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(null, null, null, null);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId1, reservationId2);
        }

        @Test
        void 여러_필터를_함께_적용할_수_있다() {
            // given
            String themeId1 = testUtil.insertTheme();
            String themeId2 = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId1 = testUtil.insertUser();
            String userId2 = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId1);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId1);
            String slotId3 = testUtil.insertSlot(DATE3, timeId, themeId2);
            String reservationId1 = testUtil.insertReservation(slotId1, userId1);
            String reservationId2 = testUtil.insertReservation(slotId2, userId2);
            String reservationId3 = testUtil.insertReservation(slotId3, userId2);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(Id.create(themeId1), Id.create(userId2), null, null);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId2);
        }

        @Test
        void 모든_필터를_함께_적용할_수_있다() {
            // given
            String themeId1 = testUtil.insertTheme();
            String themeId2 = testUtil.insertTheme();
            String timeId = testUtil.insertReservationTime();
            String userId1 = testUtil.insertUser();
            String userId2 = testUtil.insertUser();
            String slotId1 = testUtil.insertSlot(DATE1, timeId, themeId1);
            String slotId2 = testUtil.insertSlot(DATE2, timeId, themeId1);
            String slotId3 = testUtil.insertSlot(DATE3, timeId, themeId2);
            String slotId4 = testUtil.insertSlot(DATE3, timeId, themeId1);
            String reservationId1 = testUtil.insertReservation(slotId1, userId1);
            String reservationId2 = testUtil.insertReservation(slotId2, userId2);
            String reservationId3 = testUtil.insertReservation(slotId3, userId2);
            String reservationId4 = testUtil.insertReservation(slotId4, userId2);

            // when
            final List<Reservation> result = sut.findAllReservedWithFilter(Id.create(themeId1), Id.create(userId2), DATE2, DATE2);

            // then
            assertThat(result).extracting(r -> r.getId().value())
                    .containsExactlyInAnyOrder(reservationId2);
        }
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
