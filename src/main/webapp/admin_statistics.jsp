<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.AdminStatisticsSummary" %>
<%@ page import="com.movieweb.model.AdminTheaterStatistic" %>
<%@ page import="com.movieweb.model.AdminPaymentMethodStatistic" %>
<%@ page import="com.movieweb.model.ManagerDailyStatistic" %>
<%@ page import="com.movieweb.model.ManagerMovieStatistic" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "statistics");
	String contextPath = request.getContextPath();
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("adminTheaters");
	if (theaters == null) {
		theaters = Collections.emptyList();
	}
	Integer selectedTheaterId = (Integer) request.getAttribute("adminTheaterId");
	List<String> failed = (List<String>) request.getAttribute("failedSections");
	if (failed == null) {
		failed = Collections.emptyList();
	}
	// A null attribute means that section failed (it is listed in failedSections), never "zero".
	AdminStatisticsSummary statistics = (AdminStatisticsSummary) request.getAttribute("statistics");
	List<ManagerDailyStatistic> dailyStatistics = (List<ManagerDailyStatistic>) request.getAttribute("dailyStatistics");
	List<ManagerMovieStatistic> topMovies = (List<ManagerMovieStatistic>) request.getAttribute("topMovies");
	List<AdminTheaterStatistic> theaterComparison = (List<AdminTheaterStatistic>) request.getAttribute("theaterComparison");
	List<AdminPaymentMethodStatistic> paymentMethods = (List<AdminPaymentMethodStatistic>) request.getAttribute("paymentMethods");
	Map<String, Integer> bookingStatuses = (Map<String, Integer>) request.getAttribute("bookingStatuses");
	String startDate = (String) request.getAttribute("startDate");
	String endDate = (String) request.getAttribute("endDate");
	String errorMessage = (String) request.getAttribute("errorMessage");
	if (startDate == null) {
		startDate = LocalDate.now().minusDays(6).toString();
	}
	if (endDate == null) {
		endDate = LocalDate.now().toString();
	}
	String scopeLabel = "All theaters";
	for (Theaters t : theaters) {
		if (selectedTheaterId != null && t.getTheater_id() == selectedTheaterId) {
			scopeLabel = t.getTheater_name();
		}
	}
	DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("dd MMM yyyy");
	DateTimeFormatter shortDate = DateTimeFormatter.ofPattern("dd MMM");
%>
<%!
	private String esc(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}
	private int pct(long value, long max) {
		return max <= 0 ? 0 : (int) Math.max(1, Math.round(value * 100.0 / max));
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Statistics | PhnetPhlyx Admin</title>
		<link rel="stylesheet" href="<%= contextPath %>/styles/main_admin.css">
		<link rel="stylesheet" href="<%= contextPath %>/styles/admin_statistics.css">
		<script defer src="<%= contextPath %>/scripts/admin_statistics.js"></script>
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<section class="statistics-container">
					<div class="statistics-header">
						<div>
							<p class="statistics-breadcrumb">Admin / Statistics</p>
							<h1>System Statistics</h1>
							<p class="statistics-subtitle">Bookings, ticket sales and successful payment revenue across the cinema system.</p>
						</div>
						<div class="statistics-theater">
							<span>Scope</span>
							<strong><%= esc(scopeLabel) %></strong>
						</div>
					</div>

					<%
						if (errorMessage != null && !errorMessage.trim().isEmpty()) {
					%>
							<div class="statistics-alert statistics-alert-error"><%= esc(errorMessage) %></div>
					<%
						}
					%>
					<%
						if (!failed.isEmpty()) {
					%>
							<div class="statistics-alert statistics-alert-error">
								Some figures could not be loaded and are not shown (<%= esc(String.join(", ", failed)) %>). The error has been logged.
							</div>
					<%
						}
					%>

					<form class="statistics-filter" method="get" action="<%= contextPath %>/admin-statistics">
						<div class="statistics-filter-copy">
							<strong>Report period</strong>
							<span>Inclusive date range, maximum 366 days.</span>
						</div>
						<label>
							<span>Theater</span>
							<select name="theaterId">
								<option value="all" <%= selectedTheaterId == null ? "selected" : "" %>>All Theaters</option>
								<%
									for (Theaters t : theaters) {
								%>
										<option value="<%= t.getTheater_id() %>" <%= selectedTheaterId != null && selectedTheaterId == t.getTheater_id() ? "selected" : "" %>>
											<%= esc(t.getTheater_name()) %><%= t.isActive() ? "" : " (inactive)" %>
										</option>
								<%
									}
								%>
							</select>
						</label>
						<label>
							<span>From</span>
							<input type="date" name="startDate" value="<%= startDate %>" required>
						</label>
						<label>
							<span>To</span>
							<input type="date" name="endDate" value="<%= endDate %>" required>
						</label>
						<button type="submit" class="statistics-button">Apply filter</button>
						<a class="statistics-reset" href="<%= contextPath %>/admin-statistics">Last 7 days</a>
					</form>
					<div class="statistics-period-note">
						<%= LocalDate.parse(startDate).format(displayDate) %> – <%= LocalDate.parse(endDate).format(displayDate) %>
						<span>·</span> <%= esc(scopeLabel) %>
					</div>

					<%
						if (statistics != null) {
					%>
							<section class="statistics-kpis" aria-label="Statistics summary">
								<article class="statistics-kpi">
									<div class="kpi-icon">B</div>
									<p>Confirmed bookings</p>
									<strong><%= String.format("%,d", statistics.getConfirmedBookings()) %></strong>
									<span>By booking date</span>
								</article>
								<article class="statistics-kpi">
									<div class="kpi-icon">₫</div>
									<p>Gross successful payments</p>
									<strong><%= String.format("%,d", statistics.getGrossRevenue()) %> <small>VND</small></strong>
									<span>By payment date; approved refunds excluded, pending requests still counted</span>
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
									<span><%= String.format("%,d", statistics.getSuccessfulPayments()) %> successful payments</span>
								</article>
								<article class="statistics-kpi">
									<div class="kpi-icon">R</div>
									<p>Refund requests</p>
									<strong><%= String.format("%,d", statistics.getRefundedBookings()) %></strong>
									<span><%= String.format("%,d", statistics.getRevenueOnRefundedBookings()) %> VND of the gross figure sits on these bookings</span>
								</article>
							</section>
					<%
						}
					%>

					<%
						if (dailyStatistics != null) {
					%>
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
					<%
						}
					%>

					<%
						if (theaterComparison != null) {
							long maxTheaterRevenue = 0;
							for (AdminTheaterStatistic row : theaterComparison) {
								maxTheaterRevenue = Math.max(maxTheaterRevenue, row.getGrossRevenue());
							}
					%>
							<section class="statistics-panel statistics-movies-panel">
								<div class="statistics-panel-heading">
									<div>
										<h2>Theater comparison</h2>
										<p>All theaters for the selected period, ranked by gross successful payments</p>
									</div>
								</div>
								<div class="statistics-table-wrap">
									<table class="statistics-table">
										<thead>
											<tr>
												<th>Theater</th>
												<th>Confirmed bookings</th>
												<th>Tickets</th>
												<th>Gross revenue</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												if (theaterComparison.isEmpty()) {
											%>
													<tr>
														<td colspan="5" class="statistics-table-empty">No theaters found.</td>
													</tr>
											<%
												} else {
													for (AdminTheaterStatistic row : theaterComparison) {
											%>
														<tr class="<%= selectedTheaterId != null && selectedTheaterId == row.getTheaterId() ? "statistics-row-selected" : "" %>">
															<td class="statistics-movie-name">
																<%= esc(row.getTheaterName()) %>
																<%
																	if (!row.isActive()) {
																%>
																		<span class="statistics-inactive-tag">inactive</span>
																<%
																	}
																%>
															</td>
															<td><%= String.format("%,d", row.getConfirmedBookings()) %></td>
															<td><%= String.format("%,d", row.getTicketsSold()) %></td>
															<td class="statistics-revenue"><%= String.format("%,d", row.getGrossRevenue()) %> VND</td>
															<td class="statistics-bar-cell">
																<span class="statistics-bar" style="width: <%= pct(row.getGrossRevenue(), maxTheaterRevenue) %>%"></span>
															</td>
														</tr>
											<%
													}
												}
											%>
										</tbody>
									</table>
								</div>
							</section>
					<%
						}
					%>

					<%
						if (topMovies != null) {
					%>
							<section class="statistics-panel statistics-movies-panel">
								<div class="statistics-panel-heading">
									<div>
										<h2>Top movies</h2>
										<p>Top 10 by successful payment revenue (tickets = seats on paid bookings)</p>
									</div>
								</div>
								<div class="statistics-table-wrap">
									<table class="statistics-table">
										<thead>
											<tr>
												<th>#</th>
												<th>Movie</th>
												<th>Tickets</th>
												<th>Gross revenue</th>
											</tr>
										</thead>
										<tbody>
											<%
												if (topMovies.isEmpty()) {
											%>
													<tr>
														<td colspan="4" class="statistics-table-empty">No movie revenue found for this period.</td>
													</tr>
											<%
												} else {
													int rank = 1;
													for (ManagerMovieStatistic movie : topMovies) {
											%>
														<tr>
															<td><span class="statistics-rank"><%= rank++ %></span></td>
															<td class="statistics-movie-name"><%= movie.getMovieName() == null ? "Unknown movie" : esc(movie.getMovieName()) %></td>
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
					<%
						}
					%>

					<div class="statistics-chart-grid">
						<%
							if (paymentMethods != null) {
								long maxMethodRevenue = 0;
								for (AdminPaymentMethodStatistic pm : paymentMethods) {
									maxMethodRevenue = Math.max(maxMethodRevenue, pm.getRevenue());
								}
						%>
								<section class="statistics-panel">
									<div class="statistics-panel-heading">
										<div>
											<h2>Payment methods</h2>
											<p>Successful payments by method</p>
										</div>
									</div>
									<div class="statistics-table-wrap">
										<table class="statistics-table">
											<thead>
												<tr>
													<th>Method</th>
													<th>Payments</th>
													<th>Gross revenue</th>
													<th></th>
												</tr>
											</thead>
											<tbody>
												<%
													if (paymentMethods.isEmpty()) {
												%>
														<tr>
															<td colspan="4" class="statistics-table-empty">No successful payments in this period.</td>
														</tr>
												<%
													} else {
														for (AdminPaymentMethodStatistic pm : paymentMethods) {
												%>
															<tr>
																<td class="statistics-movie-name"><%= esc(pm.getMethod()) %></td>
																<td><%= String.format("%,d", pm.getPayments()) %></td>
																<td class="statistics-revenue"><%= String.format("%,d", pm.getRevenue()) %> VND</td>
																<td class="statistics-bar-cell">
																	<span class="statistics-bar" style="width: <%= pct(pm.getRevenue(), maxMethodRevenue) %>%"></span>
																</td>
															</tr>
												<%
														}
													}
												%>
											</tbody>
										</table>
									</div>
								</section>
						<%
							}
						%>
						<%
							if (bookingStatuses != null) {
						%>
								<section class="statistics-panel">
									<div class="statistics-panel-heading">
										<div>
											<h2>Bookings by status</h2>
											<p>Bookings created in the period, by their current status</p>
										</div>
									</div>
									<div class="statistics-table-wrap">
										<table class="statistics-table">
											<thead>
												<tr>
													<th>Status</th>
													<th>Bookings</th>
												</tr>
											</thead>
											<tbody>
												<%
													if (bookingStatuses.isEmpty()) {
												%>
														<tr>
															<td colspan="2" class="statistics-table-empty">No bookings in this period.</td>
														</tr>
												<%
													} else {
														for (Map.Entry<String, Integer> e : bookingStatuses.entrySet()) {
												%>
															<tr>
																<td class="statistics-movie-name"><%= esc(e.getKey()) %></td>
																<td><%= String.format("%,d", e.getValue()) %></td>
															</tr>
												<%
														}
													}
												%>
											</tbody>
										</table>
									</div>
								</section>
						<%
							}
						%>
					</div>

					<p class="statistics-footnote">
						Revenue is gross successful payment revenue, not net. A booking marked REFUNDED keeps its SUCCESS payment until an admin approves the refund on the Refunds page, so money on pending requests is still counted above; once approved, the payment becomes REFUNDED and is no longer counted. Bookings and tickets count CONFIRMED bookings by booking date; revenue uses the payment date; "tickets" in Top movies counts seats on bookings with a successful payment. Inactive and deleted theaters, rooms and movies stay in historical figures.
					</p>

					<%
						if (dailyStatistics != null) {
					%>
							<script>
								window.adminStatisticsData = {
									labels: [
										<%
											for (int i = 0; i < dailyStatistics.size(); i++) {
										%>
												"<%= dailyStatistics.get(i).getDate().format(shortDate) %>"<%= i < dailyStatistics.size() - 1 ? "," : "" %>
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
		</section>
	</body>
</html>