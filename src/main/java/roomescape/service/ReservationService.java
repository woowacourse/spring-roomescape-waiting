package roomescape.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationSavePolicy;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationDto;
import roomescape.service.dto.ReservationTimeInfoDto;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationSavePolicy reservationSavePolicy;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              WaitingRepository waitingRepository,
                              ReservationSavePolicy reservationSavePolicy) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
        this.reservationSavePolicy = reservationSavePolicy;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation saveReservation(ReservationDto reservationDto) {
        reservationSavePolicy.validate(reservationDto);

        Reservation reservation = new Reservation(reservationDto);
        return reservationRepository.save(reservation);
    }

    public void deleteReservation(long id) {
        reservationRepository.findById(id).ifPresentOrElse(
                reservation -> {
                    reservationRepository.deleteById(id);
                    updateWaitingToReservation(reservation);
                },
                () -> {
                    throw new NotFoundException("[ERROR] 존재하지 않는 예약입니다.");
                }
        );
    }

    public ReservationTimeInfoDto findReservationTimesInformation(LocalDate date, long themeId) {
        List<ReservationTime> bookedTimes = reservationRepository.findReservationTimeBooked(date, themeId);
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        return new ReservationTimeInfoDto(bookedTimes, allTimes);
    }

    public List<Reservation> findReservationsByConditions(long memberId, long themeId, LocalDate from, LocalDate to) {
        return reservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, from, to);
    }

    public List<Reservation> findReservationsByMember(LoginMember member) {
        return reservationRepository.findByMemberId(member.getId());
    }

    private void updateWaitingToReservation(Reservation reservation) {
        List<Waiting> waitings = waitingRepository.findByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());

        if (waitings.isEmpty()) {
            return;
        }

        List<Waiting> sortWaitings = waitings.stream()
                .sorted(Comparator.comparing(Waiting::getCreated_at))
                .toList();
        Waiting waiting = sortWaitings.get(0);
        waitingRepository.delete(waiting);

        Reservation newReservation = new Reservation(
                waiting.getDate(), waiting.getTime(), waiting.getTheme(), waiting.getMember());
        reservationRepository.save(newReservation);
    }
}
