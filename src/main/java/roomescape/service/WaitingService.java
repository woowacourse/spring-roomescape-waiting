package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.exception.*;
import roomescape.service.param.CreateWaitingParam;
import roomescape.service.result.WaitingResult;
import roomescape.service.result.WaitingWithRankResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public WaitingService(MemberRepository memberRepository,
                          ThemeRepository themeRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          WaitingRepository waitingRepository) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<WaitingResult> findAll() {
        return WaitingResult.from(waitingRepository.findAll());
    }

    public List<WaitingWithRankResult> findWaitingsWithRankByMemberId(Long memberId) {
        return WaitingWithRankResult.from(waitingRepository.findWaitingWithRankByMemberId(memberId));
    }

    public WaitingResult create(CreateWaitingParam createWaitingParam) {
        ReservationTime reservationTime = reservationTimeRepository.findById(createWaitingParam.timeId()).orElseThrow(
                () -> new NotFoundReservationTimeException(createWaitingParam.timeId() + "에 해당하는 정보가 없습니다."));
        Theme theme = themeRepository.findById(createWaitingParam.themeId()).orElseThrow(
                () -> new NotFoundThemeException(createWaitingParam.themeId() + "에 해당하는 정보가 없습니다."));
        Member member = memberRepository.findById(createWaitingParam.memberId()).orElseThrow(
                () -> new NotFoundMemberException(createWaitingParam.memberId() + "에 해당하는 정보가 없습니다."));
        LocalDate date = createWaitingParam.date();
        validateDuplicateWaiting(member, date, reservationTime, theme);

        Waiting waiting = waitingRepository.save(
                Waiting.createNew(
                        member,
                        date,
                        reservationTime,
                        theme
                )
        );
        return WaitingResult.from(waiting);
    }

    public void delete(Long memberId, Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(
                () -> new NotFoundWaitingException(waitingId + "에 해당하는 정보가 없습니다."));
        if(!Objects.equals(waiting.getMember().getId(), memberId)) { //TODO: 한번에 쿼리로 할지 고민, 내부에 물어볼지 고민
            throw new DeletionNotAllowedException("잘못된 삭제 요청입니다.");
        }

        waitingRepository.deleteById(waitingId);
    }

    private void validateDuplicateWaiting(final Member member, final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        boolean isExistWaiting = waitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                member.getId(),
                date,
                reservationTime.getId(),
                theme.getId());

        if (isExistWaiting) {
            throw new UnAvailableReservationException("예약 대기가 이미 존재합니다.");
        }
    }
}
