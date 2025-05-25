package roomescape.domain.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        themeRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 수 기준으로 테마 순위를 조회한다")
    void findThemeRankingTest() {
        // given
        Theme theme1 = themeRepository.save(Theme.withoutId("테마1", "설명1", "썸네일1"));
        Theme theme2 = themeRepository.save(Theme.withoutId("테마2", "설명2", "썸네일2"));
        Theme theme3 = themeRepository.save(Theme.withoutId("테마3", "설명3", "썸네일3"));

        Member member = Member.of(1L, "user", "user@example.com", "password", Role.USER);
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(14, 0));
        LocalDateTime date = LocalDateTime.now();

        Waiting waitingReserved = Waiting.waitingWithoutId(date, ReservationStatus.RESERVED);
        Waiting waitingReserved2 = Waiting.waitingWithoutId(date, ReservationStatus.RESERVED);
        Waiting waitingReserved3 = Waiting.waitingWithoutId(date, ReservationStatus.RESERVED);
        Waiting waitingReserved4 = Waiting.waitingWithoutId(date, ReservationStatus.RESERVED);

        reservationRepository.save(Reservation.withoutId(member, theme1, date.toLocalDate(), time, waitingReserved));
        reservationRepository.save(Reservation.withoutId(member, theme1, date.toLocalDate(), time, waitingReserved2));
        reservationRepository.save(Reservation.withoutId(member, theme1, date.toLocalDate(), time, waitingReserved3));

        reservationRepository.save(Reservation.withoutId(member, theme2, date.toLocalDate(), time, waitingReserved4));

        // when
        List<Theme> result = themeRepository.findThemeRanking(
                date.toLocalDate().minusDays(1),
                date.toLocalDate().plusDays(1),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(theme1.getId());
        assertThat(result.get(1).getId()).isEqualTo(theme2.getId());
        assertThat(result.get(2).getId()).isEqualTo(theme3.getId());
    }
}
