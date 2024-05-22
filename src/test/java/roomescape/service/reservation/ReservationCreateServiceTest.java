package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.ReservationTime;
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
import roomescape.service.dto.request.ReservationSaveRequest;

class ReservationCreateServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;

    @Autowired
    private ReservationCreateService reservationCreateService;

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
    @DisplayName("예약 가능한 시간인 경우 성공한다.")
    void checkDuplicateReservationTime_Success() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().plusDays(1L), 1L, 1L);
        Member member = memberRepository.findById(1L).get();

        assertThatCode(() -> reservationCreateService.create(request, member))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약된 시간인 경우 예외가 발생한다.")
    void checkDuplicateReservationTime_Failure() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().plusDays(1L), 1L, 1L);
        Member member = memberRepository.findById(1L).get();
        reservationCreateService.create(request, member);

        ReservationSaveRequest request2 = new ReservationSaveRequest(
                LocalDate.now().plusDays(1L), 1L, 1L);

        assertThatThrownBy(() -> reservationCreateService.create(request2, member))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("해당 시간에 이미 예약된 테마입니다.");
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성시 예외가 발생한다.")
    void checkReservationDateTimeIsFuture_Failure() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().minusDays(1L), 1L, 1L);
        Member member = memberRepository.findById(1L).get();

        assertThatThrownBy(() -> reservationCreateService.create(request, member))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약 생성은 불가능합니다.");
    }
}
