package roomescape.reservation.repository;

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
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.repository.JdbcReservationSlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.JdbcReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.domain.ReservationStatus.PENDING_PAYMENT;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;

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

    private ReservationSlot slot1;
    private ReservationSlot slot2;
    private ReservationSlot slot3;
    private ReservationSlot slot4;

    private JdbcReservationRepository jdbcReservationRepository;
    private JdbcReservationTimeRepository jdbcReservationTimeRepository;
    private JdbcReservationDateRepository jdbcReservationDateRepository;
    private JdbcThemeRepository jdbcThemeRepository;
    private JdbcReservationSlotRepository jdbcReservationSlotRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        jdbcReservationDateRepository = new JdbcReservationDateRepository(jdbcTemplate);
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
        jdbcReservationSlotRepository = new JdbcReservationSlotRepository(jdbcTemplate);

        ReservationTime time1 = jdbcReservationTimeRepository.save(ReservationTimeFixture.time15());
        ReservationTime time2 = jdbcReservationTimeRepository.save(ReservationTimeFixture.time16());
        reservationTime1 = jdbcReservationTimeRepository.findById(time1.getId()).get();
        reservationTime2 = jdbcReservationTimeRepository.findById(time2.getId()).get();

        reservationDate1 = jdbcReservationDateRepository.save(ReservationDate.create(date1));
        reservationDate2 = jdbcReservationDateRepository.save(ReservationDate.create(date2));
        theme = jdbcThemeRepository.save(Theme.create("테마", "설명", "썸네일"));

        slot1 = jdbcReservationSlotRepository.save(ReservationSlot.of(reservationDate1, reservationTime1, theme));
        slot2 = jdbcReservationSlotRepository.save(ReservationSlot.of(reservationDate1, reservationTime2, theme));
        slot3 = jdbcReservationSlotRepository.save(ReservationSlot.of(reservationDate2, reservationTime1, theme));
        slot4 = jdbcReservationSlotRepository.save(ReservationSlot.of(reservationDate2, reservationTime2, theme));
    }

    @Test
    @DisplayName("예약 정보를 단건 조회한다.")
    void findById() {
        // given
        Reservation saved = save(ReservationFixture.reservation(name, slot1));

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
        // given & when
        Optional<Reservation> actual = jdbcReservationRepository.findById(Long.MIN_VALUE);

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("모든 예약 정보를 조회한다.")
    void findAll() {
        // given
        saveAll(List.of(
                Reservation.reserve(name, slot1.getId(), RESERVED, LocalDateTime.now()),
                Reservation.reserve(name, slot2.getId(), RESERVED, LocalDateTime.now())
        ));

        // when
        List<ReservationWithSlotInformation> actual = jdbcReservationRepository.findAll();

        // then
        assertThat(actual)
                .hasSize(2);
    }

    @Test
    @DisplayName("특정 슬롯의 예약+대기 목록을 조회하면 예약요청시각 오름차순으로 정렬된다.")
    void findAllByName() {
        // given
        LocalDateTime t1 = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime t2 = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime t3 = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime t4 = LocalDateTime.now().plusHours(4).truncatedTo(ChronoUnit.MICROS);
        saveAll(List.of(
                ReservationFixture.reservation(name, slot1, t1),
                ReservationFixture.reservation(name, slot2, t2),
                ReservationFixture.reservation(name, slot3, t3),
                ReservationFixture.reservation(name, slot4, t4)
        ));

        // when
        List<ReservationWithSlotInformation> result = jdbcReservationRepository.findByMemberName(name);

        // then
        Assertions.assertThat(result)
                .extracting(ReservationWithSlotInformation::reservedAt)
                .containsExactly(t1, t2, t3, t4);
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void save() {
        // given & when
        jdbcReservationRepository.save(ReservationFixture.reservation(name, slot1));

        // then
        assertThat(jdbcReservationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("슬롯 ID로 활성 예약+대기 목록을 조회한다.")
    void findReservedAndWaitingBySlotId() {
        // given
        save(ReservationFixture.reservation(name, slot1));

        // when
        List<Reservation> result = jdbcReservationRepository.findReservedAndWaitingBySlotId(slot1.getId());

        // then
        assertThat(result)
                .hasSize(1);
        assertThat(jdbcReservationRepository.findReservedAndWaitingBySlotId(slot3.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("결제 대기 상태의 예약도 활성 예약+대기 목록에 포함된다.")
    void findReservedAndWaitingBySlotId_includes_pending_payment() {
        // given
        save(Reservation.reserve(name, slot1.getId(), PENDING_PAYMENT, LocalDateTime.now()));

        // when
        List<Reservation> result = jdbcReservationRepository.findReservedAndWaitingBySlotId(slot1.getId());

        // then
        assertThat(result)
                .hasSize(1);
    }

    @Test
    @DisplayName("예약을 취소하면 상태가 CANCELED가 된다.")
    void updateState_canceled() {
        // given
        Reservation saved = save(ReservationFixture.reservation(name, slot1));
        saved.cancel(name);

        // when
        jdbcReservationRepository.updateStatus(saved);
        Reservation actual = jdbcReservationRepository.findById(saved.getId()).get();

        // then
        Assertions.assertThat(actual.getStatus())
                .isEqualTo(CANCELED);
    }

    @Test
    @DisplayName("이용가능한 날짜/시간으로 예약을 변경할 수 있다.")
    void updateSchedule() {
        // given
        Reservation saved = save(ReservationFixture.reservation(name, slot1));
        saved.reschedule(slot3.getId(), name, RESERVED);

        // when
        jdbcReservationRepository.updateSchedule(saved);
        Reservation actual = jdbcReservationRepository.findById(saved.getId()).get();

        // then
        Assertions.assertThat(actual.getSlotId())
                .isEqualTo(slot3.getId());
    }

    private List<Reservation> saveAll(List<Reservation> reservations) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations) {
            result.add(save(r));
        }
        return result;
    }

    private Reservation save(Reservation reservation) {
        return jdbcReservationRepository.save(reservation);
    }

}
