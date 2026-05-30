package roomescape.reservationwaiting.repository;

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
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;

@JdbcTest(properties = "spring.sql.init.data-locations=")
@Import({JdbcReservationWaitingRepository.class, JdbcMemberRepository.class,
        JdbcReservationTimeRepository.class, JdbcThemeRepository.class})
class ReservationWaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcReservationWaitingRepository waitingRepository;

    @Autowired
    private JdbcMemberRepository memberRepository;

    @Autowired
    private JdbcReservationTimeRepository timeRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    private Member member;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES ('user1', 'user1@test.com', '1234')");
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES ('user2', 'user2@test.com', '1234')");
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES ('user3', 'user3@test.com', '1234')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");

        member = memberRepository.findByEmail("user1@test.com").get();
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = '10:00:00'", Long.class);
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마A'", Long.class);
        time = timeRepository.findById(timeId).get();
        theme = themeRepository.findById(themeId).get();
    }

    @DisplayName("예약 대기를 저장하면 ID가 부여된다.")
    @Test
    void 대기_저장_성공() {
        ReservationWaiting saved = waitingRepository.save(
                ReservationWaiting.of(member, LocalDate.now().plusDays(1), time, theme));
        assertThat(saved.getId()).isNotNull().isPositive();
    }

    @DisplayName("ID로 예약 대기를 조회한다.")
    @Test
    void ID로_대기_조회_성공() {
        ReservationWaiting saved = waitingRepository.save(
                ReservationWaiting.of(member, LocalDate.now().plusDays(1), time, theme));
        assertThat(waitingRepository.findById(saved.getId())).isPresent();
    }

    @DisplayName("예약 대기를 삭제하면 더 이상 조회되지 않는다.")
    @Test
    void 대기_삭제_성공() {
        ReservationWaiting saved = waitingRepository.save(
                ReservationWaiting.of(member, LocalDate.now().plusDays(1), time, theme));
        waitingRepository.deleteById(saved.getId());
        assertThat(waitingRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("회원 ID로 예약 대기 목록을 조회한다.")
    @Test
    void 회원ID로_대기_조회() {
        waitingRepository.save(ReservationWaiting.of(member, LocalDate.now().plusDays(1), time, theme));
        assertThat(waitingRepository.findByMemberId(member.getId())).hasSize(1);
    }

    @DisplayName("회원·날짜·시간·테마가 모두 일치하는 대기가 있으면 true를 반환한다.")
    @Test
    void 대기_존재_여부_true() {
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(ReservationWaiting.of(member, date, time, theme));
        assertThat(waitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                member.getId(), date, time.getId(), theme.getId())).isTrue();
    }

    @DisplayName("대기 신청 순번을 계산한다.")
    @Test
    void 대기_순번_계산() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Long member2Id = memberRepository.findByEmail("user2@test.com").get().getId();
        Long member3Id = memberRepository.findByEmail("user3@test.com").get().getId();

        jdbcTemplate.update("INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                member2Id, pastDate, time.getId(), theme.getId());
        jdbcTemplate.update("INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                member3Id, pastDate, time.getId(), theme.getId());

        Long id2 = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_waiting WHERE member_id = ?", Long.class, member2Id);
        Long id3 = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_waiting WHERE member_id = ?", Long.class, member3Id);

        assertThat(waitingRepository.calculateTurn(id2, pastDate, time.getId(), theme.getId())).isEqualTo(1L);
        assertThat(waitingRepository.calculateTurn(id3, pastDate, time.getId(), theme.getId())).isEqualTo(2L);
    }
}
