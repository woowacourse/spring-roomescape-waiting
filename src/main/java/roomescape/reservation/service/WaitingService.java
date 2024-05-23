package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateWaitingRequest;
import roomescape.reservation.dto.response.ConfirmReservationResponse;
import roomescape.reservation.dto.response.CreateWaitingResponse;
import roomescape.reservation.dto.response.FindWaitingResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Slot;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
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

    public CreateWaitingResponse createReservation(AuthInfo authInfo, CreateWaitingRequest createWaitingRequest) {

        ReservationTime reservationTime = findReservationTime(createWaitingRequest.timeId());
        Theme theme = findTheme(createWaitingRequest.themeId());
        Member member = findMember(authInfo.getMemberId());

        checkBothWaitingAndReservationNotExist(createWaitingRequest, createWaitingRequest.date(), theme.getName(),
                reservationTime.getStartAt());
        checkMemberAlreadyHasReservation(createWaitingRequest, authInfo);
        checkMemberAlreadyHasWaiting(createWaitingRequest, authInfo);

        Waiting waiting = createWaitingRequest.toWaiting(member, reservationTime, theme);
        return CreateWaitingResponse.from(waitingRepository.save(waiting));
    }

    private void checkBothWaitingAndReservationNotExist(final CreateWaitingRequest createWaitingRequest,
                                                        final LocalDate date, final String themeName,
                                                        final LocalTime time) {
        if (bothWaitingAndReservationNotExist(createWaitingRequest)) {
            throw new IllegalArgumentException(date + " " + time + "의 " + themeName + " 테마는 바로 예약 가능하여 대기가 불가능합니다.");
        }
    }

    private boolean bothWaitingAndReservationNotExist(CreateWaitingRequest createWaitingRequest) {
        boolean notExistsWaiting = !waitingRepository.existsByDateAndReservationTimeIdAndThemeId(
                createWaitingRequest.date(),
                createWaitingRequest.timeId(),
                createWaitingRequest.themeId());
        boolean notExistsReservation = !reservationRepository.existsByDateAndReservationTimeIdAndThemeId(
                createWaitingRequest.date(),
                createWaitingRequest.timeId(),
                createWaitingRequest.themeId());
        return notExistsWaiting && notExistsReservation;
    }

    private void checkMemberAlreadyHasReservation(CreateWaitingRequest createWaitingRequest, AuthInfo authInfo) {
        if (reservationRepository.existsByDateAndReservationTimeIdAndThemeIdAndMemberId(
                createWaitingRequest.date(),
                createWaitingRequest.timeId(),
                createWaitingRequest.themeId(),
                authInfo.getMemberId())) {
            throw new IllegalArgumentException("이미 본인의 예약이 존재하여 대기를 생성할 수 없습니다.");
        }
    }

    private void checkMemberAlreadyHasWaiting(CreateWaitingRequest createWaitingRequest, AuthInfo authInfo) {
        if (waitingRepository.existsByDateAndReservationTimeIdAndThemeIdAndMemberId(
                createWaitingRequest.date(),
                createWaitingRequest.timeId(),
                createWaitingRequest.themeId(),
                authInfo.getMemberId())) {
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

    public ConfirmReservationResponse confirmWaiting(Long id) {
        Waiting waiting = findWaiting(id);
        checkReservationExists(waiting);
        checkFirstWaiting(waiting);

        Reservation reservation = new Reservation(waiting.getMember(), waiting.getDate(), waiting.getReservationTime(),
                waiting.getTheme());
        waitingRepository.deleteById(id);
        return ConfirmReservationResponse.from(reservationRepository.save(reservation));
    }

    private void checkReservationExists(Waiting waiting) {
        if (reservationRepository.existsByDateAndReservationTimeIdAndThemeId(
                waiting.getDate(), waiting.getReservationTime().getId(), waiting.getTheme().getId())) {
            throw new IllegalArgumentException("이미 예약이 존재하여 대기를 예약으로 변경할 수 없습니다.");
        }
    }

    private void checkFirstWaiting(Waiting waiting) {
        if (waitingRepository.existsByDateAndReservationTimeIdAndThemeIdAndIdLessThan(
                waiting.getDate(), waiting.getReservationTime().getId(), waiting.getTheme().getId(), waiting.getId())) {
            throw new IllegalArgumentException(waiting.getId() + "보다 앞선 대기가 존재하여 예약으로 변경할 수 없습니다.");
        }
    }

    private Waiting findWaiting(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 대기가 존재하지 않아 예약으로 변경할 수 없습니다."));
    }

    public List<FindWaitingResponse> getWaitings() {
        Map<Slot, List<Waiting>> waitingsGroupBySlot = waitingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Waiting::getSlot));

        return waitingsGroupBySlot.values().stream()
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
