package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.ReservationWaitingRepository;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationWaitingResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationWaitingServiceTest {
    @Autowired
    private ReservationWaitingService reservationWaitingService;
    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;
    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        member = memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.MEMBER));
    }

    @DisplayName("새로운 예약 대기를 저장한다.")
    @Test
    void create() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationRequest request = new ReservationRequest(
                date, reservationTime.getId(), theme.getId()
        );

        // when
        ReservationWaitingResponse response = reservationWaitingService.create(request, member.getId());

        // then
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(response.id()).isPositive();
        assertions.assertThat(response.time().id()).isEqualTo(reservationTime.getId());
        assertions.assertThat(response.theme().id()).isEqualTo(theme.getId());
        assertions.assertThat(response.theme().id()).isEqualTo(theme.getId());
        assertions.assertAll();
    }
}
