<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.sql.Time" %>
<%@ page import="com.movieweb.model.Movies" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.Showtimes" %>
<%
	String contextPath = request.getContextPath();
	List<Movies> movies = (List<Movies>) request.getAttribute("movies");
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
	List<Showtimes> showtimes = (List<Showtimes>) request.getAttribute("showtimes");
	Integer selectedMovieId = (Integer) request.getAttribute("selectedMovieId");
	Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
	String selectedDate = (String) request.getAttribute("selectedDate");
	LocalDate today = LocalDate.now();
	LocalDate maxDate = today.plusDays(6);
	if (selectedDate == null || selectedDate.trim().isEmpty()) {
		selectedDate = today.toString();
	}
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	/* Find the manager's currently selected theater.
	 * The filter already validated that this theater belongs to the manager.
	 */
	Theaters currentTheater = null;
	if (currentTheaterId != null && theaters != null) {
		for (Theaters theater : theaters) {
			if (theater.getTheater_id() == currentTheaterId) {
				currentTheater = theater;
				break;
			}
		}
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta
			name="viewport"
			content="width=device-width, initial-scale=1.0">
		<title>Create Booking - Sales & Bookings</title>
		<link
			rel="stylesheet"
			href="<%= contextPath %>/styles/main_manager.css">
		<link
			rel="stylesheet"
			href="<%= contextPath %>/styles/pos_create_booking.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<div class="create-booking-page">
			<div class="create-booking-container">
				<!-- Header -->
				<div class="create-booking-header">
					<div>
						<div class="breadcrumb">
							Sales &amp; Bookings
							<span>/</span>
							Create Booking
						</div>
						<h1 class="page-title">
							Create Booking
						</h1>
					</div>
					<div class="pos-badge">
						POS
					</div>
				</div>
				<!-- Show Selection -->
				<section class="booking-section">
					<div class="section-header">
						<div class="section-number">
							01
						</div>
						<div>
							<h2 class="section-title">
								Show Selection
							</h2>
							<p class="section-description">
								Select the movie and date.
							</p>
						</div>
					</div>
					<div class="form-grid">
						<!-- Movie -->
						<div class="form-group">
							<label
								class="form-label"
								for="movieSelect">
								Movie
								<span class="required">*</span>
							</label>
							<select
								id="movieSelect"
								class="form-select">
								<option value="">
									-- Select Movie --
								</option>
								<%
									if (movies != null && !movies.isEmpty()) {
										for (Movies movie : movies) {
											boolean selected = selectedMovieId != null && selectedMovieId.intValue() == movie.getMovie_id();
								%>
											<option
												value="<%= movie.getMovie_id() %>"
												<%= selected ? "selected" : "" %>>
												<%= movie.getMovie_name() %>
											</option>
								<%
										}
									}
								%>
							</select>
						</div>
						<!-- Current Theater -->
						<div class="form-group">
							<label class="form-label">
								Theater
								<span class="required">*</span>
							</label>
							<%
								if (currentTheater != null) {
							%>
									<div class="selected-theater">
										<span class="selected-theater-name">
											<%= currentTheater.getTheater_name() %>
										</span>
									</div>
							<%
								} 
								else {
							%>
									<div class="select-disabled">
										<span>
											No theater selected
										</span>
									</div>
							<%
								}
							%>
						</div>
						<!-- Date -->
						<div class="form-group form-group-date">
							<label
								class="form-label"
								for="dateSelect">
								Date
								<span class="required">*</span>
							</label>
							<input
								type="date"
								id="dateSelect"
								class="form-date"
								value="<%= selectedDate %>"
								min="<%= today.format(dateFormatter) %>"
								max="<%= maxDate.format(dateFormatter) %>">
							<span class="field-hint">
								Available for the next 7 days
							</span>
						</div>
					</div>
				</section>
				<!-- Showtime -->
				<section class="booking-section">
					<div class="section-header">
						<div class="section-number">
							02
						</div>
						<div>
							<h2 class="section-title">
								Showtime
							</h2>
							<p class="section-description">
								Select one available showtime.
							</p>
						</div>
					</div>
					<%
						if (selectedMovieId == null) {
					%>
							<div class="empty-state">
								<div class="empty-icon">
									—
								</div>
								<div>
									<strong>
										Select a movie first
									</strong>
									<p>
										Available showtimes will appear here.
									</p>
								</div>
							</div>
					<%
						} 
						else if (currentTheaterId == null) {
					%>
							<div class="empty-state">
								<div class="empty-icon">
									—
								</div>
								<div>
									<strong>
										No theater selected
									</strong>
									<p>
										Please select a theater from the manager navigation.
									</p>
								</div>
							</div>
					<%
						} 
						else if (showtimes == null || showtimes.isEmpty()) {
					%>
							<div class="empty-state">
								<div class="empty-icon">
									—
								</div>
								<div>
									<strong>
										No showtimes available
									</strong>
									<p>
										Try another date or movie.
									</p>
								</div>
							</div>
					<%
						} 
						else {
					%>
							<div class="showtime-grid">

							    <%
							        for (Showtimes showtime : showtimes) {					
							            String showtimeText = "";							
							            if (showtime.getStart_at() != null) {							
							                String startAt = showtime.getStart_at().toString();					
							                int spaceIndex = startAt.indexOf(" ");
							                if (spaceIndex >= 0 && startAt.length() >= spaceIndex + 6) {
							                    showtimeText = startAt.substring(spaceIndex + 1, spaceIndex + 6);
							                }
							            }
							    %>
								        <label class="showtime-option">							
								            <input
								                type="radio"
								                name="showtime_id"
								                value="<%= showtime.getShowtime_id() %>">							
								            <span class="showtime-card">						
								                <span class="showtime-time">
								                    <%= showtimeText %>
								                </span>							
								                <span class="showtime-label">
												    <%= showtime.getRoom_name() != null
												        ? showtime.getRoom_name()
												        : "Unknown Room" %>
												    ·
												    <%= showtime.getRoom_type_name() != null
												        ? showtime.getRoom_type_name()
												        : "Unknown Type" %>
												</span>					
								            </span>						
								        </label>					
							    <%
							        }
							    %>
							</div>
					<%
						}
					%>
				</section>
				<!-- Ticket Type -->
				<section class="booking-section">
					<div class="section-header">
						<div class="section-number">
							03
						</div>
						<div>
							<h2 class="section-title">
								Ticket Type
							</h2>
							<p class="section-description">
								Select the ticket type for this booking.
							</p>
						</div>
					</div>
					<div class="ticket-type-grid">
						<!-- Normal -->
						<label class="ticket-type-option">
							<input
								type="radio"
								name="ticket_type_id"
								value="1"
								checked>
							<span class="ticket-type-card">
								<span class="ticket-icon">
									N
								</span>
								<span class="ticket-content">
									<span class="ticket-name">
										NORMAL
									</span>
									<span class="ticket-discount">
										Standard price
									</span>
								</span>
							</span>
						</label>
						<!-- Children -->
						<label class="ticket-type-option">
							<input
								type="radio"
								name="ticket_type_id"
								value="2">
							<span class="ticket-type-card">
								<span class="ticket-icon">
									C
								</span>
								<span class="ticket-content">
									<span class="ticket-name">
										CHILDREN
									</span>
									<span class="ticket-discount">
										20% discount
									</span>
								</span>
							</span>
						</label>
						<!-- Student -->
						<label class="ticket-type-option">
							<input
								type="radio"
								name="ticket_type_id"
								value="3">
							<span class="ticket-type-card">
								<span class="ticket-icon">
									S
								</span>
								<span class="ticket-content">
									<span class="ticket-name">
										STUDENT
									</span>
									<span class="ticket-discount">
										20% discount
									</span>
								</span>
							</span>
						</label>
						<!-- Elder -->
						<label class="ticket-type-option">
							<input
								type="radio"
								name="ticket_type_id"
								value="4">
							<span class="ticket-type-card">
								<span class="ticket-icon">
									E
								</span>
								<span class="ticket-content">
									<span class="ticket-name">
										ELDER
									</span>
									<span class="ticket-discount">
										20% discount
									</span>
								</span>
							</span>
						</label>
					</div>
				</section>
				<!-- Actions -->
				<div class="action-area">
					<div class="action-left">
						<a
							href="<%= contextPath %>/sales-bookings"
							class="btn btn-back">
							← Back
						</a>
					</div>
					<div class="action-right">
						<button
							type="button"
							class="btn btn-continue"
							id="continueBookingButton">
							Continue →
						</button>
					</div>
				</div>
			</div>
		</div>
		<script>
		    const contextPath = "<%= contextPath %>";
		</script>
		<script
			src="<%= contextPath %>/scripts/pos_create_booking.js">
		</script>
	</body>
</html>