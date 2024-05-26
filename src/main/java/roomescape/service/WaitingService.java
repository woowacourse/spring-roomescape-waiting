package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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

@Service
public class WaitingService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(ReservationTimeRepository reservationTimeRepository,
                          WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Waiting> findAllWaitings() {
        return waitingRepository.findAll();
    }

    public Waiting saveWaiting(ReservationDto reservationDto) {
        ReservationTime time = findReservationTime(reservationDto);

        LocalDate date = reservationDto.getDate();
        validateIsFuture(date, time.getStartAt());
        validateDuplication(date, time.getId(), reservationDto.getThemeId(), reservationDto.getMemberId());
        validateExistReservation(date, time.getId(), reservationDto.getThemeId(), reservationDto.getMemberId());

        Waiting waiting = new Waiting(reservationDto);
        return waitingRepository.save(waiting);
    }

    public List<Waiting> findWaitingsByMember(LoginMember member) {
        return waitingRepository.findByMemberId(member.getId());
    }

    public void deleteWaiting(long id) {
        validateExistence(id);
        waitingRepository.deleteById(id);
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        Optional<ReservationTime> time = reservationTimeRepository.findById(timeId);
        return time.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private void validateIsFuture(LocalDate date, LocalTime time) {
        LocalDateTime timeToBook = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (timeToBook.isBefore(now)) {
            throw new BadRequestException("[ERROR] 현재 이전 예약은 할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, long timeId, long themeId, long memberId) {
        boolean isExist = waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복되는 예약 대기는 추가할 수 없습니다.");
        }
    }

    private void validateExistReservation(LocalDate date, long timeId, long themeId, long memberId) {
        List<Reservation> reservations = reservationRepository
                .findByTimeIdAndThemeIdAndDate(timeId, themeId, date);
        if (reservations.isEmpty()) {
            throw new BadRequestException("[ERROR] 예약이 존재하지 않아 예약 대기가 불가합니다.");
        }
        Reservation reservation = reservations.get(0);
        if (isMembersReservation(reservation, memberId)) {
            throw new BadRequestException("[ERROR] 이미 예약이 존재합니다.");
        }
    }

    private boolean isMembersReservation(Reservation reservation, long memberId) {
        return reservation.getMember().hasId(memberId);
    }

    private void validateExistence(long id) {
        boolean isNotExist = !waitingRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 예약입니다.");
        }
    }
}
