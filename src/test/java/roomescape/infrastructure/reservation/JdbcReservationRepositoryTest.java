package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.infrastructure.member.JdbcMemberRepository;

@JdbcTest
@Import(value = {
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        JdbcMemberRepository.class})
class JdbcReservationRepositoryTest {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2000, 1, 1, 12, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcReservationRepository jdbcReservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("id로 예약을 조회한다.")
    void shouldReturnReservationWhenReservationIdExist() {
        long id = createReservation().getId();
        Optional<Reservation> foundReservation = jdbcReservationRepository.findById(id);
        assertThat(foundReservation).isPresent();
    }

    @Test
    @DisplayName("id로 예약을 조회시 존재하지 않으면 빈 객체를 반환한다.")
    void shouldEmptyReservationWhenReservationIdNotExist() {
        Optional<Reservation> reservation = jdbcReservationRepository.findById(99L);
        assertThat(reservation).isEmpty();
    }

    @Test
    @DisplayName("존재하는 모든 예약을 반환한다.")
    void shouldReturnAllReservationsWhenFindAll() {
        createReservation();
        List<Reservation> reservations = jdbcReservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("예약을 저장하면 id를 가진 예약을 저장 후 반환한다.")
    void shouldReturnReservationWithIdWhenReservationSave() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.create(new Theme("theme1", "desc", "url"));
        Member member = memberRepository.save(MemberFixture.createMember("오리"));
        Reservation reservationWithoutId = new Reservation(
                member,
                LocalDate.of(2024, 12, 25),
                reservationTime,
                theme,
                BASE_TIME
        );
        Reservation reservationWithId = jdbcReservationRepository.create(reservationWithoutId);
        int totalRowCount = getTotalRowCount();
        assertAll(
                () -> assertThat(reservationWithId.getId()).isNotNull(),
                () -> assertThat(totalRowCount).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("id로 예약을 삭제한다.")
    void shouldDeleteReservationWhenReservationIdExist() {
        long id = createReservation().getId();
        jdbcReservationRepository.deleteById(id);
        int totalRowCount = getTotalRowCount();
        assertThat(totalRowCount).isZero();
    }

    @Test
    @DisplayName("예약 시간 id를 가진 예약의 개수를 조회한다.")
    void shouldReturnCountOfReservationWhenReservationTimeUsed() {
        ReservationTime time = createReservation().getTime();
        boolean exists = jdbcReservationRepository.existsByTimeId(time.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간으로 저장된 예약이 있는지 확인한다.")
    void shouldReturnIsExistReservationWhenReservationsNameAndDateAndTimeIsSame() {
        Reservation reservation = createReservation();
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();

        boolean exists = jdbcReservationRepository.existsBy(reservation.getDate(), time.getId(), theme.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 테마, 사용자로 예약을 조회한다.")
    @Sql(scripts = "/insert-reservations.sql")
    void findByMemberAndThemeBetweenDates() {
        List<Reservation> reservations = jdbcReservationRepository.findByMemberAndThemeBetweenDates(
                2L,
                3L,
                LocalDate.of(1999, 12, 24),
                LocalDate.of(1999, 12, 29));
        assertThat(reservations).hasSize(2);
    }

    private Reservation createReservation() {
        String memberSql = "insert into member (id, name, email, password) values (?, ?, ?, ?)";
        String reservationSql = "insert into reservation (id, member_id, date, time_id, theme_id, created_at) values (?, ?, ?, ?, ?, ?)";

        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.create(new Theme("theme1", "desc", "url"));
        LocalDate date = LocalDate.of(2024, 12, 25);
        Member member = MemberFixture.createMember("오리");
        jdbcTemplate.update(memberSql, 1L, member.getName(), member.getEmail(), member.getPassword());
        jdbcTemplate.update(reservationSql, 1L, 1L, date, reservationTime.getId(), theme.getId(), BASE_TIME);
        return new Reservation(1L, member, date, reservationTime, theme, BASE_TIME);
    }

    private int getTotalRowCount() {
        String sql = "select count(*) from reservation";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
