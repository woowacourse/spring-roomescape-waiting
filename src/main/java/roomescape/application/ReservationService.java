package roomescape.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.MyReservationResponse;
import roomescape.application.dto.ReservationCriteria;
import roomescape.application.dto.ReservationRequest;
import roomescape.application.dto.ReservationResponse;
import roomescape.application.dto.WaitingRequest;
import roomescape.application.dto.WaitingResponse;
import roomescape.application.dto.WaitingWithRankResponse;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationFactory;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.dto.WaitingWithRank;
import roomescape.domain.repository.WaitingCommandRepository;
import roomescape.domain.WaitingFactory;
import roomescape.domain.repository.WaitingQueryRepository;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.domain.repository.TimeQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ReservationService {

    private final ReservationFactory reservationFactory;
    private final ReservationCommandRepository reservationCommandRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final TimeQueryRepository timeQueryRepository;
    private final ThemeQueryRepository themeQueryRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final WaitingQueryRepository waitingQueryRepository;
    private final Clock clock;

    public ReservationService(ReservationFactory reservationFactory,
                              ReservationCommandRepository reservationCommandRepository,
                              ReservationQueryRepository reservationQueryRepository,
                              TimeQueryRepository timeQueryRepository,
                              ThemeQueryRepository themeQueryRepository,
                              MemberQueryRepository memberQueryRepository,
                              WaitingQueryRepository waitingQueryRepository,
                              Clock clock) {
        this.reservationFactory = reservationFactory;
        this.reservationCommandRepository = reservationCommandRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.timeQueryRepository = timeQueryRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.memberQueryRepository = memberQueryRepository;
        this.waitingQueryRepository = waitingQueryRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse create(LoginMember loginMember, ReservationRequest request) {
        LocalDate date = request.date();
        Time time = timeQueryRepository.getById(request.timeId());
        Theme theme = themeQueryRepository.getById(request.themeId());
        if (reservationQueryRepository.existsByDateAndTimeAndTheme(date, time, theme)) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_RESERVATION, "이미 존재하는 예약입니다.");
        }
        Member member = memberQueryRepository.getById(loginMember.id());
        Reservation reservation = reservationFactory.create(member, date, time, theme, clock);
        return ReservationResponse.from(reservationCommandRepository.save(reservation));
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation reservation = reservationQueryRepository.getById(id);
        reservationCommandRepository.delete(reservation);
    }

    public List<ReservationResponse> findAll() {
        List<Reservation> reservations = reservationQueryRepository.findAll();
        return convertToReservationResponses(reservations);
    }

    private List<ReservationResponse> convertToReservationResponses(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findByCriteria(ReservationCriteria reservationCriteria) {
        Long themeId = reservationCriteria.themeId();
        Long memberId = reservationCriteria.memberId();
        LocalDate dateFrom = reservationCriteria.dateFrom();
        LocalDate dateTo = reservationCriteria.dateTo();
        return reservationQueryRepository.findByCriteria(themeId, memberId, dateFrom, dateTo).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findMyReservationsAndWaiting(Long memberId) {
        Stream<MyReservationResponse> reservationsStream = reservationQueryRepository.findAllByMemberIdOrderByDateDesc(memberId).stream()
                .map(MyReservationResponse::convert);

        Stream<MyReservationResponse> waitingsStream = waitingQueryRepository.findWaitingWithRankByMemberId(memberId).stream()
                .map(MyReservationResponse::convert);

        return Stream.concat(reservationsStream, waitingsStream)
                .sorted(Comparator.comparing(MyReservationResponse::date).reversed())
                .toList();
    }
}
