package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.jdbc.Sql;
import roomescape.Fixtures;
import roomescape.reservation.domain.Reservation;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationRepository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.Fixtures.themeFixture;

@JdbcTest
@Import(ReservationDao.class)
@DisplayName("예약 DAO")
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationDaoTest {

    private final ReservationRepository reservationRepository;
    private final SimpleJdbcInsert simpleJdbcInsertWithReservationTime;

    @Autowired
    public ReservationDaoTest(ReservationRepository reservationRepository, DataSource dataSource) {
        this.reservationRepository = reservationRepository;
        this.simpleJdbcInsertWithReservationTime = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @DisplayName("예약 DAO는 생성 요청이 들어오면 DB에 값을 저장한다.")
    @Test
    void save() {
        // given
        ReservationTime reservationTime = Fixtures.reservationTimeFixture;
        Long reservationTimeId = simpleJdbcInsertWithReservationTime.executeAndReturnKey(
                    new BeanPropertySqlParameterSource(reservationTime)
                ).longValue();
        ReservationTime newReservationTime = new ReservationTime(reservationTimeId, reservationTime.getStartAt());
        Reservation reservation = new Reservation(
                Fixtures.memberFixture,
                LocalDate.of(2024, 11, 16),
                newReservationTime,
                new Theme(1L, themeFixture)
        );

        // when
        Reservation newReservation = reservationRepository.save(reservation);
        Optional<Reservation> actual = reservationRepository.findById(newReservation.getId());

        // then
        assertThat(actual.isPresent()).isTrue();
    }

    @DisplayName("예약 DAO는 조회 요청이 들어오면 id에 맞는 값을 반환한다.")
    @Test
    void findById() {
        // when
        Optional<Reservation> actual = reservationRepository.findById(1L);

        // then
        assertThat(actual.isPresent()).isTrue();
    }

    @DisplayName("예약 DAO는 조회 요청이 들어오면 저장한 모든 값을 반환한다.")
    @Test
    void findAll() {
        // when
        List<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations.size()).isEqualTo(9);
    }

    @DisplayName("예약 DAO는 주어진 기간 동안의 모든 예약을 반환한다.")
    @Test
    void findByDateBetween() {
        // given
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 8);

        // when
        List<Reservation> reservations = reservationRepository.findByDateBetween(startDate, endDate);

        // then
        assertThat(reservations).hasSize(5);
    }

    @DisplayName("예약 DAO는 주어진 날짜와 테마에 맞는 예약을 반환한다.")
    @Test
    void findByDateAndThemeId() {
        // given
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 8);
        Long themeId = 2L;
        Long memberId = 1L;

        // when
        List<Reservation> reservations = reservationRepository.findByDateBetweenAndMemberIdAndThemeId(
                startDate,
                endDate,
                memberId,
                themeId
        );

        // then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("예약 DAO는 주어진 기간, 테마, 사용자에 맞는 예약을 반환한다.")
    @Test
    void findByDateBetweenAndMemberIdAndThemeId() {
        // given
        LocalDate date = LocalDate.of(2024, 12, 2);
        Long themeId = 2L;

        // when
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);

        // then
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("예약 DAO는 삭제 요청이 들어오면 id에 맞는 값을 삭제한다.")
    @Test
    void deleteById() {
        // given
        Long id = 1L;

        // when
        reservationRepository.deleteById(id);
        Optional<Reservation> actual = reservationRepository.findById(id);

        // then
        assertThat(actual.isPresent()).isFalse();
    }
}
