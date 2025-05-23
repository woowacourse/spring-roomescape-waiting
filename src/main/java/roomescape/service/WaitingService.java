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
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

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
        Member member = getMemberFromRepository(createWaitingParam);

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
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(
                () -> new NotFoundWaitingException(waitingId + "에 해당하는 정보가 없습니다."));
        if(!Objects.equals(waiting.getMember().getId(), memberId)) {
            throw new DeletionNotAllowedException("잘못된 삭제 요청입니다.");
        }

        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void approve(final Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(
                () -> new NotFoundWaitingException(waitingId + "에 해당하는 정보가 없습니다."));

        Reservation newReservation = Reservation.createNew(
                waiting.getMember(),
                waiting.getSchedule().getDate(),
                waiting.getSchedule().getTime(),
                waiting.getSchedule().getTheme());
        reservationRepository.save(newReservation);
        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void approveFirst(Long themeId, LocalDate date, Long timeId) {
        ReservationTime reservationTime = getReservationTimeFromRepository(timeId);
        Theme theme = getThemeFromRepository(themeId);

        List<Waiting> waitings = waitingRepository.findByScheduleOrderByCreatedAt(new Schedule(date, reservationTime, theme));
        Waiting firstWaiting = waitings.getFirst();
        approve(firstWaiting.getId());
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

    private Theme getThemeFromRepository(final Long themeId) {
        return themeRepository.findById(themeId).orElseThrow(
                () -> new NotFoundThemeException(themeId + "에 해당하는 정보가 없습니다."));
    }

    private ReservationTime getReservationTimeFromRepository(final Long createWaitingParam) {
        return reservationTimeRepository.findById(createWaitingParam).orElseThrow(
                () -> new NotFoundReservationTimeException(createWaitingParam + "에 해당하는 정보가 없습니다."));
    }

    private Member getMemberFromRepository(final CreateWaitingParam createWaitingParam) {
        Member member = memberRepository.findById(createWaitingParam.memberId()).orElseThrow(
                () -> new NotFoundMemberException(createWaitingParam.memberId() + "에 해당하는 정보가 없습니다."));
        return member;
    }
}
