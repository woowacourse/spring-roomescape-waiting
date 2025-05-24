package roomescape.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ReservationTestFixture;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.member.model.Role;
import roomescape.reservation.application.UserReservationService;
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.support.IntegrationTestSupport;

class UserReservationServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserReservationService userReservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        ReservationTime reservationTime = ReservationTime.builder()
            .startAt(LocalTime.parse("10:00"))
            .build();

        ReservationTheme theme = ReservationTheme.builder()
            .name("이름")
            .description("설명")
            .thumbnail("썸네일")
            .build();

        Member member = Member.builder()
            .name("어드민")
            .email("admin@naver.com")
            .password("1234")
            .role(Role.ADMIN)
            .build();
        reservationTimeRepository.save(reservationTime);
        reservationThemeRepository.save(theme);
        memberRepository.save(member);
    }

    @DisplayName("요청된 예약 정보로 예약을 진행할 수 있다")
    @Test
    void createFuture() {
        // given
        LocalDate date = LocalDate.now().plusDays(20);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 1L;
        CreateReservationServiceRequest request = new CreateReservationServiceRequest(memberId, date, timeId, themeId);

        // when
        ReservationServiceResponse response = userReservationService.create(request);

        // then
        List<Reservation> reservations = reservationRepository.getConfirmedAll();
        assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(1);
            softly.assertThat(reservations.getFirst().getMember().getId()).isEqualTo(response.id());
            softly.assertThat(reservations.getFirst().getDate()).isEqualTo(response.date());
        });
    }

    @DisplayName("요청한 예약 시간이 과거라면 예외를 발생시킨다")
    @Test
    void pastException() {
        // given
        String name = "웨이드";
        LocalDate date = LocalDate.now().minusDays(10);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 1L;
        CreateReservationServiceRequest request = new CreateReservationServiceRequest( memberId, date, timeId, themeId);

        // when & then
        Assertions.assertThatThrownBy(() -> userReservationService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @DisplayName("예약 요청한 테마, 예약 시간에 이미 예약이 있다면 예외를 발생시킨다")
    @Test
    void duplicationException() {
        // given
        LocalDate date = LocalDate.now().minusDays(10);
        ReservationTime reservationTime = ReservationTestFixture.getReservationTimeFixture();
        ReservationTheme reservationTheme = ReservationTestFixture.getReservationThemeFixture();
        Reservation reservation = ReservationTestFixture.createReservation(date, reservationTime, reservationTheme);

        reservationTimeRepository.save(reservationTime);
        reservationThemeRepository.save(reservationTheme);
        reservationRepository.save(reservation);
        Long memberId = 1L;
        CreateReservationServiceRequest request = new CreateReservationServiceRequest(
            memberId, date, reservationTime.getId(), reservationTheme.getId());

        // when & then
        Assertions.assertThatThrownBy(() -> userReservationService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
