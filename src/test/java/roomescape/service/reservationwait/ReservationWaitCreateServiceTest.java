package roomescape.service.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.ReservationWaitStatus;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.Role;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;
import roomescape.service.dto.request.ReservationWaitSaveRequest;

class ReservationWaitCreateServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;

    @Autowired
    private ReservationWaitCreateService reservationWaitCreateService;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        themeRepository.save(new Theme("방탈출 1", "1번 방탈출", "썸네일 1"));
        memberRepository.save(new Member(new MemberName("사용자1"),
                new MemberEmail("user1@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
    }

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void createReservationWait_Success() {
        ReservationWaitSaveRequest request = new ReservationWaitSaveRequest(
                LocalDate.now().plusDays(1L), 1L, 1L);
        Member member = memberRepository.findById(1L).get();

        reservationWaitCreateService.create(request, member);

        assertThat(reservationWaitRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("이미 예약 대기 중인 경우 예외가 발생한다.")
    void checkAlreadyWait_Failure() {
        Member member = memberRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        LocalDate date = LocalDate.now().plusDays(1L);
        reservationWaitRepository.save(new ReservationWait(member, date, time, theme, ReservationWaitStatus.WAITING));

        ReservationWaitSaveRequest request = new ReservationWaitSaveRequest(
                date, 1L, 1L);

        assertThatThrownBy(() -> reservationWaitCreateService.create(request, member))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 예약 대기 중입니다.");
    }

    @Test
    @DisplayName("이미 예약 중인 경우 예외가 발생한다.")
    void checkAlreadyBooked_Failure() {
        Member member = memberRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        LocalDate date = LocalDate.now().plusDays(1L);
        reservationRepository.save(new Reservation(member, date, time, theme));

        ReservationWaitSaveRequest request = new ReservationWaitSaveRequest(
                date, 1L, 1L);

        assertThatThrownBy(() -> reservationWaitCreateService.create(request, member))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 예약 중입니다.");
    }
}
