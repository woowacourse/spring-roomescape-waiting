package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static roomescape.model.Role.MEMBER;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import roomescape.controller.request.MemberLoginRequest;
import roomescape.exception.AuthenticationException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = "/test_data.sql")
class MemberServiceTest {

    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final MemberService memberService;

    @Autowired
    MemberServiceTest(MemberRepository memberRepository, MemberService memberService) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
    }

    @DisplayName("아이디와 비밀번호가 같은 유저가 존재하면 해당 유저를 반환한다.")
    @Test
    void should_find_member_when_member_exist() {
        Member member = new Member("배키", MEMBER, "hello@email.com", "1234");
        memberRepository.save(member);
        MemberLoginRequest request = new MemberLoginRequest("1234", "hello@email.com");

        Member findMember = memberService.findMemberByEmailAndPassword(request);

        assertThat(findMember).isEqualTo(member);
    }

    @DisplayName("아이디와 비밀번호 같은 유저가 없으면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_member_not_exist() {
        Member member = new Member(1L, "배키", MEMBER, "hello@email.com", "1234");
        memberRepository.save(member);
        MemberLoginRequest request = new MemberLoginRequest("1111", "sun@email.com");

        assertThatThrownBy(() -> memberService.findMemberByEmailAndPassword(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @DisplayName("아이디를 통해 사용자 이름을 조회한다.")
    @Test
    void should_find_member_name_when_give_id() {
        Member member = new Member(1L, "배키", MEMBER, "hello@email.com", "1234");
        memberRepository.save(member);

        String memberNameById = memberService.findMemberNameById(1L);

        assertThat(memberNameById).isEqualTo("배키");
    }

    @DisplayName("주어진 아이디에 해당하는 사용자가 없으면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_member_id_not_exist() {
        assertThatThrownBy(() -> memberService.findMemberNameById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("아이디를 통해 사용자 이름을 조회한다.")
    @Test
    void should_find_member_when_give_id() {
        Member member = new Member(1L, "배키", MEMBER, "hello@email.com", "1234");
        memberRepository.save(member);

        Member memberById = memberService.findMemberById(1L);

        assertThat(memberById).isEqualTo(member);
    }

    @DisplayName("주어진 아이디에 해당하는 사용자가 없으면 예외가 발생한다.")
    @Test
    void should_not_find_member_and_throw_exception_when_member_id_not_exist() {
        assertThatThrownBy(() -> memberService.findMemberById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("모든 사용자를 조회한다.")
    @Test
    void should_find_all_members() {
        memberRepository.save(new Member(1L, "썬", MEMBER, "sun@email.com", "1111"));
        memberRepository.save(new Member(2L, "배키", MEMBER, "dmsgml@email.com", "2222"));

        List<Member> members = memberService.findAllMembers();

        assertThat(members).hasSize(2);
    }
}
