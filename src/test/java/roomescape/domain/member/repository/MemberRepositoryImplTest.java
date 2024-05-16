package roomescape.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_MEMBER;
import static roomescape.fixture.MemberFixture.NULL_ID_MEMBER;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RepositoryTest;
import roomescape.domain.member.domain.Member;

class MemberRepositoryImplTest extends RepositoryTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;
    private MemberRepository memberRepository;


    @BeforeEach
    void setUp() {
        memberRepository = new MemberRepositoryImpl(jpaMemberRepository);
        jpaMemberRepository.save(NULL_ID_MEMBER);
    }

    @AfterEach
    void setDown() {
        jpaMemberRepository.deleteAll();
    }


    @DisplayName("email과 password로 member를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_email_and_password() {
        Member expectedMember = MEMBER_MEMBER;

        Member actualMember = memberRepository.findByEmailAndPassword("dodo@gmail.com", "123123").get();

        assertThat(actualMember).isEqualTo(expectedMember);
    }
}
