<%@ page import="java.util.List,java.util.Map,java.text.NumberFormat,java.text.SimpleDateFormat,java.util.Locale,java.sql.Timestamp" %>
<%@ page import="com.movieweb.model.Promotions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    Integer bookingId = (Integer) request.getAttribute("bookingId");
    Integer bookingPrice = (Integer) request.getAttribute("bookingPrice");
    Promotions promotion = (Promotions) request.getAttribute("promotion");
    Boolean isExpired = (Boolean) request.getAttribute("isExpired");
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
    boolean expired = Boolean.TRUE.equals(isExpired);
    boolean showtimeUnavailable = !"SCHEDULED".equalsIgnoreCase(showtimeStatus) || (showtimeStart != null && !showtimeStart.after(new java.util.Date()));
    boolean canPay = "PENDING".equalsIgnoreCase(bookingStatus) && !expired && !showtimeUnavailable;
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>Checkout | PhnetPhlyx</title>
	    <link rel="stylesheet" href="<%= request.getContextPath() %>/styles/main.css">
	    <link rel="stylesheet" href="<%= request.getContextPath() %>/styles/payment.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<%@ include file="/components/header.jsp" %>
		
		<main class="PaymentPage">
		    <div class="PaymentContainer">
		
		        <div class="PaymentHeading">
		            <div>
		                <p class="PaymentEyebrow">PHNETPHLYX CHECKOUT</p>
		                <h1>Payment</h1>
		                <p class="PaymentSubtitle">
		                    Review your booking and select a saved VISA card.
		                </p>
		            </div>
		
		            <div class="PaymentSecureLabel">
		                <span aria-hidden="true">&#128274;</span>
		                Demo checkout
		            </div>
		        </div>
		
		        <%
		            if (request.getAttribute("paymentError") != null) {
		        %>
				        <div class="PaymentNotice PaymentNoticeError" role="alert">
				            <strong>Payment unsuccessful</strong>
				            <p><%= request.getAttribute("paymentError") %></p>
				        </div>
		        <%
		            }
		        %>
		
		        <%
		            if (paymentSuccessful) {
		        %>
				        <div class="PaymentNotice PaymentNoticeSuccess">
				            <div>
				                <h2>Payment successful!</h2>
				                <p>Your booking has been confirmed.</p>
				                <p class="TransactionCode">
				                    Transaction code:
				                    <strong><%= transactionCode == null ? "Unavailable" : transactionCode %></strong>
				                </p>
				            </div>
				        </div>
		        <%
		            } 
		            else if (!canPay) {
		        %>
				        <div class="PaymentNotice PaymentNoticeError">
				            <h2>Checkout unavailable</h2>
				            <%
				                if (expired) {
						            %>
						                <p>Your 15-minute seat reservation has expired.</p>
						            <%
				                } 
				                else if ("PENDING".equalsIgnoreCase(bookingStatus) && showtimeUnavailable) {
						            %>
						                <p>This showtime is no longer available for payment.</p>
						            <%
				                } 
				                else if ("CONFIRMED".equalsIgnoreCase(bookingStatus)) {
						            %>
						                <p>This booking is already confirmed.</p>
						            <%
				                } 
				                else {
						            %>
						                <p>This booking is no longer awaiting payment.</p>
						            <%
				                }
				            %>
				        </div>
		        <%
		            }
		        %>
		        <div class="PaymentLayout">
		            <section class="PaymentPanel BookingSummary">
		                <div class="PanelHeading">
		                    <div>
		                        <p class="PanelEyebrow">YOUR ORDER</p>
		                        <h2>Booking details</h2>
		                    </div>
		                    <span class="BookingId">#<%= bookingId %></span>
		                </div>
		                <div class="MovieSummary">
		                    <div class="MoviePlaceholder" aria-hidden="true">
		                        <span>&#127916;</span>
		                    </div>
		                    <div class="MovieSummaryText">
		                        <h3><%= movieName %></h3>
		                        <p><%= theaterName %></p>
		                        <p><%= roomName %></p>
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
				                                    – <%= timeFormat.format(showtimeEnd) %>
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
		                <div class="SeatSummaryHeading">
		                    <h3>Selected seats</h3>
		                    <span>
		                        <%= bookingSeats == null ? 0 : bookingSeats.size() %>
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
						                            <span class="SeatLabel"><%= seatLabel %></span>
						                            <div>
						                                <strong><%= seat.get("seatType") %></strong>
						                                <span class="SeatSubtext">
						                                    <%= seat.get("ticketType") %>
						                                </span>
						                            </div>
						                        </div>
						                        <span class="SeatPrice">
						                            <%= currencyFormat.format(((Number) seat.get("finalPrice")).intValue()) %>
						                            VND
						                        </span>
						                    </div>
				                    <%
				                        }
				                    %>
				                </div>
		                <%
		                    } 
		                    else {
		                %>
				                <p class="EmptySeats">No seats were found for this booking.</p>
		                <%
		                    }
		                %>
		                
		                <div class="SummaryDivider"></div>
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
						            <strong class="PromotionDiscount">
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
<div class="TotalRow">
						    <span>Total amount</span>
						    <strong>
						        <%= currencyFormat.format(bookingPrice == null ? 0 : bookingPrice) %> VND
						    </strong>
						</div>
		                <p class="BookingFootnote">
		                    Your seats are held for 15 minutes after the booking is created.
		                </p>
		            </section>
		            <section class="PaymentPanel PaymentMethodPanel">
		                <div class="PanelHeading">
		                    <div>
		                        <p class="PanelEyebrow">PAYMENT METHOD</p>
		                        <h2>Pay with VISA</h2>
		                    </div>
		                    <span class="VisaLogo">VISA</span>
		                </div>
		                <p class="PaymentMethodDescription">
		                    Choose one of your saved VISA cards to complete this
		                    simulated payment.
		                </p>
		
		                <%
		                    if (canPay) {
		                %>
				                <%
				                    if (visaCards != null && !visaCards.isEmpty()) {
				                %>
						                <form action="<%= request.getContextPath() %>/payment" method="post" id="PaymentForm">
						                    <input type="hidden" name="booking_id" value="<%= bookingId %>">
						                    <div class="VisaCardList">
						                        <%
						                            for (int i = 0; i < visaCards.size(); i++) {
						                                Map<String, Object> card = visaCards.get(i);
						                        %>
								                        <label class="VisaCardOption">
								                            <input type="radio" name="payment_method_id" value="<%= card.get("paymentMethodId") %>" <%= i == 0 ? "checked" : "" %> required>
								
								                            <span class="VisaCardVisual">
								                                <span class="CardTopLine">
								                                    <span class="CardChip" aria-hidden="true"></span>
								                                    <span class="VisaLogo">VISA</span>
								                                </span>
								
								                                <span class="CardNumber">
								                                    <%= card.get("maskedNumber") %>
								                                </span>
								
								                                <span class="CardBottomLine">
								                                    <span>Saved card</span>
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
						
						                    <div class="DemoPaymentNotice">
						                        <strong>Demo payment only</strong>
						                        <p>
						                            No real money will be charged. The system
						                            will create a simulated successful payment
						                            record in the database.
						                        </p>
						                    </div>
						
						                    <button type="submit" class="PayButton" id="PayButton">
						                        <span>Pay</span>
						                        <span>
						                            <%= currencyFormat.format(bookingPrice == null ? 0 : bookingPrice) %>
						                            VND
						                        </span>
						                    </button>
						
						                    <p class="PaymentTerms">
						                        By continuing, you confirm this simulated payment.
						                    </p>
						                </form>
				                <%
				                    } 
				                    else {
				                %>
						                <div class="NoCardsNotice">
						                    <span class="NoCardsIcon">&#128179;</span>
						                    <h3>No saved VISA cards</h3>
						                    <p>
						                        Add a VISA card to your payment methods before
						                        checking out.
						                    </p>
						                    <a class="SecondaryButton" href="<%= request.getContextPath() %>/payment-method">
						                        Manage payment methods
						                    </a>
						                </div>
				                <%
				                    }
				                %>
		                <%
		                    } 
		                    else if (!paymentSuccessful) {
		                %>
				                <p class="PaymentUnavailableText">
				                    You cannot pay for this booking right now.
				                </p>
		                <%
		                    }
		                %>
		
		                <%
		                    if (paymentSuccessful) {
		                %>
				                <div class="PaymentSuccessDetails">
				                    <p><span>Payment method</span><strong>VISA</strong></p>
				                    <p>
				                        <span>Amount paid</span>
				                        <strong>
				                            <%= currencyFormat.format(bookingPrice == null ? 0 : bookingPrice) %>
				                            VND
				                        </strong>
				                    </p>
				                </div>
				                <a class="PayButton SuccessReturnButton" href="<%= request.getContextPath() %>/main.jsp">
				                    Return to home
				                </a>
		                <%
		                    }
		                %>
		            </section>
		        </div>
		    </div>
		</main>
		
		<%@ include file="/components/footer.jsp" %>
		
		<script>
		    const paymentForm = document.getElementById("PaymentForm");
		    const payButton = document.getElementById("PayButton");
		    if (paymentForm && payButton) {
		        paymentForm.addEventListener("submit", function () {
		            payButton.disabled = true;
		            payButton.classList.add("PayButtonLoading");
		            payButton.innerHTML = "<span>Processing payment...</span>";
		        });
		    }
		</script>
		
		<!-- Remove the text 
		"Choose one of your saved VISA cards to complete this simulated payment."
		after paying successfully -->
		<script>
		    const paymentSuccessful = <%= paymentSuccessful %>;
		    if (paymentSuccessful) {
		        const paymentDescription = document.querySelector(".PaymentMethodDescription");
		        if (paymentDescription) {
		            paymentDescription.remove();
		        }
		    }
		</script>
	</body>
</html>