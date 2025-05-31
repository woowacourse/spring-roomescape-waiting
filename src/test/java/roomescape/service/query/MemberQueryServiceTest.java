package roomescape.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.member.MemberResponseDto;
import roomescape.exception.UnauthorizationException;
import roomescape.repository.JpaMemberRepository;

class MemberQueryServiceTest {

    @Mock
    private JpaMemberRepository memberRepository;

    @InjectMocks
    private MemberQueryService memberQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("ID로 회원 조회 성공 테스트")
    @Test
    void findMemberById() {
        // given
        long memberId = 1L;
        Member expectedMember = new Member(memberId, "홍길동", "hong@example.com", Role.USER, "password");
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(expectedMember));
        
        // when
        Member actualMember = memberQueryService.findMemberById(memberId);
        
        // then
        assertEquals(expectedMember.getId(), actualMember.getId());
        assertEquals(expectedMember.getName(), actualMember.getName());
        assertEquals(expectedMember.getEmail(), actualMember.getEmail());
        assertEquals(expectedMember.getRole(), actualMember.getRole());
    }
    
    @DisplayName("존재하지 않는 ID로 회원 조회 시 예외 발생")
    @Test
    void findMemberByIdNotFound() {
        // given
        long nonExistentMemberId = 999L;
        
        when(memberRepository.findById(nonExistentMemberId)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(UnauthorizationException.class, () -> memberQueryService.findMemberById(nonExistentMemberId));
    }
    
    @DisplayName("모든 회원 조회 테스트")
    @Test
    void findAllMembers() {
        // given
        List<Member> members = List.of(
                new Member(1L, "홍길동", "hong@example.com", Role.USER, "password"),
                new Member(2L, "김철수", "kim@example.com", Role.USER, "password"),
                new Member(3L, "관리자", "admin@example.com", Role.ADMIN, "password")
        );
        
        when(memberRepository.findAll()).thenReturn(members);
        
        // when
        List<MemberResponseDto> memberDtos = memberQueryService.findAllMembers();
        
        // then
        assertThat(memberDtos).hasSize(3);
        assertThat(memberDtos.get(0).id()).isEqualTo(1L);
        assertThat(memberDtos.get(0).name()).isEqualTo("홍길동");
        assertThat(memberDtos.get(0).email()).isEqualTo("hong@example.com");
        assertThat(memberDtos.get(1).id()).isEqualTo(2L);
        assertThat(memberDtos.get(2).id()).isEqualTo(3L);
    }
}
