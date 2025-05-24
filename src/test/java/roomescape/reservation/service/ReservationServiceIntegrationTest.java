package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.auth.dto.LoginMember;
import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@Sql("/data.sql")
class ReservationServiceIntegrationTest {

    private ReservationService reservationService;

    private Clock clock = Clock.systemDefaultZone();

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Member member;
    private Theme theme;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(clock, reservationRepository, reservationTimeRepository,
                themeRepository, memberRepository);
        member = memberRepository.save(Member.withDefaultRole("홍길동", "hong@example.com", "password"));
        theme = themeRepository.save(Theme.of("테마명", "테마 설명", "thumbnail.jpg"));
        time = reservationTimeRepository.save(ReservationTime.from(LocalTime.of(13, 0)));
    }

    @Test
    void 예약_생성_성공() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationRequest request = new ReservationRequest(date, time.getId(), theme.getId());
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        // when
        ReservationResponse response = reservationService.saveReservation(request, loginMember);
        Reservation reservation = reservationRepository.findById(response.id()).orElseThrow();

        // then
        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime()).isEqualTo(time);
        assertThat(reservation.getTheme()).isEqualTo(theme);
        assertThat(reservation.getMember().getId()).isEqualTo(member.getId());
        assertThat(reservation.getReservationStatus().getStatus()).isEqualTo(Status.BOOKED);
    }

    @Test
    void 예약_시간_검증_실패() {
        // given
        LocalDate date = LocalDate.now().minusDays(1);
        ReservationRequest request = new ReservationRequest(date, time.getId(), theme.getId());
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        // when & then
        assertThatThrownBy(() ->
                reservationService.saveReservation(request, loginMember)
        )
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약은 현재 시간 이후로 가능합니다.");
    }

    @Test
    void 대기_목록_관리_성공() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationRequest waitingRequest1 = new ReservationRequest(date, time.getId(), theme.getId());
        ReservationRequest waitingRequest2 = new ReservationRequest(date, time.getId(), theme.getId());
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        ReservationResponse response1 = reservationService.saveReservation(waitingRequest1, loginMember);
        ReservationResponse response2 = reservationService.saveReservation(waitingRequest2, loginMember);
        Reservation waitingReservation1 = reservationRepository.findById(response1.id()).orElseThrow();
        Reservation waitingReservation2 = reservationRepository.findById(response2.id()).orElseThrow();

        // when
        reservationService.deleteReservation(waitingReservation1.getId());
        Reservation updatedReservation2 = reservationRepository.findById(waitingReservation2.getId()).orElseThrow();

        // then
        assertThat(updatedReservation2.getReservationStatus().getStatus()).isEqualTo(Status.BOOKED);
        assertThat(updatedReservation2.getReservationStatus().getRank()).isEqualTo(0L);
    }
}
