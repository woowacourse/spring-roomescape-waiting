package roomescape.reservation.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.JdbcMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.testutil.JdbcRepositoryTest;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;

@JdbcRepositoryTest
class JdbcReservationRepositoryTest {

    private final ReservationRepository jdbcReservationRepository;
    private final ReservationTimeRepository jdbcReservationTimeRepository;
    private final ThemeRepository jdbcThemeRepository;
    private final MemberRepository jdbcMemberRepository;

    @Autowired
    JdbcReservationRepositoryTest(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate, dataSource);
        this.jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate, dataSource);
        this.jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate, dataSource);
        this.jdbcMemberRepository = new JdbcMemberRepository(jdbcTemplate, dataSource);
    }

    @Test
    @DisplayName("Reservation를 저장한 후 저장한 row의 id값을 반환한다.")
    void save() {
        Reservation savedReservation = jdbcReservationRepository.save(ReservationFixture.getOne());
        assertThat(savedReservation.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Reservation 테이블의 있는 모든 데이터를 조회한다.")
    void findAll() {
        // given
        List<ReservationTime> savedTimes = ReservationTimeFixture.get(2).stream()
                .map(jdbcReservationTimeRepository::save)
                .toList();
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.getOne());
        Member savedMember = jdbcMemberRepository.save(MemberFixture.getOne());

        Reservation saveReservation1 = jdbcReservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(savedMember, savedTimes.get(0), savedTheme));
        Reservation saveReservation2 = jdbcReservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(savedMember, savedTimes.get(1), savedTheme));

        // when & then
        assertThat(jdbcReservationRepository.findAll())
                .containsExactly(saveReservation1, saveReservation2);
    }

    @Test
    @DisplayName("Reservation 테이블의 주어진 id와 동일한 데이터를 조회한다.")
    void findById() {
        // given
        Member savedMember = jdbcMemberRepository.save(MemberFixture.getOne());
        ReservationTime savedTime = jdbcReservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.getOne());
        Reservation savedReservation = jdbcReservationRepository.save(
                new Reservation(null, savedMember, LocalDate.now(), savedTime, savedTheme));

        // when
        Optional<Reservation> reservation = jdbcReservationRepository.findById(savedReservation.getId());

        // then
        assertThat(reservation).isEqualTo(Optional.of(savedReservation));
    }

    @Test
    @DisplayName("Reservation 테이블에 주어진 id와 없는 경우 빈 옵셔널을 반환한다.")
    void findById_Return_EmptyOptional() {
        assertThat(jdbcReservationRepository.findById(1L)).isNotPresent();
    }

    @Test
    @DisplayName("Reservation 테이블의 주어진 date, theme_id와 동일한 데이터를 조회한다.")
    void findAllByDateAndThemeId() {
        // given
        Member savedMember = jdbcMemberRepository.save(MemberFixture.getOne());
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.getOne());
        ReservationTime savedTimes = jdbcReservationTimeRepository.save(ReservationTimeFixture.getOne());

        Reservation reservation1 = jdbcReservationRepository.save(
                ReservationFixture.getOneWithDateTimeTheme(LocalDate.parse("2024-02-01"), savedTimes, savedTheme));
        Reservation reservation2 = jdbcReservationRepository.save(
                ReservationFixture.getOneWithDateTimeTheme(LocalDate.parse("2024-10-01"), savedTimes, savedTheme));

        // when
        List<Reservation> reservations =
                jdbcReservationRepository.findAllByDateAndThemeId(reservation1.getDate(), savedTheme.getId());

        // then
        assertThat(reservations).isEqualTo(List.of(reservation1));
    }

    @Test
    @DisplayName("Reservation 테이블에 주어진 date, theme_id와 없는 경우 빈 리스트를 반환한다.")
    void findAllByDateAndThemeId_Return_EmptyCollection() {
        assertThat(jdbcReservationRepository.findAllByDateAndThemeId(LocalDate.of(1000, 03, 03), 20L)).isEmpty();
    }

    @Test
    @DisplayName("해당 id값과 일치하는 예약이 존재하는 경우 참을 반환한다.")
    void existsById() {
        jdbcReservationRepository.save(ReservationFixture.getOne());

        assertTrue(jdbcReservationRepository.existsById(1L));
    }

    @Test
    @DisplayName("해당 id값과 일치하는 예약이 존재하지 않은 경우 거짓을 반환한다.")
    void existsById_WhenNotExist() {
        assertFalse(jdbcReservationRepository.existsById(100L));
    }

    @Test
    @DisplayName("해당 예약 시간 id값과 일치하는 예약이 존재하는 경우 참을 반환한다.")
    void existsByTimeId() {
        assertFalse(jdbcReservationRepository.existsByTimeId(1L));
    }

    @Test
    @DisplayName("해당 예약 시간 id값과 일치하는 예약이 존재하지 않은 경우 거짓을 반환한다.")
    void existsByTimeId_WhenNotExist() {
        assertFalse(jdbcReservationRepository.existsByTimeId(7L));
    }

    @Test
    @DisplayName("해당 테마 id값과 일치하는 예약이 존재하는 경우 참을 반환한다.")
    void existsByThemeId() {
        Theme theme = jdbcThemeRepository.save(ThemeFixture.getOne());
        jdbcReservationRepository.save(ReservationFixture.getOneWithTheme(theme));

        assertTrue(jdbcReservationRepository.existsByThemeId(1L));
    }

    @Test
    @DisplayName("해당 테마 id값과 일치하는 예약이 존재하지 않은 경우 거짓을 반환한다.")
    void existsByThemeId_WhenNotExist() {
        assertFalse(jdbcReservationRepository.existsByThemeId(1L));
    }

    @Test
    @DisplayName("날짜와 시간 컬럼의 값이 동일할 경우 참을 반환한다.")
    void existsByDateAndTime_WhenSameName() {
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.getOne());
        ReservationTime savedTime = jdbcReservationTimeRepository.save(ReservationTimeFixture.getOne());
        Reservation saveReservation = jdbcReservationRepository.save(
                ReservationFixture.getOneWithDateTimeTheme(LocalDate.parse("2024-01-01"), savedTime, savedTheme));

        assertTrue(jdbcReservationRepository.existsByDateAndTimeIdAndThemeId(
                saveReservation.getDate(),
                savedTime.getId(),
                savedTheme.getId()));
    }

    @Test
    @DisplayName("날짜 또는 시간 중 하나라도 다를 경우 거짓을 반환한다.")
    void existsByDateAndTime_isFalse() {
        assertFalse(jdbcReservationRepository.existsByDateAndTimeIdAndThemeId(
                ReservationFixture.getOne().getDate(),
                ReservationTimeFixture.getOne().getId(),
                ThemeFixture.getOne().getId()));
    }

    @Test
    @DisplayName("Reservation 테이블에 주어진 id와 동일한 데이터를 삭제한다.")
    void deleteById() {
        jdbcReservationRepository.deleteById(2L);
        assertThat(jdbcReservationRepository.findById(2L)).isNotPresent();
    }
}
