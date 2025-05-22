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
import roomescape.entity.WaitingReservation;
import roomescape.exception.custom.InvalidMemberException;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.exception.custom.InvalidWaitingException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ConfirmReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingReservationRepository;

@Service
public class WaitingReservationService {

    private final MemberRepository memberRepository;
    private final WaitingReservationRepository waitingReservationRepository;
    private final ConfirmReservationRepository confirmReservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingReservationService(WaitingReservationRepository waitingReservationRepository,
                                     MemberRepository memberRepository,
                                     ConfirmReservationRepository confirmReservationRepository,
                                     ReservationTimeRepository reservationTimeRepository,
                                     ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.waitingReservationRepository = waitingReservationRepository;
        this.confirmReservationRepository = confirmReservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingReservation addWaiting(CreateWaitingRequest request, LoginMemberRequest loginMemberRequest) {
        return createWaiting(loginMemberRequest.id(), request.theme(), request.date(), request.time());
    }

    public List<WaitingWithRank> findALlWaitingWithRank(Long memberId){
        return waitingReservationRepository.findWaitingsWithRankByMemberId(memberId);
    }

    public void deleteById(Long id) {
        waitingReservationRepository.deleteById(id);
    }

    private WaitingReservation createWaiting(
            long memberId,
            long themeId,
            LocalDate date,
            long timeId
    ) {
        validateReservationExists(timeId, themeId, date);
        validateNoDuplicateWaiting(memberId, timeId, themeId, date);
        WaitingReservation waitingReservation = getWaitingOrThrow(memberId, themeId, date, timeId);
        return waitingReservationRepository.save(waitingReservation);
    }

    private void validateReservationExists(Long timeId, Long themeId, LocalDate date) {
        boolean exists = confirmReservationRepository.existsByTimeIdAndThemeIdAndDate(timeId, themeId, date);
        if (!exists) {
            throw new InvalidWaitingException("예약이 존재하지 않으니 예약대기가 아닌 예약을 해주시기 바랍니다.");
        }
    }

    private void validateNoDuplicateWaiting(Long memberId, Long timeId, Long themeId, LocalDate date) {
        boolean exists = waitingReservationRepository.existsByMemberIdAndTimeIdAndThemeIdAndDate(memberId, timeId, themeId, date);
        if (exists) {
            throw new InvalidWaitingException("이미 예약대기가 존재합니다.");
        }
    }

    private WaitingReservation getWaitingOrThrow(long memberId, long themeId, LocalDate date, long timeId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 ID입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마입니다."));

        return new WaitingReservation(member, date, reservationTime, theme);
    }

    public List<WaitingReservation> findAll() {
        return waitingReservationRepository.findAll();
    }
}
