package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.fixture.ReservationFixture;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;
import roomescape.domain.role.RoleRepository;
import roomescape.exception.UnAuthorizedException;

@ServiceTest
@Import(ReservationFixture.class)
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
    private RoleRepository roleRepository;

    @Autowired
    private ReservationBookingService reservationBookingService;

    @Autowired
    private Clock clock;

    @Autowired
    private ReservationFixture reservationFixture;

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

    @Test
    @DisplayName("예약 삭제 요청시 예약이 존재하면 예약을 삭제한다.")
    void shouldDeleteReservationWhenReservationExist() {
        Reservation reservation = reservationFixture.saveReservation();
        Member member = reservation.getMember();
        reservationBookingService.cancelReservation(member.getId(), reservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("다른 사람의 예약을 삭제하는 경우, 예외를 반환한다.")
    void shouldThrowExceptionWhenDeleteOtherMemberReservation() {
        Long reservationId = reservationFixture.saveReservation().getId();
        long memberId = memberRepository.save(MemberFixture.createMember("other")).getId();
        assertThatCode(() -> reservationBookingService.cancelReservation(memberId, reservationId))
                .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    @DisplayName("관리자가 다른 사람의 예약을 삭제하는 경우, 예약이 삭제된다.")
    void shouldDeleteReservationWhenAdmin() {
        Reservation reservation = reservationFixture.saveReservation();
        Member admin = new Member("admin", "admin@admin.com", "12341234");
        roleRepository.save(new MemberRole(admin, Role.ADMIN));
        reservationBookingService.cancelReservation(admin.getId(), reservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("예약 삭제 요청시 예약이 존재하지 않으면 예외를 반환한다.")
    void shouldThrowsIllegalArgumentExceptionWhenReservationDoesNotExist() {
        assertThatCode(() -> reservationBookingService.cancelReservation(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

}
