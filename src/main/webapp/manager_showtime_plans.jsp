<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.time.*" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="com.movieweb.model.*" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%
	request.setAttribute("currentManagerPage", "plans");
	Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
	List<Showtime_plans> plans = (List<Showtime_plans>) request.getAttribute("plans");
	Showtime_plans plan = (Showtime_plans) request.getAttribute("plan");
	List<Showtime_plan_items> items = (List<Showtime_plan_items>) request.getAttribute("items");
	String error = (String) request.getAttribute("errorMessage");
	String success = (String) request.getAttribute("successMessage");

	String currentTheaterName = null;
	if (theaters != null && currentTheaterId != null) {
		for (Theaters t : theaters) {
			if (t.getTheater_id() == currentTheaterId.intValue()) {
				currentTheaterName = t.getTheater_name();
				break;
			}
		}
	}
	NumberFormat money = NumberFormat.getIntegerInstance(Locale.US);
	DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	LocalDateTime now = LocalDateTime.now();
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Planner History - PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_manager.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_showtimes.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_showtime_planner.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout2">
			<jsp:include page="/components/header_manager.jsp" />
			<%@ include file="components/manager_navigation.jsp" %>
			<main class="ManagerMain">
				<div class="showtimes-container">
					<div class="showtimes-header">
						<div>
							<span class="showtimes-breadcrumb">
								Manage Theaters / Budget Showtime Planner / History<%= plan == null ? "" : " / Plan #" + plan.getPlan_id() %>
							</span>
							<h2><%= plan == null ? "Planner History" : "Plan #" + plan.getPlan_id() %></h2>
							<p>
								<%= plan == null
									? "Every budget plan that was turned into showtimes in the selected theater."
									: "The figures below are the ones recorded when the plan was created." %>
							</p>
							<%
								if (currentTheaterId != null) {
							%>
									<div class="showtimes-current-theater">
										Current Theater:
										<strong><%= currentTheaterName == null ? "Theater ID " + currentTheaterId : ViewUtils.h(currentTheaterName) %></strong>
									</div>
							<%
								}
							%>
						</div>
						<div>
							<%
								if (plan != null) {
							%>
									<a class="showtimes-button" href="${pageContext.request.contextPath}/manager-showtime-plans">All plans</a>
									<%
										if (plan.getDeleted_at() == null) {
									%>
											<button type="button" class="showtimes-button showtimes-button-danger" id="btnOpenDelete">Delete plan</button>
									<%
										}
									%>
							<%
								}
							%>
							<a class="showtimes-button showtimes-button-primary" href="${pageContext.request.contextPath}/manager-showtime-planner">New plan</a>
						</div>
					</div>

					<%
						if (error != null && !error.trim().isEmpty()) {
					%>
							<div class="showtimes-message error-message"><%= ViewUtils.h(error) %></div>
					<%
						}
					%>
					<%
						if (success != null && !success.trim().isEmpty()) {
					%>
							<div class="showtimes-message success-message"><%= ViewUtils.h(success) %></div>
					<%
						}
					%>

					<%
						if (currentTheaterId == null) {
					%>
							<div class="showtimes-empty-state">
								<h3>No Theater Available</h3>
								<p>You do not currently have an active assigned theater. Please contact an administrator.</p>
							</div>

					<%
						} else if (plan != null) {
							int removed = 0, paidLocked = 0, deletable = 0;
							boolean canDelete = plan.getDeleted_at() == null;
							if (items != null) {
								for (Showtime_plan_items it : items) {
									if (it.getDeleted_at() != null) {
										removed++;
									} else if (it.isHas_paid()) {
										paidLocked++;
									} else {
										deletable++;
									}
								}
							}
					%>
						<%
							if (plan.getDeleted_at() != null) {
						%>
								<div class="showtimes-message error-message">
									This plan was deleted on <%= plan.getDeleted_at().toLocalDateTime().format(dateTimeFmt) %>.
									The showtimes marked KEPT below were not deleted and are now ordinary showtimes. Its profit no longer counts in the totals.
								</div>
						<%
							}
						%>
						<section class="showtimes-section">
							<div class="planner-stats">
								<div class="planner-stat">
									<span class="planner-stat-label">Budget</span>
									<span class="planner-stat-value"><%= money.format(plan.getBudget()) %></span>
									<span class="planner-stat-unit">VND</span>
								</div>
								<div class="planner-stat">
									<span class="planner-stat-label">Showtimes created</span>
									<span class="planner-stat-value"><%= plan.getShowtime_count() %></span>
									<span class="planner-stat-unit"><%= plan.getLive_count() %> still scheduled</span>
								</div>
								<div class="planner-stat">
									<span class="planner-stat-label">Cost of showtimes</span>
									<span class="planner-stat-value"><%= money.format(plan.getTotal_cost()) %></span>
									<span class="planner-stat-unit">VND</span>
								</div>
								<div class="planner-stat planner-stat-profit <%= plan.getProfit() < 0 ? "is-negative" : "" %>">
									<span class="planner-stat-label">Profit (leftover)</span>
									<span class="planner-stat-value"><%= money.format(plan.getProfit()) %></span>
									<span class="planner-stat-unit">VND</span>
								</div>
							</div>
							<div class="showtimes-current-theater">
								Movie: <strong><%= ViewUtils.h(plan.getMovie_name()) %></strong>
								&middot; Author / sponsor: <strong><%= plan.getClient_name() == null ? "—" : ViewUtils.h(plan.getClient_name()) %></strong>
								&middot; Ticket price: <strong><%= money.format(plan.getBase_price()) %></strong>
								&middot; Period: <strong><%= plan.getPeriod_start().toLocalDate().format(dateFmt) %> &ndash; <%= plan.getPeriod_end().toLocalDate().format(dateFmt) %></strong>
								&middot; Gap: <strong><%= plan.getGap_minutes() %> min</strong>
								&middot; By <strong><%= ViewUtils.h(plan.getManager_name()) %></strong>
								on <strong><%= plan.getCreated_at().toLocalDateTime().format(dateTimeFmt) %></strong>
							</div>
						</section>

						<section class="showtimes-section">
							<div class="showtimes-section-heading">
								<div>
									<h3 class="showtimes-section-title">Showtimes of this plan</h3>
									<%
										if (removed > 0) {
									%>
											<p class="showtimes-section-description">
												<%= removed %> showtime(s) were already removed from Showtimes Management.
												The profit above is not recalculated.
											</p>
									<%
										}
									%>
								</div>
								<span class="showtimes-count"><%= items == null ? 0 : items.size() %> showtime(s)</span>
							</div>
							<div class="showtimes-table-wrapper">
								<table class="showtimes-table">
									<thead>
										<tr>
											<th>Showtime ID</th>
											<th>Room</th>
											<th>Start</th>
											<th>End</th>
											<th>Cost (VND)</th>
											<th>Status</th>
										</tr>
									</thead>
									<tbody>
									<%
										if (items != null) {
											for (Showtime_plan_items it : items) {
												String status;
												String cls;
												if (it.getDeleted_at() != null) {
													status = "REMOVED";
													cls = "removed";
												} else if (it.isHas_paid()) {
													status = "PAID - KEPT";
													cls = "locked";
												} else if (!canDelete) {
													status = "KEPT";
													cls = "scheduled";
												} else if (it.getEnd_at().toLocalDateTime().isBefore(now)) {
													status = "FINISHED";
													cls = "completed";
												} else {
													status = "SCHEDULED";
													cls = "scheduled";
												}
									%>
										<tr class="<%= it.isHas_paid() ? "planner-row-locked" : "" %>">
											<td><%= it.getShowtime_id() %></td>
											<td><%= ViewUtils.h(it.getRoom_name()) %></td>
											<td><%= it.getStart_at().toLocalDateTime().format(dateTimeFmt) %></td>
											<td><%= it.getEnd_at().toLocalDateTime().format(dateTimeFmt) %></td>
											<td class="planner-num"><%= money.format(it.getCost()) %></td>
											<td><span class="showtimes-status status-<%= cls %>"><%= status %></span></td>
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
							if (canDelete) {
						%>
						<dialog id="deleteDialog" class="planner-dialog" data-deletable="<%= deletable %>">
							<form id="deleteForm" action="${pageContext.request.contextPath}/manager-showtime-plans" method="post">
								<input type="hidden" name="action" value="delete">
								<input type="hidden" name="planId" value="<%= plan.getPlan_id() %>">

								<div class="planner-dialog-head">
									<h3>Delete plan #<%= plan.getPlan_id() %></h3>
									<p>
										The plan will be deleted. Ticked showtimes are deleted with it.
										<strong>Untick a showtime to keep it</strong>; it stays in Showtimes Management as a normal showtime.
									</p>
									<%
										if (paidLocked > 0) {
									%>
											<div class="showtimes-message planner-warning">
												<%= paidLocked %> showtime(s) already have paid bookings. They are locked and
												<strong>will not be deleted</strong>.
											</div>
									<%
										}
									%>
								</div>

								<div class="planner-dialog-tools">
									<span><strong id="pickedCount"><%= deletable %></strong> of <%= deletable %> deletable showtime(s) selected</span>
									<div class="planner-adjust-buttons">
										<button type="button" class="showtimes-button" id="btnPickAll">Select all</button>
										<button type="button" class="showtimes-button" id="btnPickNone">Clear</button>
									</div>
								</div>

								<div class="planner-dialog-list">
									<table class="showtimes-table">
										<thead>
											<tr>
												<th></th>
												<th>ID</th>
												<th>Room</th>
												<th>Start</th>
												<th>End</th>
												<th>Status</th>
											</tr>
										</thead>
										<tbody>
										<%
											if (items != null) {
												for (Showtime_plan_items it : items) {
													boolean pickable = it.getDeleted_at() == null && !it.isHas_paid();
													String status = it.getDeleted_at() != null ? "REMOVED" : (it.isHas_paid() ? "PAID - KEPT" : "WILL BE DELETED");
													String cls = it.getDeleted_at() != null ? "removed" : (it.isHas_paid() ? "locked" : "scheduled");
										%>
											<tr class="<%= it.isHas_paid() ? "planner-row-locked" : "" %>">
												<td>
													<input type="checkbox" class="pick-check" name="showtimeId" value="<%= it.getShowtime_id() %>"
														<%= pickable ? "checked" : "disabled" %>
														title="<%= it.isHas_paid() ? "Has paid bookings - cannot be deleted" : (it.getDeleted_at() != null ? "Already removed" : "Untick to keep this showtime") %>"
														aria-label="Delete this showtime with the plan">
												</td>
												<td><%= it.getShowtime_id() %></td>
												<td><%= ViewUtils.h(it.getRoom_name()) %></td>
												<td><%= it.getStart_at().toLocalDateTime().format(dateTimeFmt) %></td>
												<td><%= it.getEnd_at().toLocalDateTime().format(dateTimeFmt) %></td>
												<td><span class="showtimes-status status-<%= cls %>" data-pickable="<%= pickable %>"><%= status %></span></td>
											</tr>
										<%
												}
											}
										%>
										</tbody>
									</table>
								</div>

								<p class="planner-confirm-note planner-left" id="deleteNote"></p>
								<div class="planner-dialog-foot">
									<button type="button" class="showtimes-button" id="btnCancelDelete">Cancel</button>
									<button type="submit" class="showtimes-button showtimes-button-danger" id="btnDelete">Delete plan</button>
								</div>
							</form>
						</dialog>
						<%
							}
						%>

					<%
						} else {
							long sumBudget = 0, sumProfit = 0;
							int sumShowtimes = 0;
							if (plans != null) {
								for (Showtime_plans p : plans) {
									if (p.getDeleted_at() != null) {
										continue;
									}
									sumBudget += p.getBudget();
									sumProfit += p.getProfit();
									sumShowtimes += p.getShowtime_count();
								}
							}
					%>
						<!-- ============ List ============ -->
						<section class="showtimes-section">
							<div class="planner-stats">
								<div class="planner-stat">
									<span class="planner-stat-label">Plans</span>
									<span class="planner-stat-value"><%= plans == null ? 0 : plans.size() %></span>
									<span class="planner-stat-unit">in this theater</span>
								</div>
								<div class="planner-stat">
									<span class="planner-stat-label">Showtimes created</span>
									<span class="planner-stat-value"><%= money.format(sumShowtimes) %></span>
									<span class="planner-stat-unit">all plans</span>
								</div>
								<div class="planner-stat">
									<span class="planner-stat-label">Budgets received</span>
									<span class="planner-stat-value"><%= money.format(sumBudget) %></span>
									<span class="planner-stat-unit">VND</span>
								</div>
								<div class="planner-stat planner-stat-profit">
									<span class="planner-stat-label">Total profit</span>
									<span class="planner-stat-value"><%= money.format(sumProfit) %></span>
									<span class="planner-stat-unit">VND</span>
								</div>
							</div>

							<%
								if (plans == null || plans.isEmpty()) {
							%>
									<div class="showtimes-empty-state">
										<h3>No plans yet</h3>
										<p>Plans appear here after you create showtimes from a budget.</p>
									</div>
							<%
								} else {
							%>
									<div class="showtimes-table-wrapper">
										<table class="showtimes-table planner-table-wide">
											<thead>
												<tr>
													<th>Plan</th>
													<th>Created</th>
													<th>Movie</th>
													<th>Author / sponsor</th>
													<th>Period</th>
													<th>Showtimes</th>
													<th>Budget (VND)</th>
													<th>Cost (VND)</th>
													<th>Profit (VND)</th>
													<th>Status</th>
													<th></th>
												</tr>
											</thead>
											<tbody>
											<%
												for (Showtime_plans p : plans) {
											%>
												<tr>
													<td>#<%= p.getPlan_id() %></td>
													<td><%= p.getCreated_at().toLocalDateTime().format(dateTimeFmt) %></td>
													<td><%= ViewUtils.h(p.getMovie_name()) %></td>
													<td><%= p.getClient_name() == null ? "—" : ViewUtils.h(p.getClient_name()) %></td>
													<td><%= p.getPeriod_start().toLocalDate().format(dateFmt) %> &ndash; <%= p.getPeriod_end().toLocalDate().format(dateFmt) %></td>
													<td><%= p.getLive_count() %> / <%= p.getShowtime_count() %></td>
													<td class="planner-num"><%= money.format(p.getBudget()) %></td>
													<td class="planner-num"><%= money.format(p.getTotal_cost()) %></td>
													<td class="planner-num planner-profit"><%= money.format(p.getProfit()) %></td>
													<td>
														<%
															if (p.getDeleted_at() != null) {
														%>
																<span class="showtimes-status status-removed">DELETED</span>
														<%
															} else {
														%>
																<span class="showtimes-status status-scheduled">ACTIVE</span>
														<%
															}
														%>
													</td>
													<td><a class="planner-link" href="${pageContext.request.contextPath}/manager-showtime-plans?id=<%= p.getPlan_id() %>">Details</a></td>
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
						</section>
					<%
						}
					%>
				</div>
			</main>
		</section>
		<script src="<%= request.getContextPath() %>/scripts/manager_showtime_plans.js"></script>
	</body>
</html>