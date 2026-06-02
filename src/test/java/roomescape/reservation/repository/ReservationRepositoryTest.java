package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.fixture.ReservationFixture.canceledReservation;
import static roomescape.reservation.fixture.ReservationFixture.reservation;
import static roomescape.reservation.fixture.ReservationFixture.waitReservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.reservation.domain.Reservation;
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


    @Nested
    @DisplayName("findById 메서드는")
    class FindByIdTest {


        @Test
        @DisplayName("id로 예약을 조회한다")
        void 성공1() {
            // given
            Reservation saved = save(
                ReservationFixture.reservation(name, reservationDate1, reservationTime1, theme));

            // when
            Reservation actual = jdbcReservationRepository.findById(saved.getId()).get();

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
        }


        @Test
        @DisplayName("잘못된 id이면 optional.empty를 반환한다")
        void 성공2() {
            // given
            Long wrongId = Long.MIN_VALUE;

            // when
            Optional<Reservation> actual = jdbcReservationRepository.findById(wrongId);

            // then
            assertThat(actual)
                .isEmpty();
        }
    }

    @Nested
    @DisplayName("findFirstWaitingByDateTimeAndThemeId 메서드는")
    class FindFirstWaitingByDateTimeAndThemeId {

        @Test
        @DisplayName("가장 이른 대기 요청 1개를 조회한다")
        void 성공1() {
            // given
            List<String> usernames = List.of("user1", "user2", "user3");
            List<Reservation> reservations = List.of(
                reservation(usernames.get(0), reservationDate1, reservationTime1, theme),
                waitReservation(usernames.get(1), reservationDate1, reservationTime1, theme),
                waitReservation(usernames.get(2), reservationDate1, reservationTime1, theme)
            );
            Reservation expected = reservations.get(1);
            saveAll(reservations);

            // when
            Optional<Reservation> actual = jdbcReservationRepository.findFirstWaitingByDateTimeAndThemeId(
                reservationDate1.getId(), reservationTime1.getId(), theme.getId());

            // then
            assertThat(actual).isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);

        }

        @Test
        @DisplayName("슬롯에 대기 요청이 없으면 Optional.empty를 반환한다")
        void 성공2() {
            // given
            List<String> usernames = List.of("user1", "user2", "user3");
            List<Reservation> reservations = List.of(
                reservation(usernames.get(0), reservationDate1, reservationTime1, theme)
            );
            saveAll(reservations);

            // when
            Optional<Reservation> actual = jdbcReservationRepository.findFirstWaitingByDateTimeAndThemeId(
                reservationDate1.getId(), reservationTime1.getId(), theme.getId());

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll 메서드는")
    class FindAllTest {


        @Test
        @DisplayName("모든 예약을 조회한다")
        void 성공() {
            // given
            List<Reservation> reservations = List.of(
                Reservation.create(name, reservationDate1, reservationTime1, theme,
                    LocalDateTime.now()),
                Reservation.create(name, reservationDate1, reservationTime2, theme,
                    LocalDateTime.now())
            );
            saveAll(reservations);

            // when
            List<Reservation> actual = jdbcReservationRepository.findAll();

            // then
            assertThat(actual)
                .hasSize(reservations.size());
        }
    }

    @Nested
    @DisplayName("save 메서드는")
    class SaveTest {


        @Test
        @DisplayName("예약을 생성한다")
        void 성공() {
            // given
            List<Reservation> emptyReservations = List.of();

            // when
            jdbcReservationRepository.save(
                reservation(name, reservationDate1, reservationTime1, theme));

            // then
            assertThat(jdbcReservationRepository.findAll())
                .hasSize(emptyReservations.size() + 1);
        }
    }

    @Nested
    @DisplayName("updateState 메서드는")
    class UpdateStateTest {


        @Test
        @DisplayName("예약 상태를 변경한다")
        void 성공() {
            // given
            Reservation canceledReservation = save(
                canceledReservation(name, reservationDate1, reservationTime1, theme));
            updateStatus(canceledReservation);

            // when
            Reservation afterReservation = jdbcReservationRepository.findById(
                canceledReservation.getId()).get();

            // then
            assertThat(afterReservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("findMyReservationsWithWaitingTurn 메서드는")
    class FindMyReservationsWithWaitingTurnTest {


        @Test
        @DisplayName("나의 예약 목록에 대기 순번을 포함해 조회한다")
        void 성공() {
            // given
            LocalDateTime requestedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
            save(reservation("예약자", reservationDate1, reservationTime1, theme));
            save(Reservation.wait("앞선 대기자", reservationDate1, reservationTime1, theme,
                requestedAt));
            Reservation waiting = save(Reservation.wait(name, reservationDate1, reservationTime1,
                theme, requestedAt.plusMinutes(1)));
            Reservation reserved = save(Reservation.create(name, reservationDate2, reservationTime2,
                theme, requestedAt.plusMinutes(2)));

            // when
            List<ReservationWithWaitingTurn> actual =
                jdbcReservationRepository.findMyReservationsWithWaitingTurn(name);

            // then
            assertAll(
                () -> assertThat(actual)
                    .hasSize(2),
                () -> assertThat(actual)
                    .filteredOn(reservation -> reservation.id().equals(waiting.getId()))
                    .singleElement()
                    .extracting("status", "waitingTurn")
                    .containsExactly(ReservationStatus.WAITING, 2L),
                () -> assertThat(actual)
                    .filteredOn(reservation -> reservation.id().equals(reserved.getId()))
                    .singleElement()
                    .extracting("status", "waitingTurn")
                    .containsExactly(ReservationStatus.RESERVED, null)
            );
        }

        @Test
        @DisplayName("지난 대기 예약은 대기 순번을 가지지 않는다")
        void 성공2() {
            // given
            ReservationDate pastDate = jdbcReservationDateRepository.save(
                ReservationDate.load(1L, LocalDate.now().minusDays(1), true));
            Reservation waiting = save(Reservation.load(1L, name, pastDate, reservationTime1, theme,
                ReservationStatus.WAITING, LocalDateTime.now()));

            // when
            List<ReservationWithWaitingTurn> actual =
                jdbcReservationRepository.findMyReservationsWithWaitingTurn(name);

            // then
            assertThat(actual)
                .filteredOn(reservation -> reservation.id().equals(waiting.getId()))
                .singleElement()
                .extracting("status", "waitingTurn")
                .containsExactly(ReservationStatus.WAITING, null);
        }
    }
}
