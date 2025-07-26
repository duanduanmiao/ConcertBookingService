package proj.concert.service.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Embeddable
@Table(name = "CONCERT_DATES")
public class ConcertDate {

    @Column(nullable = false)
    private Long concertId;

    @Column(name = "DATE", nullable = false)
    private LocalDateTime date;


    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
