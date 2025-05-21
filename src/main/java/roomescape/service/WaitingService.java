package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.query.WaitingWithRank;
import roomescape.dto.request.CreateWaitingRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.entity.Member;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
import roomescape.exception.custom.InvalidMemberException;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.exception.custom.InvalidWaitingException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
public class WaitingService {

    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          MemberRepository memberRepository,
                          ReservationRepository reservationRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public Waiting addWaiting(CreateWaitingRequest request, LoginMemberRequest loginMemberRequest) {
        return createWaiting(loginMemberRequest.id(), request.themeId(), request.date(), request.timeId());
    }

    public List<WaitingWithRank> findALlWaitingWithRank(Long memberId){
        return waitingRepository.findWaitingsWithRankByMemberId(memberId);
    }

    private Waiting createWaiting(
            long memberId,
            long themeId,
            LocalDate date,
            long timeId
    ) {
        validateReservationExists(timeId, themeId, date);
        validateNoDuplicateWaiting(memberId, timeId, themeId, date);
        Waiting waiting = getWaitingOrThrow(memberId, themeId, date, timeId);
        return waitingRepository.save(waiting);
    }

    private void validateReservationExists(Long timeId, Long themeId, LocalDate date) {
        boolean exists = reservationRepository.existsByTimeIdAndThemeIdAndDate(timeId, themeId, date);
        if (!exists) {
            throw new InvalidWaitingException("예약이 존재하지 않으니 예약대기가 아닌 예약을 해주시기 바랍니다.");
        }
    }

    private void validateNoDuplicateWaiting(Long memberId, Long timeId, Long themeId, LocalDate date) {
        boolean exists = waitingRepository.existsByMemberIdAndTimeIdAndThemeIdAndDate(memberId, timeId, themeId, date);
        if (exists) {
            throw new InvalidWaitingException("이미 예약대기가 존재합니다.");
        }
    }

    private Waiting getWaitingOrThrow(long memberId, long themeId, LocalDate date, long timeId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 ID입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마입니다."));

        return new Waiting(member, date, reservationTime, theme);
    }
}
