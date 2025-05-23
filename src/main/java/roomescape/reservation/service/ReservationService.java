package roomescape.reservation.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationReadFilteredRequest;
import roomescape.reservation.dto.response.ReservationAdminCreateResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationReadFilteredResponse;
import roomescape.reservation.dto.response.ReservationReadResponse;
import roomescape.reservation.dto.response.ReservationWaitingReadMemberResponse;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.entity.WaitingWithRank;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public ReservationCreateResponse createReservation(Long memberId, ReservationCreateRequest request) {
        Reservation saved = create(memberId, request.timeId(), request.themeId(), request.date());
        return ReservationCreateResponse.from(saved);
    }

    public ReservationAdminCreateResponse createReservationByAdmin(ReservationAdminCreateRequest request) {
        Reservation saved = create(request.memberId(), request.timeId(), request.themeId(), request.date());
        return ReservationAdminCreateResponse.from(saved);
    }

    private Reservation create(Long memberId, Long timeId, Long themeId, LocalDate date) {
        Member member = getMemberById(memberId);
        ReservationTime time = getReservationTimeById(timeId);
        Theme theme = getThemeById(themeId);

        ReservationSlot reservationSlot = reservationSlotRepository.findByDateAndTimeIdAndThemeId(
                        date, timeId, themeId)
                .orElse(reservationSlotRepository.save(new ReservationSlot(date, time, theme)));

        Reservation reservation = new Reservation(reservationSlot, member);
        validateDateTime(reservation);
        validateDuplicated(reservation);

        return reservationRepository.save(reservation);
    }

    public List<ReservationReadResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationReadResponse::from)
                .toList();
    }

    public List<ReservationReadFilteredResponse> getFilteredReservations(ReservationReadFilteredRequest request) {
        List<Reservation> reservations = reservationRepository.findAllByReservationSlot_ThemeIdAndMemberIdAndReservationSlot_DateBetween(
                request.themeId(), request.memberId(), request.dateFrom(), request.dateTo());

        return reservations.stream()
                .map(ReservationReadFilteredResponse::from)
                .toList();
    }

    public List<ReservationWaitingReadMemberResponse> getReservationsByMember(LoginMember loginMember) {
        Member member = getMemberById(loginMember.id());

        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(loginMember.id());

        return ReservationWaitingReadMemberResponse.of(reservations, waitingWithRanks);
    }

    @Transactional
    public void deleteReservation(Long id) {
        if (reservationRepository.existsById(id)) {
            Reservation reservation = getReservationById(id);
            reservationRepository.deleteById(id);
            reservationRepository.flush();
            changeWaitingToReservation(reservation);
            return;
        }
        reservationRepository.deleteById(id);
    }

    private void changeWaitingToReservation(Reservation reservation) {
        waitingRepository.findFirstByReservationSlot(reservation.getReservationSlot())
                .ifPresent(waiting -> {
                    waitingRepository.delete(waiting);
                    Reservation reservationByWaiting = new Reservation(waiting.getReservationSlot(), waiting.getMember());
                    reservationRepository.save(reservationByWaiting);
                });
    }

    private void validateDateTime(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = reservation.getDateTime();
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("과거 날짜는 예약할 수 없습니다.");
        }
    }

    private void validateDuplicated(Reservation reservation) {
        boolean hasDuplicate = reservationRepository.existsByReservationSlot(reservation.getReservationSlot());
        if (hasDuplicate) {
            throw new ConflictException("이미 예약이 존재합니다.");
        }
    }

    private Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약이 존재하지 않습니다."));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
    }

    private ReservationTime getReservationTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));
    }
}
