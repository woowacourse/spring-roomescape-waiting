package roomescape.member.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.dao.MemberJdbcDao;
import roomescape.member.exception.MemberExceptionCode;

@ExtendWith(MockitoExtension.class)
class MemberRepositoryTest {

    @InjectMocks
    private MemberRepository memberRepository;

    @Mock
    private MemberJdbcDao memberJdbcDao;

    @Test
    @DisplayName("일치하는 멤버가 없는 경우 예외를 던진다.")
    void shouldThrowException_WhenIdNotFound() {
        when(memberJdbcDao.findIdByEmailAndPassword("email", "password"))
                .thenReturn(Optional.ofNullable(null));

        Throwable noneMatchMember = assertThrows(RoomEscapeException.class,
                () -> memberRepository.findIdByEmailAndPassword("email", "password"));

        assertEquals(noneMatchMember.getMessage(), MemberExceptionCode.ID_AND_PASSWORD_NOT_MATCH_OR_EXIST.getMessage());
    }
}
