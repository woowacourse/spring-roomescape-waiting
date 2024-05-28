package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.OperationNotAllowedException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse save(ReservationRequest request) {
        Member member = getMemberById(request.memberId());
        ReservationTime reservationTime = getReservationTimeById(request.timeId());
        Theme theme = getThemeById(request.themeId());
        validateRequest(request, reservationTime);
        Reservation reservation = new Reservation(member, request.date(), reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getFilteredReservations(
            Long themeId,
            Long memberId,
            LocalDate from,
            LocalDate to
    ) {
        List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId,
                from, to);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteById(Long id) {
        reservationRepository.delete(getReservationById(id));
    }

    public boolean checkReservationExists(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.existsByDateAndReservationTimeIdAndThemeId(date, timeId, themeId);
    }

    private ReservationTime getReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 예약 시간을 찾을 수 없습니다."));
    }

    private Theme getThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 테마를 찾을 수 없습니다."));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 회원을 찾을 수 없습니다."));
    }

    private Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 예약을 찾을 수 없습니다."));
    }

    private void validateRequest(ReservationRequest request, ReservationTime reservationTime) {
        validateNotPast(request.date(), reservationTime.getStartAt());
        validateNotDuplicatedReservation(request.date(), request.timeId(), request.themeId());
    }

    private void validateNotDuplicatedReservation(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndReservationTimeIdAndThemeId(date, timeId, themeId)) {
            throw new OperationNotAllowedException("예약이 이미 존재합니다.");
        }
    }

    private void validateNotPast(LocalDate date, LocalTime time) {
        LocalDateTime reservationDateTime = date.atTime(time);
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new OperationNotAllowedException("지나간 시간에 대한 예약은 할 수 없습니다.");
        }
    }
}
