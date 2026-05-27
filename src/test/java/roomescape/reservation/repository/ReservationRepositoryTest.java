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
            Assertions.assertThat(actual)
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
            Assertions.assertThat(actual)
                .isEmpty();
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
    @DisplayName("findAllByName 메서드는")
    class FindAllByNameTest {


        @Test
        @DisplayName("요청한 예약자 이름을 가진 모든 예약을 조회한다")
        void 성공() {
            // given
            List<Reservation> reservations = saveAll(List.of(
                ReservationFixture.reservation(name, reservationDate1, reservationTime1, theme),
                ReservationFixture.reservation(name, reservationDate1, reservationTime2, theme),
                ReservationFixture.reservation(name, reservationDate2, reservationTime1, theme),
                ReservationFixture.reservation(name, reservationDate2, reservationTime2, theme)
            ));
            reservations.sort(
                Comparator.comparing((Reservation reservation) -> reservation.getDate().getDate(),
                        Comparator.reverseOrder())
                    .thenComparing(reservation -> reservation.getTime().getStartAt())
            );

            // when
            List<Reservation> actual = jdbcReservationRepository.findAllByNameOrderByDateAndTime(
                name);

            // then
            Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(reservations);
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
    @DisplayName("existsByDateAndTimeAndThemeId 메서드는")
    class ExistsByDateAndTimeAndThemeId {


        @Test
        @DisplayName("날짜와 시간 및 테마을 가진 예약이 존재하는지 확인한다")
        void 성공() {
            // given
            save(reservation(name, reservationDate1, reservationTime1, theme));
            Long wrongDateId = reservationDate2.getId();

            // when & then
            assertThat(
                jdbcReservationRepository.existsByDateAndTimeAndThemeId(reservationDate1.getId(),
                    reservationTime1.getId(), theme.getId()))
                .isTrue();
            assertThat(jdbcReservationRepository.existsByDateAndTimeAndThemeId(wrongDateId,
                reservationTime1.getId(), theme.getId()))
                .isFalse();
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
            Assertions.assertThat(afterReservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("updateSchedule 메서드는")
    class UpdateScheduleTest {


        @Test
        @DisplayName("예약 날짜 및 시간을 변경한다")
        void 성공() {
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
    }
}
