package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public ReservationResponse reserve(final ReservationSaveRequest saveRequest, final Member member) {
        ReservationTime reservationTime = findReservationTimeById(saveRequest.timeId());
        Theme theme = findThemeById(saveRequest.themeId());
        validateDuplicateReservation(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId());

        Reservation reservation = saveRequest.toEntity(member, reservationTime, theme, Status.RESERVED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDuplicateReservation(final LocalDate date, final long timeId, final long themeId) {
        boolean isDuplicated = reservationRepository.existsByDateAndTimeIdAndThemeIdAndStatus(
                date, timeId, themeId, Status.RESERVED
        );
        if (isDuplicated) {
            throw new IllegalArgumentException("[ERROR] 중복된 예약이 존재합니다.");
        }
    }

    public ReservationResponse registerWaiting(final ReservationSaveRequest saveRequest, final Member member) {
        ReservationTime reservationTime = findReservationTimeById(saveRequest.timeId());
        Theme theme = findThemeById(saveRequest.themeId());
        validateAlreadyRegistered(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId(), member.getId());
        validateAlreadyReserved(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId(), member.getId());

        Reservation reservation = saveRequest.toEntity(member, reservationTime, theme, Status.PENDING);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private ReservationTime findReservationTimeById(final long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 예약 가능 시간 번호를 입력하였습니다."));
    }

    private Theme findThemeById(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 테마 번호를 입력하였습니다."));
    }

    private void validateAlreadyRegistered(
            final LocalDate date, final long timeId,
            final long themeId, final long memberId
    ) {
        boolean alreadyRegistered = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(
                date, timeId, themeId, memberId, Status.PENDING
        );
        if (alreadyRegistered) {
            throw new IllegalArgumentException("[ERROR] 이미 예약 대기를 등록하였습니다.");
        }
    }

    private void validateAlreadyReserved(
            final LocalDate date, final long timeId,
            final long themeId, final long memberId
    ) {
        boolean alreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(
                date, timeId, themeId, memberId, Status.RESERVED
        );
        if (alreadyReserved) {
            throw new IllegalArgumentException("[ERROR] 이미 예약 완료한 예약 대기 요청입니다.");
        }
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
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes =
                StreamSupport.stream(reservationTimeRepository.findAll().spliterator(), false)
                        .toList();

        return reservationTimes.stream()
                .map(time -> new SelectableTimeResponse(
                        time.getId(),
                        time.getStartAt(),
                        isAlreadyBooked(time, reservations)
                ))
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationTime reservationTime, final List<Reservation> reservations) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().getId() == reservationTime.getId());
    }

    public List<Reservation> findByDateBetween(final LocalDate startDate, final LocalDate endDate) {
        return reservationRepository.findByDateBetween(startDate, endDate);
    }

    public List<MemberReservationResponse> findAllByMemberId(final long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        List<MemberReservationResponse> responses = new ArrayList<>();
        for (Reservation reservation : reservations) {
            int rank = countWaitingRank(reservation);
            responses.add(MemberReservationResponse.of(reservation, rank));
        }
        return responses;
    }

    public int countWaitingRank(final Reservation reservation) {
        if (reservation.getStatus().equals(Status.PENDING)) {
            return reservationRepository.countAlreadyRegisteredWaitings(
                    reservation.getId(),
                    reservation.getDate(), reservation.getTime().getId(),
                    reservation.getTheme().getId(), reservation.getStatus()
            ) + 1;
        }
        return 0;
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

    public void validateAlreadyHasReservationByTimeId(final long timeId) {
        List<Reservation> reservations = reservationRepository.findByTimeId(timeId);
        if (!reservations.isEmpty()) {
            throw new IllegalArgumentException("[ERROR]  (timeId : " + timeId + ") 에 해당하는 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }

    public void validateAlreadyHasReservationByThemeId(final long themeId) {
        if (!reservationRepository.findByThemeId(themeId).isEmpty()) {
            throw new IllegalArgumentException("[ERROR]  (themeId : " + themeId + ") 에 해당하는 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }
}
