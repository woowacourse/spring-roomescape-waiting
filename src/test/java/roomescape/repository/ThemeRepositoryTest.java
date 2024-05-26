package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.THEME1;
import static roomescape.TestFixture.THEME2;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;

class ThemeRepositoryTest extends DBTest {

    @DisplayName("특정 기간동안 가장 많이 예약된 테마를 갯수만큼 조회한다.")
    @Test
    void findMostReservedThemeInPeriodByCount() {
        // given
        Member member = memberRepository.save(MEMBER1);
        ReservationTime time = timeRepository.save(RESERVATION_TIME_10AM);
        Theme theme1 = themeRepository.save(THEME1);
        Theme theme2 = themeRepository.save(THEME2);

        reservationRepository.save(new Reservation(member, LocalDate.now(), time, theme1, Status.CONFIRMED));
        reservationRepository.save(
                new Reservation(member, LocalDate.now().minusDays(1), time, theme2, Status.CONFIRMED));

        // when: LIMIT 을 확인하기 위해 1개만 조회한다.
        List<Theme> themes = themeRepository.findMostReservedThemeInPeriodByCount(LocalDate.now().minusDays(1),
                LocalDate.now(), 1);

        // then
        assertThat(themes).hasSize(1);
        assertThat(themes).extracting(Theme::getName).containsAnyOf(theme1.getName(), theme2.getName());
    }
}
