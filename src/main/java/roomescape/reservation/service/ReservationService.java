package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.MemberReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.SelectableTimeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationResponse save(final ReservationSaveRequest saveRequest, final Member member) {
        ReservationTime reservationTime = findReservationTimeById(saveRequest);
        Theme theme = findThemeById(saveRequest);
        validateDuplicateReservation(saveRequest);

        Reservation reservation = saveRequest.toEntity(member, reservationTime, theme, Status.RESERVATION);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private ReservationTime findReservationTimeById(final ReservationSaveRequest reservationSaveRequest) {
        return reservationTimeRepository.findById(reservationSaveRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 예약 가능 시간 번호를 입력하였습니다."));
    }

    private Theme findThemeById(final ReservationSaveRequest reservationSaveRequest) {
        return themeRepository.findById(reservationSaveRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 테마 번호를 입력하였습니다."));
    }

    private void validateDuplicateReservation(ReservationSaveRequest saveRequest) {
        if (hasDuplicateReservation(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId())) {
            throw new IllegalArgumentException("[ERROR] 중복된 예약이 존재합니다.");
        }
    }

    private boolean hasDuplicateReservation(final LocalDate date, final long timeId, final long themeId) {
        return !reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).isEmpty();
    }

    public List<ReservationResponse> getAllResponses() {
        return getAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<Reservation> getAllReservations() {
        return StreamSupport.stream(reservationRepository.findAll().spliterator(), false)
                .toList();
    }

    public List<SelectableTimeResponse> findSelectableTimes(final LocalDate date, final long themeId) {
        List<Long> usedTimeIds = reservationRepository.findTimeIdsByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes =
                StreamSupport.stream(reservationTimeRepository.findAll().spliterator(), false)
                        .toList();

        return reservationTimes.stream()
                .map(time -> new SelectableTimeResponse(
                        time.getId(),
                        time.getStartAt(),
                        isAlreadyBooked(time, usedTimeIds)
                ))
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationTime reservationTime, final List<Long> usedTimeIds) {
        return usedTimeIds.contains(reservationTime.getId());
    }

    public List<Reservation> findByDateBetween(final LocalDate startDate, final LocalDate endDate) {
        return reservationRepository.findByDateBetween(startDate, endDate);
    }

    public List<MemberReservationResponse> findAllByMemberId(final long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    public ReservationDeleteResponse delete(final long id) {
        validateNotExitsReservationById(id);
        return new ReservationDeleteResponse(reservationRepository.deleteById(id));
    }

    private void validateNotExitsReservationById(final long id) {
        if (reservationRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (id : " + id + ") 에 대한 예약이 존재하지 않습니다.");
        }
    }

    public void validateAlreadyHasReservationByTimeId(final long id) {
        List<Reservation> reservations = reservationRepository.findByTimeId(id);
        if (!reservations.isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }

    public void validateAlreadyHasReservationByThemeId(final long id) {
        if (!reservationRepository.findByThemeId(id).isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 해당 테마를 사용 중인 예약이 있어 삭제할 수 없습니다.");
        }
    }
}
