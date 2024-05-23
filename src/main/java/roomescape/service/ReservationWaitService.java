package roomescape.service;

import static roomescape.domain.ReservationStatus.RESERVED;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWait;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationWaitRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.service.dto.request.wait.WaitRequest;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationWaitService {
    private final ReservationWaitRepository waitRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveReservationWait(WaitRequest request, long memberId) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId()).get(0);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(AuthenticationFailureException::new);

        long waitCount = waitRepository.findPriorityIndex()
                .orElse(RESERVED.getStartIndex());
        waitRepository.save(new ReservationWait(member, reservation, waitCount + 1L));
    }

    @Transactional
    public void deleteReservationWait(Long waitId) {
        waitRepository.deleteById(waitId);
    }
}
