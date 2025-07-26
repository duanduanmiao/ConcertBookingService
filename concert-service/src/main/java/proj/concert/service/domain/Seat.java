package proj.concert.service.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.ColumnDefault;
import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;
import proj.concert.common.types.BookingStatus;

import javax.persistence.*;
import javax.ws.rs.DefaultValue;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "SEAT")
@IdClass(Seat.class)
public class Seat implements Serializable {

	@Id
	@Column(name = "LABEL")
	private String label;

	@Column(name = "ISBOOKED")
	private boolean isBooked;

	@Id
	@Column(name = "DATE")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime date;

	@Column(name = "COST")
	private BigDecimal cost;

	@Column(name = "BOOKING_ID")
	private long bookingId;


	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal cost) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.cost = cost;
	}

	public Seat() {}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public void setBooked(boolean booked) {
		isBooked = booked;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public long getBookingId() {
		return bookingId;
	}

	public void setBookingId(long bookingId) {
		this.bookingId = bookingId;
	}
}
