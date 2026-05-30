package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.repository.JdbcMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFactory;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;

@JdbcTest(properties = "spring.sql.init.data-locations=")
@Import({JdbcReservationRepository.class, JdbcMemberRepository.class,
        JdbcReservationTimeRepository.class, JdbcThemeRepository.class, ReservationFactory.class})
class ReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcMemberRepository memberRepository;

    @Autowired
    private JdbcReservationTimeRepository timeRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private ReservationFactory reservationFactory;

    private Member member;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES ('user1', 'user1@test.com', '1234')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");

        member = memberRepository.findByEmail("user1@test.com").get();
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = '10:00:00'", Long.class);
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마A'", Long.class);
        time = timeRepository.findById(timeId).get();
        theme = themeRepository.findById(themeId).get();
    }

    @DisplayName("예약을 저장하면 ID가 부여된다.")
    @Test
    void 예약_저장_성공() {
        Reservation saved = reservationRepository.save(
                reservationFactory.create(member, LocalDate.now().plusDays(1), time, theme));
        assertThat(saved.getId()).isNotNull().isPositive();
    }

    @DisplayName("ID로 예약을 조회하면 예약 정보가 반환된다.")
    @Test
    void ID로_예약_조회_성공() {
        Reservation saved = reservationRepository.save(
                reservationFactory.create(member, LocalDate.now().plusDays(1), time, theme));
        assertThat(reservationRepository.findById(saved.getId())).isPresent();
    }

    @DisplayName("회원 ID로 예약 목록을 조회한다.")
    @Test
    void 회원ID로_예약_조회() {
        reservationRepository.save(reservationFactory.create(member, LocalDate.now().plusDays(1), time, theme));
        reservationRepository.save(reservationFactory.create(member, LocalDate.now().plusDays(2), time, theme));
        assertThat(reservationRepository.findByMemberId(member.getId())).hasSize(2);
    }

    @DisplayName("예약을 삭제하면 더 이상 조회되지 않는다.")
    @Test
    void 예약_삭제_성공() {
        Reservation saved = reservationRepository.save(
                reservationFactory.create(member, LocalDate.now().plusDays(1), time, theme));
        reservationRepository.deleteById(saved.getId());
        assertThat(reservationRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("날짜·시간·테마가 일치하는 예약이 있으면 true를 반환한다.")
    @Test
    void 예약_존재_여부_true() {
        LocalDate date = LocalDate.now().plusDays(1);
        reservationRepository.save(reservationFactory.create(member, date, time, theme));
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId())).isTrue();
    }

    @DisplayName("날짜·시간·테마가 일치하는 예약이 없으면 false를 반환한다.")
    @Test
    void 예약_존재_여부_false() {
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(
                LocalDate.now().plusDays(1), time.getId(), theme.getId())).isFalse();
    }

    @DisplayName("날짜·테마·시간으로 예약 ID를 조회한다.")
    @Test
    void 날짜_테마_시간으로_예약ID_조회() {
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation saved = reservationRepository.save(
                reservationFactory.create(member, date, time, theme));
        assertThat(reservationRepository.findReservationId(date, theme.getId(), time.getId()).id())
                .isEqualTo(saved.getId());
    }
}
