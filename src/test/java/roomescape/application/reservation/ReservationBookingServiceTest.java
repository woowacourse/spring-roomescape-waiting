package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@ServiceTest
class ReservationBookingServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private ReservationBookingService reservationBookingService;

    @Test
    @DisplayName("중복된 예약을 하는 경우 예외를 반환한다.")
    void shouldReturnIllegalStateExceptionWhenDuplicatedReservationCreate() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationRequest request = new ReservationRequest(
                member.getId(),
                LocalDate.of(2024, 1, 1),
                time.getId(),
                theme.getId()
        );
        reservationRepository.save(request.toReservation(member, time, theme, LocalDateTime.now(clock)));

        assertThatCode(() -> reservationBookingService.bookReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 예약입니다.");
    }
}
