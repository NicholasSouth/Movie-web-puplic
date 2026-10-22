<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List,java.util.ArrayList" %>
<%@ page import="com.movieweb.model.Seats" %>
<%
	String contextPath = request.getContextPath();
	List<Seats> seats = (List<Seats>) request.getAttribute("seats");
	List<String> rowNames = new ArrayList<>();
	int maxCol = 0;
	if (seats != null) {
		for (Seats seat : seats) {
			String row = seat.getSeat_row().trim();
			if (!rowNames.contains(row)) {
				rowNames.add(row);
			}
			if (seat.getSeat_col() > maxCol) {
				maxCol = seat.getSeat_col();
			}
		}
	}
	String bookingError = (String) request.getAttribute("bookingError");
	Integer showtimeId = (Integer) request.getAttribute("showtimeId");
	Integer ticketTypeId = (Integer) request.getAttribute("ticketTypeId");
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta
			name="viewport"
			content="width=device-width, initial-scale=1">
		<title>POS Seat Selection</title>
		<link
			rel="stylesheet"
			href="<%= contextPath %>/styles/main_manager.css">
		<link
			rel="stylesheet"
			href="<%= contextPath %>/styles/pos_seat_selection.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<div class="theater">
			<div class="screen">
				SCREEN
			</div>
			<div
				class="seats"
				style="
					grid-template-columns:
						repeat(
							<%= maxCol %>,
							minmax(0, 1fr)
						);
					grid-template-rows:
						repeat(
							<%= rowNames.size() %>,
							minmax(
								var(--seat-size),
								1fr
							)
						);
				">
				<%
					if (seats != null) {
						for (Seats seat : seats) {
							String row = seat.getSeat_row().trim();
							String stateClass = "";
							String label = row + seat.getSeat_col();
							String typeName = seat.getSeat_type_name();
							String typeClass = "";
							String typeLabel = "";
							if (typeName != null && !typeName.trim().equalsIgnoreCase("Normal")) {
								typeClass = " type-" + typeName.trim().toLowerCase();
								typeLabel = typeName.trim();
							}
							if (!seat.isActive()) {
								stateClass = " unavailable";
								label = "X";
								typeClass = "";
								typeLabel = "";
							} 
							else if (seat.isBooked()) {
								stateClass = " booked";
							}
				%>
							<div
								class="seat<%= typeClass %><%= stateClass %>"
								data-seat-id="<%= seat.getSeat_id() %>"
								data-name="<%= row %><%= seat.getSeat_col() %>"
								data-price="<%= seat.getPrice() %>"
								style="
									grid-row:
										<%= rowNames.indexOf(row) + 1 %>;
									grid-column:
										<%= seat.getSeat_col() %>;
								">
								<span class="seat-label">
									<%= label %>
								</span>
								<%
									if (!typeLabel.isEmpty()) {
								%>
										<span class="seat-type">
											<%= typeLabel %>
										</span>
								<%
									}
								%>
							</div>
				<%
						}
					}
				%>
			</div>
			<div class="legend">
				<div class="legend-item">
					<span class="swatch"></span>
					Available
				</div>
				<div class="legend-item">
					<span class="swatch selected"></span>
					Selected
				</div>
				<div class="legend-item">
					<span class="swatch booked"></span>
					Booked
				</div>
				<div class="legend-item">
					<span class="swatch unavailable">X</span>
					Unavailable
				</div>
			</div>
		</div>
		<!-- Error message -->
		<%
			if ("seat".equals(bookingError)) {
		%>
				<p class="booking-error">
					One or more selected seats are unavailable.
					Please select your seats again.
				</p>
		<%
			} 
			else if ("promotion".equals(bookingError)) {
		%>
				<p class="booking-error">
					The promotion code is invalid, inactive,
					expired, or has reached its usage limit.
				</p>
		<%
			} 
			else if ("booking".equals(bookingError)) {
		%>
				<p class="booking-error">
					Booking failed. Please select your seats again
					and try again.
				</p>
		<%
			}
		%>
		<div class="summary">
			<div class="summary-info">
				<span>
					Seat:
					<span class="summary-seat">-</span>
				</span>
				<span class="summary-total">
					Total: 0đ
				</span>
			</div>
			<form
				id="bookingForm"
				class="confirm-form"
				action="<%= contextPath %>/manager-create-booking"
				method="post">
				<input
					type="hidden"
					name="showtime_id"
					value="<%= showtimeId %>">
				<input
					type="hidden"
					name="ticket_type_id"
					value="<%= ticketTypeId %>">
				<!-- Promotion -->
				<div class="promotion-container">
					<input
						type="text"
						name="promotion_code"
						id="promotionCode"
						class="promotion-input"
						placeholder="Promotion code"
						maxlength="20">
					<button
						type="button"
						id="applyPromotionBtn"
						class="promotion-btn">
						Apply
					</button>
				</div>
				<p
					id="promotionMessage"
					class="promotion-message">
				</p>
				<button
					type="submit"
					class="confirm-btn"
					disabled>
					Confirm
				</button>
			</form>
		</div>
		<script
			src="<%= contextPath %>/scripts/pos_seat_selection.js">
		</script>
	</body>
</html>