package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateWaitingRequest;
import roomescape.reservation.dto.response.CreateWaitingResponse;
import roomescape.reservation.dto.response.FindWaitingResponse;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Slot;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingServiceValidator waitingServiceValidator;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository,
                          MemberRepository memberRepository,
                          WaitingServiceValidator waitingServiceValidator) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingServiceValidator = waitingServiceValidator;
    }

    public CreateWaitingResponse createWaiting(AuthInfo authInfo, CreateWaitingRequest createWaitingRequest) {
        Member member = getMember(authInfo.getMemberId());
        LocalDate date = createWaitingRequest.date();
        ReservationTime reservationTime = getReservationTime(createWaitingRequest.timeId());
        Theme theme = getTheme(createWaitingRequest.themeId());
        Slot slot = new Slot(date, reservationTime, theme);
        Waiting waiting = Waiting.create(member, slot);

        waitingServiceValidator.checkBothWaitingAndReservationNotExist(slot);
        waitingServiceValidator.checkMemberAlreadyHasReservation(member, slot);
        waitingServiceValidator.checkMemberAlreadyHasWaiting(member, slot);

        return CreateWaitingResponse.from(waitingRepository.save(waiting));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 회원이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<FindWaitingResponse> getWaitings() {
        Map<Slot, List<Waiting>> waitingGroupsBySlot = waitingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Waiting::getSlot));

        return waitingGroupsBySlot.values().stream()
                .map(this::convertWaitingsToResponses)
                .flatMap(List::stream)
                .toList();
    }

    private List<FindWaitingResponse> convertWaitingsToResponses(List<Waiting> waitings) {
        Long minId = getFirstId(waitings);
        return waitings.stream()
                .map(waiting -> FindWaitingResponse.from(waiting, minId.equals(waiting.getId())))
                .toList();
    }

    private long getFirstId(List<Waiting> waitings) {
        return waitings.stream()
                .min(Comparator.comparing(Waiting::getId))
                .map(Waiting::getId)
                .orElseThrow(() -> new IllegalStateException("대기 식별자가 존재하지 않습니다."));
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }
}
