package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;

@JdbcTest
@Import(ReservationJdbcRepository.class)
class ReservationJdbcRepositoryTest {

    @Autowired
    private ReservationJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void save_저장하면_생성된_id를_반환한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "매장");

        Long id = repository.save(
                Fixtures.reservationOf(userId, themeId, timeId, storeId, LocalDate.of(2026, 5, 6)));

        assertThat(id).isPositive();
    }

    @Test
    void findById_조인된_theme과_time을_매핑하여_반환한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long reservationId = DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", timeId);

        Reservation found = repository.findById(reservationId).orElseThrow();

        assertThat(found.getId()).isEqualTo(reservationId);
        assertThat(found.getUser().getName()).isEqualTo("브라운");
        assertThat(found.getDate()).isEqualTo(LocalDate.of(2026, 5, 6));
        assertThat(found.getTheme().getId()).isEqualTo(themeId);
        assertThat(found.getTheme().getName()).isEqualTo("공포");
        assertThat(found.getTime().getId()).isEqualTo(timeId);
        assertThat(found.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void findById_없는_id이면_Optional_empty를_반환한다() {
        assertThat(repository.findById(9999L)).isEmpty();
    }

    @Test
    void findAllByStoreIds_페이징은_id_오름차순으로_limit과_offset을_적용한다() {
        Long userA = DbFixtures.insertMember(jdbcTemplate, "A");
        Long userB = DbFixtures.insertMember(jdbcTemplate, "B");
        Long userC = DbFixtures.insertMember(jdbcTemplate, "C");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        DbFixtures.insertReservation(jdbcTemplate, userA, themeId, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, userB, themeId, "2026-05-02", timeId);
        DbFixtures.insertReservation(jdbcTemplate, userC, themeId, "2026-05-03", timeId);
        Long storeId = DbFixtures.defaultStoreId(jdbcTemplate);

        List<Reservation> firstPage = repository.findAllByStoreIds(List.of(storeId), 2, 0);
        List<Reservation> secondPage = repository.findAllByStoreIds(List.of(storeId), 2, 2);

        assertThat(firstPage).extracting(r -> r.getUser().getName()).containsExactly("A", "B");
        assertThat(secondPage).extracting(r -> r.getUser().getName()).containsExactly("C");
    }

    @Test
    void findAllByStoreIds_다른_매장_예약은_제외한다() {
        Long userA = DbFixtures.insertMember(jdbcTemplate, "A");
        Long userB = DbFixtures.insertMember(jdbcTemplate, "B");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeA = DbFixtures.insertStore(jdbcTemplate, "A매장");
        Long storeB = DbFixtures.insertStore(jdbcTemplate, "B매장");
        DbFixtures.insertReservationInStore(jdbcTemplate, userA, themeId, "2026-05-01", timeId, storeA);
        DbFixtures.insertReservationInStore(jdbcTemplate, userB, themeId, "2026-05-02", timeId, storeB);

        List<Reservation> result = repository.findAllByStoreIds(List.of(storeA), 10, 0);

        assertThat(result).extracting(r -> r.getUser().getName()).containsExactly("A");
    }

    @Test
    void update_지정한_id의_필드를_갱신한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        Long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");
        Long time1 = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long time2 = DbFixtures.insertTime(jdbcTemplate, "11:00");
        Long reservationId = DbFixtures.insertReservation(jdbcTemplate, userId, themeA, "2026-06-01", time1);
        Long storeId = DbFixtures.defaultStoreId(jdbcTemplate);

        int affected = repository.update(
                Fixtures.reservationOf(userId, themeB, time2, storeId, LocalDate.of(2026, 6, 2))
                        .withId(reservationId));

        assertThat(affected).isEqualTo(1);
        Reservation found = repository.findById(reservationId).orElseThrow();
        assertThat(found.getDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(found.getTheme().getId()).isEqualTo(themeB);
        assertThat(found.getTime().getId()).isEqualTo(time2);
    }

    @Test
    void deleteById_삭제된_예약은_조회되지_않는다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long reservationId = DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", timeId);

        repository.deleteById(reservationId);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM reservation WHERE id = ?", Integer.class, reservationId);
        assertThat(count).isZero();
    }

    @Test
    void findTimeIdsByThemeIdAndDate_매칭하는_time_id만_반환한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "유저");
        Long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        Long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");
        Long time1 = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long time2 = DbFixtures.insertTime(jdbcTemplate, "11:00");
        Long time3 = DbFixtures.insertTime(jdbcTemplate, "12:00");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeA, "2026-05-06", time1);
        DbFixtures.insertReservation(jdbcTemplate, userId, themeA, "2026-05-06", time2);
        DbFixtures.insertReservation(jdbcTemplate, userId, themeA, "2026-05-07", time3);
        DbFixtures.insertReservation(jdbcTemplate, userId, themeB, "2026-05-06", time3);

        List<Long> timeIds = repository.findTimeIdsByThemeIdAndDate(themeA, LocalDate.of(2026, 5, 6));

        assertThat(timeIds).containsExactlyInAnyOrder(time1, time2);
    }

    @Test
    void existsByDateAndTimeIdAndThemeId_같은_조합이_있으면_true() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", timeId);

        boolean exists = repository.existsByDateAndTimeIdAndThemeId(
                LocalDate.of(2026, 5, 6), timeId, themeId);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId_같은_조합이_없으면_false() {
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");

        boolean exists = repository.existsByDateAndTimeIdAndThemeId(
                LocalDate.of(2026, 5, 6), timeId, themeId);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByReservationTimeId_해당_시간을_참조하는_예약이_있으면_true() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", timeId);

        assertThat(repository.existsByReservationTimeId(timeId)).isTrue();
    }

    @Test
    void existsByReservationTimeId_해당_시간을_참조하는_예약이_없으면_false() {
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");

        assertThat(repository.existsByReservationTimeId(timeId)).isFalse();
    }

    @Test
    void existsByReservationTimeId_다른_시간을_참조하는_예약만_있으면_false() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long usedTimeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long targetTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", usedTimeId);

        assertThat(repository.existsByReservationTimeId(targetTimeId)).isFalse();
    }


    @Test
    void existsByDateAndTimeAndThemeAndStoreAndUser_해당_방탈출_슬롯에_사용자가_이미_예약_혹은_대기를_건_경우_true() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "매장");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", timeId,
                storeId);

        assertThat(repository.existsByDateAndTimeAndThemeAndStoreAndUser(date, timeId, themeId,
                storeId, userId)).isTrue();
    }

    @Test
    void existsByDateAndTimeAndThemeAndStoreAndUser_해당_방탈출_슬롯에_사용자가_아직_예약_혹은_대기를_걸지_않은_경우_false() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "매장");

        assertThat(repository.existsByDateAndTimeAndThemeAndStoreAndUser(date, timeId, themeId,
                storeId, userId)).isFalse();
    }


    @Test
    void existsByDateAndTimeAndThemeAndStoreAndStatus_해당_방탈출_슬롯에_예약_확정자가_존재하는_경우_true() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "매장");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, date.toString(), timeId, storeId);

        assertThat(repository.existsByDateAndTimeAndThemeAndStoreAndStatus(date, timeId, themeId, storeId,
                ReservationStatus.RESERVED)).isTrue();
    }

    @Test
    void existsByDateAndTimeAndThemeAndStoreAndStatus_해당_방탈출_슬롯에_예약_확정자가_존재하지_않는_경우_false() {
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "매장");

        assertThat(repository.existsByDateAndTimeAndThemeAndStoreAndStatus(date, timeId, themeId, storeId,
                ReservationStatus.RESERVED)).isFalse();
    }
}
