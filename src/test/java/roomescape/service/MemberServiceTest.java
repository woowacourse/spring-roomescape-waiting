package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.FindMemberResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("성공: 전체 멤버 조회")
    @Test
    void findAll() {
        memberRepository.save(new Member("러너덕", "learnerdeock@test.com", "123a!", Role.ADMIN));
        memberRepository.save(new Member("트레", "tre@test.com", "123a!", Role.USER));
        memberRepository.save(new Member("안돌", "andole@test.com", "123a!", Role.USER));

        assertThat(memberService.findAll())
            .extracting(FindMemberResponse::id)
            .containsExactly(1L, 2L, 3L);
    }

    @DisplayName("성공: ID로 멤버 조회")
    @Test
    void findById() {
        memberRepository.save(new Member("러너덕", "learnerdeock@test.com", "123a!", Role.ADMIN));
        memberRepository.save(new Member("트레", "tre@test.com", "123a!", Role.USER));
        memberRepository.save(new Member("안돌", "andole@test.com", "123a!", Role.USER));

        Member member = memberService.findById(2L);
        assertAll(
            () -> assertThat(member.getId()).isEqualTo(2L),
            () -> assertThat(member.getName()).isEqualTo("트레"),
            () -> assertThat(member.getEmail()).isEqualTo("tre@test.com"),
            () -> assertThat(member.getPassword()).isEqualTo("123a!"),
            () -> assertThat(member.getRole()).isEqualTo("USER")
        );
    }

    @DisplayName("실패: ID로 멤버 조회 - ID 존재하지 않음")
    @Test
    void name() {
        assertThatThrownBy(() -> memberService.findById(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다.");
    }
}
