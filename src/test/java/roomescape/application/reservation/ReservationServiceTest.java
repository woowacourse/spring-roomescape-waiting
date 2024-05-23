package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.MEMBER_ARU;
import static roomescape.fixture.MemberFixture.MEMBER_PK;

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
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

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

    @Test
    @DisplayName("정상적인 예약 요청을 받아서 저장한다.")
    void shouldReturnReservationResponseWhenValidReservationRequestSave() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("themeName", "desc", "url"));
        Member member = memberRepository.save(MEMBER_ARU.create());
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
        Member member = memberRepository.save(MEMBER_ARU.create());
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
        Member member = memberRepository.save(MEMBER_ARU.create());
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
    @DisplayName("과거 시간을 예약하는 경우 예외를 반환한다.")
    void shouldThrowsIllegalArgumentExceptionWhenReservationDateIsBeforeCurrentDate() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(MEMBER_ARU.create());
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
    @DisplayName("예약을 조작할 수 있는 권한을 확인한다.")
    void permissionCheck() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("themeName", "desc", "url"));
        Member member = memberRepository.save(MEMBER_ARU.create());
        Member otherMember = memberRepository.save(MEMBER_PK.create());
        Reservation reservation = reservationRepository.save(new Reservation(
                member,
                LocalDate.of(2024, 1, 1),
                time,
                theme,
                LocalDateTime.parse("2023-01-01T12:00:00"))
        );
        assertAll(
                () -> assertThat(reservationService.hasNoAccessToReservation(member.getId(), reservation.getId()))
                        .isFalse(),
                () -> assertThat(reservationService.hasNoAccessToReservation(otherMember.getId(), reservation.getId()))
                        .isTrue()
        );
    }

}
