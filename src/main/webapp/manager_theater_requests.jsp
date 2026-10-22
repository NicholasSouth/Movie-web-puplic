<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.ManagerTheaterOption" %>
<%@ page import="com.movieweb.model.ManagerTheaterRequest" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%
	/* Always load the page through the servlet. */
	if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD) {
		response.sendRedirect(request.getContextPath() + "/manager-theater-requests");
		return;
	}
	Users loggedInUser = (Users) session.getAttribute("user");
	if (loggedInUser == null || !"MANAGER".equalsIgnoreCase(loggedInUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	List<ManagerTheaterOption> options = (List<ManagerTheaterOption>) request.getAttribute("theaterOptions");
	List<ManagerTheaterRequest> history = (List<ManagerTheaterRequest>) request.getAttribute("requestHistory");
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Theater Requests - Manager</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_manager.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_profile.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_theater_requests.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout2">
			<jsp:include page="/components/header_manager.jsp" />
			<%@ include file="components/manager_navigation.jsp" %>
			
			<main class="ManagerMain manager-profile-main manager-requests-main">
				<!-- Page heading -->
				<section class="profile-section">
					<div class="section-header">
						<div>
							<h2>Theater Requests</h2>
							<p class="section-description">
								Ask an administrator to assign you to a theater or remove you from one.
								Changes only take effect after an administrator approves the request.
							</p>
						</div>
						<a href="<%= contextPath %>/manager-profile#assigned-theaters" class="request-back-link">Back to profile</a>
					</div>
					
					<%
						if (flash != null) {
					%>
							<div class="request-alert request-alert-success" role="status"><%= ViewUtils.h(flash) %></div>
					<%
						}
					%>
					<%
						if (flashError != null) {
					%>
							<div class="request-alert request-alert-error" role="alert"><%= ViewUtils.h(flashError) %></div>
					<%
						}
					%>
					<%
						if (loadError) {
					%>
							<div class="manager-load-error" role="alert">
								<p>Unable to load theater requests. Please try again.</p>
								<a href="<%= contextPath %>/manager-theater-requests" class="theater-button">Try Again</a>
							</div>
					<%
						}
					%>
					
					<!-- Theater list -->
					<%
						if (options != null && !options.isEmpty()) {
					%>
							<div class="request-list">
								<%
									for (ManagerTheaterOption o : options) {
										String pending = o.getPendingType();
										String stateLabel;
										String stateClass;
										if ("ASSIGN".equals(pending)) {
											stateLabel = "Assignment requested";
											stateClass = "request-badge-pending";
										} else if ("REMOVE".equals(pending)) {
											stateLabel = "Removal requested";
											stateClass = "request-badge-pending";
										} else if (o.isAssigned()) {
											stateLabel = "Assigned";
											stateClass = "request-badge-assigned";
										} else {
											stateLabel = "Not assigned";
											stateClass = "request-badge-none";
										}
								%>
										<article class="request-row">
											<div class="request-row-info">
												<h3>
													<%= ViewUtils.h(o.getTheaterName()) %>
													<%
														if (!o.isActive()) {
													%>
															<span class="request-badge request-badge-none">Inactive</span>
													<%
														}
													%>
												</h3>
												<p><%= ViewUtils.h(o.getTheaterAddress()) %></p>
											</div>
											<span class="request-badge <%= stateClass %>"><%= stateLabel %></span>
											
											<%
												if (pending == null) {
											%>
													<form class="request-form" method="post" action="<%= contextPath %>/manager-theater-requests">
														<input type="hidden" name="theaterId" value="<%= o.getTheaterId() %>">
														<input type="hidden" name="action" value="<%= o.isAssigned() ? "remove" : "assign" %>">
														<input type="text" name="note" maxlength="500" placeholder="Note for the administrator (optional)"
														       aria-label="Note for the administrator">
														<button type="submit" class="<%= o.isAssigned() ? "request-button-danger" : "theater-button" %>"
														        <% if (o.isAssigned()) { %>onclick="return confirm('Request removal from this theater?');"<% } %>>
															<%= o.isAssigned() ? "Request removal" : "Request assignment" %>
														</button>
													</form>
											<%
												} else {
											%>
													<span class="request-waiting">Waiting for review</span>
											<%
												}
											%>
										</article>
								<%
									}
								%>
							</div>
					<%
						} else if (!loadError) {
					%>
							<div class="booking-placeholder">
								<h3>No theaters available</h3>
								<p>There are no active theaters to request right now.</p>
							</div>
					<%
						}
					%>
				</section>
				
				<!-- Request history -->
				<section class="profile-section">
					<div class="section-header">
						<div>
							<h2>My Requests</h2>
							<p class="section-description">Your latest 20 requests.</p>
						</div>
					</div>
					<%
						if (history != null && !history.isEmpty()) {
					%>
							<div class="request-table-wrap">
								<table class="request-table">
									<thead>
										<tr>
											<th>Sent</th>
											<th>Theater</th>
											<th>Type</th>
											<th>Status</th>
											<th>Note</th>
											<th></th>
										</tr>
									</thead>
									<tbody>
										<%
											for (ManagerTheaterRequest r : history) {
												String st = r.getStatus();
												String cls = "PENDING".equals(st) ? "request-badge-pending"
														: "APPROVED".equals(st) ? "request-badge-assigned"
														: "request-badge-none";
										%>
												<tr>
													<td><%= ViewUtils.dateTime(r.getCreatedAt()) %></td>
													<td><%= ViewUtils.h(r.getTheaterName()) %></td>
													<td><%= "ASSIGN".equals(r.getRequestType()) ? "Assign" : "Remove" %></td>
													<td>
														<span class="request-badge <%= cls %>"><%= ViewUtils.h(st) %></span>
													</td>
													<td>
														<%= ViewUtils.h(ViewUtils.text(r.getNote(), "-")) %>
														<%
															if (ViewUtils.hasText(r.getReviewNote())) {
														%>
																<br>
																<span class="request-review-note">Admin: <%= ViewUtils.h(r.getReviewNote()) %></span>
														<%
															}
														%>
													</td>
													<td>
														<%
															if ("PENDING".equals(st)) {
														%>
																<form method="post" action="<%= contextPath %>/manager-theater-requests">
																	<input type="hidden" name="action" value="cancel">
																	<input type="hidden" name="requestId" value="<%= r.getRequestId() %>">
																	<button type="submit" class="request-button-link">Cancel</button>
																</form>
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
					<%
						} else if (!loadError) {
					%>
							<div class="booking-placeholder">
								<h3>No requests yet</h3>
								<p>Requests you send will appear here.</p>
							</div>
					<%
						}
					%>
				</section>
			</main>
		</section>
	</body>
</html>