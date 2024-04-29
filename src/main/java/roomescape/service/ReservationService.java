package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.MemberReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.service.exception.OperationNotAllowedException;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
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

    public ReservationResponse addMemberReservation(MemberReservationRequest request, Member member) {
        ReservationTime reservationTime = findValidatedReservationTime(request.timeId());
        Theme theme = findValidatedTheme(request.themeId());
        validateNotPast(request.date(), reservationTime.getStartAt());
        validateNotDuplicatedReservation(request.date(), request.timeId(), request.themeId());

        Reservation reservation = new Reservation(member, request.date(), reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse addAdminReservation(AdminReservationRequest request) {
        ReservationTime reservationTime = findValidatedReservationTime(request.timeId());
        Theme theme = findValidatedTheme(request.themeId());
        Member customer = findValidatedMember(request.memberId());
        validateNotPast(request.date(), reservationTime.getStartAt());
        validateNotDuplicatedReservation(request.date(), request.timeId(), request.themeId());

        Reservation reservation = new Reservation(customer, request.date(), reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public void deleteById(Long id) {
        findValidatedReservation(id);
        reservationRepository.deleteById(id);
    }

    private Reservation findValidatedReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 예약을 찾을 수 없습니다."));
    }

    private ReservationTime findValidatedReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 예약 시간을 찾을 수 없습니다."));
    }

    private Theme findValidatedTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 테마를 찾을 수 없습니다."));
    }

    private Member findValidatedMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 회원을 찾을 수 없습니다."));
    }

    private void validateNotDuplicatedReservation(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new OperationNotAllowedException("예약이 이미 존재합니다.");
        }
    }

    private void validateNotPast(LocalDate date, LocalTime time) {
        LocalDateTime reservationDateTime = date.atTime(time);
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new OperationNotAllowedException("지나간 시간에 대한 예약은 할 수 없습니다.");
        }
    }

    public List<ReservationResponse> getFilteredReservations(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.filter(themeId, memberId, dateFrom, dateTo);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
