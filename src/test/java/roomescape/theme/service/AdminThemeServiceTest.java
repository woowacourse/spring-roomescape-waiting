package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.AdminThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:service-test;DB_CLOSE_DELAY=-1"
})
@Transactional
class AdminThemeServiceTest {

    @Autowired
    private AdminThemeService adminThemeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;

    @Test
    @DisplayName("테마를 생성하면 응답에 정보가 담기고 DB에 저장된다")
    void 테마_생성_시_응답과_DB에_저장된다() {
        AdminThemeResponse response = adminThemeService.createTheme(
                new AdminThemeRequest("테마A", "설명A", "https://a.com", 25000L));

        assertThat(response.id()).isNotNull().isPositive();
        assertThat(response.name()).isEqualTo("테마A");
        assertThat(themeRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("전체 테마 목록을 조회한다")
    void 전체_테마_목록을_조회한다() {
        adminThemeService.createTheme(new AdminThemeRequest("테마A", "설명A", "https://a.com", 25000L));
        adminThemeService.createTheme(new AdminThemeRequest("테마B", "설명B", "https://b.com", 25000L));

        assertThat(adminThemeService.getAllThemes()).hasSize(2);
    }

    @Test
    @DisplayName("테마를 삭제하면 DB에서 제거된다")
    void 테마_삭제_시_DB에서_제거된다() {
        AdminThemeResponse saved = adminThemeService.createTheme(
                new AdminThemeRequest("테마A", "설명A", "https://a.com", 25000L));

        adminThemeService.deleteTheme(saved.id());

        assertThat(themeRepository.findById(saved.id())).isEmpty();
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
    void 예약이_존재하는_테마는_삭제할_수_없다() {
        Theme theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        Member member = memberRepository.save(Member.of("user1", "user1@test.com", "1234"));
        ReservationTime time = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        reservationRepository.save(Reservation.restore(null, member, LocalDate.now().plusDays(1), time, theme));

        assertThatThrownBy(() -> adminThemeService.deleteTheme(theme.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }
}
