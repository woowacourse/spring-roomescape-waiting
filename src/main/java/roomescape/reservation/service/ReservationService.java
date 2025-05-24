package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

        if (isAlreadyReserved(date, timeId, themeId)) {
            Reservation waited = Reservation.wait(reserver, reservationDateTime, theme);
            Reservation saved = reservationRepository.save(waited);
            return ReservationResponse.from(saved);
        }

        Reservation reserved = Reservation.reserve(reserver, reservationDateTime, theme);
        Reservation saved = reservationRepository.save(reserved);
        return ReservationResponse.from(saved);
    }

    private boolean isAlreadyReserved(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.deleteById(reservation.getId());
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
            List<Reservation> sameSlotReservations = reservationRepository.findByDateAndTimeIdAndThemeId(
                    myReservation.getDate(),
                    myReservation.getTimeId(),
                    myReservation.getTheme()
            ).stream().sorted(Comparator.comparing(Reservation::getReservedAt)).toList();

            int myReservationNumber = sameSlotReservations.indexOf(myReservation);

            MyReservationResponse response = new MyReservationResponse(
                    myReservation.getId(),
                    myReservation.getThemeName(),
                    myReservation.getDate(),
                    myReservation.getStartAt(),
                    myReservationNumber == 0 ? "예약" : myReservationNumber + "번째 예약 대기"
            );
            responses.add(response);
        }

        return responses;
    }

    public List<WaitingReservationResponse> getWaitingReservations() {
        List<Reservation> waitingReservations = reservationRepository.findByStatus(ReservationStatus.WAITING);

        return WaitingReservationResponse.from(waitingReservations);
    }
}
