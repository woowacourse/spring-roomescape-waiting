package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.ValidationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberJpaRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationOfMemberResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationJpaRepository;
import roomescape.reservation.repository.ReservationTimeJpaRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeJpaRepository;

@Service
public class ReservationService {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final ThemeJpaRepository themeJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    public ReservationService(
            ReservationJpaRepository ReservationJpaRepository,
            ReservationTimeJpaRepository reservationTimeJpaRepository,
            ThemeJpaRepository themeJpaRepository,
            MemberJpaRepository memberJpaRepository
    ) {
        this.reservationJpaRepository = ReservationJpaRepository;
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
        this.themeJpaRepository = themeJpaRepository;
        this.memberJpaRepository = memberJpaRepository;
    }

    public ReservationResponse addReservation(
            ReservationRequest reservationRequest,
            MemberRequest memberRequest
    ) {
        ReservationTime reservationTime = reservationTimeJpaRepository.getById(reservationRequest.timeId());
        Theme theme = themeJpaRepository.getById(reservationRequest.themeId());

        Reservation reservation = new Reservation(
                reservationRequest.date(),
                reservationTime,
                theme,
                memberRequest.toMember()
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationJpaRepository.save(reservation));
    }

    public ReservationResponse addReservation(AdminReservationRequest adminReservationRequest) {
        Member member = memberJpaRepository.getById(adminReservationRequest.memberId());
        ReservationTime reservationTime = reservationTimeJpaRepository.getById(adminReservationRequest.timeId());
        Theme theme = themeJpaRepository.getById(adminReservationRequest.themeId());

        Reservation reservation = new Reservation(
                adminReservationRequest.date(),
                reservationTime,
                theme,
                member
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationJpaRepository.save(reservation));
    }

    private void validateIsBeforeNow(Reservation reservation) {
        if (reservation.isBeforeNow()) {
            throw new ValidationException("과거 시간은 예약할 수 없습니다.");
        }
    }

    private void validateIsDuplicated(Reservation reservation) {
        if (reservationJpaRepository.existsByDateAndReservationTimeAndTheme(reservation.getDate(),
                reservation.getReservationTime(), reservation.getTheme())) {
            throw new DuplicationException("이미 예약이 존재합니다.");
        }
    }

    public List<ReservationResponse> findReservations() {
        return reservationJpaRepository.findAll()
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> searchReservations(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        Theme theme = themeJpaRepository.getById(themeId);
        Member member = memberJpaRepository.getById(memberId);
        return reservationJpaRepository.findByThemeAndMember(theme, member)
                .stream()
                .filter(reservation -> reservation.isBetweenInclusive(dateFrom, dateTo))
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationOfMemberResponse> findReservationsByMember(Member member) {
        return reservationJpaRepository.findByMember(member)
                .stream()
                .map(ReservationOfMemberResponse::new)
                .toList();
    }

    public void deleteReservation(Long id) {
        reservationJpaRepository.deleteById(id);
    }
}
