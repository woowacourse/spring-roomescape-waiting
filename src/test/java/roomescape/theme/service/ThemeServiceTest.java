package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:service-test;DB_CLOSE_DELAY=-1"
})
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;

    private Theme themeA;
    private Theme themeB;
    private Theme themeC;
    private Member member;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        themeA = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        themeB = themeRepository.save(Theme.restore(null, "테마B", "설명B", "https://b.com"));
        themeC = themeRepository.save(Theme.restore(null, "테마C", "설명C", "https://c.com"));
        member = memberRepository.save(Member.restore(null, "user1", "user1@test.com", "1234"));
        time = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    private void reserveInLastWeek(Theme theme, int count) {
        for (int day = 1; day <= count; day++) {
            reservationRepository.save(
                    Reservation.restore(null, member, LocalDate.now().minusDays(day), time, theme));
        }
    }

    @Test
    @DisplayName("최근 일주일간 예약 수 기준으로 인기 테마를 내림차순 조회한다")
    void 최근_일주일_예약수_기준_인기_테마를_내림차순_조회한다() {
        reserveInLastWeek(themeA, 3);
        reserveInLastWeek(themeB, 2);
        reserveInLastWeek(themeC, 1);

        List<ThemeResponse> result = themeService.getTopThemes(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("테마A");
        assertThat(result.get(1).name()).isEqualTo("테마B");
        assertThat(result.get(2).name()).isEqualTo("테마C");
    }

    @Test
    @DisplayName("인기 테마 조회 시 limit만큼만 반환한다")
    void 인기_테마_조회_시_limit만큼만_반환한다() {
        reserveInLastWeek(themeA, 3);
        reserveInLastWeek(themeB, 2);
        reserveInLastWeek(themeC, 1);

        assertThat(themeService.getTopThemes(2)).hasSize(2);
    }

    @Test
    @DisplayName("ID로 테마를 조회한다")
    void ID로_테마를_조회한다() {
        assertThat(themeService.getById(themeA.getId()).getName()).isEqualTo("테마A");
    }

    @Test
    @DisplayName("존재하지 않는 테마를 조회하면 예외가 발생한다")
    void 존재하지_않는_테마_조회_시_예외가_발생한다() {
        Theme temp = themeRepository.save(Theme.restore(null, "임시테마", "설명", "https://t.com"));
        themeRepository.deleteById(temp.getId());

        assertThatThrownBy(() -> themeService.getById(temp.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }
}
