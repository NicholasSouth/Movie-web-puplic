<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.movieweb.model.Rooms" %>
<%@ page import="com.movieweb.model.Seats" %>
<%@ page import="com.movieweb.model.Seat_types" %>
<%
	String contextPath = request.getContextPath();
	Rooms room = (Rooms) request.getAttribute("room");
	List<Seats> seats = (List<Seats>) request.getAttribute("seats");
	List<Seat_types> seatTypes = (List<Seat_types>) request.getAttribute("seatTypes");
	String successMessage = (String) request.getAttribute("successMessage");
	String errorMessage = (String) request.getAttribute("errorMessage");
	if (seats == null) {
		seats = new ArrayList<Seats>();
	}
	if (seatTypes == null) {
		seatTypes = new ArrayList<Seat_types>();
	}
	List<String> rowNames = new ArrayList<String>();
	int maxCol = 0;
	for (Seats seat : seats) {
		String row = seat.getSeat_row() == null ? "" : seat.getSeat_row().trim();
		if (!row.isEmpty() && !rowNames.contains(row)) {
			rowNames.add(row);
		}
		if (seat.getSeat_col() > maxCol) {
			maxCol = seat.getSeat_col();
		}
	}
	java.util.Collections.sort(rowNames);
	Integer roomId = room == null ? null : room.getRoom_id();
%>
<%!
	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#x27;");
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Manage Seats | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= contextPath %>/styles/main_manager.css">
		<link rel="stylesheet" href="<%= contextPath %>/styles/manager_seats.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<div class="Layout2">
			<jsp:include page="/components/header_manager.jsp" />
			<jsp:include page="/components/manager_navigation.jsp" />
			<main class="ManagerMain ManagerSeatsMain">
				<div class="ManagerSeatsPage">
					<div class="ManagerSeatsHeading">
						<div>
							<p class="ManagerSeatsEyebrow">MANAGE THEATERS / ROOMS</p>
							<h1>Seat Management</h1>
							<p class="ManagerSeatsSubtitle">
								<%
									if (room != null) {
								%>
										Manage the seat layout for <strong><%= escapeHtml(room.getRoom_name()) %></strong>.
								<%
									} else {
								%>
										Manage this room's seat layout.
								<%
									}
								%>
							</p>
						</div>
						<a class="ManagerSeatsButton ManagerSeatsButtonSecondary" href="<%= contextPath %>/manager-rooms">
							Back to Rooms
						</a>
					</div>
					<%
						if (successMessage != null && !successMessage.trim().isEmpty()) {
					%>
							<div class="ManagerSeatsNotice ManagerSeatsNoticeSuccess" role="status">
								<%= escapeHtml(successMessage) %>
							</div>
					<%
						}
					%>
					<%
						if (errorMessage != null && !errorMessage.trim().isEmpty()) {
					%>
							<div class="ManagerSeatsNotice ManagerSeatsNoticeError" role="alert">
								<%= escapeHtml(errorMessage) %>
							</div>
					<%
						}
					%>
					<%
						if (room != null && roomId != null) {
					%>
							<section class="ManagerSeatsPanel ManagerSeatsLayoutPanel">
								<div class="ManagerSeatsPanelHeading">
									<div>
										<h2>Seat Layout</h2>
										<p>Select a seat on the map to manage its type or status.</p>
									</div>
									<div class="ManagerSeatsStats">
										<span><strong><%= seats.size() %></strong> seats</span>
										<span><strong><%= rowNames.size() %></strong> rows</span>
										<span><strong><%= maxCol %></strong> columns</span>
									</div>
								</div>
								<div class="ManagerSeatsLegend">
									<span class="legend-item"><span class="swatch"></span> Active</span>
									<span class="legend-item"><span class="swatch selected"></span> Selected</span>
									<span class="legend-item"><span class="swatch booked"></span> Has booking / hold</span>
									<span class="legend-item"><span class="swatch unavailable"></span> Inactive</span>
								</div>
								<%
									if (seats.isEmpty()) {
								%>
										<div class="ManagerSeatsEmpty">
											<h3>This room has no seats yet</h3>
											<p>Generate a full layout or add seats one at a time using the tools below.</p>
										</div>
								<%
									} else {
								%>
										<div class="theater ManagerSeatsTheater">
											<div class="screen">SCREEN</div>
											<div class="seats ManagerSeatsGrid" style="--manager-seat-columns: <%= Math.max(1, maxCol) %>;">
												<%
													for (int rowIndex = 0; rowIndex < rowNames.size(); rowIndex++) {
														String rowName = rowNames.get(rowIndex);
														for (Seats seat : seats) {
															String seatRow = seat.getSeat_row() == null ? "" : seat.getSeat_row().trim();
															if (!rowName.equals(seatRow)) {
																continue;
															}
															String typeName = seat.getSeat_type_name() == null
																	? "Normal" : seat.getSeat_type_name().trim();
															String typeClass = " type-" + typeName.toLowerCase().replaceAll("[^a-z0-9_-]", "");
															String stateClass = "";
															String seatLabel = seatRow + seat.getSeat_col();
															if (!seat.isActive()) {
																stateClass = " unavailable";
																seatLabel = "X";
																typeClass = "";
															}
															String seatTitle = seatRow + seat.getSeat_col() + " - " + typeName
																	+ (seat.isActive() ? " - Active" : " - Inactive");
												%>
															<button 
																type="button" 
																class="seat ManagerSeatButton<%= typeClass %><%= stateClass %>" 
																style="grid-row: <%= rowIndex + 1 %>; grid-column: <%= seat.getSeat_col() %>;" 
																data-seat-id="<%= seat.getSeat_id() %>" 
																data-seat-row="<%= escapeHtml(seatRow) %>" 
																data-seat-col="<%= seat.getSeat_col() %>" 
																data-seat-type-id="<%= seat.getSeat_type_id() %>" 
																data-seat-type-name="<%= escapeHtml(typeName) %>" 
																data-seat-active="<%= seat.isActive() %>" 
																title="<%= escapeHtml(seatTitle) %>" 
																aria-label="<%= escapeHtml(seatTitle) %>">
																<span class="seat-label"><%= escapeHtml(seatLabel) %></span>
															</button>
												<%
														}
													}
												%>
											</div>
										</div>
								<%
									}
								%>
							</section>
							<div class="ManagerSeatsTools">
								<section class="ManagerSeatsPanel">
									<div class="ManagerSeatsPanelHeading">
										<div>
											<h2>Generate Full Layout</h2>
											<p>Only available when this room has no seat records yet.</p>
										</div>
									</div>
									<form method="post" action="<%= contextPath %>/manager-seats" class="ManagerSeatsForm">
										<input type="hidden" name="action" value="generate">
										<input type="hidden" name="room_id" value="<%= roomId %>">
										<label>
											Number of rows (A–Z)
											<input type="number" name="row_count" min="1" max="26" required>
										</label>
										<label>
											Seats per row
											<input type="number" name="column_count" min="1" max="100" required>
										</label>
										<label>
											Default seat type
											<select name="seat_type_id" required>
												<option value="">Choose seat type</option>
												<%
													for (Seat_types type : seatTypes) {
												%>
														<option value="<%= type.getSeat_type_id() %>">
															<%= escapeHtml(type.getSeat_type_name()) %> 
															(<%= escapeHtml(type.getPrice_modify()) %>)
														</option>
												<%
													}
												%>
											</select>
										</label>
										<button type="submit" class="ManagerSeatsButton" onclick="return confirm('Generate this seat layout? This action is only for an empty room.');">
											Generate Seats
										</button>
									</form>
								</section>
								<section class="ManagerSeatsPanel">
									<div class="ManagerSeatsPanelHeading">
										<div>
											<h2>Add Individual Seat</h2>
											<p>Add a seat at a specific row and column.</p>
										</div>
									</div>
									<form method="post" action="<%= contextPath %>/manager-seats" class="ManagerSeatsForm">
										<input type="hidden" name="action" value="addSeat">
										<input type="hidden" name="room_id" value="<%= roomId %>">
										<label>
											Row (A–Z)
											<input 
												type="text" 
												name="seat_row" 
												minlength="1" 
												maxlength="1" 
												pattern="[A-Za-z]" 
												placeholder="e.g. A" 
												required>
										</label>
										<label>
											Column
											<input 
												type="number" 
												name="seat_col" 
												min="1" 
												max="100" 
												placeholder="e.g. 1" 
												required>
										</label>
										<label>
											Seat type
											<select name="seat_type_id" required>
												<option value="">Choose seat type</option>
												<%
													for (Seat_types type : seatTypes) {
												%>
														<option value="<%= type.getSeat_type_id() %>">
															<%= escapeHtml(type.getSeat_type_name()) %> 
															(<%= escapeHtml(type.getPrice_modify()) %>)
														</option>
												<%
													}
												%>
											</select>
										</label>
										<button type="submit" class="ManagerSeatsButton">Add Seat</button>
									</form>
								</section>
							</div>
							<section class="ManagerSeatsPanel ManagerSeatsEditorPanel">
								<div class="ManagerSeatsPanelHeading">
									<div>
										<h2>Selected Seat</h2>
										<p id="ManagerSeatEditorHint">Select a seat in the map to edit it.</p>
									</div>
								</div>
								<div id="ManagerSeatEditor" class="ManagerSeatEditor" hidden>
									<div class="ManagerSeatSelectedSummary">
										<span class="ManagerSeatSelectedLabel">Seat</span>
										<strong id="ManagerSelectedSeatLabel">—</strong>
										<span id="ManagerSelectedSeatStatus" class="ManagerSeatStatus">—</span>
									</div>
									<div class="ManagerSeatEditorActions">
										<form method="post" action="<%= contextPath %>/manager-seats" class="ManagerSeatsForm ManagerSeatTypeForm">
											<input type="hidden" name="action" value="updateType">
											<input type="hidden" name="room_id" value="<%= roomId %>">
											<input type="hidden" name="seat_id" id="ManagerEditSeatId">
											<label>
												Seat type
												<select name="seat_type_id" id="ManagerEditSeatType" required>
													<%
														for (Seat_types type : seatTypes) {
													%>
															<option value="<%= type.getSeat_type_id() %>">
																<%= escapeHtml(type.getSeat_type_name()) %> 
																(<%= escapeHtml(type.getPrice_modify()) %>)
															</option>
													<%
														}
													%>
												</select>
											</label>
											<button type="submit" class="ManagerSeatsButton">Update Type</button>
										</form>
										<form method="post" action="<%= contextPath %>/manager-seats" id="ManagerSeatStatusForm">
											<input type="hidden" name="room_id" value="<%= roomId %>">
											<input type="hidden" name="seat_id" id="ManagerStatusSeatId">
											<input type="hidden" name="action" id="ManagerSeatStatusAction">
											<button type="submit" class="ManagerSeatsButton ManagerSeatsButtonSecondary" id="ManagerSeatStatusButton">
												Deactivate Seat
											</button>
										</form>
										<form method="post" action="<%= contextPath %>/manager-seats" id="ManagerSeatDeleteForm">
											<input type="hidden" name="action" value="delete">
											<input type="hidden" name="room_id" value="<%= roomId %>">
											<input type="hidden" name="seat_id" id="ManagerDeleteSeatId">
											<button type="submit" class="ManagerSeatsButton ManagerSeatsButtonDanger" onclick="return confirm('Soft-delete this seat? It will be removed from the active layout and its record will be retained.');">
												Delete Seat
											</button>
										</form>
									</div>
									<p class="ManagerSeatsEditorNote">
										Seats with relevant confirmed bookings or active holds cannot be deactivated or deleted.
									</p>
								</div>
							</section>
					<%
						}
					%>
				</div>
			</main>
		</div>
		<script>
		(function () {
			"use strict";
			var seatButtons = document.querySelectorAll(".ManagerSeatButton");
			var editor = document.getElementById("ManagerSeatEditor");
			var hint = document.getElementById("ManagerSeatEditorHint");
			if (!editor || !seatButtons.length) {
				return;
			}
			var selectedLabel = document.getElementById("ManagerSelectedSeatLabel");
			var selectedStatus = document.getElementById("ManagerSelectedSeatStatus");
			var editSeatId = document.getElementById("ManagerEditSeatId");
			var editSeatType = document.getElementById("ManagerEditSeatType");
			var statusSeatId = document.getElementById("ManagerStatusSeatId");
			var statusAction = document.getElementById("ManagerSeatStatusAction");
			var statusButton = document.getElementById("ManagerSeatStatusButton");
			var deleteSeatId = document.getElementById("ManagerDeleteSeatId");
			seatButtons.forEach(function (button) {
				button.addEventListener("click", function () {
					seatButtons.forEach(function (item) {
						item.classList.remove("selected");
					});
					button.classList.add("selected");
					var seatId = button.getAttribute("data-seat-id");
					var row = button.getAttribute("data-seat-row");
					var col = button.getAttribute("data-seat-col");
					var typeId = button.getAttribute("data-seat-type-id");
					var active = button.getAttribute("data-seat-active") === "true";
					selectedLabel.textContent = row + col;
					selectedStatus.textContent = active ? "Active" : "Inactive";
					selectedStatus.className = "ManagerSeatStatus " + (active ? "is-active" : "is-inactive");
					editSeatId.value = seatId;
					editSeatType.value = typeId;
					statusSeatId.value = seatId;
					statusAction.value = active ? "deactivate" : "activate";
					statusButton.textContent = active ? "Deactivate Seat" : "Activate Seat";
					deleteSeatId.value = seatId;
					hint.textContent = "Editing seat " + row + col + ".";
					editor.hidden = false;
					editor.scrollIntoView({ behavior: "smooth", block: "nearest" });
				});
			});
			var statusForm = document.getElementById("ManagerSeatStatusForm");
			statusForm.addEventListener("submit", function (event) {
				var action = statusAction.value;
				var message = action === "deactivate"
					? "Deactivate this seat? Seats with relevant bookings or active holds will be protected."
					: "Activate this seat?";
				if (!window.confirm(message)) {
					event.preventDefault();
				}
			});
		})();
		</script>
	</body>
</html>