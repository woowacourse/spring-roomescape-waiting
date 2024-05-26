package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
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

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation saveReservation(ReservationDto reservationDto) {
        ReservationTime time = findReservationTime(reservationDto);

        LocalDate date = reservationDto.getDate();
        validateIsFuture(date, time.getStartAt());
        validateDuplication(date, time.getId(), reservationDto.getThemeId());

        Reservation reservation = new Reservation(reservationDto);
        return reservationRepository.save(reservation);
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        Optional<ReservationTime> time = reservationTimeRepository.findById(timeId);
        return time.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
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

    private void validateIsFuture(LocalDate date, LocalTime time) {
        LocalDateTime timeToBook = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (timeToBook.isBefore(now)) {
            throw new BadRequestException("[ERROR] 현재 이전 예약은 할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, long timeId, long themeId) {
        boolean isExist = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복되는 예약은 추가할 수 없습니다.");
        }
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
