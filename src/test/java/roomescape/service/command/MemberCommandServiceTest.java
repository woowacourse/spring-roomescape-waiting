package roomescape.service.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.member.MemberSignupResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.repository.JpaMemberRepository;

public class MemberCommandServiceTest {

    @Mock
    private JpaMemberRepository memberRepository;

    @InjectMocks
    private MemberCommandService memberCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("회원 가입 성공 테스트")
    @Test
    void registerMember() {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto("홍길동", "hong@example.com", "password123");
        Member member = new Member(requestDto.name(), requestDto.email(), Role.USER, requestDto.password());
        Member savedMember = new Member(1L, member.getName(), member.getEmail(), member.getRole(), member.getPassword());

        when(memberRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // when
        MemberSignupResponseDto response = memberCommandService.registerMember(requestDto);

        // then
        assertEquals(savedMember.getId(), response.id());
        assertEquals(savedMember.getName(), response.name());
        assertEquals(savedMember.getEmail(), response.email());
    }

    @DisplayName("이미 존재하는 이메일로 회원 가입 시 예외 발생")
    @Test
    void registerMemberWithDuplicateEmail() {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto("홍길동", "hong@example.com", "password123");

        when(memberRepository.existsByEmail(requestDto.email())).thenReturn(true);

        // when & then
        assertThrows(DuplicateContentException.class, () -> memberCommandService.registerMember(requestDto));
    }
}
