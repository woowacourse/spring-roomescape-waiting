package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.JdbcReservationTimeRepository;
import roomescape.test_util.JdbcTestUtil;

@JdbcTest
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {

    private static final LocalTime TIME1 = LocalTime.of(10, 0);
    private static final LocalTime TIME2 = LocalTime.of(15, 0);

    private final JdbcReservationTimeRepository sut;
    private final JdbcTestUtil testUtil;

    @Autowired
    public JdbcReservationTimeRepositoryTest(final JdbcReservationTimeRepository sut, final JdbcTemplate jdbcTemplate) {
        this.sut = sut;
        this.testUtil = new JdbcTestUtil(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        testUtil.deleteAll();
    }

    @Test
    void 예약_시간을_저장할_수_있다() {
        assertThatCode(() -> sut.save(ReservationTime.create(TIME1)))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_예약_시간을_찾을_수_있다() {
        // given
        String timeId1 = "1";
        String timeId2 = "2";

        testUtil.insertReservationTime(timeId1, TIME1);
        testUtil.insertReservationTime(timeId2, TIME2);

        // when
        final List<ReservationTime> result = sut.findAll();

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getId().value()).isEqualTo(timeId1);
        assertThat(result.get(1).getId().value()).isEqualTo(timeId2);
    }

    @Test
    void 해당_날짜와_테마에_예약되지_않은_모든_예약_시간을_찾을_수_있다() {
        // given
        String timeId1 = generateId();
        String timeId2 = generateId();
        String timeId3 = generateId();
        String themeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertReservationTime(timeId1, LocalTime.of(10, 0));
        testUtil.insertReservationTime(timeId2, LocalTime.of(12, 0));
        testUtil.insertReservationTime(timeId3, LocalTime.of(14, 0));
        testUtil.insertTheme(themeId, "주홍색 연구");
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, LocalDate.now().plusDays(10), timeId1, themeId, userId);

        // when
        final List<ReservationTime> result = sut.findAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId));

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getId().value()).isEqualTo(timeId2);
        assertThat(result.get(1).getId().value()).isEqualTo(timeId3);
    }

    @Test
    void 해당_날짜와_테마에_예약된_모든_예약_시간을_찾을_수_있다() {
        // given
        String timeId1 = generateId();
        String timeId2 = generateId();
        String timeId3 = generateId();
        String themeId = generateId();
        String userId = generateId();
        String reservationId = generateId();

        testUtil.insertReservationTime(timeId1, LocalTime.of(10, 0));
        testUtil.insertReservationTime(timeId2, LocalTime.of(12, 0));
        testUtil.insertReservationTime(timeId3, LocalTime.of(14, 0));
        testUtil.insertTheme(themeId, "주홍색 연구");
        testUtil.insertUser(userId, "돔푸");
        testUtil.insertReservation(reservationId, LocalDate.now().plusDays(10), timeId1, themeId, userId);

        // when
        final List<ReservationTime> result = sut.findNotAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId));

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst().getId().value()).isEqualTo(timeId1);
    }

    @Test
    void ID_기준으로_예약_시간을_찾을_수_있다() {
        // given
        String timeId = generateId();
        testUtil.insertReservationTime(timeId, TIME1);

        // when
        final Optional<ReservationTime> result = sut.findById(Id.create(timeId));

        // then
        assertThat(result.isPresent()).isTrue();
        final ReservationTime reservationTime = result.get();
        assertThat(reservationTime.getId().value()).isEqualTo(timeId);
        assertThat(reservationTime.getStartTime().value()).isEqualTo(TIME1);
    }

    @Test
    void ID_기준으로_존재하는지_확인할_수_있다() {
        // given
        String timeId = generateId();
        testUtil.insertReservationTime(timeId, TIME1);

        // when
        final boolean result = sut.existById(Id.create(timeId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 시간_기준으로_존재하는지_확인할_수_있다() {
        // given
        String timeId = generateId();
        testUtil.insertReservationTime(timeId, TIME1);

        // when
        final boolean result = sut.existByTime(TIME1);

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 29})
    void 두_시간_사이에_존재하는지_확인할_수_있다(int minute) {
        // given
        String timeId = generateId();
        testUtil.insertReservationTime(timeId, LocalTime.of(10, minute));

        // when
        final boolean result = sut.existBetween(LocalTime.of(10, 0), LocalTime.of(10, 30));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void ID를_통해_예약_시간을_삭제할_수_있다() {
        // given
        String timeId = generateId();
        testUtil.insertReservationTime(timeId, TIME1);

        // when
        sut.deleteById(Id.create(timeId));

        // then
        assertThat(testUtil.countReservationTime()).isEqualTo(0);
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
