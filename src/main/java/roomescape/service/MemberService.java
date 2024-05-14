package roomescape.service;

import static roomescape.exception.ExceptionType.INVALID_TOKEN;
import static roomescape.exception.ExceptionType.LOGIN_FAIL;
import static roomescape.service.mapper.UserInfoMapper.toResponse;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Sha256Encryptor;
import roomescape.dto.LoginRequest;
import roomescape.dto.UserInfo;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.service.mapper.UserInfoMapper;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final Sha256Encryptor encryptor;

    public MemberService(MemberRepository memberRepository, Sha256Encryptor encryptor) {
        this.memberRepository = memberRepository;
        this.encryptor = encryptor;
    }

    public long login(LoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();
        String encryptedPassword = encryptor.encrypt(password);
        Member loginSuccessMember = memberRepository.findByEmailAndEncryptedPassword(email, encryptedPassword)
                .orElseThrow(() -> new RoomescapeException(LOGIN_FAIL));
        return loginSuccessMember.getId();
    }

    public UserInfo findByUserId(long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RoomescapeException(INVALID_TOKEN));
        return toResponse(member);
    }

    public List<UserInfo> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(UserInfoMapper::toResponse)
                .toList();
    }
}
