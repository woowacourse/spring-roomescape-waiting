package roomescape.member.service;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;

@DataJpaTest
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void save() {
        // given
        MemberRequest request = new MemberRequest("hong@example.com", "password", "홍길동");

        // when
        memberService.save(request);

        // then
        final Member saved = memberRepository.findByEmailAndPassword(request.email(),
                request.password()).get();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(saved.getName()).isEqualTo("홍길동");
            soft.assertThat(saved.getEmail()).isEqualTo("hong@example.com");
            soft.assertThat(saved.getPassword()).isEqualTo("password");
            soft.assertThat(saved.getRole()).isEqualTo(MemberRole.MEMBER);
        });
    }

    @Test
    void findAll() {
        // given
        Member m1 = Member.withRole("A", "a@a.com", "pw", MemberRole.ADMIN);
        Member m2 = Member.withRole("B", "b@b.com", "pw", MemberRole.MEMBER);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<MemberResponse> responses = memberService.findAllMember();

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(responses).hasSize(2);
            soft.assertThat(responses.get(0).name()).isEqualTo("A");
            soft.assertThat(responses.get(0).email()).isEqualTo("a@a.com");
            soft.assertThat(responses.get(0).role()).isEqualTo(MemberRole.ADMIN.name());

            soft.assertThat(responses.get(1).name()).isEqualTo("B");
            soft.assertThat(responses.get(1).email()).isEqualTo("b@b.com");
            soft.assertThat(responses.get(1).role()).isEqualTo(MemberRole.MEMBER.name());
        });
    }
}
