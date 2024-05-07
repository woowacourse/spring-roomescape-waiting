package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@JdbcTest
class JdbcReservationRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;


    private ReservationTime savedReservationTime;
    private Theme savedTheme;
    private Member savedMember;
    private Reservation savedReservation;

    @Autowired
    private JdbcReservationRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationRepository = new JdbcReservationRepository(jdbcTemplate);
        this.reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        this.themeRepository = new JdbcThemeRepository(jdbcTemplate);
        this.memberRepository = new JdbcMemberRepository(jdbcTemplate);
    }


    @BeforeEach
    void saveReservation() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("hi", "happy", "abcd.html");
        savedTheme = themeRepository.save(theme);

        Member member = new Member("sudal", "aa@aa", "aa", Role.ADMIN);
        savedMember = memberRepository.save(member);

        Reservation reservation = new Reservation(savedMember, LocalDate.of(2999, 3, 28), savedReservationTime, savedTheme);
        savedReservation = reservationRepository.save(reservation);
    }

    @DisplayName("DB 예약 추가 테스트")
    @Test
    void save() {
        Assertions.assertAll(
                () -> assertThat(savedReservation.getTimeId()).isEqualTo(savedReservationTime.getId()),
                () -> assertThat(savedReservation.getThemeId()).isEqualTo(savedTheme.getId()),
                () -> assertThat(savedReservation.getMemberId()).isEqualTo(savedMember.getId())
        );
    }

    @DisplayName("DB 모든 예약 조회 테스트")
    @Test
    void findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAllReservations();
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("DB 예약 삭제 테스트")
    @Test
    void delete() {
        reservationRepository.delete(savedReservation.getId());
        List<Reservation> reservations = reservationRepository.findAllReservations();
        assertThat(reservations).isEmpty();
    }
}
