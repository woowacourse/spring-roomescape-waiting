package roomescape.unit.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.infrastructure.JpaMemberRepository;
import roomescape.infrastructure.MemberRepositoryAdapter;

@DataJpaTest
class MemberRepositoryAdapterTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    private MemberRepositoryAdapter memberRepositoryAdapter;

    @BeforeEach
    void setUp() {
        memberRepositoryAdapter = new MemberRepositoryAdapter(jpaMemberRepository);
    }

    @Test
    @Sql(value = "/sql/testMember.sql")
    void 모든_멤버_조회_테스트() {
        //given
        List<Member> members = memberRepositoryAdapter.findAll();

        //when & then
        assertThat(members.size()).isEqualTo(4);
    }

    @Test
    @Sql(value = "/sql/testMember.sql")
    void id로_멤버_조회_테스트() {
        //given
        Long id = 4L;
        String expectedName = "스테판커리";
        Member member = memberRepositoryAdapter.findById(id).get();

        //when & then
        assertThat(member.getName()).isEqualTo(expectedName);
    }

    @Test
    @Sql(value = "/sql/testMember.sql")
    void email로_멤버_조회_테스트() {
        //given
        String email = "harden@google.com";
        String expectedName = "제임스하든";
        Member member = memberRepositoryAdapter.findByEmail(email).get();

        //when & then
        assertThat(member.getName()).isEqualTo(expectedName);
    }

    @Test
    @Sql(value = "/sql/testMember.sql")
    void 멤버_저장_테스트() {
        //given
        Member member = Member.createWithoutId("르브론제임스", "james@yahoo.com", "1234", Role.MEMBER);
        memberRepositoryAdapter.save(member);

        List<Member> allMember = memberRepositoryAdapter.findAll();
        //when & then
        assertThat(allMember.size()).isEqualTo(5);
    }
}
