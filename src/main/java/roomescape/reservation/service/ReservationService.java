package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.common.exception.PastDateException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.dto.ReservationMineResponse;
import roomescape.reservation.dto.ReservationWithRank;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationSlotService reservationSlotService;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public Reservation saveConfirm(final Member member, final LocalDate date, final Long timeId, final Long themeId) {
        validateDate(date);

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new DataNotFoundException("해당 테마가 존재하지 않습니다."));
        ReservationSlot slot = new ReservationSlot(date, time, theme);

        if (reservationRepository.existsConfirmedReservationBySlot(slot)) {
            throw new DataExistException("해당 시간에 이미 예약된 테마입니다.");
        }

        return reservationRepository.save(new Reservation(member, slot, ReservationStatus.CONFIRMED));
    }

    @Transactional
    public Reservation saveWaiting(final Member member, final LocalDate date, final Long timeId, final Long themeId) {
        validateDate(date);

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new DataNotFoundException("해당 테마가 존재하지 않습니다."));
        ReservationSlot slot = new ReservationSlot(date, time, theme);

        if (!reservationRepository.existsConfirmedReservationBySlot(slot)) {
            throw new DataExistException("해당 시간에 예약 가능한 테마입니다. 일반 예약을 진행해주세요.");
        }

        return reservationRepository.save(new Reservation(member, slot, ReservationStatus.WAITING));
    }

    private void validateDate(final LocalDate date) {
        if (!date.isAfter(LocalDate.now())) {
            throw new PastDateException("과거 시간은 예약 등록을 할 수 없습니다. date = " + date);
        }
    }

    @Transactional
    public void deleteById(final Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + id));

        if (reservation.isConfirmed()) {
            approveNextWaitingReservation(reservation.getSlot());
        }

        reservationRepository.deleteById(id);
    }

    private void approveNextWaitingReservation(ReservationSlot slot) {
        reservationRepository.findReservationsBySlotAndStatus(slot, ReservationStatus.WAITING)
                .stream()
                .findFirst()
                .ifPresent(waitingReservation -> {
                    waitingReservation.confirmReservation();
                });
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<AvailableReservationTime> findAvailableReservationTimes(final LocalDate date, final Long themeId) {
        return reservationSlotService.findAvailableSlots(date, themeId);
    }

    public List<ReservationMineResponse> findReservationsByMember(final Member member) {
        List<ReservationWithRank> confirmedReservations = reservationRepository
                .findReservationsWithRankByMemberAndStatus(member, ReservationStatus.CONFIRMED);
        List<ReservationWithRank> waitingReservations = reservationRepository
                .findReservationsWithRankByMemberAndStatus(member, ReservationStatus.WAITING);

        return Stream.concat(
                confirmedReservations.stream().map(ReservationMineResponse::from),
                waitingReservations.stream().map(ReservationMineResponse::from)).toList();
    }
}
