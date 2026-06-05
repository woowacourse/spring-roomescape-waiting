package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
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
    void findAllByUserId_예약_확정과_예약_대기를_상태와_예약일시_생성순으로_정렬해_조회한다() {
        Long brown = DbFixtures.insertMember(jdbcTemplate, "브라운"); //주인공
        Long charles = DbFixtures.insertMember(jdbcTemplate, "샤를");
        Long aron = DbFixtures.insertMember(jdbcTemplate, "아론");
        Long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        Long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");
        Long time = DbFixtures.insertTime(jdbcTemplate, "10:00");

        long reservedBrownId = DbFixtures.insertReservation(jdbcTemplate, brown, themeA, "2026-06-01", time,
                ReservationStatus.RESERVED.name());
        DbFixtures.insertReservation(jdbcTemplate, charles, themeB, "2026-06-01", time,
                ReservationStatus.RESERVED.name());
        DbFixtures.insertReservation(jdbcTemplate, aron, themeB, "2026-06-01", time, ReservationStatus.WAITING.name());
        long waitingBrownId = DbFixtures.insertReservation(jdbcTemplate, brown, themeB, "2026-06-01", time,
                ReservationStatus.WAITING.name());

        List<Reservation> results = repository.findAllByUserId(brown);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(reservation -> reservation.getUser().getName())
                .containsOnly("브라운");
        assertThat(results).extracting(Reservation::getId)
                .containsExactly(reservedBrownId, waitingBrownId);
        assertThat(results).extracting(
                        Reservation::getStatus,
                        Reservation::getDate,
                        reservation -> reservation.getTime().getStartAt(),
                        reservation -> reservation.getTheme().getName()
                )
                .containsExactly(
                        tuple(ReservationStatus.RESERVED, LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), "A"),
                        tuple(ReservationStatus.WAITING, LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), "B")
                );
    }

    @Test
    void findWaitingReservationsWithOrderByUserId_두_개의_슬롯에_건_대기는_슬롯별로_순번을_독립_계산한다() {
        Long brown = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long charles = DbFixtures.insertMember(jdbcTemplate, "샤를");
        Long aron = DbFixtures.insertMember(jdbcTemplate, "아론");
        Long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        Long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");
        Long time = DbFixtures.insertTime(jdbcTemplate, "10:00");

        long slotAFirstWaitingId = DbFixtures.insertReservation(jdbcTemplate, aron, themeA, "2026-06-01", time,
                ReservationStatus.WAITING.name());
        long slotABrownWaitingId = DbFixtures.insertReservation(jdbcTemplate, brown, themeA, "2026-06-01", time,
                ReservationStatus.WAITING.name());
        long slotBFirstWaitingId = DbFixtures.insertReservation(jdbcTemplate, charles, themeB, "2026-06-02", time,
                ReservationStatus.WAITING.name());
        long slotBSecondWaitingId = DbFixtures.insertReservation(jdbcTemplate, aron, themeB, "2026-06-02", time,
                ReservationStatus.WAITING.name());
        long slotBBrownWaitingId = DbFixtures.insertReservation(jdbcTemplate, brown, themeB, "2026-06-02", time,
                ReservationStatus.WAITING.name());

        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 09:00:00",
                slotAFirstWaitingId);
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 10:00:00",
                slotABrownWaitingId);
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 09:00:00",
                slotBFirstWaitingId);
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 10:00:00",
                slotBSecondWaitingId);
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 11:00:00",
                slotBBrownWaitingId);

        Map<Reservation, Integer> results = repository.findWaitingReservationsWithOrderByUserId(brown);

        assertThat(results).hasSize(2);
        assertThat(results.keySet()).extracting(Reservation::getId)
                .containsExactly(slotABrownWaitingId, slotBBrownWaitingId);
        assertThat(results.values()).containsExactly(2, 3);
        assertThat(results.keySet()).extracting(
                        Reservation::getStatus,
                        Reservation::getDate,
                        reservation -> reservation.getTheme().getName()
                )
                .containsExactly(
                        tuple(ReservationStatus.WAITING, LocalDate.of(2026, 6, 1), "A"),
                        tuple(ReservationStatus.WAITING, LocalDate.of(2026, 6, 2), "B")
                );
    }

    @Test
    void findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate_가장_먼저_등록된_대기를_반환한다() {
        Long brown = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long charles = DbFixtures.insertMember(jdbcTemplate, "샤를");
        Long aron = DbFixtures.insertMember(jdbcTemplate, "아론");
        Long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        Long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");
        Long time = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeId = DbFixtures.defaultStoreId(jdbcTemplate);
        LocalDate date = LocalDate.of(2026, 6, 1);
        DbFixtures.insertReservation(jdbcTemplate, brown, themeA, "2026-06-01", time,
                ReservationStatus.RESERVED.name());
        Long firstWaitingId = DbFixtures.insertReservation(jdbcTemplate, charles, themeA, "2026-06-01", time,
                ReservationStatus.WAITING.name());
        Long secondWaitingId = DbFixtures.insertReservation(jdbcTemplate, aron, themeA, "2026-06-01", time,
                ReservationStatus.WAITING.name());
        DbFixtures.insertReservation(jdbcTemplate, brown, themeB, "2026-06-01", time,
                ReservationStatus.WAITING.name());

        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 09:00:00",
                secondWaitingId);
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 08:00:00",
                firstWaitingId);

        Reservation result = repository.findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate(
                date, time, themeA, storeId).orElseThrow();

        assertThat(result.getId()).isEqualTo(firstWaitingId);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate_매칭되는_대기가_없으면_empty를_반환한다() {
        Long brown = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeId = DbFixtures.defaultStoreId(jdbcTemplate);
        DbFixtures.insertReservation(jdbcTemplate, brown, themeId, "2026-06-01", timeId,
                ReservationStatus.RESERVED.name());

        assertThat(repository.findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate(
                LocalDate.of(2026, 6, 1), timeId, themeId, storeId)).isEmpty();
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

        int affected = repository.update(Fixtures.reservation(
                Fixtures.memberWithId(userId, "브라운"),
                new Theme(themeB, "B", "설명", "https://thumbnail.url"),
                LocalDate.of(2026, 6, 2),
                new ReservationTime(time2, LocalTime.of(11, 0)),
                Fixtures.storeWithId(storeId, "매장"),
                ReservationStatus.WAITING
        ).withId(reservationId));

        assertThat(affected).isEqualTo(1);
        Reservation found = repository.findById(reservationId).orElseThrow();
        assertThat(found.getDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(found.getTheme().getId()).isEqualTo(themeB);
        assertThat(found.getTime().getId()).isEqualTo(time2);
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void updateWaitingToReserved_예약_대기를_예약_확정으로_변경한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long reservationId = DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-06-01", timeId,
                ReservationStatus.WAITING.name());
        Reservation waitingReservation = repository.findById(reservationId).orElseThrow();

        int affected = repository.updateWaitingToReserved(waitingReservation.confirm());

        assertThat(affected).isEqualTo(1);
        assertThat(repository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void updateWaitingToReserved_예약_대기가_아니면_0을_반환한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long reservationId = DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-06-01", timeId,
                ReservationStatus.RESERVED.name());
        Reservation reservation = repository.findById(reservationId).orElseThrow();

        int affected = repository.updateWaitingToReserved(reservation);

        assertThat(affected).isZero();
        assertThat(repository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
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
    void existsReservedByDateAndTimeAndThemeAndStore_같은_확정_예약이_있으면_true() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "강남점");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", timeId, storeId);

        boolean exists = repository.existsReservedByDateAndTimeAndThemeAndStore(
                LocalDate.of(2026, 5, 6), timeId, themeId, storeId);

        assertThat(exists).isTrue();
    }

    @Test
    void existsReservedByDateAndTimeAndThemeAndStore_같은_확정_예약이_없으면_false() {
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "강남점");

        boolean exists = repository.existsReservedByDateAndTimeAndThemeAndStore(
                LocalDate.of(2026, 5, 6), timeId, themeId, storeId);

        assertThat(exists).isFalse();
    }

    @Test
    void existsReservedByDateAndTimeAndThemeAndStore_같은_조합의_대기만_있으면_false() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeId = DbFixtures.insertStore(jdbcTemplate, "강남점");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-06", timeId, storeId,
                ReservationStatus.WAITING.name());

        boolean exists = repository.existsReservedByDateAndTimeAndThemeAndStore(
                LocalDate.of(2026, 5, 6), timeId, themeId, storeId);

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
}
