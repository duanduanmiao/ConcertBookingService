package proj.concert.service.domain;


import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOOKING")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "BOOKING_ID")
    private long bookingId;

    @Column(name = "USER_ID")
    private long userId;

    @Column(name = "CONCERT_ID")
    private long concertId;

    @Column(name = "DATE")
    private LocalDateTime date;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "bookingId")
    private List<Seat> seats = new ArrayList<>();



    public Booking() {
    }

    public Booking(long userId, long concertId, LocalDateTime date) {
        this.userId = userId;
        this.concertId = concertId;
        this.date = date;
    }


    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

}
