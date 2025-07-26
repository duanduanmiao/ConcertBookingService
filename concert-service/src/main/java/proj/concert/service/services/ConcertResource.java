package proj.concert.service.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import proj.concert.common.dto.*;
import proj.concert.common.types.BookingStatus;
import proj.concert.service.domain.*;
import proj.concert.service.jaxrs.LocalDateTimeParam;


@Path("/concert-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ConcertResource {
    // Used to interact with a database
    private final EntityManager entityManager = PersistenceManager.instance().createEntityManager();
    // Thread pool - For allocation of multiple threads
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    // Used to save AsyncResponse
    private static final Map<ConcertInfoSubscriptionDTO, AsyncResponse> subs = new ConcurrentHashMap<>();

    // Create a ConvertDTO from a Concert object
    private ConcertDTO createConvertDTO(Concert concert) {
        ConcertDTO concertDTO = new ConcertDTO(concert.getId(), concert.getTitle(), concert.getImageName(), concert.getBlurb());
        List<PerformerDTO> performerDTOs = new ArrayList<>();
        for (Performer performer : concert.getPerformers()) {
            performerDTOs.add(new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb()));
        }
        concertDTO.setPerformers(performerDTOs);
        List<LocalDateTime> dates = new ArrayList<>(concert.getDates());
        concertDTO.setDates(dates);
        return concertDTO;
    }

    //Retrieve an unique concert by id
    @GET
    @Path("concerts/{id}")
    public Response getConcert(@PathParam("id") Long id) {
        Concert concert;
        try {
            String SQL = "select c from Concert c where c.id = :id";
            concert = entityManager.createQuery(SQL, Concert.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return Response.status(404).build();
        } finally {
            entityManager.close();
        }
        ConcertDTO concertDTO = createConvertDTO(concert);
        return Response.ok(concertDTO).build();
    }

    //Retrieve all concerts
    @GET
    @Path("concerts")
    public Response getAllConcerts() {
        List<Concert> concerts;
        try {
            concerts = entityManager.createQuery("select c from Concert c", Concert.class)
                    .getResultList();
        } catch (Exception e) {
            return Response.status(404).build();
        } finally {
            entityManager.close();
        }
        List<ConcertDTO> concertDTOs = new ArrayList<>();
        for (Concert concert : concerts) {
            ConcertDTO concertDTO = createConvertDTO(concert);
            concertDTOs.add(concertDTO);
        }
        return Response.ok(concertDTOs).build();
    }

    //Get information about all concerts
    @GET
    @Path("concerts/summaries")
    public Response getConcertSummaries() {
        TypedQuery<Concert> query = entityManager.createQuery("select c from Concert c", Concert.class);
        List<Concert> concerts = query.getResultList();
        List<ConcertSummaryDTO> concertSummaryDTOList = new ArrayList<>();
        for (Concert concert : concerts) {
            concertSummaryDTOList.add(new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName()));
        }
        entityManager.close();
        return Response.ok(concertSummaryDTOList).build();
    }

    //Retrieve an unique performer by id
    @GET
    @Path("performers/{id}")
    public Response getPerformer(@PathParam("id") Long id) {
        Performer performer;
        try {
            performer = entityManager.createQuery(
                            "select p from Performer p where p.id = :id", Performer.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return Response.status(404).build();
        } finally {
            entityManager.close();
        }
        PerformerDTO performerDTO = new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb());
        return Response.ok(performerDTO).build();
    }

    //Retrieve all performers
    @GET
    @Path("performers")
    public Response getAllPerformers() {
        List<Performer> performers;
        try {
            performers = entityManager.createQuery("select p from Performer p", Performer.class).getResultList();
        } catch (Exception e) {
            return Response.status(404).build();
        } finally {
            entityManager.close();
        }
        List<PerformerDTO> performerDTOs = new ArrayList<>();
        for (Performer performer : performers) {
            performerDTOs.add(new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb()));
        }
        return Response.ok(performerDTOs).build();
    }

    //Log in
    @POST
    @Path("login")
    public Response login(UserDTO userDTO) {
        User user;
        try {
            user = entityManager.createQuery(
                            "select u from User u where u.userName = :username and u.passWord = :password", User.class)
                    .setParameter("username", userDTO.getUsername())
                    .setParameter("password", userDTO.getPassword())
                    .getSingleResult();
        } catch (Exception e) {
            return Response.status(401).build();
        } finally {
            entityManager.close();
        }
        NewCookie newCookie = new NewCookie("auth", user.getId().toString());
        return Response.ok().cookie(newCookie).build();
    }

    //Book Seats
    //@param bookingRequestDTO - booking information
    //@return - uri of booked seat which can check the information of seat
    @POST
    @Path("bookings")
    public Response booking(BookingRequestDTO bookingRequestDTO, @CookieParam("auth") Cookie cookie) {
        int statusNumber = verify(cookie, bookingRequestDTO.getDate(), bookingRequestDTO.getConcertId());
        if (statusNumber != 200) return Response.status(statusNumber).build();
        LocalDateTime date = bookingRequestDTO.getDate();
        List<String> seatLabels = bookingRequestDTO.getSeatLabels();
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            Booking booking = new Booking(Long.parseLong(cookie.getValue()), bookingRequestDTO.getConcertId(), date);
            List<Seat> seats = entityManager.createQuery("select s from Seat s where s.date = :date and s.label in :labels", Seat.class)
                    .setParameter("date", bookingRequestDTO.getDate())
                    .setParameter("labels", seatLabels)
                    .getResultList();
            for (Seat s : seats) {
                if (s.isBooked()) return Response.status(403).build();
            }
            booking.setSeats(seats);
            entityManager.persist(booking);
            entityManager.createQuery(
                            "update Seat s set s.isBooked = :isBooked where s.label in (select s.label from Seat s where s.date = :date and s.label in :labels)")
                    .setParameter("isBooked", true)
                    .setParameter("date", date)
                    .setParameter("labels", seatLabels)
                    .executeUpdate();
            List<Booking> bookings = entityManager.createQuery("select b from Booking b where b.date = :date", Booking.class)
                    .setParameter("date", date)
                    .getResultList();
            int total = 120;
            for (Booking b : bookings) {
                total -= b.getSeats().size();
            }
            int percentageBooked = (120 - total) * 100 / 120;
            for (ConcertInfoSubscriptionDTO cis : subs.keySet()) {
                if (cis.getConcertId() == bookingRequestDTO.getConcertId() && cis.getDate().equals(date)) {
                    if (cis.getPercentageBooked() < percentageBooked) {
                        notification(cis, total);
                    }
                }
            }
            tx.commit();
        } catch (Exception e) {
            return Response.status(403).build();
        } finally {
            entityManager.close();
            executorService.shutdown();
        }
        StringBuilder bookingInfo = new StringBuilder("concert-service/bookingInfo?");
        bookingInfo.append("concert_id=").append(bookingRequestDTO.getConcertId());
        bookingInfo.append("&date=").append(bookingRequestDTO.getDate());
        bookingInfo.append("&labels=");
        for (String label : seatLabels) {
            bookingInfo.append(label).append("_");
        }
        bookingInfo = new StringBuilder(bookingInfo.substring(0, bookingInfo.length() - 1));
        bookingInfo.append("&user_id=").append(cookie.getValue());
        URI uri = null;
        try {
            uri = new URI(bookingInfo.toString());
        } catch (URISyntaxException uriSyntaxException) {
            System.out.println(uriSyntaxException.getMessage());
        }
        return Response.created(uri).build();
    }


    /***
     * Check the information of seats
     * @param date - the date of concert
     * @param isBooked - the state of seat
     * @param cookie - token
     * @return - information of seats
     */
    @GET
    @Path("seats/{date}")
    public Response seat(@PathParam("date") LocalDateTimeParam date, @QueryParam("status") BookingStatus isBooked, @CookieParam("auth") Cookie cookie) {
        List<Seat> seats;
        try {
            seats = entityManager.createQuery(
                            "select s from Seat s where s.date = :date", Seat.class)
                    .setParameter("date", date.getLocalDateTime())
                    .getResultList();
        } catch (Exception e) {
            return Response.status(403).build();
        } finally {
            entityManager.close();
        }
        List<Seat> newSeats = new ArrayList<>();
        if (isBooked == BookingStatus.Booked) {
            for (Seat s : seats) {
                if (s.isBooked()) newSeats.add(s);
            }
            seats = newSeats;
        } else if (isBooked == BookingStatus.Unbooked) {
            for (Seat s : seats) {
                if (!s.isBooked()) newSeats.add(s);
            }
            seats = newSeats;
        }
        List<SeatDTO> seatDTOs = new ArrayList<>();
        for (Seat seat : seats) {
            seatDTOs.add(new SeatDTO(seat.getLabel(), seat.getCost()));
        }
        return Response.ok(seatDTOs).build();
    }

    /***
     * Return booking information
     * @param concertId - the id of concert
     * @param date - the date of concert
     * @param labels - the label of seat
     * @param user_id - the id of user
     * @param cookie - token
     * @return the information of booking
     */
    @GET
    @Path("bookingInfo")
    public Response returnBookingInfo(
            @QueryParam("concert_id") Long concertId,
            @QueryParam("date") LocalDateTimeParam date,
            @QueryParam("labels") String labels,
            @QueryParam("user_id") String user_id,
            @CookieParam("auth") Cookie cookie) {
        if (!cookie.getValue().equals(user_id)) {
            return Response.status(403).build();
        }
        LocalDateTime localDate = date.getLocalDateTime();
        List<Seat> seats;
        try {
            seats = entityManager.createQuery("select s from Seat s where s.date = :date and s.label in :labels", Seat.class)
                    .setParameter("date", localDate)
                    .setParameter("labels", Arrays.asList(labels.split("_")))
                    .getResultList();
        } catch (Exception e) {
            return Response.status(403).build();
        } finally {
            entityManager.close();
        }
        List<SeatDTO> seatDTOs = new ArrayList<>();
        seats.forEach(s -> seatDTOs.add(new SeatDTO(s.getLabel(), s.getCost())));
        BookingDTO bookingDTO = new BookingDTO(concertId, localDate, seatDTOs);
        return Response.ok(bookingDTO).build();
    }

    /***
     * Get the information of booking by the current user
     * @param cookie - token
     * @return - the booking information by the current user
     */
    @GET
    @Path("bookings")
    public Response getBookings(@CookieParam("auth") Cookie cookie) {
        if (cookie == null) return Response.status(401).build();
        Long user_id = Long.valueOf(cookie.getValue());
        List<Booking> bookings;
        try {
            bookings = entityManager.createQuery("select b from Booking b where b.userId = :userId", Booking.class)
                    .setParameter("userId", user_id)
                    .getResultList();
        } catch (Exception e) {
            return Response.status(403).build();
        } finally {
            entityManager.close();
        }
        List<BookingDTO> bookingDTOs = new ArrayList<>();
        bookings.forEach(b -> {
            List<SeatDTO> seatDTOs = new ArrayList<>();
            b.getSeats().forEach(s -> seatDTOs.add(new SeatDTO(s.getLabel(), s.getCost())));
            bookingDTOs.add(new BookingDTO(b.getConcertId(), b.getDate(), seatDTOs));
        });
        return Response.ok(bookingDTOs).build();
    }

    //Subscribe concert
    //implement async subscribeConcertInfo endpoint for concert notifications
    @POST
    @Path("subscribe/concertInfo")
    public void subscribeConcertInfo(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO,
                                     @CookieParam("auth") Cookie cookie,
                                     @Suspended AsyncResponse sub) {
        long concertId = concertInfoSubscriptionDTO.getConcertId();
        LocalDateTime date = concertInfoSubscriptionDTO.getDate();
        int percentageBooked = concertInfoSubscriptionDTO.getPercentageBooked();
        try {
            int statusNumber = verify(cookie, date, concertId);
            if (statusNumber != 200) {
                executorService.submit(() -> {
                    sub.resume(Response.status(statusNumber).build());
                });
            }
            List<Booking> bookings = entityManager.createQuery("select b from Booking b where b.date = :date", Booking.class)
                    .setParameter("date", date)
                    .getResultList();
            int seatRemaining = 120;
            for (Booking b : bookings) {
                seatRemaining -= b.getSeats().size();
            }
            int percentageRemaining = seatRemaining * 100 / 120;
            subs.put(concertInfoSubscriptionDTO, sub);
            if (percentageRemaining < percentageBooked) {
                notification(concertInfoSubscriptionDTO, percentageRemaining);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            entityManager.close();
            executorService.shutdown();
        }
    }

    //Helper methods
    //Send notification to user
    public void notification(ConcertInfoSubscriptionDTO subscription, int percentageNumber) {
        synchronized (subs) {
            for (ConcertInfoSubscriptionDTO cis : subs.keySet()) {
                ConcertInfoNotificationDTO concertInfoNotificationDTO = new ConcertInfoNotificationDTO(percentageNumber);
                subs.get(subscription).resume(concertInfoNotificationDTO);
            }
        }
        subs.clear();
    }

    //Verify the operator's authority
    private int verify(Cookie cookie, LocalDateTime date, long concertId) {
        if (cookie == null) return 401;
        List<LocalDateTime> concertDates = entityManager.createQuery("select cd from Concert c, in (c.dates) cd", LocalDateTime.class)
                .getResultList();
        if (!concertDates.contains(date)) {
            return 400;
        }
        List<Long> concertIds = entityManager.createQuery("select c.id from Concert c", Long.class)
                .getResultList();
        if (!concertIds.contains(concertId)) {
            return 400;
        }
        return 200;
    }

}

