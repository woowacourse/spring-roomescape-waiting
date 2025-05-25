package roomescape.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.member.application.dto.MemberRequest;
import roomescape.member.application.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.EmailAlreadyExistsException;
import roomescape.member.infrastructure.BcryptPasswordEncoder;
import roomescape.member.infrastructure.MemberRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import({
        MemberService.class,
        MemberRepositoryAdapter.class,
        BcryptPasswordEncoder.class
})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("회원 생성 - 성공")
    @Test
    void create_success() {
        // given
        String name = "에드";
        String email = "ed@example.com";
        String password = "password123";
        MemberRequest request = new MemberRequest(email, password, name);

        // when
        MemberResponse response = memberService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(name);
        assertThat(response.email()).isEqualTo(email);
    }

    @DisplayName("회원 생성 - 이메일 중복 시 예외 발생")
    @Test
    void create_duplicateEmail() {
        // given
        String name = "에드";
        String email = "ed@example.com";
        String password = "password123";

        Member member = MemberFixture.createMember(name, email, password);
        memberRepository.save(member);

        MemberRequest request = new MemberRequest(email, password, name);

        // when & then
        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @DisplayName("모든 회원 조회 - 성공")
    @Test
    void findAll_success() {
        // given
        Member member1 = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member1);

        Member member2 = MemberFixture.createMember("김진우", "jinu@example.com", "password456");
        memberRepository.save(member2);

        // when
        List<MemberResponse> responses = memberService.findAll();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(member1.getId());
        assertThat(responses.get(0).name()).isEqualTo(member1.getName().getValue());
        assertThat(responses.get(0).email()).isEqualTo(member1.getEmail().getValue());
        assertThat(responses.get(1).id()).isEqualTo(member2.getId());
        assertThat(responses.get(1).name()).isEqualTo(member2.getName().getValue());
        assertThat(responses.get(1).email()).isEqualTo(member2.getEmail().getValue());
    }
}
