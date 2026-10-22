<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.ManagerStatistics" %>
<%@ page import="com.movieweb.model.ManagerMovieStatistic" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	
	request.setAttribute("currentManagerPage", "dashboard");
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
	Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
	String selectedTheaterId = currentTheaterId == null ? "" : String.valueOf(currentTheaterId);
	Number todayBookingsValue = (Number) request.getAttribute("todayBookings");
	Number todayRevenueValue = (Number) request.getAttribute("todayRevenue");
	Number upcomingShowtimeCountValue = (Number) request.getAttribute("upcomingShowtimeCount");
	Number activeRoomCountValue = (Number) request.getAttribute("activeRoomCount");
	int todayBookings = todayBookingsValue == null ? 0 : todayBookingsValue.intValue();
	long todayRevenue = todayRevenueValue == null ? 0L : todayRevenueValue.longValue();
	int upcomingShowtimeCount = upcomingShowtimeCountValue == null ? 0 : upcomingShowtimeCountValue.intValue();
	int activeRoomCount = activeRoomCountValue == null ? 0 : activeRoomCountValue.intValue();
	List<Map<String, Object>> dashboardShowtimes = (List<Map<String, Object>>) request.getAttribute("dashboardShowtimes");
	List<Map<String, Object>> recentBookings = (List<Map<String, Object>>) request.getAttribute("recentBookings");
	List<Map<String, Object>> dashboardRooms = (List<Map<String, Object>>) request.getAttribute("dashboardRooms");
	if (dashboardShowtimes == null) {
		dashboardShowtimes = Collections.emptyList();
	}
	if (recentBookings == null) {
		recentBookings = Collections.emptyList();
	}
	if (dashboardRooms == null) {
		dashboardRooms = Collections.emptyList();
	}
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
	boolean hasTheater = currentTheaterId != null;
	ManagerStatistics dashboardStatistics = (ManagerStatistics) request.getAttribute("dashboardStatistics");
    ManagerMovieStatistic dashboardTopMovie = (ManagerMovieStatistic) request.getAttribute("dashboardTopMovie");
    boolean dashboardStatisticsAvailable = Boolean.TRUE.equals(request.getAttribute("dashboardStatisticsAvailable"));
    if (dashboardStatistics == null) {
        dashboardStatistics = new ManagerStatistics();
    }
%>
<%!
	private String escapeDashboardHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
	/* CSS class for a status badge, e.g. "CONFIRMED" -> "manager-status-confirmed".
	 * Only letters, digits, "_" and "-" are kept, so the value is safe inside class="". */
	private String statusClass(Object value) {
		if (value == null) {
			return "manager-status-unknown";
		}
		String clean = String.valueOf(value).toLowerCase().replaceAll("[^a-z0-9_-]", "");
		return clean.isEmpty() ? "manager-status-unknown" : "manager-status-" + clean;
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Manager Dashboard | PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_manager.css">
	 	<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_statistics.css">
	 	<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout2">
			<jsp:include page="/components/header_manager.jsp" />
			<%@ include file="components/manager_navigation.jsp" %>
			<main class="ManagerMain">
				<!-- Dashboard heading and theater selector -->
				<div class="manager-header">
					<div class="manager-header-info">
						<h2>Manager Dashboard</h2>
						<p>
							Welcome back, 
							<strong><%= escapeDashboardHtml(currentUser.getFullName()) %></strong>.
						</p>
					</div>
					<%
						if (theaters != null && !theaters.isEmpty()) {
					%>
							<form class="manager-theater-filter" method="get" action="${pageContext.request.contextPath}/manager-theater">
								<label for="theater-filter">Theater</label>
								<select id="theater-filter" name="theaterId" onchange="this.form.submit()">
									<%
										for (Theaters theater : theaters) {
									%>
											<option value="<%= theater.getTheater_id() %>" <%= String.valueOf(theater.getTheater_id()).equals(selectedTheaterId) ? "selected" : "" %>>
												<%= escapeDashboardHtml(theater.getTheater_name()) %>
											</option>
									<%
										}
									%>
								</select>
							</form>
					<%
						}
					%>
				</div>
				<%
					if (!hasTheater) {
				%>
						<section class="manager-panel">
							<div class="manager-panel-header">
								<h3>No Theater Available</h3>
							</div>
							<div class="manager-panel-content">
								<p>
									You currently have no active theater assigned 
									to your manager account. Please contact an 
									administrator for assistance.
								</p>
							</div>
						</section>
				<%
					} else {
				%>
						<!-- Summary cards (each card has its own color class) -->
						<section class="manager-summary">
							<div class="manager-summary-card manager-summary-card-bookings">
								<h3>Today's Bookings</h3>
								<p><%= todayBookings %></p>
								<span>Confirmed bookings made today</span>
							</div>
							<div class="manager-summary-card manager-summary-card-revenue">
								<h3>Today's Revenue</h3>
								<p><%= currencyFormat.format(todayRevenue) %></p>
								<span>Successful payments today (VND)</span>
							</div>
							<div class="manager-summary-card manager-summary-card-showtimes">
								<h3>Upcoming Showtimes</h3>
								<p><%= upcomingShowtimeCount %></p>
								<span>Remaining showtimes today</span>
							</div>
							<div class="manager-summary-card manager-summary-card-rooms">
								<h3>Active Rooms</h3>
								<p><%= activeRoomCount %></p>
								<span>Available rooms in this theater</span>
							</div>
						</section>
						<!-- Upcoming showtimes -->
						<section class="manager-panel manager-panel-showtimes">
							<div class="manager-panel-header">
								<h3>Upcoming Showtimes</h3>
								<a href="${pageContext.request.contextPath}/manager-showtimes">
									View All
								</a>
							</div>
							<div class="manager-panel-content">
								<%
									if (dashboardShowtimes.isEmpty()) {
								%>
										<p>
											There are no upcoming showtimes for today.
										</p>
								<%
									} else {
								%>
										<div class="manager-table-wrapper">
											<table class="manager-data-table">
												<thead>
													<tr>
														<th>Movie</th>
														<th>Room</th>
														<th>Start</th>
														<th>End</th>
														<th>Status</th>
													</tr>
												</thead>
												<tbody>
													<%
														for (Map<String, Object> showtime : dashboardShowtimes) {
															Object startAt = showtime.get("startAt");
															Object endAt = showtime.get("endAt");
															String startText = startAt instanceof java.util.Date
																	? timeFormat.format((java.util.Date) startAt)
																	: "--";
															String endText = endAt instanceof java.util.Date
																	? timeFormat.format((java.util.Date) endAt)
																	: "--";
													%>
															<tr>
																<td>
																	<%= showtime.get("movieName") == null
																			? "Unknown movie"
																			: escapeDashboardHtml(String.valueOf(showtime.get("movieName"))) %>
																</td>
																<td>
																	<%= showtime.get("roomName") == null
																			? "Unknown room"
																			: escapeDashboardHtml(String.valueOf(showtime.get("roomName"))) %>
																</td>
																<td><%= startText %></td>
																<td><%= endText %></td>
																<td>
																	<span class="manager-status <%= statusClass(showtime.get("status")) %>">
																		<%= showtime.get("status") == null
																				? "Unknown"
																				: escapeDashboardHtml(String.valueOf(showtime.get("status"))) %>
																	</span>
																</td>
															</tr>
													<%
														}
													%>
												</tbody>
											</table>
										</div>
								<%
									}
								%>
							</div>
						</section>
						<!-- Recent bookings: includes customer and POS bookings -->
						<section class="manager-panel manager-panel-bookings">
							<div class="manager-panel-header">
								<h3>Recent Bookings</h3>
								<a href="${pageContext.request.contextPath}/sales-bookings">
									View All
								</a>
							</div>
							<div class="manager-panel-content">
								<%
									if (recentBookings.isEmpty()) {
								%>
										<p>No bookings have been recorded for this theater yet.</p>
								<%
									} else {
								%>
										<div class="manager-table-wrapper">
											<table class="manager-data-table">
												<thead>
													<tr>
														<th>Booking</th>
														<th>Movie</th>
														<th>Customer / Staff</th>
														<th>Type</th>
														<th>Booked At</th>
														<th>Booking Status</th>
														<th>Payment</th>
													</tr>
												</thead>
												<tbody>
													<%
														for (Map<String, Object> booking : recentBookings) {
															Object bookAt = booking.get("bookAt");
															String bookAtText = bookAt instanceof java.util.Date
																	? dateTimeFormat.format((java.util.Date) bookAt)
																	: "--";
													%>
															<tr>
																<td>
																	<strong>
																		#<%= booking.get("bookingId") == null
																				? "--"
																				: booking.get("bookingId") %>
																	</strong>
																	<br>
																	<span class="manager-muted">
																		<%= booking.get("transactionCode") == null
																				? "No transaction code"
																				: escapeDashboardHtml(String.valueOf(booking.get("transactionCode"))) %>
																	</span>
																</td>
																<td>
																	<%= booking.get("movieName") == null
																			? "Unknown movie"
																			: escapeDashboardHtml(String.valueOf(booking.get("movieName"))) %>
																</td>
																<td>
																	<%= booking.get("customerName") == null
																			? "Unknown"
																			: escapeDashboardHtml(String.valueOf(booking.get("customerName"))) %>
																</td>
																<td>
																	<%= booking.get("bookingSource") == null
																			? "Unknown"
																			: escapeDashboardHtml(String.valueOf(booking.get("bookingSource"))) %>
																</td>
																<td><%= bookAtText %></td>
																<td>
																	<span class="manager-status <%= statusClass(booking.get("bookingStatus")) %>">
																		<%= booking.get("bookingStatus") == null
																				? "Unknown"
																				: escapeDashboardHtml(String.valueOf(booking.get("bookingStatus"))) %>
																	</span>
																</td>
																<td>
																	<span class="manager-status <%= statusClass(booking.get("paymentStatus") == null ? "unpaid" : booking.get("paymentStatus")) %>">
																		<%= booking.get("paymentStatus") == null
																				? "Unpaid"
																				: escapeDashboardHtml(String.valueOf(booking.get("paymentStatus"))) %>
																	</span>
																</td>
															</tr>
													<%
														}
													%>
												</tbody>
											</table>
										</div>
								<%
									}
								%>
							</div>
						</section>
						<!-- Active rooms preview -->
						<section class="manager-panel manager-panel-rooms">
							<div class="manager-panel-header">
								<h3>Active Rooms</h3>
								<a href="${pageContext.request.contextPath}/manager-rooms">
									Manage Rooms
								</a>
							</div>
							<div class="manager-panel-content">
								<%
									if (dashboardRooms.isEmpty()) {
								%>
										<p>No active rooms were found for this theater.</p>
								<%
									} else {
								%>
										<div class="manager-table-wrapper">
											<table class="manager-data-table">
												<thead>
													<tr>
														<th>Room</th>
														<th>Active Seats</th>
													</tr>
												</thead>
												<tbody>
													<%
														for (Map<String, Object> room : dashboardRooms) {
													%>
															<tr>
																<td>
																	<%= room.get("roomName") == null
																			? "Unnamed room"
																			: escapeDashboardHtml(String.valueOf(room.get("roomName"))) %>
																</td>
																<td>
																	<%= room.get("seatCount") == null
																			? 0
																			: room.get("seatCount") %>
																</td>
															</tr>
													<%
														}
													%>
												</tbody>
											</table>
										</div>
								<%
									}
								%>
							</div>
						</section>
						<!-- Keep the Statistics panel in place -->
						<section class="manager-panel manager-panel-statistics">
							<div class="manager-panel-header">
								<h3>Statistics</h3>
								<a href="${pageContext.request.contextPath}/manager-statistics">
									View Statistics
								</a>
							</div>
							<div class="manager-panel-content">
								<%
									if (!dashboardStatisticsAvailable) {
								%>
										<p>
											Statistics are temporarily unavailable.
											Please try again later.
										</p>
								<%
									} else {
								%>
										<p class="dashboard-statistics-period">
											Last 7 days, including today · Current theater only
										</p>
										<section class="statistics-kpis" aria-label="Statistics summary">
											<article class="statistics-kpi">
												<div class="kpi-icon">T</div>
												<p>Tickets Sold</p>
												<strong>
													<%= String.format("%,d", dashboardStatistics.getTicketsSold()) %>
												</strong>
												<span>Confirmed bookings by booking date</span>
											</article>
											<article class="statistics-kpi">
												<div class="kpi-icon">₫</div>
												<p>7-Day Revenue</p>
												<strong>
													<%= String.format("%,d", dashboardStatistics.getGrossRevenue()) %>
													<small>VND</small>
												</strong>
												<span>Successful payments by payment date</span>
											</article>
											<article class="statistics-kpi">
												<div class="kpi-icon">A</div>
												<p>Average Paid Booking</p>
												<strong>
													<%= String.format("%,d", dashboardStatistics.getAveragePaidBookingValue()) %>
													<small>VND</small>
												</strong>
												<span>Revenue ÷ successful payments</span>
											</article>
											<article class="statistics-kpi">
												<div class="kpi-icon">★</div>
												<p>Top Movie</p>
												<%
													if (dashboardTopMovie != null) {
												%>
														<strong class="dashboard-top-movie-name">
															<%= escapeDashboardHtml(dashboardTopMovie.getMovieName()) %>
														</strong>
														<span>
															<%= String.format("%,d", dashboardTopMovie.getTicketsSold()) %>
															tickets sold
														</span>
												<%
													} 
													else {
												%>
														<strong class="dashboard-top-movie-name">
															No data yet
														</strong>
														<span>No successful ticket sales in this period</span>
												<%
													}
												%>
											</article>
										</section>
								<%
									}
								%>
							</div>
						</section>
				<%
					}
				%>
			</main>
		</section>
	</body>
</html>
