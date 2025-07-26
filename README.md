Project - A Concert Booking Service
==========

This project is a RESTful web service designed for managing concert ticket bookings. It supports seat reservations, performer browsing, user authentication, and subscription notifications for concerts that are about to sell out.

System description
----------
This system simulates a concert booking service for a small venue with 120 seats, divided into three price bands: Silver, Gold, and Platinum.

Key Business Requirements:

- 🎶 A concert may run on multiple dates, and each concert features at least one performer.

- 🎤 Performers may participate in multiple concerts.

- 👀 Users can browse concerts, performers, and seating with or without authentication.

- ✅ Authenticated users can:
  - 🎫 Make all-or-nothing reservations (no partial bookings allowed).
  - 🔒 View only their own bookings, not other users' bookings.
  - 🛎️ Subscribe to notifications for concerts that are nearly sold out.

- 🚫 Double-booking is strictly prohibited, ensuring that a seat can only be booked by one user per concert per date.

---
Features
----------
- 🎶 **Concert and Performer Information Browsing**  
  Retrieve details about available concerts, performers, and scheduled dates.

- 🎫 **Real-Time Seat Availability Checking**  
  Query available seats and pricing information for selected concerts and dates.

- 🔒 **Secure User Authentication**  
  Token-based session management allowing secure login and protected booking features.

- ✅ **Transactional All-or-Nothing Booking**  
  Ensures that either all requested seats are booked successfully or none at all, preventing partial reservations or double-booking.

- 🛎️ **Subscription-Based Notifications**  
  Users can subscribe to concert sellout notifications, keeping them informed when seats are running out.

---
## Project Structure

This project is organized as a **multi-module Maven project** named `project-concert`, consisting of three modules:

- `concert-client`:  
  A pre-built web application demonstrating the user interface.

- `concert-common`:  
  A shared module defining data transfer objects and types.

- `concert-service`:  
  Our core implementation, providing the backend web service and persistence logic.

The parent project's POM manages common dependencies and properties for all modules.

---

## System Architecture

The system follows a **tiered architecture**, where a browser-based web application communicates via HTTP with the backend web service.

---

## Client Module

A pre-built web application (HTML5, CSS, JavaScript, JSP, JSTL) demonstrating the user interface powered by our backend web service.  
We integrated and tested our service using this provided client without modifying its source code.

---

## Common Module

A shared module defining:
- 9 DTO classes for data transfer
- Jackson serializers for `LocalDateTime`
- Enumerations such as `Genre` and `BookingStatus`

---

## Service Module (our core implementation)

The service module contains:
- Domain model (Concert, Performer, ConcertDate, Booking, Seat, User)
- RESTful API endpoints using JAX-RS
- Persistence layer with JPA and Hibernate
- Concurrency control and subscription notification management
- Database utilities (e.g., ConcertUtils, TheatreLayout)
- Integration tests verifying system functionality

The module includes database initialization and logging configurations.  
Concert and performer data are preloaded using the `db-init.sql` script configured in `persistence.xml`.

All provided integration tests pass successfully using Maven's `verify` goal, confirming correct system behavior.

---
Appendix: The client webapp.
----------
The `concert-client` project contains a complete web application which is designed to communicate with your web service from both its server-side (Java) and client-side (JavaScript) code, to deliver its functionality.

#### App usage
Using the application, browsing to `/Concerts` or simply `/` (within the webapp's *context*) will present the "homepage" of the app, allowing the user to view concerts. On this page, users may scroll through the list of concerts on the left side of the page. Clicking on a concert will cause that concert's info to be displayed in the detail view.

![](./spec/concerts-page.PNG)

On the detail view, users can see more information about the currently selected concert, as well as a list of performers and dates for that concert.

Clicking on a performer's image will display a modal dialog containing more information about that performer:

![](./spec/view-performer.PNG)

If the user hasn't logged in, then clicking on the "Book!" button next to one of a concert's dates will prompt the user to sign in:

![](./spec/please-login.PNG)

If the user has already signed in, they will be redirected to the seat selection page for their selected concert and date. See `concert-service/src/main/resources/db-init.sql` for the data that the server is initialised with, including possible users and their passwords.

![](./spec/booking-page.PNG)

Here, users can see a visual seat map of the venue, along with an indicator of which seats have already been booked. Clicking unbooked seats will toggle a blue halo around that seat - an indicator that the seat has been selected for booking, as can be seen in the above screenshot. A list of selected seats - along with the total ticket price - can be seen below the seat map.

Once the user has made their selection, they can click the "Book!" button. Assuming they have "sufficient funds" (which will always be true for this test case), and that no other user has booked the same seats in the meantime, the user will be presented with a success dialog box:

![](./spec/booking-success.PNG)

Dismissing the dialog will redirect the user back to the concerts page, where they may continue to book tickets at other concerts / dates. (or more tickets for the same concert / date).

---
## Running the Project

To run the complete system, both the client and service modules need to be deployed to a servlet container such as Tomcat.

### Recommended Setup (Verified with IntelliJ IDEA Ultimate + Tomcat 8.5+)

1. Build the project using Maven: mvn clean package

This will generate two WAR files:
- `concert-client/target/concert-client.war`
- `concert-service/target/concert-service.war`

2. Deploy both WAR files to a local Tomcat 8.5+ server:
- Set the client WAR context path to `/concert-webapp`
- Set the service WAR context path to `/webservice`

3. Access the web application in your browser at: http://localhost:10000/concert-webapp/

> **Note**: During development and testing, I used Maven's `verify` goal to run all provided integration tests and validate the system functionality.

---
## 🛠️ Technologies Used
- **Java** (JAX-RS, JPA/Hibernate)
- **Maven** (multi-module project structure)
- **HTML, CSS, JavaScript** (Client-side web application)
- **Tomcat 8.5+** (for deployment)
- **Log4j** (for logging)
- **H2 Database** (in-memory persistence for demonstration)

---
## 🧑‍🤝‍🧑 Team Contribution

This project was completed as part of a team assignment during my Computer Science studies. Our team worked collaboratively on all aspects of the system, including:

- Domain model design and JPA mapping
- RESTful API implementation with JAX-RS
- Concurrency control strategy to prevent double-bookings
- Integration with a pre-built client-side web application

---
## 🧑‍💻 Personal Contribution
While the project was a team effort, I personally contributed to:
- Designing and implementing parts of the domain model (e.g., Concert, Performer, Seat Classes)
- Developing API endpoints to retrieve concert and performer information
- Participating in team planning, code reviews, and testing

---

## 📝 License
This project was originally developed as part of a university course assignment and has been adapted for public showcase. The front-end web application (concert-client) and the database were provided by the course instructors. Our team implemented the full backend logic in the concert-service module and integrated it with the provided client.

---

