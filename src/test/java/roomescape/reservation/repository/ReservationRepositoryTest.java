package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.fixture.ReservationFixture.canceledReservation;
import static roomescape.reservation.fixture.ReservationFixture.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import roomescape.reservation.domain.ReservationStatus;
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
        Reservation saved = save(Reservation.create(name, reservationDate1, reservationTime1, theme, LocalDateTime.now()));

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
                Reservation.create(name, reservationDate1, reservationTime1, theme, LocalDateTime.now()),
                Reservation.create(name, reservationDate1, reservationTime2, theme, LocalDateTime.now())
        );
        saveAll(reservations);

        // when
        List<Reservation> actual = jdbcReservationRepository.findAll();

        // then
        assertThat(actual)
                .hasSize(reservations.size());
    }

    @Test
    @DisplayName("나의 예약들을 조회하면 날짜는 내림차순, 시간은 오름차순으로 정렬해 모두 조회한다.")
    void findAllByName() {
        // given
        List<Reservation> reservations = saveAll(List.of(
                Reservation.create(name, reservationDate1, reservationTime1, theme, LocalDateTime.now()),
                Reservation.create(name, reservationDate1, reservationTime2, theme, LocalDateTime.now()),
                Reservation.create(name, reservationDate2, reservationTime1, theme, LocalDateTime.now()),
                Reservation.create(name, reservationDate2, reservationTime2, theme, LocalDateTime.now()))
        );
        reservations.sort(
                Comparator.comparing((Reservation reservation) -> reservation.getDate().getDate(), Comparator.reverseOrder())
                        .thenComparing(reservation -> reservation.getTime().getStartAt())
        );

        // when
        List<Reservation> actual = jdbcReservationRepository.findAllByNameOrderByDateAndTime(name);

        // then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(reservations);
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
        Long wrongDateId = reservationDate2.getId();

        // when & then
        assertThat(jdbcReservationRepository.existsByDateAndTimeAndThemeId(reservationDate1.getId(), reservationTime1.getId(), theme.getId()))
                .isTrue();
        assertThat(jdbcReservationRepository.existsByDateAndTimeAndThemeId(wrongDateId, reservationTime1.getId(), theme.getId()))
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
