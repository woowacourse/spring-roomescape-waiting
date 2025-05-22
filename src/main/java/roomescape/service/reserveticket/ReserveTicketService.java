package roomescape.service.reserveticket;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Reserver;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reserveticket.ReservationTicketWaitings;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reserveticket.ReserveTicketRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.strategy.NonDuplicateCheckStrategy;
import roomescape.service.reservation.strategy.ReservationValidateStrategy;

@Service
public class ReserveTicketService {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ReserveTicketRepository reserveTicketRepository;
    private final ReservationValidateStrategy reservationValidateStrategy;

    public ReserveTicketService(MemberService memberService, ReservationService reservationService,
                                ReserveTicketRepository reserveTicketRepository,
                                ReservationValidateStrategy reservationValidateStrategy) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.reserveTicketRepository = reserveTicketRepository;
        this.reservationValidateStrategy = reservationValidateStrategy;
    }

    @Transactional
    public long addReservation(AddReservationDto newReservationDto, Long memberId) {
        Reserver reserver = memberService.getMemberById(memberId);
        long reservationId = reservationService.addReservation(newReservationDto, reservationValidateStrategy,
                ReservationStatus.RESERVATION);
        Reservation reservation = reservationService.getReservationById(reservationId);

        return reserveTicketRepository.save(
                new ReserveTicket(null, reservation, reserver)).getId();
    }

    public List<ReserveTicketWaiting> allReservationTickets() {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAll();
        ReservationTicketWaitings reservationTicketWaitings = new ReservationTicketWaitings(reserveTickets);
        return reservationTicketWaitings.reserveTicketWaitings();
    }

    public List<ReserveTicketWaiting> memberReservationWaitingTickets(Long memberId) {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAll();
        ReservationTicketWaitings reservationTicketWaitings = new ReservationTicketWaitings(reserveTickets);
        return reservationTicketWaitings.reserveTicketWaitings()
                .stream()
                .filter(reserveTicketWaiting -> reserveTicketWaiting.isSameMember(memberId))
                .toList();
    }

    @Transactional
    public void deleteReservation(long id) {
        reserveTicketRepository.deleteById(id);
    }

    public List<ReserveTicketWaiting> searchReservations(Long themeId, Long memberId, LocalDate dateFrom,
                                                         LocalDate dateTo) {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAllByReserverId(memberId);

        reserveTickets.removeIf(reservationMember ->
                reservationService.searchReservation(
                        reservationMember.getReservationId(), themeId, dateFrom, dateTo
                ).isEmpty()
        );

        return reserveTickets.stream()
                .map((reserveTicket -> new ReserveTicketWaiting(reserveTicket.getId(), reserveTicket.getName(),
                        reserveTicket.getDate(),
                        reserveTicket.getStartAt(), reserveTicket.getReservationStatus(), 1,
                        reserveTicket.getThemeName(), reserveTicket.getMemberId())))
                .toList();
    }

    @Transactional
    public long addWaitingReservation(AddReservationDto addReservationDto, Long reserverId) {
        long reservationId = reservationService.addReservation(addReservationDto, new NonDuplicateCheckStrategy(),
                ReservationStatus.PREPARE);

        validateIsReservationDoesntExist(
                addReservationDto.themeId(),
                addReservationDto.date(),
                addReservationDto.timeId());
        validateIsSameWaitingExist(
                addReservationDto.themeId(),
                addReservationDto.date(),
                addReservationDto.timeId(),
                reserverId);

        Reservation reservation = reservationService.getReservationById(reservationId);
        Reserver reserver = memberService.getMemberById(reserverId);
        ReserveTicket reserveTicket = new ReserveTicket(null, reservation, reserver);
        return reserveTicketRepository.save(reserveTicket).getId();
    }

    private void validateIsReservationDoesntExist(Long themeId, LocalDate date, Long timeId) {
        List<ReserveTicket> reserveTicket = reserveTicketRepository.findAllBySameReservation(themeId, date, timeId,
                ReservationStatus.RESERVATION);

        if (reserveTicket.isEmpty()) {
            throw new InvalidReservationException("예약이 존재하지 않습니다");
        }
    }

    private void validateIsSameWaitingExist(Long themeId, LocalDate date, Long timeId, Long reserverId) {
        List<ReserveTicket> reserveTicket = reserveTicketRepository.findAllBySameReservation(themeId, date, timeId,
                ReservationStatus.PREPARE);

        if (reserveTicket.isEmpty()) {
            return;
        }

        Optional<ReserveTicket> existReserveTicket = reserveTicket.stream()
                .filter((currentReserveTicket) -> currentReserveTicket.getMemberId() == reserverId)
                .findAny();

        if (existReserveTicket.isPresent()) {
            throw new InvalidReservationException("중복된 대기 요청입니다.");
        }
    }

    @Transactional
    public void changeWaitingToReservation(Long id) {
        ReserveTicket reserveTicket = getReservationTicketById(id);
        Reservation reservation = reserveTicket.getReservation();

        List<ReserveTicket> reservationTicket = reserveTicketRepository.findAllBySameReservation(
                reservation.getTheme().getId(), reservation.getDate(),
                reservation.getReservationTime().getId(), ReservationStatus.RESERVATION);
        if (!reservationTicket.isEmpty()) {
            throw new InvalidReservationException("예약이 존재하기 때문에 예약대기를 예약으로 변경할 수 없습니다.");
        }
        reservation.waitingToReservation();
    }

    private ReserveTicket getReservationTicketById(Long id) {
        return reserveTicketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("예약 티켓을 찾을 수 없습니다"));
    }
}
