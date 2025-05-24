package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationDto;
import roomescape.application.dto.ReservationWaitingDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final MemberService memberService;

    public ReservationQueryService(ReservationRepository reservationRepository, MemberService memberService) {
        this.reservationRepository = reservationRepository;
        this.memberService = memberService;
    }


    public List<ReservationDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ReservationDto.from(reservations);
    }

    public List<ReservationWaitingDto> getReservationsByMember(Long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        List<Reservation> memberReservations = reservationRepository.findByMember(member);
        return memberReservations.stream()
                .map(reservation -> new ReservationWaitingDto(
                                reservation.getId(),
                                reservation.getTheme().getName(),
                                reservation.getDate(),
                                reservation.getTime().getStartAt(),
                                ReservationStatus.name(reservation.getWaiting().getStatus())
                        )
                )
                .toList();
    }

    public List<ReservationDto> searchReservationsWith(
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
        return ReservationDto.from(reservations);
    }
}
