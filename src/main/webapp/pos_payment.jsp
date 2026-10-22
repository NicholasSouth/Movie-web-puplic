<%@ page import="java.util.List,java.util.Map,java.text.NumberFormat,java.text.SimpleDateFormat,java.util.Locale,java.sql.Timestamp" %>
<%@ page import="com.movieweb.model.Promotions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
	Integer bookingId = (Integer) request.getAttribute("bookingId");
	Integer bookingPrice = (Integer) request.getAttribute("bookingPrice");
	Promotions promotion = (Promotions) request.getAttribute("promotion");
	String bookingStatus = (String) request.getAttribute("bookingStatus");
	String showtimeStatus = (String) request.getAttribute("showtimeStatus");
	String paymentStatus = (String) request.getAttribute("paymentStatus");
	String transactionCode = (String) request.getAttribute("transactionCode");
	String movieName = (String) request.getAttribute("movieName");
	String theaterName = (String) request.getAttribute("theaterName");
	String roomName = (String) request.getAttribute("roomName");
	Timestamp showtimeStart = (Timestamp) request.getAttribute("showtimeStart");
	Timestamp showtimeEnd = (Timestamp) request.getAttribute("showtimeEnd");
	List<Map<String, Object>> bookingSeats = (List<Map<String, Object>>) request.getAttribute("bookingSeats");
	List<Map<String, Object>> visaCards = (List<Map<String, Object>>) request.getAttribute("visaCards");
	NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);
	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	boolean paymentSuccessful = "SUCCESS".equalsIgnoreCase(paymentStatus);
	boolean canPay = "PENDING".equalsIgnoreCase(bookingStatus) && "SCHEDULED".equalsIgnoreCase(showtimeStatus);
	String paymentError = (String) request.getAttribute("paymentError");
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta 
			name="viewport" 
			content="width=device-width, initial-scale=1.0">
		<title>POS Payment | PhnetPhlyx</title>
		<link 
			rel="stylesheet" 
			href="<%= request.getContextPath() %>/styles/main_manager.css">
		<link 
			rel="stylesheet" 
			href="<%= request.getContextPath() %>/styles/pos_payment.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<%@ include file="/components/header_manager.jsp" %>
		<main class="PaymentPage POSPaymentPage">
			<div class="PaymentContainer">
				<!-- Heading -->
				<div class="PaymentHeading">
					<div>
						<p class="PaymentEyebrow">
							PHNETPHLYX POS
						</p>
						<h1>Payment</h1>
						<p class="PaymentSubtitle">
							Complete payment for this manager booking.
						</p>
					</div>
					<div class="PaymentSecureLabel">
						<span aria-hidden="true">
							&#128274;
						</span>
						Demo POS payment
					</div>
				</div>
				<!-- Error -->
				<%
					if (paymentError != null) {
				%>
						<div 
							class="PaymentNotice PaymentNoticeError" 
							role="alert">
							<strong>
								Payment unsuccessful
							</strong>
							<p>
								<%= paymentError %>
							</p>
						</div>
				<%
					}
				%>
				<!-- Success -->
				<%
					if (paymentSuccessful) {
				%>
						<div 
							class="PaymentNotice PaymentNoticeSuccess">
							<div>
								<h2>
									Payment successful!
								</h2>
								<p>
									The booking has been confirmed.
								</p>
								<p class="TransactionCode">
									Transaction code:
									<strong>
										<%= transactionCode == null 
											? "Unavailable" 
											: transactionCode %>
									</strong>
								</p>
							</div>
						</div>
				<%
					}
				%>
				<div class="PaymentLayout">
					<!-- BOOKING SUMMARY -->
					<section 
						class="PaymentPanel BookingSummary">
						<div class="PanelHeading">
							<div>
								<p class="PanelEyebrow">
									POS ORDER
								</p>
								<h2>
									Booking details
								</h2>
							</div>
							<span class="BookingId">
								#<%= bookingId %>
							</span>
						</div>
						<div class="MovieSummary">
							<div 
								class="MoviePlaceholder" 
								aria-hidden="true">
								<span>
									&#127916;
								</span>
							</div>
							<div class="MovieSummaryText">
								<h3>
									<%= movieName %>
								</h3>
								<p>
									<%= theaterName %>
								</p>
								<p>
									<%= roomName %>
								</p>
								<%
									if (showtimeStart != null) {
								%>
										<p class="ShowtimeDate">
											<%= dateFormat.format(showtimeStart) %>
										</p>
										<p class="ShowtimeTime">
											<%= timeFormat.format(showtimeStart) %>
											<%
												if (showtimeEnd != null) {
											%>
													–
													<%= timeFormat.format(showtimeEnd) %>
											<%
												}
											%>
										</p>
								<%
									}
								%>
							</div>
						</div>
						<div class="SummaryDivider"></div>
						<!-- Seats -->
						<div class="SeatSummaryHeading">
							<h3>
								Selected seats
							</h3>
							<span>
								<%= bookingSeats == null 
									? 0 
									: bookingSeats.size() %>
								seat(s)
							</span>
						</div>
						<%
							if (bookingSeats != null && !bookingSeats.isEmpty()) {
						%>
								<div class="SeatList">
									<%
										for (Map<String, Object> seat : bookingSeats) {
											String seatLabel = String.valueOf(seat.get("seatRow")) + seat.get("seatCol");
									%>
											<div class="SeatItem">
												<div class="SeatItemInfo">
													<span class="SeatLabel">
														<%= seatLabel %>
													</span>
													<div>
														<strong>
															<%= seat.get("seatType") %>
														</strong>
														<span 
															class="SeatSubtext">
															<%= seat.get("ticketType") %>
														</span>
													</div>
												</div>
												<span class="SeatPrice">
													<%= currencyFormat.format(((Number)seat.get("finalPrice")).intValue()) %>
													VND
												</span>
											</div>
									<%
										}
									%>
								</div>
						<%
							} else {
						%>
								<p class="EmptySeats">
									No seats were found for this booking.
								</p>
						<%
							}
						%>
						<div class="SummaryDivider"></div>
						<!-- Promotion -->
						<%
							if (promotion != null) {
						%>
								<div class="PromotionRow">
									<span>
										Promotion
										<strong>
											<%= promotion.getPromotion_code() %>
										</strong>
									</span>
									<strong 
										class="PromotionDiscount">
										<%= promotion.getPrice_modify() %>
										<%
											if (promotion.getPrice_modify() != null 
												&& !promotion.getPrice_modify().endsWith("%")) {
										%>
												,000 VND
										<%
											}
										%>
									</strong>
								</div>
								<div class="SummaryDivider"></div>
						<%
							}
						%>
						<%
	/* Automatic discounts (weekday / off-hour) are already inside the price; tell the buyer about them. */
	java.util.List<String> autoDiscounts = com.movieweb.service.PricingService.autoDiscountNotices(showtimeStart);
	if (!autoDiscounts.isEmpty()) {
%>
	<div class="AutoDiscountNotice">
		<strong>Automatic discounts included in this price</strong>
		<ul>
			<%
				for (String note : autoDiscounts) {
			%>
					<li><%= note %></li>
			<%
				}
			%>
		</ul>
	</div>
<%
	}
%>
<!-- Total -->
						<div class="TotalRow">
							<span>
								Total amount
							</span>
							<strong>
								<%= currencyFormat.format(bookingPrice == null 
											? 0 
											: bookingPrice) %>
								VND
							</strong>
						</div>
						<p class="BookingFootnote">
							This is a simulated POS payment. 
							No real money will be charged.
						</p>
					</section>
					<!-- PAYMENT -->
					<section 
						class="PaymentPanel PaymentMethodPanel">
						<div class="PanelHeading">
							<div>
								<p class="PanelEyebrow">
									PAYMENT METHOD
								</p>
								<h2>
									Choose payment
								</h2>
							</div>
							<span class="POSPaymentBadge">
								POS
							</span>
						</div>
						<%
							if (canPay && !paymentSuccessful) {
						%>
								<form 
									action="<%= request.getContextPath() %>/manager-payment" 
									method="post" 
									id="POSPaymentForm">
									<input 
										type="hidden" 
										name="booking_id" 
										value="<%= bookingId %>">
									<!-- Payment methods -->
									<div class="POSPaymentMethods">
										<!-- VISA -->
										<label 
											class="POSPaymentOption">
											<input 
												type="radio" 
												name="payment_method" 
												value="VISA" 
												required>
											<span 
												class="POSPaymentOptionContent">
												<span 
													class="POSPaymentIcon">
													VISA
												</span>
												<span>
													<strong>
														VISA
													</strong>
													<small>
														Pay with a saved VISA card
													</small>
												</span>
											</span>
										</label>
										<!-- CASH -->
										<label 
											class="POSPaymentOption">
											<input 
												type="radio" 
												name="payment_method" 
												value="CASH">
											<span 
												class="POSPaymentOptionContent">
												<span 
													class="POSPaymentIcon">
													&#128181;
												</span>
												<span>
													<strong>
														Cash
													</strong>
													<small>
														Customer pays with cash
													</small>
												</span>
											</span>
										</label>
										<!-- QR -->
										<label 
											class="POSPaymentOption">
											<input 
												type="radio" 
												name="payment_method" 
												value="QR">
											<span 
												class="POSPaymentOptionContent">
												<span 
													class="POSPaymentIcon">
													QR
												</span>
												<span>
													<strong>
														QR Payment
													</strong>
													<small>
														Customer pays by QR
													</small>
												</span>
											</span>
										</label>
										<div
											    id="QRPaymentSection"
											    class="QRPaymentSection"
											    style="display: none;">
											    <h3>Scan to Pay</h3>
											    <p>
											        Scan the QR code below to complete the payment.
											    </p>
											
											    <img
											        src="<%= request.getContextPath() %>/pictures/assessments/qr.png"
											        alt="QR Payment"
											        class="QRPaymentImage">
											</div>
									</div>
									<!-- VISA cards -->
									<div 
										id="VisaCardSection" 
										class="VisaCardSection" 
										style="display: none;">
										<h3>
											Select VISA card
										</h3>
										<%
											if (visaCards != null && !visaCards.isEmpty()) {
										%>
												<div class="VisaCardList">
													<%
														for (int i = 0; i < visaCards.size(); i++) {
															Map<String, Object> card = visaCards.get(i);
													%>
															<label 
																class="VisaCardOption">
																<input 
																	type="radio" 
																	name="payment_method_id" 
																	value="<%= card.get("paymentMethodId") %>">
																<span 
																	class="VisaCardVisual">
																	<span 
																		class="CardTopLine">
																		<span 
																			class="CardChip">
																		</span>
																		<span 
																			class="VisaLogo">
																			VISA
																		</span>
																	</span>
																	<span 
																		class="CardNumber">
																		<%= card.get("maskedNumber") %>
																	</span>
																	<span 
																		class="CardBottomLine">
																		<span>
																			Saved card
																		</span>
																		<span>
																			Expires
																			<%= card.get("expiredDate") %>
																		</span>
																	</span>
																</span>
															</label>
													<%
														}
													%>
												</div>
										<%
											} 
											else {
										%>
												<div class="NoCardsNotice">
													<span 
														class="NoCardsIcon">
														&#128179;
													</span>
													<h3>
														No saved VISA cards
													</h3>
													<p>
														A saved VISA card is not 
														available for this payment.
													</p>
												</div>
										<%
											}
										%>
									</div>
									<div class="DemoPaymentNotice">
										<strong>
											Demo payment only
										</strong>
										<p>
											No real money will be charged. 
											This will create a simulated 
											successful payment record.
										</p>
									</div>
									<button 
										type="submit" 
										class="PayButton" 
										id="PayButton" 
										disabled>
										<span>
											Confirm payment
										</span>
										<span>
											<%= currencyFormat.format(
													bookingPrice == null 
														? 0 
														: bookingPrice) %>
											VND
										</span>
									</button>
									<p class="PaymentTerms">
										Select a payment method 
										before confirming.
									</p>
								</form>
						<%
							} 
							else if (paymentSuccessful) {
						%>
								<div 
									class="PaymentSuccessDetails">
									<p>
										<span>
											Payment method
										</span>
										<strong>
											<%= request.getAttribute(
													"paymentMethod") == null 
													? "POS" 
													: request.getAttribute(
															"paymentMethod") %>
										</strong>
									</p>
									<p>
										<span>
											Amount paid
										</span>
										<strong>
											<%= currencyFormat.format(
													bookingPrice == null 
														? 0 
														: bookingPrice) %>
											VND
										</strong>
									</p>
								</div>
								<a 
									class="PayButton SuccessReturnButton" 
									href="<%= request.getContextPath() %>/main_manager.jsp">
									Return to dashboard
								</a>
						<%
							}
						%>
					</section>
				</div>
			</div>
		</main>
		<script 
			src="<%= request.getContextPath() %>/scripts/pos_payment.js">
		</script>
	</body>
</html>