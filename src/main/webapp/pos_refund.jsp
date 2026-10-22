<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.net.URLEncoder" %>
<%
	String contextPath = request.getContextPath();
	List<Map<String, Object>> refundHistory = (List<Map<String, Object>>) request.getAttribute("refundHistory");
	String selectedStatus = (String) request.getAttribute("selectedStatus");
	if (selectedStatus == null) {
		selectedStatus = request.getParameter("status");
	}
	if (!"REFUNDED".equals(selectedStatus) && !"CANCELLED".equals(selectedStatus)) {
		selectedStatus = "ALL";
	}
	String transactionCode = (String) request.getAttribute("transactionCode");
	if (transactionCode == null) {
		transactionCode = "";
	}
	Integer currentPageAttribute = (Integer) request.getAttribute("currentPage");
	int currentPage = currentPageAttribute != null ? currentPageAttribute : 1;
	Integer totalPagesAttribute = (Integer) request.getAttribute("totalPages");
	int totalPages = totalPagesAttribute != null ? totalPagesAttribute : 1;
	Integer totalBookingsAttribute = (Integer) request.getAttribute("totalBookings");
	int displayedCount = totalBookingsAttribute != null ? totalBookingsAttribute : 0;
	String refundError = (String) request.getAttribute("refundError");
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	String encodedTransactionCode = URLEncoder.encode(transactionCode, "UTF-8");
	String encodedStatus = URLEncoder.encode(selectedStatus, "UTF-8");
	request.setAttribute("currentManagerPage", "refunds");
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Refund History | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= contextPath %>/styles/main_manager.css">
		<link rel="stylesheet" href="<%= contextPath %>/styles/pos_refund.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<div class="Layout2">
			<%@ include file="components/header_manager.jsp" %>
			<%@ include file="components/manager_navigation.jsp" %>
			<main class="ManagerMain">
				<div class="POSRefundPage">
					<!-- Page Header -->
					<header class="RefundPageHeader">
						<div>
							<span class="RefundBreadcrumb">
								Process Orders / Refunds
							</span>
							<h1>Refund History</h1>
							<p>
								Review refund requests and completed cancellations 
								for your selected theater.
							</p>
						</div>
						<a href="<%= contextPath %>/sales-bookings" class="RefundBackButton">
							Back to Sales
						</a>
					</header>
					<!-- Error Message -->
					<%
						if (refundError != null) {
					%>
							<div class="RefundMessage RefundMessageError">
								<%= refundError %>
							</div>
					<%
						}
					%>
					<!-- Status Explanation -->
					<section class="RefundStatusGuide">
						<div class="RefundGuideItem">
							<span class="RefundStatus RefundStatusPending">
								REFUNDED
							</span>
							<p>
								Refund request submitted. Awaiting admin review.
							</p>
						</div>
						<div class="RefundGuideItem">
							<span class="RefundStatus RefundStatusCancelled">
								CANCELLED
							</span>
							<p>
								Admin-approved cancellation completed.
							</p>
						</div>
					</section>
					<!-- Refund History -->
					<section class="RefundSection">
						<div class="RefundSectionHeader">
							<div>
								<h2>Refund Records</h2>
								<p>
									Booking and payment statuses are shown separately.
								</p>
							</div>
							<span class="RefundCount">
								<%= displayedCount %> records
							</span>
						</div>
						<!-- Search and Status Filter -->
						<form class="RefundFilter" action="<%= contextPath %>/manager-refunds" method="get">
							<input 
								type="text" 
								id="transactionCode" 
								name="transactionCode" 
								placeholder="Search transaction code..." 
								value="<%= transactionCode.replace("&", "&amp;")
														.replace("\"", "&quot;")
														.replace("<", "&lt;")
														.replace(">", "&gt;") %>"
							/>
							<button type="submit" class="RefundFilterButton">
								Search
							</button>
							<label for="statusFilter">
								Booking status
							</label>
							<select id="statusFilter" name="status">
								<option value="ALL" <%= "ALL".equals(selectedStatus) ? "selected" : "" %>>
									All Refund Records
								</option>
								<option value="REFUNDED" <%= "REFUNDED".equals(selectedStatus) ? "selected" : "" %>>
									Awaiting Admin Review
								</option>
								<option value="CANCELLED" <%= "CANCELLED".equals(selectedStatus) ? "selected" : "" %>>
									Cancelled
								</option>
							</select>
						</form>
						<div class="RefundBookingList">
							<%
								if (refundHistory != null && !refundHistory.isEmpty()) {
							%>
									<%
										for (Map<String, Object> booking : refundHistory) {
											String bookingStatus = (String) booking.get("bookingStatus");
											int bookingId = ((Number) booking.get("bookingId")).intValue();
											String movieName = (String) booking.get("movieName");
											String theaterName = (String) booking.get("theaterName");
											String roomName = (String) booking.get("roomName");
											String paymentStatus = (String) booking.get("paymentStatus");
											String paymentMethod = (String) booking.get("paymentMethod");
											String cardTransactionCode = (String) booking.get("transactionCode");
											Object priceObject = booking.get("price");
											int price = priceObject != null ? ((Number) priceObject).intValue() : 0;
											java.util.Date bookAt = (java.util.Date) booking.get("bookAt");
											java.util.Date startAt = (java.util.Date) booking.get("startAt");
											boolean cancelled = "CANCELLED".equalsIgnoreCase(bookingStatus);
									%>
											<article class="RefundBookingCard">
												<!-- Card Header -->
												<div class="RefundCardHeader">
													<div>
														<span class="RefundBookingId">
															BOOKING #<%= bookingId %>
														</span>
														<h3>
															<%= movieName != null ? movieName : "Unknown Movie" %>
														</h3>
													</div>
													<span class="RefundStatus <%= cancelled ? "RefundStatusCancelled" : "RefundStatusPending" %>">
														<%= bookingStatus != null ? bookingStatus : "UNKNOWN" %>
													</span>
												</div>
												<!-- Booking Information -->
												<div class="RefundInfoGrid">
													<div class="RefundInfoItem">
														<span>Theater</span>
														<strong>
															<%= theaterName != null ? theaterName : "-" %>
														</strong>
													</div>
													<div class="RefundInfoItem">
														<span>Room</span>
														<strong>
															<%= roomName != null ? roomName : "-" %>
														</strong>
													</div>
													<div class="RefundInfoItem">
														<span>Showtime</span>
														<strong>
															<%= startAt != null ? dateFormat.format(startAt) : "-" %>
														</strong>
													</div>
													<div class="RefundInfoItem">
														<span>Original Booking Date</span>
														<strong>
															<%= bookAt != null ? dateFormat.format(bookAt) : "-" %>
														</strong>
													</div>
												</div>
												<!-- Payment Information -->
												<div class="RefundPayment">
													<div class="RefundPaymentDetails">
														<span class="RefundFieldLabel">
															Payment Method
														</span>
														<strong>
															<%= paymentMethod != null ? paymentMethod : "N/A" %>
														</strong>
													</div>
													<div class="RefundPaymentDetails">
														<span class="RefundFieldLabel">
															Payment Status
														</span>
														<span class="RefundPaymentStatus <%= "REFUNDED".equalsIgnoreCase(paymentStatus) ? "RefundPaymentStatusComplete" : "RefundPaymentStatusDefault" %>">
															<%= paymentStatus != null ? paymentStatus : "UNKNOWN" %>
														</span>
													</div>
													<div class="RefundPaymentDetails">
														<span class="RefundFieldLabel">
															Transaction Code
														</span>
														<strong>
															<%= cardTransactionCode != null && !cardTransactionCode.trim().isEmpty() ? cardTransactionCode : "-" %>
														</strong>
													</div>
													<div class="RefundTotal">
														<span>Booking Total</span>
														<strong>
															<%= String.format("%,d", price) %> VND
														</strong>
													</div>
												</div>
												<!-- Status Explanation -->
												<div class="RefundCardNote <%= cancelled ? "RefundCardNoteComplete" : "RefundCardNotePending" %>">
													<%
														if (cancelled) {
													%>
															This booking has been cancelled. 
															Payment status: 
															<%= paymentStatus != null ? paymentStatus : "UNKNOWN" %>.
													<%
														} else {
													%>
															This refund request is awaiting admin review. 
															The payment has not necessarily been refunded yet.
													<%
														}
													%>
												</div>
											</article>
									<%
										}
									%>
							<%
								}
							%>
							<!-- Empty State -->
							<%
								if (displayedCount == 0) {
							%>
									<div class="RefundEmptyState">
										<div class="RefundEmptyIcon">—</div>
										<h3>No refund records found</h3>
										<p>
											No refund records match your search and 
											selected status. Try another transaction 
											code or choose a different status.
										</p>
									</div>
							<%
								}
							%>
						</div>
						<!-- Pagination -->
						<%
							if (totalPages > 1) {
						%>
								<nav class="RefundPagination" aria-label="Refund history pages">
									<%
										if (currentPage > 1) {
									%>
											<a href="<%= contextPath %>/manager-refunds?status=<%= encodedStatus %>&amp;transactionCode=<%= encodedTransactionCode %>&amp;page=<%= currentPage - 1 %>">
												Previous
											</a>
									<%
										}
									%>
									<%
										for (int pageNumber = 1; pageNumber <= totalPages; pageNumber++) {
									%>
											<%
												if (pageNumber == currentPage) {
											%>
													<span class="active" aria-current="page">
														<%= pageNumber %>
													</span>
											<%
												} else {
											%>
													<a href="<%= contextPath %>/manager-refunds?status=<%= encodedStatus %>&amp;transactionCode=<%= encodedTransactionCode %>&amp;page=<%= pageNumber %>">
														<%= pageNumber %>
													</a>
											<%
												}
											%>
									<%
										}
									%>
									<%
										if (currentPage < totalPages) {
									%>
											<a href="<%= contextPath %>/manager-refunds?status=<%= encodedStatus %>&amp;transactionCode=<%= encodedTransactionCode %>&amp;page=<%= currentPage + 1 %>">
												Next
											</a>
									<%
										}
									%>
								</nav>
						<%
							}
						%>
					</section>
				</div>
			</main>
		</div>
	</body>
</html>