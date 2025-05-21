package roomescape.service.reserveticket;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Reserver;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.repository.reserveticket.ReserveTicketRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.strategy.ReservationValidateStrategy;

@Service
public class ReserveTicketService {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ReserveTicketRepository reserveTicketRepository;
    private final ReservationValidateStrategy reservationRepository;

    public ReserveTicketService(MemberService memberService, ReservationService reservationService,
                                ReserveTicketRepository reserveTicketRepository,
                                ReservationValidateStrategy reservationRepository) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.reserveTicketRepository = reserveTicketRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public long addReservation(AddReservationDto newReservationDto, Long memberId) {
        Reserver reserver = memberService.getMemberById(memberId);
        long reservationId = reservationService.addReservation(newReservationDto, reservationRepository);
        Reservation reservation = reservationService.getReservationById(reservationId);
        Long themeId = reservation.getTheme().getId();
        int countSameThemeDateTimeReservation = reservationService.countSameThemeDateTimeReservation(themeId,
                reservation.getDate(), reservation.getReservationTime().getId());

        return reserveTicketRepository.save(
                new ReserveTicket(reservation, reserver, countSameThemeDateTimeReservation)).getId();
    }

    public List<ReserveTicket> allReservations() {
        return reserveTicketRepository.findAll();
    }

    @Transactional
    public void deleteReservation(long id) {
        reserveTicketRepository.deleteById(id);
    }

    public List<ReserveTicket> searchReservations(Long themeId, Long memberId, LocalDate dateFrom,
                                                  LocalDate dateTo) {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAllByReserverId(memberId);

        reserveTickets.removeIf(reservationMember ->
                reservationService.searchReservation(
                        reservationMember.getReservationId(), themeId, dateFrom, dateTo
                ).isEmpty()
        );

        return reserveTickets;
    }

    public List<ReserveTicket> memberReservations(Long memberId) {
        return reserveTicketRepository.findAllByReserverId(memberId);
    }

    public void addWaitingReservation(long themeId, LocalDate date, long timeId, int minWeight, long memberId) {
        int count = reserveTicketRepository.countSameWaitingReservation(themeId, date, timeId, minWeight);
        reservationService.addReservation(new AddReservationDto(""))
        new ReserveTicket()
        reserveTicketRepository.save()

    }
}
