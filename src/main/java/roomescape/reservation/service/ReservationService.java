package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.reservation.service.out.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.service.ReservationTimeService;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        return ReservationResponse.from(reservations);
    }

    public ReservationResponse reserve(ReserveCommand reserveCommand) {
        LocalDate date = reserveCommand.date();
        Long timeId = reserveCommand.timeId();

        isAlreadyReservedTime(date, timeId);

        ReservationDateTime reservationDateTime = ReservationDateTime.create(
                new ReservationDate(date), reservationTimeService.getReservationTime(timeId)
        );
        Theme theme = themeService.getTheme(reserveCommand.themeId());
        Member reserver = memberService.getMember(reserveCommand.memberId());

        Reservation reserved = Reservation.reserve(reserver, reservationDateTime, theme);
        Reservation saved = reservationRepository.save(reserved);

        return ReservationResponse.from(saved);
    }

    private void isAlreadyReservedTime(LocalDate date, Long timeId) {
        if (reservationRepository.existsSameDateTime(date, timeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 존재하는 시간입니다.");
        }
    }

    public void deleteById(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.deleteById(reservation.getId());
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약을 찾을 수 없습니다."));
    }

    public List<ReservationResponse> getFilteredReservations(Long themeId,
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
}
