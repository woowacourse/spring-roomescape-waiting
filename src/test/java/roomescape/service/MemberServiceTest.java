package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Member;
import roomescape.dto.MemberRegisterRequest;
import roomescape.dto.MemberRegisterResponse;
import roomescape.dto.MemberResponse;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
})
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;


    @Test
    void addMember() {
        //given
        final MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest("test", "test", "차니");

        //when
        final MemberRegisterResponse expected = memberService.addMember(memberRegisterRequest);

        //then
        assertAll(
                () -> assertThat(expected.id()).isEqualTo(1L),
                () -> assertThat(expected.name()).isEqualTo("차니"),
                () -> assertThat(expected.email()).isEqualTo("test")
        );

    }

    @Test
    void getAllMembers() {
        //given
        final MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest("test", "test", "차니");
        memberService.addMember(memberRegisterRequest);

        //when
        final List<MemberResponse> expected = memberService.getAllMembers();

        //then
        assertThat(expected).hasSize(1);

    }

    @Test
    void getMemberById() {
        //given
        final MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest("test", "test", "차니");
        final MemberRegisterResponse saved = memberService.addMember(memberRegisterRequest);

        //when
        final Member expected = memberService.getMemberById(saved.id());

        //then
        assertAll(
                () -> assertThat(expected.getId()).isEqualTo(1L),
                () -> assertThat(expected.getName()).isEqualTo("차니"),
                () -> assertThat(expected.getEmail()).isEqualTo("test")
        );
    }
}
