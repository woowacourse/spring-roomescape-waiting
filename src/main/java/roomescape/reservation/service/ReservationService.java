package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NoElementsException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.controller.response.WaitingReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.service.ReservationTimeService;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberQueryService memberQueryService;

    @Transactional
    public ReservationResponse reserve(ReserveCommand reserveCommand) {
        LocalDate date = reserveCommand.date();
        Long timeId = reserveCommand.timeId();
        Long themeId = reserveCommand.themeId();

        ReservationDateTime reservationDateTime = ReservationDateTime.create(
                new ReservationDate(date), reservationTimeService.getReservationTime(timeId));
        Theme theme = themeService.getTheme(themeId);
        Member reserver = memberQueryService.getMember(reserveCommand.memberId());

        LocalDateTime reservedAt = LocalDateTime.now();
        Reservation reservation = isAlreadyReserved(date, timeId, themeId)
                ? Reservation.wait(reserver, reservationDateTime, theme, reservedAt)
                : Reservation.reserve(reserver, reservationDateTime, theme, reservedAt);

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    private boolean isAlreadyReserved(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.existsBy(date, timeId, themeId);
    }

    @Transactional
    public void cancel(Long id) {
        Reservation reservation = getReservation(id);
        ReservationSlot reservationSlot = new ReservationSlot(
                reservationRepository.findByDateAndTimeIdAndThemeId(
                        reservation.getDate(),
                        reservation.getTimeId(),
                        reservation.getTheme()
                )
        );

        if (reservation.isReserved() && reservationSlot.hasWaiting()) {
            reservationSlot.getNext(reservation).reserve();
        }
        reservationRepository.deleteById(id);
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoElementsException("예약을 찾을 수 없습니다."));
    }

    public List<ReservationResponse> getFilteredReservations(
            Long themeId,
            Long memberId,
            LocalDate from,
            LocalDate to
    ) {
        if (themeId == null && memberId == null && from == null && to == null) {
            return getAllReservations();
        }

        List<Reservation> reservations = reservationRepository.findFilteredReservations(
                themeId, memberId, from, to
        );

        return ReservationResponse.from(reservations);
    }

    private List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        return ReservationResponse.from(reservations);
    }

    public List<MyReservationResponse> getMyReservations(Long memberId) {
        List<Reservation> myReservations = reservationRepository.findByMemberId(memberId);

        List<MyReservationResponse> responses = new ArrayList<>();
        for (Reservation myReservation : myReservations) {
            ReservationSlot reservationSlot = new ReservationSlot(
                    reservationRepository.findByDateAndTimeIdAndThemeId(
                            myReservation.getDate(),
                            myReservation.getTimeId(),
                            myReservation.getTheme()
                    )
            );
            MyReservationResponse response = MyReservationResponse.from(
                    myReservation, reservationSlot.getOrder(myReservation));
            responses.add(response);
        }
        return responses;
    }

    public List<WaitingReservationResponse> getWaitingReservations() {
        List<Reservation> waitingReservations = reservationRepository.findByStatus(ReservationStatus.WAITING);

        return WaitingReservationResponse.from(waitingReservations);
    }
}
