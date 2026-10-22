<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.AdminDashboardSummary" %>
<%@ page import="com.movieweb.model.AdminTheaterStatistic" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
	// Defense in depth: AdminFilter already enforces this.
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "dashboard");

	List<Theaters> theaters = (List<Theaters>) request.getAttribute("adminTheaters");
	if (theaters == null) {
		theaters = Collections.emptyList();
	}
	Integer selectedTheaterId = (Integer) request.getAttribute("adminTheaterId");
	boolean summaryAvailable = Boolean.TRUE.equals(request.getAttribute("adminSummaryAvailable"));
	boolean breakdownAvailable = Boolean.TRUE.equals(request.getAttribute("adminBreakdownAvailable"));
	AdminDashboardSummary summary = (AdminDashboardSummary) request.getAttribute("adminSummary");
	if (summary == null) {
		summary = new AdminDashboardSummary();
		summaryAvailable = false;
	}
	List<AdminTheaterStatistic> breakdown = (List<AdminTheaterStatistic>) request.getAttribute("adminTheaterBreakdown");
	if (breakdown == null) {
		breakdown = Collections.emptyList();
	}
	NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
	String scopeLabel = "All theaters";
	for (Theaters t : theaters) {
		if (selectedTheaterId != null && t.getTheater_id() == selectedTheaterId) {
			scopeLabel = t.getTheater_name();
		}
	}
%>
<%!
	private String esc(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Admin Dashboard | PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_admin.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Admin Dashboard</h2>
						<p>
							Welcome back, <strong><%= esc(currentUser.getFullName()) %></strong>.
							Showing: <strong><%= esc(scopeLabel) %></strong>
						</p>
					</div>
					<!-- Admin theater selector: "All Theaters" + any non-deleted theater -->
					<form class="admin-theater-filter" method="get" action="${pageContext.request.contextPath}/admin-dashboard">
						<label for="theater-filter">Theater</label>
						<select id="theater-filter" name="theaterId" onchange="this.form.submit()">
							<option value="all" <%= selectedTheaterId == null ? "selected" : "" %>>All Theaters</option>
							<%
								for (Theaters theater : theaters) {
							%>
									<option value="<%= theater.getTheater_id() %>"
										<%= selectedTheaterId != null && selectedTheaterId == theater.getTheater_id() ? "selected" : "" %>>
										<%= esc(theater.getTheater_name()) %><%= theater.isActive() ? "" : " (inactive)" %>
									</option>
							<%
								}
							%>
						</select>
					</form>
				</div>

				<%
					if (!summaryAvailable) {
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3>Dashboard unavailable</h3>
							</div>
							<div class="admin-panel-content">
								<p>The dashboard figures could not be loaded. The error has been logged; please try again later.</p>
							</div>
						</section>
				<%
					} else {
				%>
						<section class="admin-summary">
							<div class="admin-summary-card admin-summary-card-bookings">
								<h3>Today's Bookings</h3>
								<p><%= nf.format(summary.getConfirmedBookingsToday()) %></p>
								<span>Confirmed bookings made today</span>
							</div>
							<div class="admin-summary-card admin-summary-card-revenue">
								<h3>Today's Revenue</h3>
								<p><%= nf.format(summary.getGrossRevenueToday()) %></p>
								<span>Gross successful payments today (VND)</span>
							</div>
							<div class="admin-summary-card admin-summary-card-showtimes">
								<h3>7-Day Revenue</h3>
								<p><%= nf.format(summary.getGrossRevenue7Days()) %></p>
								<span><%= nf.format(summary.getTicketsSold7Days()) %> tickets · <%= nf.format(summary.getConfirmedBookings7Days()) %> confirmed bookings</span>
							</div>
							<div class="admin-summary-card admin-summary-card-rooms">
								<h3>Refund Requests</h3>
								<p><%= nf.format(summary.getRefundedBookings()) %></p>
								<span>Bookings in REFUNDED status (not yet reviewed) · <a href="<%= request.getContextPath() %>/admin-refunds">Review</a></span>
							</div>
						</section>

						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3>System Overview</h3>
							</div>
							<div class="admin-panel-content">
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<tbody>
											<tr>
												<td>Active theaters</td>
												<td><%= nf.format(summary.getActiveTheaters()) %></td>
											</tr>
											<tr>
												<td>Active user accounts</td>
												<td><%= nf.format(summary.getActiveUsers()) %></td>
											</tr>
											<tr>
												<td>Active manager accounts</td>
												<td><%= nf.format(summary.getActiveManagers()) %></td>
											</tr>
											<tr>
												<td>Reported comments awaiting review</td>
												<td><a href="${pageContext.request.contextPath}/admin-reports"><%= nf.format(summary.getReportedComments()) %></a></td>
											</tr>
											<tr>
												<td>Manager accounts pending approval</td>
												<td><a href="${pageContext.request.contextPath}/admin-managers?status=pending"><%= nf.format(summary.getPendingManagers()) %></a></td>
											</tr>
											<tr>
												<td>Deactivated manager accounts</td>
												<td><%= nf.format(summary.getDeactivatedManagers()) %></td>
											</tr>
										</tbody>
									</table>
								</div>
								<p class="admin-muted">Account and theater counts are system-wide; they do not follow the theater selector.</p>
							</div>
						</section>
				<%
					}
				%>

				<section class="admin-panel">
					<div class="admin-panel-header">
						<h3>Revenue by Theater (last 7 days)</h3>
					</div>
					<div class="admin-panel-content">
						<%
							if (!breakdownAvailable) {
						%>
								<p>Theater figures could not be loaded. The error has been logged.</p>
						<%
							} else if (breakdown.isEmpty()) {
						%>
								<p>No theaters found.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Theater</th>
												<th>Status</th>
												<th>Confirmed Bookings</th>
												<th>Tickets Sold</th>
												<th>Gross Revenue (VND)</th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminTheaterStatistic row : breakdown) {
											%>
													<tr>
														<td><%= esc(row.getTheaterName()) %></td>
														<td>
															<span class="admin-status <%= row.isActive() ? "admin-status-confirmed" : "admin-status-cancelled" %>">
																<%= row.isActive() ? "Active" : "Inactive" %>
															</span>
														</td>
														<td><%= nf.format(row.getConfirmedBookings()) %></td>
														<td><%= nf.format(row.getTicketsSold()) %></td>
														<td><%= nf.format(row.getGrossRevenue()) %></td>
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
						<p class="admin-muted">Revenue is gross successful payment revenue by payment date. Approved refunds are excluded (their payment becomes REFUNDED), but a REFUNDED booking keeps its SUCCESS payment until an admin approves the refund on the Refunds page. Bookings and tickets count CONFIRMED bookings by booking date.</p>
					</div>
				</section>
			</main>
		</section>
	</body>
</html>