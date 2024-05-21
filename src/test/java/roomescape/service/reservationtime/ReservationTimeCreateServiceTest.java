package roomescape.service.reservationtime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;
import roomescape.service.dto.request.ReservationTimeSaveRequest;

class ReservationTimeCreateServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeCreateService reservationTimeCreateService;

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
    @DisplayName("존재하지 않는 예약 시간인 경우 생성에 성공한다")
    void checkDuplicateTime_Success() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(12, 0));

        assertThatCode(() -> reservationTimeCreateService.createReservationTime(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간인 경우 생성 시 예외가 발생한다.")
    void checkDuplicateTime_Failure() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationTimeCreateService.createReservationTime(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 예약 시간입니다.");
    }
}
