package roomescape.member.service;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.Password;
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
                Password.createForMember(request.password())).get();
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
        Member member1 = Member.builder()
                .name("철수")
                .email("test@naver.com")
                .password(Password.createForMember("1234"))
                .role(MemberRole.ADMIN)
                .build();
        Member member2 = Member.builder()
                .name("영희")
                .email("kakao@naver.com")
                .password(Password.createForMember("4321"))
                .role(MemberRole.MEMBER)
                .build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<MemberResponse> responses = memberService.findAllMember();

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(responses).hasSize(2);
            soft.assertThat(responses.get(0).name()).isEqualTo("철수");
            soft.assertThat(responses.get(0).email()).isEqualTo("test@naver.com");
            soft.assertThat(responses.get(0).role()).isEqualTo(MemberRole.ADMIN.name());

            soft.assertThat(responses.get(1).name()).isEqualTo("영희");
            soft.assertThat(responses.get(1).email()).isEqualTo("kakao@naver.com");
            soft.assertThat(responses.get(1).role()).isEqualTo(MemberRole.MEMBER.name());
        });
    }
}
