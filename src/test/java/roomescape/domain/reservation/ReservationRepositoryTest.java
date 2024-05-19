package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@DataJpaTest
@Sql("/reservation.sql")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        member = memberRepository.findById(1L).orElseThrow();
        time = reservationTimeRepository.findById(1L).orElseThrow();
        theme = themeRepository.findById(1L).orElseThrow();
    }

    @Test
    @DisplayName("조건에 해당하는 모든 예약들을 조회한다.")
    void findAll() {
        List<Reservation> reservations = reservationRepository.findAllByConditions(
                member.getId(),
                theme.getId(),
                LocalDate.of(2024, 4, 8),
                LocalDate.of(2024, 4, 10)
        );
        Reservation reservation = reservations.get(0);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2);
            softly.assertThat(reservation.getMember().getId()).isEqualTo(member.getId());
            softly.assertThat(reservation.getTheme().getId()).isEqualTo(theme.getId());
        });
    }

    @Test
    @DisplayName("예약 시간 id에 해당하는 예약이 존재하는지 확인한다.")
    void existsByTimeId() {
        assertThat(reservationRepository.existsByTimeId(time.getId())).isTrue();
    }

    @Test
    @DisplayName("테마 id에 해당하는 예약이 존재하는지 확인한다.")
    void existsByThemeId() {
        assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue();
    }

    @Test
    @DisplayName("날짜와 시간 id, 테마 id에 해당하는 예약이 존재하는지 확인한다.")
    void existsByReservation() {
        LocalDate existsDate = LocalDate.of(2024, 4, 8);
        LocalDate notExistsDate = LocalDate.of(2024, 5, 5);

        assertThat(reservationRepository.existsByReservation(existsDate, 1L, 1L)).isTrue();
        assertThat(reservationRepository.existsByReservation(notExistsDate, 1L, 1L)).isFalse();
    }
}
