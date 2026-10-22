<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.time.*" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="com.movieweb.model.*" %>
<%@ page import="com.movieweb.service.ShowtimePlanDraft" %>
<%@ page import="com.movieweb.service.ShowtimePlannerService" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%
	request.setAttribute("currentManagerPage", "planner");
	Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
	List<Rooms> rooms = (List<Rooms>) request.getAttribute("rooms");
	Map<Integer, Integer> seatCounts = (Map<Integer, Integer>) request.getAttribute("seatCounts");
	List<Movies> movies = (List<Movies>) request.getAttribute("movies");
	Theaters theater = (Theaters) request.getAttribute("theater");
	ShowtimePlanDraft draft = (ShowtimePlanDraft) request.getAttribute("draft");
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
	DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE dd/MM/yyyy", Locale.ENGLISH);
	DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
	ShowtimePlannerService.Params dp = draft == null ? null : draft.getParams();

	String vClient = dp == null || dp.clientName == null ? "" : dp.clientName;
	int vMovie = dp == null ? 0 : dp.movieId;
	String vBudget = dp == null ? "" : money.format(dp.budget);
	String vPrice = dp == null ? "" : String.valueOf(dp.basePrice);
	String vFrom = dp == null ? LocalDate.now().plusDays(1).toString() : dp.dateFrom.toString();
	String vTo = dp == null ? LocalDate.now().plusDays(7).toString() : dp.dateTo.toString();
	int vGap = dp == null ? 20 : dp.gapMinutes;
	String vEarliest = dp == null || dp.earliest == null ? "" : dp.earliest.format(timeFmt);
	String vLatest = dp == null || dp.latest == null ? "" : dp.latest.format(timeFmt);
	String theaterHours = theater == null || theater.getOpen_time() == null || theater.getClosing_time() == null
			? "" : theater.getOpen_time().toLocalTime().format(timeFmt) + " – " + theater.getClosing_time().toLocalTime().format(timeFmt);
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Budget Showtime Planner - PhnetPhlyx</title>
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
							<span class="showtimes-breadcrumb">Manage Theaters / Budget Showtime Planner</span>
							<h2>Budget Showtime Planner</h2>
							<p>
								An author gives you a budget. Enter it with the original ticket price and the
								planner fills as many sensible showtimes as possible. Adjust the number, and
								whatever is left of the budget is your profit.
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
						<a class="showtimes-button" href="${pageContext.request.contextPath}/manager-showtime-plans">View History</a>
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
						} else {
					%>

					<section class="showtimes-section">
						<div class="showtimes-section-heading">
							<div>
								<h3 class="showtimes-section-title">1. Budget and period</h3>
								<p class="showtimes-section-description">
									Nothing is saved yet. The planner only builds a preview you can adjust.
									<%
										if (!theaterHours.isEmpty()) {
									%>
											Theater hours: <%= theaterHours %>.
									<%
										}
									%>
								</p>
							</div>
						</div>
						<form class="showtimes-create-form" action="${pageContext.request.contextPath}/manager-showtime-planner" method="post">
							<input type="hidden" name="action" value="generate">
							<div class="showtimes-form-grid">
								<div class="showtimes-input-group">
									<label for="clientName">Author / sponsor (optional)</label>
									<input type="text" id="clientName" name="clientName" maxlength="100" value="<%= ViewUtils.h(vClient) %>" placeholder="Who is paying?">
								</div>
								<div class="showtimes-input-group">
									<label for="movieId">Movie</label>
									<select id="movieId" name="movieId" required>
										<option value="">Select a movie</option>
										<%
											if (movies != null) {
												for (Movies m : movies) {
										%>
													<option value="<%= m.getMovie_id() %>" <%= m.getMovie_id() == vMovie ? "selected" : "" %>>
														<%= ViewUtils.h(m.getMovie_name()) %> (<%= m.getDuration_minute() %> min)
													</option>
										<%
												}
											}
										%>
									</select>
								</div>
								<div class="showtimes-input-group">
									<label for="budget">Budget received (VND)</label>
									<input type="text" id="budget" name="budget" inputmode="numeric" autocomplete="off" required
										value="<%= vBudget %>" placeholder="e.g. 5,000,000,000">
									<small id="budgetHint"></small>
								</div>
								<div class="showtimes-input-group">
									<label for="price">Original ticket price (VND)</label>
									<input type="number" id="price" name="price" min="1" step="1" required value="<%= vPrice %>" placeholder="e.g. 90000">
									<small>One showtime costs: usable seats of the room &times; this price.</small>
								</div>
								<div class="showtimes-input-group">
									<label for="dateFrom">First day</label>
									<input type="date" id="dateFrom" name="dateFrom" required value="<%= vFrom %>">
								</div>
								<div class="showtimes-input-group">
									<label for="dateTo">Last day (max 31 days)</label>
									<input type="date" id="dateTo" name="dateTo" required value="<%= vTo %>">
								</div>
								<div class="showtimes-input-group">
									<label for="gapMinutes">Gap between showtimes (minutes)</label>
									<input type="number" id="gapMinutes" name="gapMinutes" min="0" max="120" step="5" value="<%= vGap %>">
									<small>Cleaning and advertisements.</small>
								</div>
								<div class="showtimes-input-group planner-time-pair">
									<label>Daily window (optional)</label>
									<div class="planner-time-row">
										<input type="time" id="earliest" name="earliest" value="<%= vEarliest %>" aria-label="Earliest start">
										<span>to</span>
										<input type="time" id="latest" name="latest" value="<%= vLatest %>" aria-label="Latest end">
									</div>
									<small>Empty = use the opening hours of the theater.</small>
								</div>
							</div>

							<div class="showtimes-input-group planner-rooms">
								<label>Rooms to use</label>
								<div class="planner-room-grid">
									<%
										if (rooms != null) {
											for (Rooms r : rooms) {
												int seats = seatCounts == null || seatCounts.get(r.getRoom_id()) == null ? 0 : seatCounts.get(r.getRoom_id());
												boolean checked = seats > 0 && (dp == null || dp.roomIds.contains(r.getRoom_id()));
									%>
												<label class="planner-room <%= seats == 0 ? "is-disabled" : "" %>">
													<input type="checkbox" name="roomIds" value="<%= r.getRoom_id() %>" <%= checked ? "checked" : "" %> <%= seats == 0 ? "disabled" : "" %>>
													<span class="planner-room-name"><%= ViewUtils.h(r.getRoom_name()) %></span>
													<span class="planner-room-seats"><%= seats == 0 ? "no usable seats" : seats + " seats" %></span>
												</label>
									<%
											}
										}
									%>
								</div>
							</div>

							<div class="showtimes-form-actions">
								<button type="submit" class="showtimes-button showtimes-button-primary">
									<%= draft == null ? "Generate plan" : "Regenerate plan" %>
								</button>
							</div>
						</form>
					</section>
					<%
						if (draft != null) {
							List<ShowtimePlannerService.Slot> slots = draft.getSlots();
							int autoCount = draft.getAutoSelectedCount();
							long autoCost = 0;
							long allCost = 0;
							TreeMap<LocalDate, List<ShowtimePlannerService.Slot>> byDay = new TreeMap<>();
							for (ShowtimePlannerService.Slot s : slots) {
								allCost += s.getCost();
								if (s.isSelected()) autoCost += s.getCost();
								byDay.computeIfAbsent(s.getStartAt().toLocalDate(), k -> new ArrayList<>()).add(s);
							}
							for (List<ShowtimePlannerService.Slot> list : byDay.values()) {
								list.sort(Comparator.comparing(ShowtimePlannerService.Slot::getStartAt)
										.thenComparing(ShowtimePlannerService.Slot::getRoomName));
							}
							boolean allUsed = !slots.isEmpty() && autoCount == slots.size();
					%>
					<section class="showtimes-section" id="plannerPreview"
						data-budget="<%= dp.budget %>" data-total="<%= slots.size() %>">
						<div class="showtimes-section-heading">
							<div>
								<h3 class="showtimes-section-title">2. Review and adjust</h3>
								<p class="showtimes-section-description">
									<strong><%= ViewUtils.h(dp.movieName) %></strong> &middot;
									<%= dp.dateFrom.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) %> &ndash; <%= dp.dateTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) %>
									<%
										if (dp.clientName != null) {
									%>
											&middot; for <strong><%= ViewUtils.h(dp.clientName) %></strong>
									<%
										}
									%>
								</p>
							</div>
							<span class="showtimes-count"><%= slots.size() %> possible slot(s)</span>
						</div>

						<%
							for (String w : draft.getWarnings()) {
						%>
								<div class="showtimes-message planner-warning"><%= ViewUtils.h(w) %></div>
						<%
							}
						%>
						<%
							if (allUsed) {
						%>
								<div class="showtimes-message planner-info">
									Every free slot in this period is already used and there is still budget left.
									To spend more of it, add rooms, lengthen the period or shorten the gap.
								</div>
						<%
							}
						%>

						<%
							if (!slots.isEmpty()) {
						%>
						<div class="planner-stats">
							<div class="planner-stat">
								<span class="planner-stat-label">Budget</span>
								<span class="planner-stat-value" id="statBudget"><%= money.format(dp.budget) %></span>
								<span class="planner-stat-unit">VND</span>
							</div>
							<div class="planner-stat">
								<span class="planner-stat-label">Showtimes</span>
								<span class="planner-stat-value" id="statCount"><%= autoCount %></span>
								<span class="planner-stat-unit">of <%= slots.size() %> possible</span>
							</div>
							<div class="planner-stat">
								<span class="planner-stat-label">Cost of showtimes</span>
								<span class="planner-stat-value" id="statCost"><%= money.format(autoCost) %></span>
								<span class="planner-stat-unit">VND</span>
							</div>
							<div class="planner-stat planner-stat-profit" id="statProfitBox">
								<span class="planner-stat-label">Leftover = profit</span>
								<span class="planner-stat-value" id="statProfit"><%= money.format(dp.budget - autoCost) %></span>
								<span class="planner-stat-unit">VND</span>
							</div>
						</div>

						<div class="planner-adjust">
							<div class="showtimes-input-group planner-adjust-count">
								<label for="countInput">Number of showtimes</label>
								<input type="number" id="countInput" min="0" max="<%= slots.size() %>" step="1" value="<%= autoCount %>">
							</div>
							<input type="range" id="countRange" min="0" max="<%= slots.size() %>" step="1" value="<%= autoCount %>" aria-label="Number of showtimes">
							<div class="planner-adjust-buttons">
								<button type="button" class="showtimes-button" id="btnAuto" data-auto="<%= autoCount %>">Reset to auto (<%= autoCount %>)</button>
								<button type="button" class="showtimes-button" id="btnClear">Clear</button>
								<button type="button" class="showtimes-button" id="btnExpand">Expand all days</button>
							</div>
						</div>
						<p class="showtimes-section-description planner-hint">
							Lowering the number removes the least valuable slots first (late and early shows).
							You can also untick single showtimes below. The total can never go above the budget.
						</p>

						<form id="confirmForm" action="${pageContext.request.contextPath}/manager-showtime-planner" method="post">
							<input type="hidden" name="action" value="confirm">
							<input type="hidden" name="plan" value="<%= draft.getToken() %>">

							<%
								int dayNo = 0;
								for (Map.Entry<LocalDate, List<ShowtimePlannerService.Slot>> day : byDay.entrySet()) {
							%>
							<details class="planner-day" <%= dayNo++ < 2 ? "open" : "" %>>
								<summary>
									<span class="planner-day-name"><%= day.getKey().format(dayFmt) %></span>
									<span class="planner-day-meta"><span class="day-selected">0</span> / <%= day.getValue().size() %> selected</span>
								</summary>
								<div class="showtimes-table-wrapper planner-table-wrapper">
									<table class="showtimes-table planner-table">
										<thead>
											<tr>
												<th></th>
												<th>Room</th>
												<th>Seats</th>
												<th>Start</th>
												<th>End</th>
												<th>Cost (VND)</th>
											</tr>
										</thead>
										<tbody>
										<%
											for (ShowtimePlannerService.Slot s : day.getValue()) {
										%>
											<tr>
												<td>
													<input type="checkbox" class="slot-check" name="slotId" value="<%= s.getId() %>"
														data-cost="<%= s.getCost() %>" data-order="<%= s.getId() %>" <%= s.isSelected() ? "checked" : "" %>
														aria-label="Include this showtime">
												</td>
												<td><%= ViewUtils.h(s.getRoomName()) %></td>
												<td><%= s.getSeatCount() %></td>
												<td><%= s.getStartAt().format(timeFmt) %></td>
												<td><%= s.getEndAt().format(timeFmt) %><%= s.getEndAt().toLocalDate().isAfter(s.getStartAt().toLocalDate()) ? " (+1)" : "" %></td>
												<td class="planner-num"><%= money.format(s.getCost()) %></td>
											</tr>
										<%
											}
										%>
										</tbody>
									</table>
								</div>
							</details>
							<%
								}
							%>

							<div class="showtimes-form-actions">
								<button type="submit" form="discardForm" class="showtimes-button">Discard plan</button>
								<button type="submit" id="btnConfirm" class="showtimes-button showtimes-button-primary">Create showtimes</button>
							</div>
							<p class="planner-confirm-note" id="confirmNote"></p>
						</form>
						<form id="discardForm" action="${pageContext.request.contextPath}/manager-showtime-planner" method="post" hidden>
							<input type="hidden" name="action" value="discard">
						</form>
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
				</div>
			</main>
		</section>
		<script src="<%= request.getContextPath() %>/scripts/manager_showtime_planner.js"></script>
	</body>
</html>