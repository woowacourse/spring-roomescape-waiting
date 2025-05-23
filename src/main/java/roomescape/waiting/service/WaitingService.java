package roomescape.waiting.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ForbiddenException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.dto.response.WaitingCreateResponse;
import roomescape.waiting.dto.response.WaitingReadResponse;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public WaitingCreateResponse createWaiting(LoginMember loginMember, WaitingCreateRequest request) {
        ReservationSlot reservationSlot = reservationSlotRepository.findByDateAndTimeIdAndThemeId(
                        request.date(), request.timeId(), request.themeId())
                .orElseThrow(() -> new NotFoundException("예약 슬롯을 찾을 수 없습니다."));

        Member member = getMemberById(loginMember.id());
        validateAvailableWaiting(reservationSlot, loginMember);

        Waiting waiting = new Waiting(reservationSlot, member);
        Waiting saved = waitingRepository.save(waiting);

        Long rank = waitingRepository.countByReservationSlotAndMemberId(reservationSlot, loginMember.id());
        return WaitingCreateResponse.from(saved, rank);
    }

    public List<WaitingReadResponse> getWaitings() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingReadResponse::from)
                .toList();
    }

    public void deleteWaiting(Long waitingId, LoginMember loginMember) {
        validateLoginMemberWithWaiting(waitingId, loginMember);
        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void acceptWaiting(Long id) {
        Waiting waiting = getWaitingById(id);

        deleteReservationByWaiting(waiting);
        reservationRepository.flush();
        waitingRepository.delete(waiting);
        Reservation reservation = new Reservation(waiting.getReservationSlot(), waiting.getMember());
        reservationRepository.save(reservation);
    }

    private void deleteReservationByWaiting(Waiting waiting) {
        reservationRepository.findByReservationSlot(waiting.getReservationSlot())
                .ifPresent(reservationRepository::delete);
    }

    private Waiting getWaitingById(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약 대기를 찾을 수 없습니다."));
    }

    private void validateLoginMemberWithWaiting(Long waitingId, LoginMember loginMember) {
        Waiting waiting = getWaitingById(waitingId);
        if (waiting.matchesMemberById(loginMember.id()) || loginMember.isAdmin()) {
            return;
        }
        throw new ForbiddenException("예약 대기를 삭제할 수 있는 권한이 없습니다.");
    }

    private void validateAvailableWaiting(ReservationSlot reservationSlot, LoginMember loginMember) {
        validateReserved(reservationSlot);
        validateDuplicateReservation(reservationSlot, loginMember);
        validateDuplicateWaiting(reservationSlot, loginMember);
    }

    private void validateReserved(ReservationSlot reservationSlot) {
        if (!reservationRepository.existsByReservationSlot(reservationSlot)) {
            throw new BadRequestException("예약이 존재하지 않아 대기를 생성할 수 없습니다.");
        }
    }

    private void validateDuplicateReservation(ReservationSlot reservationSlot, LoginMember loginMember) {
        if (reservationRepository.existsByReservationSlotAndMemberId(reservationSlot, loginMember.id())) {
            throw new BadRequestException("중복된 예약이 존재합니다.");
        }
    }

    private void validateDuplicateWaiting(ReservationSlot reservationSlot, LoginMember loginMember) {
        if (waitingRepository.existsByReservationSlotAndMemberId(reservationSlot, loginMember.id())) {
            throw new BadRequestException("중복된 예약 대기가 존재합니다.");
        }
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
    }
}
