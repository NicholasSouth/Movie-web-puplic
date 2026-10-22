<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.movieweb.model.ManagerStatistics" %>
<%@ page import="com.movieweb.model.ManagerDailyStatistic" %>
<%@ page import="com.movieweb.model.ManagerMovieStatistic" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%
	request.setAttribute("currentManagerPage", "statistics");
	Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
	ManagerStatistics statistics = (ManagerStatistics) request.getAttribute("statistics");
	List<ManagerDailyStatistic> dailyStatistics = (List<ManagerDailyStatistic>) request.getAttribute("dailyStatistics");
	List<ManagerMovieStatistic> topMovies = (List<ManagerMovieStatistic>) request.getAttribute("topMovies");
	String startDate = (String) request.getAttribute("startDate");
	String endDate = (String) request.getAttribute("endDate");
	String errorMessage = (String) request.getAttribute("errorMessage");
	if (statistics == null) {
		statistics = new ManagerStatistics();
	}
	if (dailyStatistics == null) {
		dailyStatistics = new ArrayList<ManagerDailyStatistic>();
	}
	if (topMovies == null) {
		topMovies = new ArrayList<ManagerMovieStatistic>();
	}
	if (startDate == null) {
		startDate = LocalDate.now().minusDays(6).toString();
	}
	if (endDate == null) {
		endDate = LocalDate.now().toString();
	}
	String currentTheaterName = null;
	if (theaters != null && currentTheaterId != null) {
		for (Theaters theater : theaters) {
			if (theater.getTheater_id() == currentTheaterId.intValue()) {
				currentTheaterName = theater.getTheater_name();
				break;
			}
		}
	}
	DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("dd MMM yyyy");
	String contextPath = request.getContextPath();
%>
<%!
	private String escapeStatisticsHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Statistics | PhnetPhlyx Manager</title>
		<link rel="stylesheet" href="<%= contextPath %>/styles/main_manager.css">
		<link rel="stylesheet" href="<%= contextPath %>/styles/manager_statistics.css">
		<script defer src="<%= contextPath %>/scripts/manager_statistics.js"></script>
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body class="Layout2">
		<%@ include file="components/header_manager.jsp" %>
		<%@ include file="components/manager_navigation.jsp" %>
		<main class="ManagerMain">
			<section class="statistics-container">
				<div class="statistics-header">
					<div>
						<p class="statistics-breadcrumb">Manage Theaters / Statistics</p>
						<h1>Theater Statistics</h1>
						<p class="statistics-subtitle">Review bookings, ticket sales, and successful payment revenue.</p>
					</div>
					<%
						if (currentTheaterName != null) {
					%>
							<div class="statistics-theater">
								<span>Current theater</span>
								<strong><%= escapeStatisticsHtml(currentTheaterName) %></strong>
							</div>
					<%
						}
					%>
				</div>
				<%
					if (errorMessage != null && !errorMessage.trim().isEmpty()) {
				%>
						<div class="statistics-alert statistics-alert-error"><%= escapeStatisticsHtml(errorMessage) %></div>
				<%
					}
					if (currentTheaterId == null) {
				%>
						<div class="statistics-empty">
							<h2>No theater selected</h2>
							<p>You do not currently have a theater selected. Choose one from the manager theater selector to view statistics.</p>
						</div>
				<%
					} else {
				%>
						<form class="statistics-filter" method="get" action="<%= contextPath %>/manager-statistics">
							<div class="statistics-filter-copy">
								<strong>Report period</strong>
								<span>Choose an inclusive date range (maximum 366 days).</span>
							</div>
							<label>
								<span>From</span>
								<input type="date" name="startDate" value="<%= startDate %>" required>
							</label>
							<label>
								<span>To</span>
								<input type="date" name="endDate" value="<%= endDate %>" required>
							</label>
							<button type="submit" class="statistics-button">Apply filter</button>
							<a class="statistics-reset" href="<%= contextPath %>/manager-statistics">Last 7 days</a>
						</form>
						<div class="statistics-period-note">
							<%= LocalDate.parse(startDate).format(displayDate) %> – <%= LocalDate.parse(endDate).format(displayDate) %>
							<span>·</span> Current theater only
						</div>
						<section class="statistics-kpis" aria-label="Statistics summary">
							<article class="statistics-kpi">
								<div class="kpi-icon">B</div>
								<p>Confirmed bookings</p>
								<strong><%= String.format("%,d", statistics.getConfirmedBookings()) %></strong>
								<span>Based on booking date</span>
							</article>
							<article class="statistics-kpi">
								<div class="kpi-icon">₫</div>
								<p>Successful payment revenue</p>
								<strong><%= String.format("%,d", statistics.getGrossRevenue()) %> <small>VND</small></strong>
								<span>Based on payment date</span>
							</article>
							<article class="statistics-kpi">
								<div class="kpi-icon">T</div>
								<p>Tickets sold</p>
								<strong><%= String.format("%,d", statistics.getTicketsSold()) %></strong>
								<span>Seats in confirmed bookings</span>
							</article>
							<article class="statistics-kpi">
								<div class="kpi-icon">A</div>
								<p>Average paid booking</p>
								<strong><%= String.format("%,d", statistics.getAveragePaidBookingValue()) %> <small>VND</small></strong>
								<span>Revenue ÷ successful payments</span>
							</article>
						</section>
						<div class="statistics-chart-grid">
							<section class="statistics-panel">
								<div class="statistics-panel-heading">
									<div>
										<h2>Daily revenue</h2>
										<p>Successful payments by payment date</p>
									</div>
								</div>
								<div class="statistics-chart-wrap">
									<canvas id="revenueChart" role="img" aria-label="Daily revenue chart"></canvas>
									<p class="statistics-chart-empty" id="revenueChartEmpty" hidden>No revenue data for this period.</p>
								</div>
							</section>
							<section class="statistics-panel">
								<div class="statistics-panel-heading">
									<div>
										<h2>Booking trend</h2>
										<p>Confirmed bookings by booking date</p>
									</div>
								</div>
								<div class="statistics-chart-wrap">
									<canvas id="bookingChart" role="img" aria-label="Daily confirmed bookings chart"></canvas>
									<p class="statistics-chart-empty" id="bookingChartEmpty" hidden>No confirmed bookings for this period.</p>
								</div>
							</section>
						</div>
						<section class="statistics-panel statistics-movies-panel">
							<div class="statistics-panel-heading">
								<div>
									<h2>Top movies</h2>
									<p>Ranked by successful payment revenue in the selected period</p>
								</div>
							</div>
							<div class="statistics-table-wrap">
								<table class="statistics-table">
									<thead>
										<tr>
											<th>#</th>
											<th>Movie</th>
											<th>Tickets</th>
											<th>Successful payment revenue</th>
										</tr>
									</thead>
									<tbody>
										<%
											if (topMovies.isEmpty()) {
										%>
												<tr><td colspan="4" class="statistics-table-empty">No movie revenue found for this period.</td></tr>
										<%
											} else {
												int rank = 1;
												for (ManagerMovieStatistic movie : topMovies) {
										%>
													<tr>
														<td><span class="statistics-rank"><%= rank++ %></span></td>
														<td class="statistics-movie-name"><%= movie.getMovieName() == null ? "Unknown movie" : escapeStatisticsHtml(movie.getMovieName()) %></td>
														<td><%= String.format("%,d", movie.getTicketsSold()) %></td>
														<td class="statistics-revenue"><%= String.format("%,d", movie.getRevenue()) %> VND</td>
													</tr>
										<%
												}
											}
										%>
									</tbody>
								</table>
							</div>
						</section>
						<p class="statistics-footnote">
							Revenue represents gross successful payments. Refund requests are not deducted until a future admin refund-approval flow updates payment records.
						</p>
						<script>
							window.managerStatisticsData = {
								labels: [
									<%
										for (int i = 0; i < dailyStatistics.size(); i++) {
											ManagerDailyStatistic item = dailyStatistics.get(i);
									%>
											"<%= item.getDate().format(DateTimeFormatter.ofPattern("dd MMM")) %>"<%= i < dailyStatistics.size() - 1 ? "," : "" %>
									<%
										}
									%>
								],
								revenue: [
									<%
										for (int i = 0; i < dailyStatistics.size(); i++) {
									%>
											<%= dailyStatistics.get(i).getRevenue() %><%= i < dailyStatistics.size() - 1 ? "," : "" %>
									<%
										}
									%>
								],
								bookings: [
									<%
										for (int i = 0; i < dailyStatistics.size(); i++) {
									%>
											<%= dailyStatistics.get(i).getBookings() %><%= i < dailyStatistics.size() - 1 ? "," : "" %>
									<%
										}
									%>
								]
							};
						</script>
				<%
					}
				%>
			</section>
		</main>
	</body>
</html>