package roomescape.service.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.domain.reservationwait.ReservationWaitStatus;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.Role;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;
import roomescape.service.dto.request.ReservationWaitSaveRequest;

class ReservationWaitDeleteServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;

    @Autowired
    private ReservationWaitDeleteService reservationWaitDeleteService;

    @BeforeEach
    void setUp() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("방탈출 1", "1번 방탈출", "썸네일 1"));
        Member member = memberRepository.save(new Member(new MemberName("사용자1"),
                new MemberEmail("user1@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
        ReservationWaitSaveRequest request = new ReservationWaitSaveRequest(
                LocalDate.now().plusDays(1L), 1L, 1L);
        reservationWaitRepository.save(new ReservationWait(member, LocalDate.now().plusDays(1L), time, theme, ReservationWaitStatus.WAITING));
    }

    @Test
    @DisplayName("예약 대기를 삭제한다.")
    void deleteReservationWait_Success() {
        reservationWaitDeleteService.cancelById(1L);

        assertThat(reservationWaitRepository.findAllByStatus(ReservationWaitStatus.WAITING)).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기 삭제 시 예외가 발생한다.")
    void deleteReservationWaitByUndefinedId_Failure() {
        assertThatThrownBy(() -> reservationWaitDeleteService.cancelById(2L))
                .isInstanceOf(InvalidRequestException.class);
    }
}
