package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import static roomescape.service.fixture.TestMemberFactory.createMember;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.model.Member;

@DataJpaTest
@Sql("/init-data.sql")
class MemberRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;


    @DisplayName("아이디와 비밀번호로 사용자를 조회한다.")
    @Test
    void should_find_member_when_given_email_and_password() {
        Member member = saveMember(createMember(1L));

        Optional<Member> findMember = memberRepository.findByEmailAndPassword(member.getEmail(), member.getPassword());

        assertThat(findMember).contains(member);
    }

    @DisplayName("아이디로 사용자를 조회한다.")
    @Test
    void should_find_member_when_given_member_id() {
        Member member = saveMember(createMember(1L));

        Optional<Member> memberById = memberRepository.findById(1L);

        assertThat(memberById).contains(member);
    }

    @DisplayName("모든 사용자를 조회한다.")
    @Test
    void should_find_all_member() {
        Member member1 = saveMember(createMember(1L));
        Member member2 = saveMember(createMember(2L));

        List<Member> allMembers = memberRepository.findAll();

        assertThat(allMembers).contains(member1, member2);
    }

    private Member saveMember(Member member) {
        entityManager.merge(member);
        return member;
    }
}
