package roomescape.integration.repository;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RepositoryBaseTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.repository.MemberRepository;

class MemberRepositoryTest extends RepositoryBaseTest {

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 멤버를_저장한다() {
        // given
        MemberEmail email = new MemberEmail("leehyeonsu4888@gmail.com");
        MemberName name = new MemberName("한스");
        MemberEncodedPassword password = new MemberEncodedPassword("password");
        MemberRole role = MemberRole.MEMBER;

        // when
        Member saved = memberRepository.save(new Member(null, name, email, password, role));

        // then
        assertThat(getMemberCount()).isEqualTo(1);
    }

    @Test
    void 이메일로_멤버를_조회한다() {
        // given
        Member member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        MemberEmail email = new MemberEmail("leehyeonsu4888@gmail.com");

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(email);

        // then
        assertSoftly(softly -> {
            softly.assertThat(foundMember).isPresent();
            Member actual = foundMember.get();
            softly.assertThat(actual.getName()).isEqualTo(member.getName());
            softly.assertThat(actual.getEmail()).isEqualTo(member.getEmail());
            softly.assertThat(actual.getPassword()).isEqualTo(member.getPassword());
            softly.assertThat(actual.getRole()).isEqualTo(member.getRole());
        });
    }

    @Test
    void id로_멤버를_조회한다() {
        // given
        Member member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();

        // when
        Optional<Member> foundMember = memberRepository.findById(member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(foundMember).isPresent();
            Member actual = foundMember.get();
            softly.assertThat(actual.getName()).isEqualTo(member.getName());
            softly.assertThat(actual.getEmail()).isEqualTo(member.getEmail());
            softly.assertThat(actual.getPassword()).isEqualTo(member.getPassword());
            softly.assertThat(actual.getRole()).isEqualTo(member.getRole());
        });
    }

    @Test
    void 모든_멤버를_조회한다() {
        // given
        Member member1 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        Member member2 = memberDbFixture.createMember(
                new MemberName("짭한스"),
                new MemberEmail("leehyeonsu488@gmail.com"),
                new MemberEncodedPassword("gdgd"),
                MemberRole.MEMBER
        );

        // when
        List<Member> members = memberRepository.findAll();

        // then
        assertSoftly(softly -> {
            softly.assertThat(members).hasSize(2);

            Member actual1 = members.get(0);
            Member actual2 = members.get(1);

            softly.assertThat(actual1.getName()).isEqualTo(member1.getName());
            softly.assertThat(actual1.getEmail()).isEqualTo(member1.getEmail());
            softly.assertThat(actual1.getPassword()).isEqualTo(member1.getPassword());
            softly.assertThat(actual1.getRole()).isEqualTo(member1.getRole());

            softly.assertThat(actual2.getName()).isEqualTo(member2.getName());
            softly.assertThat(actual2.getEmail()).isEqualTo(member2.getEmail());
            softly.assertThat(actual2.getPassword()).isEqualTo(member2.getPassword());
            softly.assertThat(actual2.getRole()).isEqualTo(member2.getRole());
        });
    }

    private long getMemberCount() {
        return memberRepository.count();
    }
}
