package roomescape.service.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;
import roomescape.service.dto.request.ReservationWaitSaveRequest;

@Sql(value = "claspath:clean_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationWaitCreateServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;

    @Autowired
    private ReservationWaitCreateService reservationWaitCreateService;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        themeRepository.save(new Theme("방탈출1", "1번 방탈출", "썸네일1"));
        memberRepository.save(
                new Member(
                        new MemberName("사용자"),
                        new MemberEmail("user@wooteco.com"),
                        new MemberPassword("user1"), Role.USER));
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
}
