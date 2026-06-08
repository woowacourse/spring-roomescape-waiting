package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.common.dto.PageResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingDto;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.test_config.fixture.SQLFixtureGenerator;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static roomescape.reservation.domain.Status.*;
import static roomescape.reservation.domain.Status.CONFIRMED;
import static roomescape.reservation.domain.Status.WAITING;

@JdbcTest
@Import({JdbcReservationRepository.class, SQLFixtureGenerator.class})
class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SQLFixtureGenerator sqlFixtureGenerator;


    @Test
    @DisplayName("id로 특정 예약을 조회한다.")
    public void findById() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme);
        Reservation reservation = sqlFixtureGenerator.insertReservation("브라운", reservationSlot, WAITING);

        // when
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservation.getId());

        // then
        assertThat(optionalReservation).isPresent();
        Reservation found = optionalReservation.get();
        assertThat(found)
                .extracting(
                        Reservation::getId, Reservation::getGuestName, Reservation::getDate,
                        Reservation::getTime, Reservation::getTheme
                ).containsExactly(
                        reservation.getId(), reservation.getGuestName(), reservation.getDate(),
                        reservation.getTime(), reservation.getTheme()
                );
    }

    @Test
    @DisplayName("예약의 목록 중 취소된 예약을 제외하고 페이지 단위로 조회한다")
    void findAllByStatusCanceledNotWithPaging() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme),
                CONFIRMED);
        sqlFixtureGenerator.insertReservation(
                "포비",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 6), time, theme),
                WAITING);
        Reservation included = sqlFixtureGenerator.insertReservation(
                "조이",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 7), time, theme),
                WAITING);
        Reservation excepted = sqlFixtureGenerator.insertReservation(
                "벨로",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 8), time, theme),
                CANCELED);

        // when
        PageResult<Reservation> pageResult = reservationRepository.findAllByStatusCanceledNot(2, 2);

        // then
        assertThat(pageResult.contents())
                .contains(included)
                .doesNotContain(excepted);
    }

    @Test
    @DisplayName("예약자 이름으로 예약 정보를 조회한다.")
    public void findByGuest() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme),
                WAITING);

        // when
        List<ReservationWaitingDto> reservationWaitingDtos = reservationRepository.findWaitingAllByGuestName(reservation.getGuestName());

        // then
        assertThat(reservationWaitingDtos)
                .extracting(
                        ReservationWaitingDto::id,
                        ReservationWaitingDto::guestName,
                        ReservationWaitingDto::date,
                        ReservationWaitingDto::time,
                        ReservationWaitingDto::theme
                ).containsExactly(
                        tuple(
                                reservation.getId(),
                                reservation.getGuestName(),
                                reservation.getDate(),
                                reservation.getTime(),
                                reservation.getTheme()
                        )
                );
    }

    @Test
    @DisplayName("예약자 이름으로 예약 정보를 조회할 때 전체 대기열 기준 순번을 반환한다.")
    public void findByGuest_waitNumber() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 5);

        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(date, time, theme);
        sqlFixtureGenerator.insertReservation(
                "브라운", reservationSlot, CONFIRMED, LocalDateTime.of(2023, 8, 1, 9, 0));
        sqlFixtureGenerator.insertReservation(
                "주디", reservationSlot, WAITING, LocalDateTime.of(2023, 8, 1, 9, 1));
        Reservation reservation = sqlFixtureGenerator.insertReservation(
                "초코칩", reservationSlot, WAITING, LocalDateTime.of(2023, 8, 1, 9, 2));

        // when
        List<ReservationWaitingDto> reservationWaitingDtos = reservationRepository.findWaitingAllByGuestName("초코칩");

        // then
        assertThat(reservationWaitingDtos)
                .extracting(ReservationWaitingDto::id, ReservationWaitingDto::waitNumber)
                .containsExactly(tuple(reservation.getId(), 2L));
    }

    @Test
    @DisplayName("특정 날짜/시간/테마의 대기 중인 예약 중 가장 우선순위가 높은 컬럼을 반환한다.")
    public void findBySlotAndStatusWaitingAndWaitingNumberIsOne() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 5);

        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(date, time, theme);
        Reservation first = sqlFixtureGenerator.insertReservation("토비", reservationSlot, WAITING);
        Reservation second = sqlFixtureGenerator.insertReservation("브라운", reservationSlot, WAITING);

        // when
        Optional<Reservation> result = reservationRepository.findBySlotAndStatusWaitingAndWaitingNumberIsOne(date, time.getId(), theme.getId());

        // then
        assertThat(result).isPresent();
        Reservation get = result.get();
        assertThat(get.getId()).isEqualTo(first.getId());
    }


    @Test
    @DisplayName("예약을 저장한다.")
    public void save() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme);
        Reservation reservation = Reservation.create(
                "브라운", reservationSlot, WAITING, LocalDateTime.now());

        // when
        Reservation saved = reservationRepository.save(reservation);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved)
                .extracting(
                        Reservation::getGuestName, Reservation::getDate,
                        Reservation::getTime, Reservation::getTheme
                ).containsExactly(
                        reservation.getGuestName(), reservation.getDate(),
                        reservation.getTime(), reservation.getTheme()
                );
    }

    @Test
    @DisplayName("예약의 슬롯, 상태를 수정한다.")
    public void updateSlotAndStatus() {
        // given
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        ReservationTime beforeTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Status beforeStatus = WAITING;
        LocalDate beforeDate = LocalDate.of(2023, 8, 5);
        Reservation reservation = sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(beforeDate, beforeTime, theme),
                beforeStatus);

        LocalDate updatedDate = LocalDate.of(2023, 9, 7);
        ReservationTime updatedTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0));
        ReservationSlot updatedSlot = sqlFixtureGenerator.insertReservationSlot(updatedDate, updatedTime, theme);
        Status updatedStatus = CONFIRMED;
        LocalDateTime lastModifiedAt = LocalDateTime.of(2023, 9, 1, 10, 0);

        // when
        boolean result = reservationRepository.updateSlotAndStatus(
                reservation.getId(), updatedSlot.getId(), updatedStatus, lastModifiedAt);

        // then
        assertThat(result).isTrue();

        Map<String, Object> map = findReservationById(reservation.getId());
        LocalDate date = ((Date) map.get("date")).toLocalDate();
        Long timeId = ((Number) map.get("time_id")).longValue();
        Status status = from((String) map.get("status"));
        LocalDateTime updatedLastModifiedAt = ((java.sql.Timestamp) map.get("last_modified_at")).toLocalDateTime();
        assertThat(date).isEqualTo(updatedDate);
        assertThat(timeId).isEqualTo(updatedTime.getId());
        assertThat(status).isEqualTo(updatedStatus);
        assertThat(updatedLastModifiedAt).isEqualTo(lastModifiedAt);
    }

    @Test
    @DisplayName("예약의 상태를 수정한다.")
    public void updateStatus() {
        // given
        Status beforeStatus = WAITING;
        Status updatedStatus = CONFIRMED;

        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme),
                beforeStatus);


        // when
        boolean result = reservationRepository.updateStatus(reservation.getId(), updatedStatus);

        // then
        assertThat(result).isTrue();

        Map<String, Object> map = findReservationById(reservation.getId());
        Status status = from((String) map.get("status"));
        assertThat(status).isEqualTo(updatedStatus);
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마를 가진 예약이 존재하는지 확인한다.")
    public void existsByDateAndTimeIdAndThemeIdAndGuestName() {
        // given
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        ReservationSlot reservationSlot1 = sqlFixtureGenerator.insertReservationSlot(
                targetDate,
                sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0)),
                sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        ReservationSlot reservationSlot2 = sqlFixtureGenerator.insertReservationSlot(
                targetDate,
                sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0)),
                sqlFixtureGenerator.insertTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png"));
        sqlFixtureGenerator.insertReservation("브라운", reservationSlot1, WAITING);

        // when
        boolean exists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(reservationSlot1, "브라운");
        boolean notExists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(reservationSlot2, "브라운");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마를 가진 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existsByDateAndTimeIdAndThemeId_AndGuestName_softDelete() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(targetDate, time, theme);
        sqlFixtureGenerator.insertDeletedReservation("브라운", reservationSlot);

        // when
        boolean exists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(reservationSlot, "브라운");

        // then
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "target, other, false",
            "target, target, true"
    })
    @DisplayName("해당 예약을 제외하고 동일 슬롯에 활성 예약이 존재하는지 확인한다.")
    public void existsActiveReservationBySlotExceptGuestName(
            String target1,
            String target2,
            boolean expected
    ) {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        ReservationTime otherTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0));

        Theme theme = sqlFixtureGenerator.insertTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        );

        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(targetDate, time, theme);
        ReservationSlot otherSlot = sqlFixtureGenerator.insertReservationSlot(targetDate, otherTime, theme);

        sqlFixtureGenerator.insertReservation(target1, reservationSlot, CONFIRMED);
        sqlFixtureGenerator.insertReservation(target2, otherSlot, CONFIRMED);

        // when
        boolean exists = reservationRepository
                .existsBySlotAndGuestNameExceptCanceled(otherSlot, target1);

        // then
        assertThat(exists).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 예약이 아닌 예약 중에서 겹치는 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existsByDateAndTimeIdAndThemeIdAndIdNot_AndStatusCanceled_softDeleteAndGuestName() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(targetDate, time, theme);
        sqlFixtureGenerator.insertDeletedReservation("브라운", reservationSlot);

        // when
        boolean exists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(reservationSlot, "브라운");

        // then
        assertThat(exists).isFalse();
    }


    @Test
    @DisplayName("존재하지 않는 예약은 취소되지 않는다.")
    public void cancelById_fail_notFound() {
        // given
        Long id = 1L;

        // when
        boolean result = reservationRepository.cancelById(id);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("특정 예약 시간 id를 가진 예약이 존재하는지 확인한다.")
    public void existByTimeId() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme),
                WAITING);

        // when
        boolean exists = reservationRepository.existByTimeId(time.getId());
        boolean notExists = reservationRepository.existByTimeId(time2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 예약 시간 id를 가진 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existByTimeId_softDelete() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        sqlFixtureGenerator.insertDeletedReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme));

        // when
        boolean exists = reservationRepository.existByTimeId(time.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 예약이 존재하는지 확인한다.")
    public void existByThemeId() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Theme theme2 = sqlFixtureGenerator.insertTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");
        sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme),
                WAITING);

        // when
        boolean exists = reservationRepository.existByThemeId(theme.getId());
        boolean notExists = reservationRepository.existByThemeId(theme2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existByThemeId_softDelete() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        sqlFixtureGenerator.insertDeletedReservation("브라운", sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme));

        // when
        boolean exists = reservationRepository.existByThemeId(theme.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 조회 시 순번을 반환해야한다.")
    public void findWaitingById() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        sqlFixtureGenerator.insertReservation("브라운", sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme), CONFIRMED);
        sqlFixtureGenerator.insertReservation("주디", sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme), WAITING);
        Reservation reservation = sqlFixtureGenerator.insertReservation("초코칩", sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme), WAITING);

        // when
        ReservationWaitingDto reservationWaitingDto = reservationRepository.findWaitingById(reservation.getId()).get();

        // then
        assertThat(reservationWaitingDto.waitNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("예약 조회에서 날짜, 시간, 테마가 같고 Confiremd인 것만 조회한다.")
    public void existsBySlotAndStatusConfirmed() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 5);
        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(date, time, theme);

        sqlFixtureGenerator.insertReservation("브라운", reservationSlot, CONFIRMED);
        sqlFixtureGenerator.insertReservation("주디", reservationSlot, WAITING);
        Reservation reservation = sqlFixtureGenerator.insertReservation("초코칩", reservationSlot, WAITING);

        // when
        boolean result = reservationRepository.existsBySlotAndStatusConfirmed(reservationSlot);

        // then
        assertThat(result).isTrue();
    }

    private Map<String, Object> findReservationById(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT r.*, s.date, s.time_id, s.theme_id
                FROM reservation r
                INNER JOIN reservation_slot s
                    ON r.slot_id = s.id
                WHERE r.id = :id
                """, new MapSqlParameterSource("id", id));
    }
}
