package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.dto.request.MemberRequest;
import roomescape.entity.Member;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaMemberRepository;

@DataJpaTest
class MemberServiceTest {

    @Autowired
    private JpaMemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    @DisplayName("회원을 생성할 수 있다.")
    void addMember() {
        MemberRequest request = new MemberRequest("이름", "이메일", "비밀번호");

        assertThat(memberService.addMember(request)).isNotNull();
    }

    @Test
    @DisplayName("중복된 이메일이 존재하면 회원을 생성할 수 없다.")
    void addMemberWithDuplicateEmail() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        memberRepository.save(member);

        MemberRequest request = new MemberRequest("이름", "이메일", "비밀번호");

        assertThatThrownBy(() -> memberService.addMember(request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("member");
    }
}
