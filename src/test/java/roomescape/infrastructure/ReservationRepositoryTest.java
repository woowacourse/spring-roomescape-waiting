package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ReservationTimeRepository;
import roomescape.business.model.repository.ThemeRepository;
import roomescape.business.model.repository.UserRepository;
import roomescape.business.model.vo.Id;
import roomescape.test_util.JpaTestUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaReservationRepository.class, JpaTestUtil.class, JpaReservationTimeRepository.class, JpaThemeRepository.class, JpaUserRepository.class})
class ReservationRepositoryTest {

    private static final LocalDate DATE1 = LocalDate.now().plusDays(3);
    private static final LocalDate DATE2 = LocalDate.now().plusDays(4);
    private static final LocalDate DATE3 = LocalDate.now().plusDays(5);
    private static final LocalTime TIME = LocalTime.of(10, 0);

    private final ReservationRepository sut;
    private final JpaTestUtil testUtil;

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    @Autowired
    ReservationRepositoryTest(ReservationRepository sut, ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository, UserRepository userRepository, JpaTestUtil testUtil) {
        this.sut = sut;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
        this.testUtil = testUtil;
    }

    @AfterEach
    void tearDown() {
        testUtil.deleteAll();
    }

    @Test
    void 예약을_저장할_수_있다() {
        // given
        final ReservationTime time = ReservationTime.create(TIME);
        final Theme theme = Theme.create("호러", "", "");
        final User user = User.create("돔푸", "dompoo@email.com", "password");
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        userRepository.save(user);

        // when, then
        assertThatCode(() -> sut.save(Reservation.create(user, DATE1, time, theme)))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_예약을_찾을_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId1 = generateId();
        String userId2 = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId1, "돔푸");
        testUtil.insertUser(userId2, "레몬");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId, userId1);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId, userId2);

        // when
        final List<Reservation> result = sut.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.getId().value()).containsExactlyInAnyOrder(reservationId1, reservationId2);
    }

    @Test
    void 필터없이_모든_예약을_찾을_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId1 = generateId();
        String userId2 = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId1, "돔푸");
        testUtil.insertUser(userId2, "레몬");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId, userId1);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId, userId2);

        // when
        final List<Reservation> result = sut.findAllWithFilter(null, null, null, null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.getId().value()).containsExactlyInAnyOrder(reservationId1, reservationId2);
    }

    @Test
    void 테마_ID로_필터링_할_수_있다() {
        // given
        String themeId1 = generateId();
        String themeId2 = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();

        testUtil.insertTheme(themeId1, "호러");
        testUtil.insertTheme(themeId2, "스릴러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId1, userId);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId2, userId);

        // when
        final List<Reservation> result = sut.findAllWithFilter(Id.create(themeId1), null, null, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId().value()).isEqualTo(reservationId1);
    }

    @Test
    void 사용자_ID로_필터링_할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId1 = generateId();
        String userId2 = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId1, "돔푸");
        testUtil.insertUser(userId2, "레몬");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId, userId1);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId, userId2);

        // when
        final List<Reservation> result = sut.findAllWithFilter(null, Id.create(userId2), null, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getName().value()).isEqualTo("레몬");
    }

    @Test
    void 날짜_범위로_필터링_할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();
        String reservationId3 = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId, userId);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId, userId);
        testUtil.insertReservation(reservationId3, DATE3, timeId, themeId, userId);

        // when
        final List<Reservation> result = sut.findAllWithFilter(null, null, DATE2, DATE3);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.getId().value()).containsExactlyInAnyOrder(reservationId2, reservationId3);
    }

    @Test
    void 시작_날짜만으로_필터링_할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();
        String reservationId3 = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId, userId);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId, userId);
        testUtil.insertReservation(reservationId3, DATE3, timeId, themeId, userId);

        // when
        final List<Reservation> result = sut.findAllWithFilter(null, null, DATE2, null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.getId().value()).containsExactlyInAnyOrder(reservationId2, reservationId3);
    }

    @Test
    void 종료_날짜만으로_필터링_할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();
        String reservationId3 = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId, userId);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId, userId);
        testUtil.insertReservation(reservationId3, DATE3, timeId, themeId, userId);

        // when
        final List<Reservation> result = sut.findAllWithFilter(null, null, null, DATE2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.getId().value()).containsExactlyInAnyOrder(reservationId1, reservationId2);
    }

    @Test
    void 여러_필터를_함께_적용할_수_있다() {
        // given
        String themeId1 = generateId();
        String themeId2 = generateId();
        String timeId = generateId();
        String userId1 = generateId();
        String userId2 = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();
        String reservationId3 = generateId();

        testUtil.insertTheme(themeId1, "호러");
        testUtil.insertTheme(themeId2, "스릴러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId1, "돔푸");
        testUtil.insertUser(userId2, "레몬");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId1, userId1);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId1, userId2);
        testUtil.insertReservation(reservationId3, DATE3, timeId, themeId2, userId2);

        // when
        final List<Reservation> result = sut.findAllWithFilter(Id.create(themeId1), Id.create(userId2), null, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId().value()).isEqualTo(reservationId2);
        assertThat(result.get(0).getTheme().getId().value()).isEqualTo(themeId1);
        assertThat(result.get(0).getUser().getName().value()).isEqualTo("레몬");
    }

    @Test
    void 모든_필터를_함께_적용할_수_있다() {
        // given
        String themeId1 = generateId();
        String themeId2 = generateId();
        String timeId = generateId();
        String userId1 = generateId();
        String userId2 = generateId();
        String reservationId1 = generateId();
        String reservationId2 = generateId();
        String reservationId3 = generateId();
        String reservationId4 = generateId();

        testUtil.insertTheme(themeId1, "호러");
        testUtil.insertTheme(themeId2, "스릴러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId1, "돔푸");
        testUtil.insertUser(userId2, "레몬");
        testUtil.insertReservation(reservationId1, DATE1, timeId, themeId1, userId1);
        testUtil.insertReservation(reservationId2, DATE2, timeId, themeId1, userId2);
        testUtil.insertReservation(reservationId3, DATE3, timeId, themeId2, userId2);
        testUtil.insertReservation(reservationId4, DATE3, timeId, themeId1, userId2);

        // when
        final List<Reservation> result = sut.findAllWithFilter(Id.create(themeId1), Id.create(userId2), DATE2, DATE2);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId().value()).isEqualTo(reservationId2);
        assertThat(result.get(0).getTheme().getId().value()).isEqualTo(themeId1);
        assertThat(result.get(0).getUser().getName().value()).isEqualTo("레몬");
        assertThat(result.get(0).getDate().value()).isEqualTo(DATE2);
    }

    @Test
    void ID_기준으로_예약을_찾을_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, DATE1, timeId, themeId, userId);

        // when
        final Optional<Reservation> result = sut.findById(Id.create(reservationId));

        // then
        assertThat(result).isPresent();
        final Reservation reservation = result.get();
        assertThat(reservation.getId().value()).isEqualTo(reservationId);
        assertThat(reservation.getDate().value()).isEqualTo(DATE1);
        assertThat(reservation.getTime().getId().value()).isEqualTo(timeId);
        assertThat(reservation.getTheme().getId().value()).isEqualTo(themeId);
    }

    @Test
    void ID_기준으로_존재하는지_확인할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, DATE1, timeId, themeId, userId);

        // when
        final boolean result = sut.existById(Id.create(reservationId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 시간_ID_기준으로_존재하는지_확인할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, DATE1, timeId, themeId, userId);

        // when
        final boolean result = sut.existByTimeId(Id.create(timeId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 테마_ID_기준으로_존재하는지_확인할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, DATE1, timeId, themeId, userId);

        // when
        final boolean result = sut.existByThemeId(Id.create(themeId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 시간_날짜_테마_기준으로_존재하는지_확인할_수_있다() {
        // given
        String timeId = generateId();
        String themeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, DATE1, timeId, themeId, userId);
        Theme theme = Theme.restore(themeId, "호러", "", "");

        // when
        final boolean result = sut.isDuplicateDateAndTimeAndTheme(DATE1, TIME, theme.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void ID를_통해_예약을_삭제할_수_있다() {
        // given
        String themeId = generateId();
        String timeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertTheme(themeId, "호러");
        testUtil.insertReservationTime(timeId, TIME);
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, DATE1, timeId, themeId, userId);

        // when
        sut.deleteById(Id.create(reservationId));

        // then
        assertThat(testUtil.countReservation()).isZero();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
