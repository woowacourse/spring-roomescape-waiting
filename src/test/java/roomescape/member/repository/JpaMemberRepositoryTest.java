package roomescape.member.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/test-data.sql")
class JpaMemberRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    MemberListCrudRepository memberRepository;

    @Nested
    @DisplayName("멤버 조회")
    class FindMember {

        @DisplayName("멤버 목록을 조회할 수 있다")
        @Test
        void test1() {
            // when
            List<Member> members = memberRepository.findAll();

            // then
            assertThat(members.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("멤버 생성")
    class CreateMember {

        @DisplayName("새 멤버를 저장할 수 있다")
        @Test
        void test1() {
            // given
            Member member = new Member(null, "테스트", "test@test.com", "wooteco7", Role.USER);

            // when
            Member newMember = memberRepository.save(member);

            // then
            assertThat(newMember.getId()).isEqualTo(4L);
        }

        @DisplayName("중복되는 테마는 저장되지 않는다")
        @Test
        void test2() {
            // given
            Member member = new Member(null, "테스트", "test@test.com", "wooteco7", Role.USER);
            entityManager.persist(member);
            entityManager.flush();
            entityManager.clear();

            // when
            memberRepository.save(member);
            List<Member> members = memberRepository.findAll();

            // then
            assertThat(members.size()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("멤버 삭제")
    class DeleteMember {

        @DisplayName("저장된 멤버를 삭제할 수 있다")
        @Test
        void test1() {
            // when
            memberRepository.deleteById(1L);
            Member expected = entityManager.find(Member.class, 1L);

            // then
            assertThat(expected).isNull();
        }
    }
}
