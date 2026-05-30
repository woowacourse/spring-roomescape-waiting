package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.fixture.ReservationFixture.canceledReservation;
import static roomescape.reservation.fixture.ReservationFixture.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.JdbcReservationTimeRepository;

@JdbcTest
class ReservationRepositoryTest {

    private final String name = "한다";
    private final LocalDate date1 = LocalDate.of(2099, 1, 1);
    private final LocalDate date2 = LocalDate.of(2099, 9, 1);
    private ReservationDate reservationDate1;
    private ReservationDate reservationDate2;
    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private Theme theme;

    private JdbcReservationRepository jdbcReservationRepository;
    private JdbcReservationTimeRepository jdbcReservationTimeRepository;
    private JdbcReservationDateRepository jdbcReservationDateRepository;
    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        jdbcReservationDateRepository = new JdbcReservationDateRepository(jdbcTemplate);
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);

        ReservationTime time1 = jdbcReservationTimeRepository.save(ReservationTimeFixture.time15());
        ReservationTime time2 = jdbcReservationTimeRepository.save(ReservationTimeFixture.time16());
        reservationTime1 = jdbcReservationTimeRepository.findById(time1.getId()).get();
        reservationTime2 = jdbcReservationTimeRepository.findById(time2.getId()).get();

        reservationDate1 = jdbcReservationDateRepository.save(ReservationDate.create(date1));
        reservationDate2 = jdbcReservationDateRepository.save(ReservationDate.create(date2));
        theme = jdbcThemeRepository.save(Theme.create("테마", "설명", "썸네일"));
    }

    @Test
    @DisplayName("예약 정보를 단건 조회한다.")
    void findById() {
        // given
        Reservation saved = save(ReservationFixture.reservation(name, reservationDate1, reservationTime1, theme));

        // when
        Reservation actual = jdbcReservationRepository.findById(saved.getId()).get();

        // then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    @DisplayName("등록되지 않은 예약을 단건 조회하면 빈 값을 반환한다.")
    void findById_wrongId() {
        // given
        Long wrongId = Long.MIN_VALUE;

        // when
        Optional<Reservation> actual = jdbcReservationRepository.findById(wrongId);

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("모든 예약 정보를 조회한다.")
    void findAll() {
        // given
        List<Reservation> reservations = List.of(
                Reservation.reserve(name, reservationDate1, reservationTime1, theme, LocalDateTime.now()),
                Reservation.reserve(name, reservationDate1, reservationTime2, theme, LocalDateTime.now())
        );
        saveAll(reservations);

        // when
        List<Reservation> actual = jdbcReservationRepository.findAll();

        // then
        assertThat(actual)
                .hasSize(reservations.size());
    }

    @Test
    @DisplayName("특정 슬롯의 예약+대기 목록을 조회하면 예약요청시각을 기준으로 오름차순 정렬된다.")
    void findAllByName() {
        // given
        LocalDateTime firstReservedAt = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime secondReservedAt = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime thirdReservedAt = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime fourthReservedAt = LocalDateTime.now().plusHours(4).truncatedTo(ChronoUnit.MICROS);

        saveAll(List.of(
                ReservationFixture.reservation(name, reservationDate1, reservationTime1, theme, firstReservedAt),
                ReservationFixture.reservation(name, reservationDate1, reservationTime2, theme, secondReservedAt),
                ReservationFixture.reservation(name, reservationDate2, reservationTime1, theme, thirdReservedAt),
                ReservationFixture.reservation(name, reservationDate2, reservationTime2, theme, fourthReservedAt)
        ));

        // when
        List<ReservationWithWaitingTurn> result = jdbcReservationRepository.findMyReservationsWithWaitingTurn(name);

        // then
        Assertions.assertThat(result)
                .extracting(ReservationWithWaitingTurn::reservedAt)
                .containsExactly(
                        firstReservedAt,
                        secondReservedAt,
                        thirdReservedAt,
                        fourthReservedAt
                );
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void save() {
        // given
        List<Reservation> emptyReservations = List.of();

        // when
        jdbcReservationRepository.save(reservation(name, reservationDate1, reservationTime1, theme));

        // then
        assertThat(jdbcReservationRepository.findAll())
                .hasSize(emptyReservations.size() + 1);
    }

    @Test
    @DisplayName("예약 날짜와 시간 ID 정보로 존재하는지 확인한다.")
    void exitsByDateAndTimeId() {
        // given
        save(reservation(name, reservationDate1, reservationTime1, theme));

        ReservationSlot slot = ReservationSlot.of(reservationDate1, reservationTime1, theme);
        ReservationSlot wrongSlot = ReservationSlot.of(reservationDate2, reservationTime1, theme);

        // when & then
        assertThat(jdbcReservationRepository.existsReservedBySlot(slot))
                .isTrue();
        assertThat(jdbcReservationRepository.existsReservedBySlot(wrongSlot))
                .isFalse();
    }

    @Test
    @DisplayName("예약을 취소하면 상태가 CANCELED가 된다.")
    void updateState_canceled() {
        // given
        Reservation canceledReservation = save(canceledReservation(name, reservationDate1, reservationTime1, theme));
        updateStatus(canceledReservation);

        // when
        Reservation afterReservation = jdbcReservationRepository.findById(canceledReservation.getId()).get();

        // then
        Assertions.assertThat(afterReservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("이용가능한 날짜/시간으로 예약을 변경할 수 있다.")
    void updateSchedule() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme));
        saved.changeSchedule(name, reservationDate2, reservationTime1);

        // when
        jdbcReservationRepository.updateSchedule(saved);

        // then
        Assertions.assertThat(jdbcReservationRepository.findById(saved.getId()).get())
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    private List<Reservation> saveAll(List<Reservation> reservations) {
        List<Reservation> savedReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            savedReservations.add(save(reservation));
        }
        return savedReservations;
    }

    private Reservation save(Reservation reservation) {
        return jdbcReservationRepository.save(reservation);
    }

    private void updateStatus(Reservation beforeReservation) {
        beforeReservation.updateStatus(ReservationStatus.CANCELED);
        jdbcReservationRepository.updateStatus(beforeReservation);
    }

}
