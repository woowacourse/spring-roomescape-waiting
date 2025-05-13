package roomescape.service.reservation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.search.SearchConditions;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.ReservationAlreadyExistsException;
import roomescape.exception.reservation.ReservationInPastException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ReservationTimeRepository timeRepository, ThemeRepository themeRepository,
                                  MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse create(ReservationRequest request, Member member) {
        ReservationTime reservationTime = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (LocalDateTime.now().isAfter(LocalDateTime.of(request.date(), reservationTime.getStartAt()))) {
            throw new ReservationInPastException();
        }

        if (reservationRepository.existsByDateAndTime(request.date(), reservationTime)) {
            throw new ReservationAlreadyExistsException();
        }

        Reservation newReservation = new Reservation(request.date(),
                reservationTime, theme, member);

        return ReservationResponse.from(reservationRepository.save(newReservation));
    }

    public List<ReservationResponse> getAll() {
        return ReservationResponse.from(reservationRepository.findAll());
    }

    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public ReservationResponse createByAdmin(AdminReservationRequest adminReservationRequest) {
        ReservationTime reservationTime = timeRepository.findById(adminReservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(adminReservationRequest.timeId()));

        Theme theme = themeRepository.findById(adminReservationRequest.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(adminReservationRequest.themeId()));

        Member member = memberRepository.findById(adminReservationRequest.memberId())
                .orElseThrow(() -> new MemberNotFoundException(adminReservationRequest.memberId()));

        Reservation newReservation = new Reservation(adminReservationRequest.date(),
                reservationTime, theme, member);

        return ReservationResponse.from(reservationRepository.save(newReservation));
    }

    @Override
    public List<ReservationResponse> getReservationsByConditions(SearchConditions searchConditions) {

        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                searchConditions.themeId(),
                searchConditions.memberId(),
                searchConditions.dateFrom(),
                searchConditions.dateTo()
        );
        return reservations.stream().
                map(reservation -> ReservationResponse.from(reservation))
                .toList();
    }
}
