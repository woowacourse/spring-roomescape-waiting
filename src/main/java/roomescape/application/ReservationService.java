package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationServiceResponse;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeService timeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public ReservationService(
            ReservationRepository reservationRepository,
            TimeService timeService,
            ThemeService themeService,
            MemberService memberService
    ) {
        this.reservationRepository = reservationRepository;
        this.timeService = timeService;
        this.themeService = themeService;
        this.memberService = memberService;
    }

    @Transactional
    public ReservationServiceResponse registerReservation(ReservationCreateServiceRequest request) {
        validateNotDuplicate(request);
        Theme theme = themeService.getThemeById(request.themeId()).toEntity();
        ReservationTime reservationTime = timeService.getTimeById(request.timeId()).toEntity();
        Member member = memberService.getMemberById(request.memberId()).toEntity();
        Reservation reservationWithoutId = Reservation.withoutId(
                member,
                theme,
                request.date(),
                reservationTime,
                ReservationStatus.RESERVED
        );
        validateNotPast(reservationWithoutId);
        Reservation reservation = reservationRepository.save(reservationWithoutId);
        return ReservationServiceResponse.from(reservation);
    }

    private void validateNotDuplicate(ReservationCreateServiceRequest request) {
        boolean duplicated = reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(),
                request.timeId(),
                request.themeId()
        );
        if (duplicated) {
            throw new IllegalArgumentException("이미 예약된 일시입니다");
        }
    }

    private void validateNotPast(Reservation reservation) {
        if (reservation.isPast()) {
            throw new IllegalArgumentException("과거 일시로 예약할 수 없습니다.");
        }
    }

    public List<ReservationServiceResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ReservationServiceResponse.from(reservations);
    }

    public List<ReservationStatusServiceResponse> getReservationsByMember(Long memberId) {
        Member member = memberService.getMemberById(memberId).toEntity();
        List<Reservation> memberReservations = reservationRepository.findByMember(member);
        return memberReservations.stream()
                .map(reservation -> new ReservationStatusServiceResponse(
                                reservation.getId(),
                                reservation.getTheme().getName(),
                                reservation.getDate(),
                                reservation.getTime().getStartAt(),
                                ReservationStatus.name(reservation.getStatus())
                        )
                )
                .toList();
    }

    public List<ReservationServiceResponse> searchReservationsWith(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository
                .findByMemberAndThemeAndDateRange(
                        memberId,
                        themeId,
                        dateFrom,
                        dateTo
                );
        return ReservationServiceResponse.from(reservations);
    }

    @Transactional
    public void deleteReservation(Long id) {
        try {
            reservationRepository.deleteById(id);
            reservationRepository.flush();
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 예약 id가 존재하지 않습니다. id: " + id);
        }
    }
}
