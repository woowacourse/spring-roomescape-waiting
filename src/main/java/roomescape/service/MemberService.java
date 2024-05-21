package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberPassword;
import roomescape.exception.member.EmailDuplicatedException;
import roomescape.exception.member.UnauthorizedEmailException;
import roomescape.exception.member.UnauthorizedPasswordException;
import roomescape.global.JwtManager;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.member.MemberLoginRequest;
import roomescape.service.dto.member.MemberResponse;
import roomescape.service.helper.Encryptor;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtManager jwtManager;
    private final Encryptor encryptor;

    public MemberService(MemberRepository memberRepository, JwtManager jwtManager, Encryptor encryptor) {
        this.memberRepository = memberRepository;
        this.jwtManager = jwtManager;
        this.encryptor = encryptor;
    }

    public void signup(MemberCreateRequest request) {
        validateAlreadyExistsEmail(request);
        MemberPassword encryptPassword = encryptor.encryptPassword(request.getPassword());
        Member member = new Member(request.getEmail(), encryptPassword, request.getName());
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public String login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(UnauthorizedEmailException::new);
        MemberPassword requestPassword = encryptor.encryptPassword(request.getPassword());
        validateWrongPassword(member, requestPassword);
        return jwtManager.generateToken(member);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::new)
                .toList();
    }

    private void validateAlreadyExistsEmail(MemberCreateRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicatedException();
        }
    }

    private void validateWrongPassword(Member member, MemberPassword requestPassword) {
        if (member.isMismatchedPassword(requestPassword)) {
            throw new UnauthorizedPasswordException();
        }
    }
}
