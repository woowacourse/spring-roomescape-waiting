package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION_DATE;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

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
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@JdbcTest
class JdbcReservationRepositoryImplTest {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    private final MemberRepository memberRepository;
    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;

    @Autowired
    JdbcReservationRepositoryImplTest(JdbcTemplate jdbcTemplate) {
        this.reservationRepository = new JdbcReservationRepositoryImpl(jdbcTemplate);
        this.reservationTimeRepository = new JdbcReservationTimeRepositoryImpl(jdbcTemplate);
        this.themeRepository = new JdbcThemeRepositoryImpl(jdbcTemplate);
        this.memberRepository = new JdbcMemberRepositoryImpl(jdbcTemplate);
    }

    @BeforeEach
    void setUp() {
        reservationTime = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        theme = themeRepository.save(VALID_THEME);
        member = memberRepository.save(VALID_MEMBER);
    }

    @DisplayName("time_id값을 통해 예약이 존재하는지를 구한다.")
    @Test
    void isTimeIdExists() {
        Reservation reservation1 = new Reservation(member, VALID_RESERVATION_DATE, reservationTime, theme);
        Reservation reservation2 = new Reservation(member, VALID_RESERVATION_DATE, reservationTime, theme);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        boolean actual = reservationRepository.isTimeIdExists(reservationTime.getId());

        assertThat(actual).isTrue();
    }

    @DisplayName("date, time_id, theme_id로 중복 예약이 존재하는지를 구한다.")
    @Test
    void isDuplication() {
        Reservation reservation = new Reservation(member, VALID_RESERVATION_DATE, reservationTime, theme);
        reservationRepository.save(reservation);
        boolean actual = reservationRepository.isDuplicated(VALID_RESERVATION_DATE.getDate(), reservationTime.getId(),
            theme.getId());

        assertThat(actual).isTrue();
    }
}
