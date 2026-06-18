package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.dto.ThemeCreationRequest;
import roomescape.domain.theme.dto.ThemeCreationResponse;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.support.exception.RoomescapeException;

@SpringBootTest
@Sql("/truncate.sql")
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        ThemeCreationRequest request = new ThemeCreationRequest("테마", "설명", "url");

        ThemeCreationResponse response = themeService.createTheme(request);

        assertThat(response.name()).isEqualTo("테마");
    }

    @Test
    @DisplayName("사용 중인 테마를 삭제하려 하면 예외가 발생한다.")
    void deleteInUseTheme() {
        Theme theme = createTheme("테마");
        ReservationDate date = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Member tester = memberRepository.save(Member.createWithoutId("테스터"));
        reservationRepository.save(Reservation.createWithoutId(tester, date, time, theme));

        assertThatThrownBy(() -> themeService.deleteTheme(theme.getId()))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("인기 테마 순위를 조회한다.")
    void getThemeRank() {
        List<ThemeRankResponse> responses = themeService.getThemeRank();

        assertThat(responses).isNotNull();
    }

    private Theme createTheme(String name) {
        return themeRepository.save(Theme.createWithoutId(name, "설명", "url"));
    }
}
