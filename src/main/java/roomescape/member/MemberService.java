package roomescape.member;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.InvalidInputException;
import roomescape.member.web.dto.LoginRequestDto;
import roomescape.member.web.dto.SignupRequestDto;

@Service
@Transactional
public class MemberService {
    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberDao memberDao, PasswordEncoder passwordEncoder) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Member login(LoginRequestDto request) {
        Member member = memberDao.findByEmail(request.email())
                .orElseThrow(() -> new InvalidInputException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!member.matchesPassword(request.password(), passwordEncoder)) {
            throw new InvalidInputException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return member;
    }

    public Member signup(SignupRequestDto request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = new Member(null, request.name(), request.email(), encodedPassword, MemberRole.USER);
        try {
            return memberDao.insert(member);
        } catch (DuplicateKeyException e) {
            throw new DuplicateEntityException("이미 사용 중인 이메일입니다.");
        }
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버입니다."));
    }
}
