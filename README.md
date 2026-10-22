# PhnetPhlyx - Movie Ticket Booking Website

A movie ticket booking website with four modes: **Guest**, **User**, **Manager** and **Admin**.
Users find movies and theaters, pick seats on a live seat map, and book and pay for tickets
(payment is only simulated). Managers run theaters and sell tickets at the counter. Admins manage
the whole system.

**Team:** Hưng, Nhân, Phú, Nguyên

## Features

| Mode | What it can do |
|---|---|
| **Guest** | Browse and search movies (name, cast, genre, tag, theater, price, rating, age rating), movie details, browse theaters and showtimes, "theater near me", register |
| **User** | Log in (e-mail / phone / @name), remember me, OTP e-mail codes, profile with avatar and banner, change password, Light / Dark theme, favourites (public or private), ratings, comments, reports, booking with live price, promotion codes, pay with saved VISA cards, booking history |
| **Manager** | Dashboard and statistics, rooms and seats, showtimes, promotions, counter (POS) sales with cash / QR / VISA, refund requests, request a theater, **Budget Planner** (create many showtimes from a budget) and Planner History, **Request a Movie** (e-mail to the admin) |
| **Admin** | Dashboard and statistics, users and managers, theater requests, theaters, refunds, reported comments, ads, promotions, movie editor (poster, cast, crew), genres and tags |

## Technologies

- Java Servlet / JSP (`javax.servlet`), no framework
- Apache Tomcat 9
- Microsoft SQL Server, plain JDBC (`PreparedStatement`)
- JavaMail (Gmail SMTP) for OTP codes, refund notices and movie requests
- HTML, CSS (CSS variables for the Light / Dark theme), JavaScript (`fetch`)

Layers: **JSP -> Servlet -> Service -> DAO -> SQL Server -> Model -> JSP**, with filters for
auto-login and for the manager and admin pages.

## Project structure

```
src/main/java/com/movieweb/
    DAO/         all SQL (one DAO per table or feature)
    model/       plain classes, one field per column
    service/     business rules (pricing, OTP, e-mail, image checks, planner)
    servlet/     request handling
    filter/      AutoLogin, manager and admin access
src/main/webapp/
    *.jsp        pages
    components/  reusable JSP parts (header, navigation, theme)
    styles/      CSS
    scripts/     JavaScript
    pictures/    images
database.sql     database creation script (schema and sample data)
```

## How to run

### 1. Requirements
- JDK 8 or newer
- Apache Tomcat 9
- Microsoft SQL Server (with SQL Server Management Studio)
- Eclipse IDE for Enterprise Java (or any IDE that can run a Tomcat 9 web project)

### 2. Create the database
1. Open `database.sql` in SQL Server Management Studio.
2. Create a database (for example `MovieWeb`), select it, and run the script.

### 3. Add the two configuration files
For security, the files that contain passwords are **not** in this repository. Two templates are
included instead. In the same folder as each template:

1. Copy `DBConnection.java.example` to `DBConnection.java` and fill in your SQL Server address,
   database name, user and password.
2. Copy `EmailService.java.example` to `EmailService.java` and fill in your Gmail address and a
   Gmail **app password** (Google account -> Security -> App passwords). Without this the site
   works, but the e-mail codes (register, forgot password), refund e-mails and movie requests
   are not sent.

### 4. Run
1. Import the project into Eclipse (File -> Import -> Existing Projects into Workspace).
2. Make sure the SQL Server JDBC driver and the JavaMail jar are in `WEB-INF/lib` (they are in the
   project) and that TCP/IP is enabled for SQL Server.
3. Right-click the project -> Run As -> Run on Server -> Tomcat 9.
4. Open `http://localhost:8080/<project-name>/` in a browser.

## Test accounts

| Mode | Username | Password |
|---|---|---|
| Guest | (no login) | |
| User | `[username]` | `[password]` |
| Manager | `[username]` | `[password]` |
| Admin | `[username]` | `[password]` |

## Notes

- Payments are **simulated**: no bank or card network is called.
- Nothing important is deleted from the database: rows use `deleted_at` (soft delete) and
  `isActive`.
- A new booking is PENDING and holds its seats for 15 minutes; it is cancelled if not paid.
