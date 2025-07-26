We discussed together how to implement the functionalities of the Concert Booking Web Service and how the domain model interacts with the database. Below is our initial plan recorded in the GitHub issue:

The core business logic has been outlined and the main features identified. Work was divided accordingly by functional modules.
The first version of the domain model has been completed, but it may be adjusted as needed during feature implementation.

1.	Concert Information Retrieval
      Retrieve Concert and Performer Information
      • Possible endpoints:
      o GET /concerts – Retrieve a list of all concerts
      o GET /performers – Retrieve a list of all performers
2.	Booking Functionality
      (1) Retrieve Seats information
      • Possible endpoint:
      o GET /concerts/{id}/tickets – Retrieve seat availability and pricing for a specific concert
      (2) Booking Seats
      • Possible Endpoint:
      o POST /concerts/{id}/tickets/reservation – Book multiple seats at once
      The booking process must ensure "all-or-nothing" logic (i.e., all requested seats are booked together, or none at all). This may be implemented logic using transaction control in the process of connecting database?
3.	User Account (Authentication and Credentials)
      • Features:
      o User registration and login (may require token/session-based authentication, but the domain model already supports the core structure)
4.	Subscription Notification Mechanism
      • Possible Endpoints:
      o POST /concerts/{id}/watch – Subscribe to a concert date for availability alerts
      o DELETE /concerts/{id}/watch – Unsubscribe from concert notifications
      o GET /users/{id}/watchlist – Retrieve the list of concerts the user is subscribed to

This is a preliminary idea without much detail, and changes may occur during the actual implementation process.

Based on these key features and endpoints, we divided the work as follows:

•	Yifei Wu: Implemented the Concert and Performer Information Retrieval features, along with the corresponding domain classes and database mappings (e.g., Concert, Performer, ConcertDate classes).

•	Ziyun Tan: Ziyun contributed to developing the backend REST API for the concert booking service. She implemented endpoints for querying seat availability, retrieving booking details, and fetching the current user’s booking history. Ziyun also ensured proper security checks using cookies and make the appropriate modifications to the corresponding domain model. Additionally, she handled database interactions with JPA and implemented exception handling to ensure robust and reliable system performance.

•	Anni Xu: Was responsible for User Account and Subscription Notification features, including their domain classes and database interactions (e.g. User class).

To improve concurrency performance, we used ConcurrentHashMap safely manage subscription requests when multiple users subscribe at the same time. Additionally, a synchronised block was used to ensure consistent notifications when booking updates occur. This minimises the chance of concurrency issues.

The domain model includes six main entities: Concert, Performer, ConcertDate, Booking, Seat, and User. Concert and Performer are linked through a many-to-many relationship, while Concert and ConcertDate are modelled using an element collection to store multiple concert dates. Booking contains a one-to-many relationship with Seat, allowing multiple seats to be reserved in a single booking, and is also associated with User and Concert by their IDs.

In the domain model classes, we used Eager fetching for Concert's performers and dates, Booking's seats, and User's bookings, ensuring related data is fully loaded when the parent entity is fetched. For the cascade operations, we apply CascadeType.PERSIST on the relationship between Performer and Concert, allowing concerts to be automatically persisted when a performer is saved.

