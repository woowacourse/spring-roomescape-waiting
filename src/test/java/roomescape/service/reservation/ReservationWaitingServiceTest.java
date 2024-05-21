package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.auth.TokenProvider;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.ReservationWaitingRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.UnauthorizedException;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationWaitingResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationWaitingServiceTest {
    @Autowired
    private ReservationWaitingService reservationWaitingService;
    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenProvider tokenProvider;
    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;
    private String token;

    @BeforeEach
    void setUp() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        member = memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.MEMBER));
        token = tokenProvider.create(member);
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
        assertions.assertThat(response.createdAt()).isBefore(LocalDateTime.now());
        assertions.assertAll();
    }

    @DisplayName("id로 등록된 예약 대기를 취소한다.")
    @Test
    void deleteById() {
        // given
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        ReservationWaiting waiting = new ReservationWaiting(member, theme, schedule);
        ReservationWaiting target = reservationWaitingRepository.save(waiting);

        // when
        reservationWaitingService.deleteById(target.getId(), tokenProvider.extractMemberId(token));

        // then
        assertThat(reservationWaitingRepository.findAll()).isEmpty();
    }

    @DisplayName("회원 정보가 일치하지 않으면 예약 취소 시 에외가 발생한다.")
    @Test
    void throwsExceptionWhenMemberIdNotMatched() {
        // given
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        ReservationWaiting waiting = new ReservationWaiting(member, theme, schedule);
        ReservationWaiting target = reservationWaitingRepository.save(waiting);

        // when & then
        Long targetId = target.getId();
        assertThatThrownBy(() -> reservationWaitingService.deleteById(targetId, -1))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("예약 대기를 취소할 권한이 없습니다.");
    }
}
