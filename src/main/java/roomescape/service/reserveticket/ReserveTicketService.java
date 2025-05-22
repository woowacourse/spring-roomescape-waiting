package roomescape.service.reserveticket;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.repository.reserveticket.ReserveTicketRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;

@Service
public class ReserveTicketService {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ReserveTicketRepository reserveTicketRepository;

    public ReserveTicketService(MemberService memberService, ReservationService reservationService,
                                ReserveTicketRepository reserveTicketRepository) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.reserveTicketRepository = reserveTicketRepository;
    }

    @Transactional
    public long addReservation(AddReservationDto newReservationDto, long memberId) {
        Member member = memberService.getMemberById(memberId);
        long reservationId = reservationService.addReservation(newReservationDto, member.getName());
        Reservation reservation = reservationService.getReservationById(reservationId);
        return reserveTicketRepository.add(reservation, member);
    }

    public List<ReserveTicket> allReservations() {
        return reserveTicketRepository.findAll();
    }

    public void deleteReservation(long id) {
        reserveTicketRepository.deleteById(id);
    }

    public List<ReserveTicket> searchReservations(Long themeId, Long memberId, LocalDate dateFrom,
                                                  LocalDate dateTo) {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAllByMemberId(memberId);

        reserveTickets.removeIf(reservationMember ->
                reservationService.searchReservation(
                        reservationMember.getReservationId(), themeId, dateFrom, dateTo
                ).isEmpty()
        );

        return reserveTickets;
    }

    public List<ReserveTicket> memberReservations(long memberId) {
        return reserveTicketRepository.findAllByMemberId(memberId);
    }
}
