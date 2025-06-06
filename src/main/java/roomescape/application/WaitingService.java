package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.application.dto.WaitingServiceResponse;
import roomescape.domain.ReservationStatus;
import roomescape.domain.WaitingOrder;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Waiting;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final GameScheduleService gameScheduleService;
    private final MemberService memberService;
    private final MessageSource messageSource;

    public WaitingService(
            WaitingRepository waitingRepository,
            GameScheduleService gameScheduleService,
            MemberService memberService,
            MessageSource messageSource
    ) {
        this.waitingRepository = waitingRepository;
        this.gameScheduleService = gameScheduleService;
        this.memberService = memberService;
        this.messageSource = messageSource;
    }

    @Transactional
    public WaitingServiceResponse registerWaiting(ReservationCreateServiceRequest request) {
        GameSchedule gameSchedule = getGameScheduleBy(request.date(), request.timeId(), request.themeId());
        Member member = memberService.getMemberEntityById(request.memberId());
        validateNotDuplicate(gameSchedule, member);

        Waiting waitingWithoutId = Waiting.withoutId(member, gameSchedule, ReservationStatus.WAITING);
        Waiting waiting = waitingRepository.save(waitingWithoutId);
        return WaitingServiceResponse.from(waiting);
    }

    private GameSchedule getGameScheduleBy(LocalDate date, Long timeId, Long themeId) {
        try {
            return gameScheduleService.getGameScheduleEntityBy(date, timeId, themeId);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("예약대기를 신청할 수 없습니다. 예약하기를 이용해주세요.");
        }
    }

    private void validateNotDuplicate(GameSchedule gameSchedule, Member member) {
        boolean duplicated = waitingRepository.existsByGameScheduleIdAndMemberId(gameSchedule.getId(), member.getId());
        if (duplicated) {
            throw new IllegalArgumentException("예약대기는 한 번만 신청할 수 있습니다.");
        }
    }

    public List<WaitingServiceResponse> getAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();
        return WaitingServiceResponse.from(waitings);
    }

    public List<ReservationStatusServiceResponse> getWaitingsByMember(Long memberId) {
        List<Waiting> memberWaitings = waitingRepository.findByMemberId(memberId);
        return memberWaitings.stream()
                .map(this::createReservationStatusDto)
                .toList();
    }

    public Optional<Waiting> findFirstWaitingEntityByGameSchedule(GameSchedule gameSchedule) {
        List<Waiting> waitings = waitingRepository.findByGameSchedule(gameSchedule);
        try {
            return Optional.of(waitings.getFirst());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    private ReservationStatusServiceResponse createReservationStatusDto(Waiting waiting) {
        WaitingOrder order = createWaitingOrder(waiting);
        return ReservationStatusServiceResponse.of(waiting, order, messageSource);
    }

    private WaitingOrder createWaitingOrder(Waiting waiting) {
        return new WaitingOrder(calculateOrder(waiting));
    }

    private Long calculateOrder(Waiting waiting) {
        GameSchedule gameSchedule = waiting.getGameSchedule();
        List<Waiting> waitings = waitingRepository.findByGameSchedule(gameSchedule);
        return waitings.stream()
                .filter(otherWaiting -> otherWaiting.getId() <= waiting.getId())
                .count();
    }

    @Transactional
    public void deleteWaiting(Long id) {
        try {
            waitingRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 예약대기 id가 존재하지 않습니다. id: " + id);
        }
    }
}
