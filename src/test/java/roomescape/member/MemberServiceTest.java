package roomescape.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.ReservationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public MemberServiceTest() {
        memberRepository = mock(MemberRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        memberService = new MemberService(memberRepository, reservationRepository);
    }

    @DisplayName("member를 생성하여 저장한다.")
    @Test
    void createMember() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        final Member expected = new Member(
                memberRequest.email(),
                memberRequest.password(),
                memberRequest.name(),
                MemberRole.MEMBER
        );

        // when
        memberService.createMember(memberRequest);

        // then
        then(memberRepository)
                .should()
                .save(expected);
    }

    @DisplayName("이미 존재하는 이메일로 생성하면, 예외가 발생한다.")
    @Test
    void createMember1() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        given(memberRepository.existsByEmail("admin@email.com"))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            memberService.createMember(memberRequest);
        }).isInstanceOf(MemberEmailConflictException.class);
    }

    @DisplayName("존재하는 모든 member를 반환한다.")
    @Test
    void readAll() {
        // given
        given(memberRepository.findAll())
                .willReturn(List.of(new Member(1L, "email", "pass", "name", MemberRole.MEMBER)));

        // when
        final List<MemberResponse> actual = memberService.readAllMember();

        // then
        assertThat(actual).hasSize(1);
    }

    @DisplayName("member가 없다면 빈 컬렉션을 반환한다.")
    @Test
    void readAll1() {
        // given & when
        final List<MemberResponse> actual = memberService.readAllMember();

        // then
        assertThat(actual).isEmpty();
    }

    @DisplayName("member로 예약 목록을 불러올 때, member를 찾을 수 없는 경우 예외를 발생시킨다")
    @Test
    void readReservationsByMember() {
        // given
        String notExistEmail = "may@gmail.com";
        given(memberRepository.findByEmail(notExistEmail))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.readAllReservationsByMember(new LoginMember("may", notExistEmail, MemberRole.MEMBER)));
    }
}
