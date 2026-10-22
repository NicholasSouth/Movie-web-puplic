<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.Rooms" %>
<%@ page import="com.movieweb.model.Room_types" %>
<%
	request.setAttribute("currentManagerPage", "rooms");
	List<Rooms> rooms = (List<Rooms>) request.getAttribute("rooms");
	List<Room_types> roomTypes = (List<Room_types>) request.getAttribute("roomTypes");
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
	if (rooms == null) {
		rooms = new java.util.ArrayList<Rooms>();
	}
	if (roomTypes == null) {
		roomTypes = new java.util.ArrayList<Room_types>();
	}
	Object theaterValue = request.getAttribute("currentTheaterId");
	String currentTheaterId = theaterValue == null ? "" : String.valueOf(theaterValue);
	String currentTheaterName = null;
	if (theaters != null && !currentTheaterId.isEmpty()) {
		for (Theaters theater : theaters) {
			if (String.valueOf(theater.getTheater_id()).equals(currentTheaterId)) {
				currentTheaterName = theater.getTheater_name();
				break;
			}
		}
	}
	String successMessage = (String) session.getAttribute("managerRoomsFlashMessage");
	String flashType = (String) session.getAttribute("managerRoomsFlashType");
	if (successMessage != null) {
		session.removeAttribute("managerRoomsFlashMessage");
		session.removeAttribute("managerRoomsFlashType");
		if ("success".equals(flashType)) {
			request.setAttribute("successMessage", successMessage);
		} else {
			request.setAttribute("errorMessage", successMessage);
		}
	}
	String errorMessage = (String) request.getAttribute("errorMessage");
	String pageSuccessMessage = (String) request.getAttribute("successMessage");
%>
<%!
	private String escapeHtml(Object value) {
		if (value == null) {
			return "";
		}
		String text = String.valueOf(value);
		return text.replace("&", "&amp;")
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
		<title>Manage Rooms - PhnetPhlyx</title>
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/main_manager.css">
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/manager_rooms.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<section class="Layout2">
			<!-- Manager Header -->
			<jsp:include page="/components/header_manager.jsp" />
			<!-- Manager Navigation -->
			<%@ include file="components/manager_navigation.jsp" %>
			<!-- Main Content -->
			<main class="ManagerMain">
				<div class="ManagerRoomsPage">
					<!-- Page Header -->
					<div class="ManagerRoomsHeading">
						<div>
							<span class="ManagerRoomsBreadcrumb">
								Manage Theaters / Rooms
							</span>
							<h2>Manage Rooms</h2>
							<p class="ManagerRoomsSubtitle">
								Create rooms and manage their types and 
								availability for your currently selected theater.
							</p>
							<%
								if (!currentTheaterId.isEmpty()) {
							%>
									<div class="ManagerRoomsCurrentTheater">
										Current Theater: 
										<strong>
											<%= currentTheaterName == null
													? "Theater ID " + escapeHtml(currentTheaterId)
													: escapeHtml(currentTheaterName) %>
										</strong>
									</div>
							<%
								}
							%>
						</div>
					</div>
					<!-- Error Message -->
					<%
						if (errorMessage != null && !errorMessage.isEmpty()) {
					%>
							<div class="ManagerRoomsNotice ManagerRoomsNoticeError" role="alert">
								<%= escapeHtml(errorMessage) %>
							</div>
					<%
						}
					%>
					<!-- Success Message -->
					<%
						if (pageSuccessMessage != null && !pageSuccessMessage.isEmpty()) {
					%>
							<div class="ManagerRoomsNotice ManagerRoomsNoticeSuccess" role="status">
								<%= escapeHtml(pageSuccessMessage) %>
							</div>
					<%
						}
					%>
					<%
						if (currentTheaterId.isEmpty()) {
					%>
							<div class="ManagerRoomsEmptyState">
								<h2>No Theater Available</h2>
								<p>
									You do not currently have an active assigned 
									theater. Please contact an administrator.
								</p>
							</div>
					<%
						} else {
					%>
							<!-- Add Room -->
							<section class="ManagerRoomsPanel">
								<div class="ManagerRoomsPanelHeading">
									<div>
										<h2>Add Room</h2>
										<p>
											Create a room for the currently 
											selected theater.
										</p>
									</div>
								</div>
								<%
									if (roomTypes.isEmpty()) {
								%>
										<div class="ManagerRoomsNotice ManagerRoomsNoticeError">
											No room types are available. Add room types before creating a room.
										</div>
								<%
									} else {
								%>
										<form class="ManagerRoomCreateForm" action="${pageContext.request.contextPath}/manager-rooms" method="post">
											<input type="hidden" name="action" value="add">
											<div class="ManagerRoomsField">
												<label for="newRoomName">Room name</label>
												<input 
													type="text" 
													id="newRoomName" 
													name="room_name" 
													maxlength="100" 
													placeholder="e.g. Room 01" 
													required>
											</div>
											<div class="ManagerRoomsField">
												<label for="newRoomType">Room type</label>
												<select id="newRoomType" name="room_type_id" required>
													<option value="">Choose a room type</option>
													<%
														for (Room_types type : roomTypes) {
													%>
															<option value="<%= type.getRoom_type_id() %>">
																<%= escapeHtml(type.getRoom_type_name()) %> 
																(Price modifier: 
																<%= escapeHtml(type.getPrice_modify()) %>)
															</option>
													<%
														}
													%>
												</select>
											</div>
											<button type="submit" class="ManagerRoomsButton ManagerRoomsButtonPrimary">
												Add Room
											</button>
										</form>
								<%
									}
								%>
							</section>
							<!-- Room List -->
							<section class="ManagerRoomsPanel">
								<div class="ManagerRoomsPanelHeading">
									<div>
										<h2>Rooms</h2>
										<p>
											Inactive rooms remain listed here. 
											Deleted rooms are excluded.
										</p>
									</div>
									<span class="ManagerRoomsCount">
										<%= rooms.size() %> room(s)
									</span>
								</div>
								<%
									if (rooms.isEmpty()) {
								%>
										<div class="ManagerRoomsEmptyState">
											<h3>No rooms found</h3>
											<p>
												Add the first room for this theater using the form above.
											</p>
										</div>
								<%
									} else {
								%>
										<div class="ManagerRoomsTableWrapper">
											<table class="ManagerRoomsTable">
												<thead>
													<tr>
														<th>Room ID</th>
														<th>Room details</th>
														<th>Room type</th>
														<th>Status</th>
														<th>Actions</th>
													</tr>
												</thead>
												<tbody>
													<%
														for (Rooms room : rooms) {
													%>
															<tr>
																<td class="ManagerRoomsId">
																	#<%= room.getRoom_id() %>
																</td>
																<td>
																	<form 
																		id="editRoom<%= room.getRoom_id() %>" 
																		class="ManagerRoomEditForm" 
																		action="${pageContext.request.contextPath}/manager-rooms" 
																		method="post">
																		<a 
																			class="ManagerRoomsButton ManagerRoomsButtonSecondary"
																		   	href="${pageContext.request.contextPath}/manager-seats?room_id=<%= room.getRoom_id() %>">
																		    Seats
																		</a>
																		<input type="hidden" name="action" value="update">
																		<input type="hidden" name="room_id" value="<%= room.getRoom_id() %>">
																		<label class="ManagerRoomsVisuallyHidden" for="roomName<%= room.getRoom_id() %>">
																			Room name
																		</label>
																		<input 
																			type="text" 
																			id="roomName<%= room.getRoom_id() %>" 
																			name="room_name" 
																			value="<%= escapeHtml(room.getRoom_name()) %>" 
																			maxlength="100" 
																			required>
																	</form>
																</td>
																<td>
																	<label class="ManagerRoomsVisuallyHidden" for="roomType<%= room.getRoom_id() %>">
																		Room type
																	</label>
																	<select form="editRoom<%= room.getRoom_id() %>" id="roomType<%= room.getRoom_id() %>" name="room_type_id" required>
																		<%
																			for (Room_types type : roomTypes) {
																		%>
																				<option value="<%= type.getRoom_type_id() %>" <%= type.getRoom_type_id() == room.getRoom_type_id() ? "selected" : "" %>>
																					<%= escapeHtml(type.getRoom_type_name()) %> 
																					(<%= escapeHtml(type.getPrice_modify()) %>)
																				</option>
																		<%
																			}
																		%>
																	</select>
																</td>
																<td>
																	<%
																		if (room.isActive()) {
																	%>
																			<span class="ManagerRoomsStatus ManagerRoomsStatusActive">
																				ACTIVE
																			</span>
																	<%
																		} else {
																	%>
																			<span class="ManagerRoomsStatus ManagerRoomsStatusInactive">
																				INACTIVE
																			</span>
																	<%
																		}
																	%>
																</td>
																<td>
																	<div class="ManagerRoomsActions">
																		<button type="submit" form="editRoom<%= room.getRoom_id() %>" class="ManagerRoomsButton ManagerRoomsButtonSecondary">
																			Save
																		</button>
																		<form action="${pageContext.request.contextPath}/manager-rooms" method="post" onsubmit="return confirm('Are you sure you want to <%= room.isActive() ? "deactivate" : "activate" %> this room?');">
																			<input type="hidden" name="action" value="<%= room.isActive() ? "deactivate" : "activate" %>">
																			<input type="hidden" name="room_id" value="<%= room.getRoom_id() %>">
																			<button type="submit" class="ManagerRoomsButton <%= room.isActive() ? "ManagerRoomsButtonWarning" : "ManagerRoomsButtonSuccess" %>">
																				<%= room.isActive() ? "Deactivate" : "Activate" %>
																			</button>
																		</form>
																		<form action="${pageContext.request.contextPath}/manager-rooms" method="post" onsubmit="return confirm('Soft-delete this room? This action may be blocked if the room has relevant future showtimes or bookings.');">
																			<input type="hidden" name="action" value="delete">
																			<input type="hidden" name="room_id" value="<%= room.getRoom_id() %>">
																			<button type="submit" class="ManagerRoomsButton ManagerRoomsButtonDanger">
																				Delete
																			</button>
																		</form>
																	</div>
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
							</section>
					<%
						}
					%>
				</div>
			</main>
		</section>
	</body>
</html>
