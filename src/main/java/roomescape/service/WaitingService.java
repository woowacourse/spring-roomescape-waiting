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

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(MemberRepository memberRepository,
                          ThemeRepository themeRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<WaitingResult> findAll() {
        return WaitingResult.from(waitingRepository.findAll());
    }

    public List<WaitingWithRankResult> findWaitingsWithRankByMemberId(Long memberId) {
        return WaitingWithRankResult.from(waitingRepository.findWaitingWithRankByMemberId(memberId));
    }

    @Transactional
    public WaitingResult create(CreateWaitingParam createWaitingParam) {
        ReservationTime reservationTime = getReservationTimeFromRepository(createWaitingParam.timeId());
        Theme theme = getThemeFromRepository(createWaitingParam.themeId());
        Member member = getMemberFromRepository(createWaitingParam.memberId());

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

    @Transactional
    public void deleteByMemberIdAndWaitingId(Long memberId, Long waitingId) {
        Waiting waiting = getWaitingFromRepository(waitingId);
        validateWaitingBelongsToMember(memberId, waiting);

        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void approve(final Long waitingId) {
        Waiting waiting = getWaitingFromRepository(waitingId);

        Reservation newReservation = Reservation.createNew(
                waiting.getMember(),
                waiting.getSchedule().getDate(),
                waiting.getSchedule().getTime(),
                waiting.getSchedule().getTheme());
        reservationRepository.save(newReservation);
        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void deleteById(final Long waitingId) {
        if(!waitingRepository.existsById(waitingId)) {
            throw new NotFoundWaitingException(waitingId + "에 해당하는 정보가 없습니다.");
        }
        waitingRepository.deleteById(waitingId);
    }

    private void validateDuplicateWaiting(final Member member, final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        boolean isExistWaiting = waitingRepository.existsByMemberIdAndSchedule(
                member.getId(),
                new Schedule(date, reservationTime, theme));

        if (isExistWaiting) {
            throw new UnAvailableReservationException("예약 대기가 이미 존재합니다.");
        }
    }

    private void validateWaitingBelongsToMember(final Long memberId, final Waiting waiting) {
        if(!waiting.getMember().isSameId(memberId)) {
            throw new DeletionNotAllowedException("잘못된 삭제 요청입니다.");
        }
    }

    private Theme getThemeFromRepository(Long themeId) {
        return themeRepository.findById(themeId).orElseThrow(
                () -> new NotFoundThemeException(themeId + "에 해당하는 정보가 없습니다."));
    }

    private ReservationTime getReservationTimeFromRepository(Long timeId) {
        return reservationTimeRepository.findById(timeId).orElseThrow(
                () -> new NotFoundReservationTimeException(timeId + "에 해당하는 정보가 없습니다."));
    }

    private Member getMemberFromRepository(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new NotFoundMemberException(memberId + "에 해당하는 정보가 없습니다."));
    }

    private Waiting getWaitingFromRepository(final Long waitingId) {
        return waitingRepository.findById(waitingId).orElseThrow(
                () -> new NotFoundWaitingException(waitingId + "에 해당하는 정보가 없습니다."));
    }
}
