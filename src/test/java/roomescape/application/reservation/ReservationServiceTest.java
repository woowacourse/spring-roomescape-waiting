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
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Password;
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
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("정상적인 예약 요청을 받아서 저장한다.")
    void shouldReturnReservationResponseWhenValidReservationRequestSave() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("themeName", "desc", "url"));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationRequest reservationRequest = new ReservationRequest(
                member.getId(),
                LocalDate.of(2024, 1, 1),
                time.getId(),
                theme.getId()
        );

        reservationService.create(reservationRequest);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약을 생성시 예외가 발생한다.")
    void shouldReturnIllegalArgumentExceptionWhenNotFoundReservationTime() {
        Theme savedTheme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationRequest request = new ReservationRequest(
                member.getId(),
                LocalDate.of(2024, 1, 1),
                99L,
                savedTheme.getId());

        assertThatCode(() -> reservationService.create(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 생성시 예외를 반환한다.")
    void shouldThrowIllegalArgumentExceptionWhenNotFoundTheme() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationRequest request = new ReservationRequest(
                member.getId(),
                LocalDate.of(2024, 1, 1),
                time.getId(),
                99L
        );
        assertThatCode(() -> reservationService.create(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

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
        reservationRepository.save(request.toReservation(member, time, theme));

        assertThatCode(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 예약입니다.");
    }

    @Test
    @DisplayName("과거 시간을 예약하는 경우 예외를 반환한다.")
    void shouldThrowsIllegalArgumentExceptionWhenReservationDateIsBeforeCurrentDate() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationRequest request = new ReservationRequest(
                member.getId(),
                LocalDate.of(1999, 12, 31),
                time.getId(),
                theme.getId()
        );

        assertThatCode(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 시간보다 과거로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void shouldReturnReservationResponsesWhenReservationsExist() {
        saveReservation();
        List<ReservationResponse> reservationResponses = reservationService.findAll();
        assertThat(reservationResponses).hasSize(1);
    }

    @Test
    @DisplayName("예약 삭제 요청시 예약이 존재하면 예약을 삭제한다.")
    void shouldDeleteReservationWhenReservationExist() {
        Reservation reservation = saveReservation();
        Member member = reservation.getMember();
        reservationService.deleteById(member.getId(), reservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("다른 사람의 예약을 삭제하는 경우, 예외를 반환한다.")
    void shouldThrowExceptionWhenDeleteOtherMemberReservation() {
        Reservation reservation = saveReservation();
        Member otherMember = memberRepository.save(MemberFixture.createMember("other"));
        assertThatCode(() -> reservationService.deleteById(otherMember.getId(), reservation.getId()))
                .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    @DisplayName("관리자가 다른 사람의 예약을 삭제하는 경우, 예약이 삭제된다.")
    void shouldDeleteReservationWhenAdmin() {
        Reservation reservation = saveReservation();
        Member admin = new Member(new MemberName("admin"), new Email("admin@admin.com"), new Password("12341234"));
        roleRepository.save(new MemberRole(admin, Role.ADMIN));
        reservationService.deleteById(admin.getId(), reservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("예약 삭제 요청시 예약이 존재하지 않으면 예외를 반환한다.")
    void shouldThrowsIllegalArgumentExceptionWhenReservationDoesNotExist() {
        assertThatCode(() -> reservationService.deleteById(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("예약의 멤버 업데이트를 수행한다.")
    void updateMemberById() {
        Reservation reservation = saveReservation();
        Member member = memberRepository.save(MemberFixture.createMember("시소"));
        reservationService.updateMemberById(reservation.getId(),
                reservation.getMember().getId(), member);

        Reservation updatedReservation = reservationRepository.getById(reservation.getId());
        assertThat(updatedReservation.getMember().getName()).isEqualTo("시소");
    }

    private Reservation saveReservation() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        Reservation reservation = new Reservation(
                member,
                LocalDate.of(2024, 1, 1),
                time,
                theme
        );
        return reservationRepository.save(reservation);
    }
}
