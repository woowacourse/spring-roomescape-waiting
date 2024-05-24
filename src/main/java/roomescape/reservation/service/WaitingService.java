package roomescape.reservation.service;

import java.time.LocalDate;
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
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(WaitingRepository waitingRepository, final ReservationRepository reservationRepository,
                          final ReservationTimeRepository reservationTimeRepository,
                          final ThemeRepository themeRepository,
                          final MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public CreateWaitingResponse createWaiting(AuthInfo authInfo, CreateWaitingRequest createWaitingRequest) {

        Member member = findMember(authInfo.getMemberId());
        LocalDate date = createWaitingRequest.date();
        ReservationTime reservationTime = findReservationTime(createWaitingRequest.timeId());
        Theme theme = findTheme(createWaitingRequest.themeId());
        Slot slot = new Slot(date, reservationTime, theme);
        Waiting waiting = Waiting.create(member, slot);

        checkBothWaitingAndReservationNotExist(slot);
        checkMemberAlreadyHasReservation(member, slot);
        checkMemberAlreadyHasWaiting(member, slot);

        return CreateWaitingResponse.from(waitingRepository.save(waiting));
    }

    private void checkBothWaitingAndReservationNotExist(Slot slot) {
        if (bothWaitingAndReservationNotExist(slot)) {
            throw new IllegalArgumentException(
                    slot.date() + " " + slot.reservationTime().getStartAt() + "의 " + slot.theme().getName()
                            + " 테마는 바로 예약 가능하여 대기가 불가능합니다.");
        }
    }

    private boolean bothWaitingAndReservationNotExist(Slot slot) {
        return !waitingRepository.existsBySlot(slot) && !reservationRepository.existsBySlot(slot);
    }

    private void checkMemberAlreadyHasReservation(Member member, Slot slot) {
        if (reservationRepository.existsBySlotAndMemberId(slot, member.getId())) {
            throw new IllegalArgumentException("이미 본인의 예약이 존재하여 대기를 생성할 수 없습니다.");
        }
    }

    private void checkMemberAlreadyHasWaiting(Member member, Slot slot) {
        if (waitingRepository.existsBySlotAndMemberId(slot, member.getId())) {
            throw new IllegalArgumentException("이미 본인의 대기가 존재하여 대기를 생성할 수 없습니다.");
        }
    }

    private ReservationTime findReservationTime(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Theme findTheme(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Member findMember(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 회원이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<FindWaitingResponse> getWaitings() {
        Map<Slot, List<Waiting>> waitingGroupsBySlot = waitingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Waiting::getSlot));

        return waitingGroupsBySlot.values().stream()
                .map(waitingList -> {
                    Long minId = waitingList.stream()
                            .mapToLong(Waiting::getId)
                            .min()
                            .orElseThrow(() -> new IllegalStateException("대기 식별자가 존재하지 않습니다."));
                    return waitingList.stream()
                            .map(waiting -> FindWaitingResponse.from(waiting, minId.equals(waiting.getId())))
                            .toList();
                }).flatMap(List::stream)
                .toList();
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }
}
