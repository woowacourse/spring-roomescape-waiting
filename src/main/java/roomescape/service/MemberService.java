package roomescape.service;

import static roomescape.exception.ExceptionType.INVALID_TOKEN;
import static roomescape.exception.ExceptionType.LOGIN_FAIL;
import static roomescape.service.mapper.MemberInfoMapper.toResponse;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.Sha256Encryptor;
import roomescape.dto.LoginRequest;
import roomescape.dto.MemberInfo;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.service.mapper.MemberInfoMapper;

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
        Member loginSuccessMember = memberRepository.findByEmailAndEncryptedPassword(
                new Email(email), new Password(encryptedPassword))
                .orElseThrow(() -> new RoomescapeException(LOGIN_FAIL));

        return loginSuccessMember.getId();
    }

    public MemberInfo findByMemberId(long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException(INVALID_TOKEN));
        return toResponse(member);
    }

    public List<MemberInfo> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberInfoMapper::toResponse)
                .toList();
    }
}
