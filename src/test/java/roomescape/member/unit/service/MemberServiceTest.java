package roomescape.member.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.error.exception.ConflictException;
import roomescape.member.dto.request.MemberCreateRequest;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원을 생성한다.")
    void createMember() {
        // given
        var request = new MemberCreateRequest("미소", "miso@email.com", "password");
        when(memberRepository.save(any()))
                .thenReturn(new Member(1L, request.name(), request.email(), request.password(), RoleType.USER));

        // when
        var response = memberService.createMember(request);

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.name()).isEqualTo(request.name()),
                () -> assertThat(response.email()).isEqualTo(request.email()),
                () -> assertThat(response.password()).isEqualTo(request.password()),
                () -> assertThat(response.role()).isEqualTo(RoleType.USER)
        );
    }

    @Test
    @DisplayName("모든 회원을 조회한다.")
    void getAllMembers() {
        // given
        var inDbMembers = List.of(
                new Member(1L, "미소", "miso@email.com", "password", RoleType.USER),
                new Member(2L, "브라운", "brown@email.com", "password", RoleType.USER)
        );
        when(memberRepository.findAll())
                .thenReturn(inDbMembers);

        // when
        var responses = memberService.getAllMembers();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.getFirst().name()).isEqualTo("미소"),
                () -> assertThat(responses.get(1).name()).isEqualTo("브라운")
        );
        verify(memberRepository).findAll();
    }

    @Test
    @DisplayName("회원을 삭제한다.")
    void deleteMember() {
        // when
        memberService.deleteMember(1L);

        // then
        verify(memberRepository).deleteById(1L);
    }

    @Test
    @DisplayName("중복된 이메일로 회원을 생성할 수 없다.")
    void validateDuplicateEmail() {
        // given
        String duplicateEmail = "miso@email.com";
        var request = new MemberCreateRequest("미소", duplicateEmail, "password");
        when(memberRepository.existsByEmail(duplicateEmail))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("중복된 이메일입니다.");

        verify(memberRepository).existsByEmail(duplicateEmail);
        verify(memberRepository, never()).save(any());
    }
}
