package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.member.LoginMember;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String date = "2060-01-01";

    private ReservationTime reservationTime;
    private Theme theme;
    private LoginMember member;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    void addInitialData() {
        jdbcTemplate.update(
            "INSERT INTO member(name, email, password, role) VALUES ('관리자', 'admin@a.com', '123a!', 'ADMIN')");
        member = memberRepository.findById(1L).orElseThrow();
        reservationTime = reservationTimeRepository.save(new ReservationTime("05:30"));
        theme = themeRepository.save(new Theme("theme_name", "theme_desc", "https://"));

        reservation1 = new Reservation(member, date, reservationTime, theme);
        reservation2 = new Reservation(member, "2070-01-01", reservationTime, theme);
    }

    @DisplayName("예약 정보를 DB에 저장한다.")
    @Test
    void save() {
        Reservation saved = reservationRepository.save(reservation1);

        assertThat(saved).isEqualTo(new Reservation(saved.getId(), member, date, reservationTime, theme));
    }

    @DisplayName("모든 예약 정보를 DB에서 조회한다.")
    @Test
    void findAll() {
        Reservation saved1 = reservationRepository.save(reservation1);
        Reservation saved2 = reservationRepository.save(reservation2);

        assertThat(reservationRepository.findAll()).containsExactly(saved1, saved2);
    }

    @DisplayName("id값을 통해 예약 정보를 DB에서 삭제한다.")
    @Test
    void deleteById() {
        Reservation saved1 = reservationRepository.save(reservation1);
        Reservation saved2 = reservationRepository.save(reservation2);

        reservationRepository.deleteById(saved1.getId());

        assertThat(reservationRepository.findAll()).containsExactly(saved2);
    }

    @DisplayName("time_id값을 통해 예약이 존재하는지를 구한다.")
    @Test
    void isExistsHavingTimeId() {
        Reservation savedReservation = reservationRepository.save(reservation1);
        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime("11:11"));

        Long existingTimeId = savedReservation.getTime().getId();
        Long nonExistingTimeId = savedReservationTime.getId();

        assertAll(
            () -> assertThat(reservationRepository.isTimeIdUsed(existingTimeId)).isTrue(),
            () -> assertThat(reservationRepository.isTimeIdUsed(nonExistingTimeId)).isFalse()
        );
    }

    @DisplayName("date, time_id, theme_id로 중복 예약이 존재하는지를 구한다.")
    @Test
    void isDuplication() {
        reservationRepository.save(reservation1);

        assertThat(
            reservationRepository.isDuplicated(date, reservationTime.getId(), theme.getId())
        ).isTrue();
    }
}
