<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.AdminRefundRow" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.net.URLEncoder" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "refunds");
	String ctx = request.getContextPath();
	List<AdminRefundRow> rows = (List<AdminRefundRow>) request.getAttribute("refundRows");
	if (rows == null) {
		rows = Collections.emptyList();
	}
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("adminTheaters");
	if (theaters == null) {
		theaters = Collections.emptyList();
	}
	Integer selectedTheaterId = (Integer) request.getAttribute("adminTheaterId");
	String view = (String) request.getAttribute("view");
	boolean pendingView = !"APPROVED".equals(view);
	String q = (String) request.getAttribute("q");
	String qEnc = URLEncoder.encode(q == null ? "" : q, "UTF-8");
	Integer pageAttr = (Integer) request.getAttribute("page");
	Integer pagesAttr = (Integer) request.getAttribute("pages");
	Integer totalAttr = (Integer) request.getAttribute("totalRefunds");
	int pageNo = pageAttr == null ? 1 : pageAttr;
	int pageTotal = pagesAttr == null ? 1 : pagesAttr;
	int total = totalAttr == null ? 0 : totalAttr;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	String[][] tabs = {{"PENDING", "Pending requests"}, {"APPROVED", "Approved refunds"}};
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
		<title>Refunds | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Refunds</h2>
						<p>Review refund requests submitted by theater managers. <%= total %> <%= pendingView ? "pending" : "approved" %> in this view.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-refunds">
						<input type="hidden" name="view" value="<%= esc(view) %>">
						<label for="refund-theater">Theater</label>
						<select id="refund-theater" name="theaterId">
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
						<input class="admin-input" type="text" name="q" maxlength="100" placeholder="Booking # or transaction code" value="<%= esc(q) %>">
						<button type="submit" class="admin-btn">Filter</button>
					</form>
				</div>

				<%
					if (flash != null) {
				%>
						<div class="admin-alert admin-alert-success"><%= esc(flash) %></div>
				<%
					}
				%>
				<%
					if (flashError != null) {
				%>
						<div class="admin-alert admin-alert-error"><%= esc(flashError) %></div>
				<%
					}
				%>
				<%
					if (loadError) {
				%>
						<div class="admin-alert admin-alert-error">The refund data could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<nav class="admin-tabs">
					<%
						for (String[] tab : tabs) {
					%>
							<a class="admin-tab <%= tab[0].equals(view) ? "active" : "" %>"
								href="<%= ctx %>/admin-refunds?view=<%= tab[0] %>&q=<%= qEnc %>"><%= tab[1] %></a>
					<%
						}
					%>
				</nav>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p><%= loadError ? "The list could not be loaded." : (pendingView ? "No refund requests are waiting for review." : "No approved refunds match this filter.") %></p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Booking</th>
												<th>Customer</th>
												<th>Showtime</th>
												<th>Theater</th>
												<th>Tickets</th>
												<th>Payment</th>
												<th>Booking status</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminRefundRow r : rows) {
											%>
													<tr>
														<td>
															<strong>#<%= r.getBookingId() %></strong>
															<br>
															<span class="admin-muted"><%= r.getBookAt() == null ? "--" : df.format(r.getBookAt()) %></span>
														</td>
														<td>
															<%= esc(r.getCustomerName()) %>
															<br>
															<span class="admin-muted"><%= esc(r.getCustomerEmail()) %></span>
														</td>
														<td>
															<%= esc(r.getMovieName()) %>
															<br>
															<span class="admin-muted"><%= r.getShowtimeStart() == null ? "--" : df.format(r.getShowtimeStart()) %></span>
														</td>
														<td>
															<%= esc(r.getTheaterName()) %>
															<br>
															<span class="admin-muted"><%= esc(r.getRoomName()) %></span>
														</td>
														<td><%= r.getTicketCount() %></td>
														<td>
															<%= String.format("%,d", r.getPaymentAmount()) %> VND<br>
															<span class="admin-muted"><%= esc(r.getPaymentMethod()) %></span><br>
															<span class="admin-transaction-code"><%= esc(r.getTransactionCode()) %></span><br>
															<span class="admin-status admin-status-<%= esc(r.getPaymentStatus() == null ? "" : r.getPaymentStatus().toLowerCase()) %>"><%= esc(r.getPaymentStatus()) %></span>
														</td>
														<td>
															<span class="admin-status admin-status-<%= esc(r.getBookingStatus() == null ? "" : r.getBookingStatus().toLowerCase()) %>"><%= esc(r.getBookingStatus()) %></span>
														</td>
														<td class="admin-actions">
															<%
																if (pendingView) {
															%>
																	<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-refunds">
																		<input type="hidden" name="bookingId" value="<%= r.getBookingId() %>">
																		<input type="hidden" name="view" value="<%= esc(view) %>">
																		<input type="hidden" name="q" value="<%= esc(q) %>">
																		<input type="hidden" name="page" value="<%= pageNo %>">
																		<button class="admin-btn admin-btn-primary" name="action" value="approve"
																			onclick="return confirm('Approve this refund? The booking becomes CANCELLED and its payment REFUNDED. This cannot be undone here.');">Approve</button>
																		<button class="admin-btn admin-btn-danger" name="action" value="reject"
																			onclick="return confirm('Reject this refund? The booking returns to CONFIRMED and the payment stays as paid.');">Reject</button>
																	</form>
															<%
																} else {
															%>
																	<span class="admin-muted">Completed</span>
															<%
																}
															%>
														</td>
													</tr>
											<%
												}
											%>
										</tbody>
									</table>
								</div>
								<div class="admin-pagination">
									<%
										if (pageNo > 1) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-refunds?view=<%= esc(view) %>&q=<%= qEnc %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-refunds?view=<%= esc(view) %>&q=<%= qEnc %>&page=<%= pageNo + 1 %>">Next</a>
									<%
										}
									%>
								</div>
						<%
							}
						%>
						<p class="admin-muted">
							A request is a booking in REFUNDED status whose payment is still SUCCESS. Approving moves the booking to CANCELLED and the payment to REFUNDED
							together. Rejecting returns the booking to CONFIRMED. Either decision sends an email to the booking owner's address (for counter sales that is the manager's account); if the email fails, the decision still stands. The money itself is not returned by this system; refund the customer through the payment provider.
							Rejected requests are not kept in a history, because the database has no table or status column for that.
						</p>
					</div>
				</section>
			</main>
		</section>
	</body>
</html>