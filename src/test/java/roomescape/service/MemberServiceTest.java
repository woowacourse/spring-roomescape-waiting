package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.JpaConfig;
import roomescape.domain.Member;
import roomescape.dto.request.MemberRegisterRequest;
import roomescape.dto.response.MemberRegisterResponse;
import roomescape.dto.response.MemberResponse;
import roomescape.domain.MemberRepository;
import roomescape.repository.impl.MemberRepositoryImpl;
import roomescape.repository.jpa.MemberJpaRepository;

@TestPropertySource(properties = {
        "spring.sql.init.mode=never",          // SQL 스크립트 실행 중지
        "spring.jpa.hibernate.ddl-auto=create-drop",  // Hibernate DDL 자동 생성 비활성화
})
@Import(JpaConfig.class)
@DataJpaTest
class MemberServiceTest {

    private MemberService memberService;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @BeforeEach
    void setUp() {
        MemberRepository memberRepository = new MemberRepositoryImpl(memberJpaRepository);
        memberService = new MemberService(memberRepository);
    }

    @DisplayName("사용자를 정상적으로 추가한다")
    @Test
    void addMember() {
        //given
        final MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest("test", "test", "차니");

        //when
        final MemberRegisterResponse expected = memberService.addMember(memberRegisterRequest);

        //then
        assertAll(
                () -> assertThat(expected.name()).isEqualTo("차니"),
                () -> assertThat(expected.email()).isEqualTo("test")
        );
    }

    @DisplayName("이미 존재하는 이메일로 사용자를 추가할 시 예외가 발생한다.")
    @Test
    void addMemberWithDuplicateEmailTest() {
        // given
        final MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest("test", "test", "차니");
        memberService.addMember(memberRegisterRequest);

        // when, then
        final MemberRegisterRequest duplicateEmailMemberRegisterRequest = new MemberRegisterRequest("test", "test", "히포");
        assertThatThrownBy(() -> memberService.addMember(duplicateEmailMemberRegisterRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("모든 사용자를 조회한다")
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

    @DisplayName("사용자의 id로 사용자를 조회한다")
    @Test
    void getMemberById() {
        //given
        final MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest("test", "test", "차니");
        final MemberRegisterResponse saved = memberService.addMember(memberRegisterRequest);

        //when
        final Member expected = memberService.getMemberById(saved.id());

        //then
        assertAll(
                () -> assertThat(expected.getName()).isEqualTo("차니"),
                () -> assertThat(expected.getEmail()).isEqualTo("test")
        );
    }
}
