<%@ page import="java.util.List,java.util.ArrayList,java.util.Calendar,java.util.Date,java.util.Map,java.util.HashMap,java.text.SimpleDateFormat,com.movieweb.model.Movies,com.movieweb.model.Theaters,com.movieweb.model.Showtimes" %>
<%
    Movies movie = (Movies) request.getAttribute("movie");
    List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
    List<Showtimes> showtimes = (List<Showtimes>) request.getAttribute("showtimes");
    Integer selectedTheaterIdAttr = (Integer) request.getAttribute("selectedTheaterId");
    int selectedTheaterId = selectedTheaterIdAttr != null ? selectedTheaterIdAttr : 0;
    String selectedDate = (String) request.getAttribute("selectedDate");
    if (selectedDate == null || selectedDate.isEmpty()) {
        selectedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
    String contextPath = request.getContextPath();
    SimpleDateFormat dateValueFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dateLabelFormat = new SimpleDateFormat("EEE, dd MMM");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    Calendar calendar = Calendar.getInstance();
    Date today = new Date();
    calendar.setTime(today);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date firstDate = calendar.getTime();
    Calendar selectedCalendar = Calendar.getInstance();
    try {
        selectedCalendar.setTime(dateValueFormat.parse(selectedDate));
    } 
    catch (Exception e) {
        selectedCalendar.setTime(firstDate);
        selectedDate = dateValueFormat.format(firstDate);
    }
    Map<Integer, List<Showtimes>> showtimesByTheater = new HashMap<Integer, List<Showtimes>>();
    if (showtimes != null) {
        for (Showtimes showtime : showtimes) {
            int theaterId = 0;

            // The servlet should provide showtimes for the selected theater.
            // Grouping is unnecessary if the servlet already filters them.
            if (showtime != null) {
                if (!showtimesByTheater.containsKey(selectedTheaterId)) {
                    showtimesByTheater.put(selectedTheaterId, new ArrayList<Showtimes>());
                }
                showtimesByTheater.get(selectedTheaterId).add(showtime);
            }
        }
    }
    Theaters selectedTheater = null;
    if (theaters != null) {
        for (Theaters theater : theaters) {
            if (theater.getTheater_id() == selectedTheaterId) {
                selectedTheater = theater;
                break;
            }
        }
    }
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>
	        Book Tickets<%= movie != null ? " - " + movie.getMovie_name() : "" %>
	        | PhnetPhlyx
	    </title>
	    <link rel="stylesheet" href="<%= contextPath %>/styles/main.css">
	    <link rel="stylesheet" href="<%= contextPath %>/styles/booking_showtimes.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<div class="BookingLayout">
		    <%@ include file="/components/header.jsp" %>
		    <%@ include file="/components/left_sidebar.jsp" %>
		
		    <main class="BookingShowtimes">
		        <div class="booking-page-heading">
		            <a href="<%= contextPath %>/main.jsp" class="back-link">
		                &larr; Home
		            </a>
		            <h1>Book Tickets</h1>
		            <p>Select a theater, date, and showtime for your movie.</p>
		        </div>
		
		        <%
		            if (movie == null) {
		        %>
				        <div class="booking-message">
				            Movie information could not be loaded. Please return to the
				            movie page and try again.
				        </div>
		        <%
		            } 
		            else {
		        %>
				        <section class="booking-movie-summary">
				            <div class="booking-movie-poster">
				                <%
				                    if (movie.getPoster_path() != null && !movie.getPoster_path().isEmpty()) {
				                %>
						                <img src="<%= contextPath %>/<%= movie.getPoster_path() %>" alt="<%= movie.getMovie_name() %>">
				                <%
				                    } 
				                    else {
				                %>
						                <div class="no-image">No Poster</div>
				                <%
				                    }
				                %>
				            </div>
				
				            <div class="booking-movie-info">
				                <h2><%= movie.getMovie_name() %></h2>
				                <p>
				                    <strong>Duration:</strong>
				                    <%= movie.getDuration_minute() %> minutes
				                </p>
				                <p>
				                    <strong>Age rating:</strong>
				                    <%= movie.getAge_rating() != null ? movie.getAge_rating() : "Not specified" %>
				                </p>
				            </div>
				        </section>
				
				        <section class="booking-selection-section">
				            <h2>1. Choose a Theater</h2>
				
				            <%
				                if (theaters == null || theaters.isEmpty()) {
				            %>
						            <div class="booking-message">
						                No theaters currently have scheduled showtimes for
						                this movie.
						            </div>
				            <%
				                } 
				                else {
				            %>
						            <form action="<%= contextPath %>/booking-showtimes" method="get" class="theater-selection-form">
						                <input type="hidden" name="movie_id" value="<%= movie.getMovie_id() %>">
						                <input type="hidden" name="date" value="<%= selectedDate %>">
						
						                <label for="theaterId">Theater</label>
						                <select id="theaterId" name="theater_id" onchange="this.form.submit()" required>
						                    <option value="">Select a theater</option>
						                    <%
						                        for (Theaters theater : theaters) {
						                    %>
								                    <option value="<%= theater.getTheater_id() %>" <%= theater.getTheater_id() == selectedTheaterId ? "selected" : "" %>>
								                        <%= theater.getTheater_name() %>
								                    </option>
						                    <%
						                        }
						                    %>
						                </select>
						            </form>
				            <%
				                }
				            %>
				        </section>
				
				        <%
				            if (selectedTheaterId > 0 && selectedTheater != null) {
				        %>
						        <section class="booking-selection-section" id="booking-schedule">
						            <h2>2. Choose a Date</h2>
						
						            <div class="booking-date-selector">
						                <%
						                    for (int i = 0; i < 7; i++) {
						                        Calendar dateCalendar = Calendar.getInstance();
						                        dateCalendar.setTime(firstDate);
						                        dateCalendar.add(Calendar.DATE, i);
						
						                        Date date = dateCalendar.getTime();
						                        String dateValue = dateValueFormat.format(date);
						                        String dateLabel = dateLabelFormat.format(date);
						                        boolean isSelected = dateValue.equals(selectedDate);
						                %>
								                <a class="booking-date <%= isSelected ? "selected" : "" %>" href="<%= contextPath %>/booking-showtimes?movie_id=<%= movie.getMovie_id() %>&theater_id=<%= selectedTheaterId %>&date=<%= dateValue %>#booking-schedule">
								                    <span><%= i == 0 ? "Today" : dateLabel.split(",")[0] %></span>
								                    <strong><%= new SimpleDateFormat("dd").format(date) %></strong>
								                    <small><%= new SimpleDateFormat("MMM").format(date) %></small>
								                </a>
						                <%
						                    }
						                %>
						            </div>
						
						            <div class="selected-theater-heading">
						                <h2>3. Choose a Showtime</h2>
						                <p><%= selectedTheater.getTheater_name() %></p>
						            </div>
						
						            <%
						                if (showtimes == null || showtimes.isEmpty()) {
						            %>
								            <div class="booking-message">
								                No showtimes are available for this movie at this
								                theater on the selected date.
								            </div>
						            <%
						                } 
						                else {
						            %>
								            <div class="booking-showtime-list">
								                <%
								                    for (Showtimes showtime : showtimes) {
								                        if (showtime == null || showtime.getStart_at() == null) {
								                            continue;
								                        }
								                %>
										                <div class="booking-showtime-card">
										                    <div class="showtime-time">
										                        <strong>
										                            <%= timeFormat.format(showtime.getStart_at()) %>
										                        </strong>
										                        <span>
										                            <%= showtime.getEnd_at() != null ? timeFormat.format(showtime.getEnd_at()) : "" %>
										                        </span>
										                    </div>
										
										                    <div class="showtime-status">
										                        <span class="status-dot"></span>
										                        <span>
															        <%= showtime.getRoom_name() != null
															            ? showtime.getRoom_name()
															            : "Unknown Room" %>
															        ·
															        <%= showtime.getRoom_type_name() != null
															            ? showtime.getRoom_type_name()
															            : "Unknown Type" %>
															    </span>
										                    </div>
										
										                    <form action="<%= contextPath %>/seat_selection" method="get">
										                        <input type="hidden" name="showtime_id" value="<%= showtime.getShowtime_id() %>">
										                        <input type="hidden" name="movie_id" value="<%= movie.getMovie_id() %>">
										
										                        <button type="submit" class="choose-showtime-button">
										                            Choose Seats
										                        </button>
										                    </form>
										                </div>
								                <%
								                    }
								                %>
								            </div>
						            <%
						                }
						            %>
						        </section>
				        <%
				            }
				        %>
		        <%
		            }
		        %>
		    </main>
		
		    <%-- Booking page does not need the right sidebar ads. --%>
		    <%@ include file="/components/footer.jsp" %>
		</div>
	</body>
</html>