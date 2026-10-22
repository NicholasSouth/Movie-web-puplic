<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.movieweb.model.Payment_methods" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%!
	/* Escape text before printing it inside the page or an attribute (the kept form values come from the request). */
	private static String esc(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
%>
<%
	request.setAttribute("currentManagerPage", "payment-method");
	/* POSPaymentMethodsServlet sends: error, success, paymentMethods,
	 * and card_number / expired_date (the values to keep when adding failed).
	 */
	String error = (String) request.getAttribute("error");
	String success = (String) request.getAttribute("success");
	List<Payment_methods> paymentMethods = (List<Payment_methods>) request.getAttribute("paymentMethods");
	String keptCardNumber = (String) request.getAttribute("card_number");
	String keptExpiredDate = (String) request.getAttribute("expired_date");
	SimpleDateFormat expiryFormat = new SimpleDateFormat("MM/yyyy");
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>POS Payment Methods - PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_manager.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/pos_payment_methods.css">
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
				<div class="payment-method-container">

					<!-- Page Header -->
					<div class="payment-method-header">
						<div>
							<span class="payment-method-breadcrumb">
								Process Orders / Payment Methods
							</span>
							<h2>POS Payment Methods</h2>
							<p>
								Manage your saved VISA cards for this theater.
							</p>
						</div>
					</div>

					<!-- Error Message -->
					<%
						if (error != null) {
					%>
							<div class="error-message">
								<%= esc(error) %>
							</div>
					<%
						}
					%>

					<!-- Success Message -->
					<%
						if (success != null) {
					%>
							<div class="success-message">
								<%= esc(success) %>
							</div>
					<%
						}
					%>

					<!-- Saved VISA Cards -->
					<section class="payment-section">
						<h3 class="payment-section-title">Theater VISA Cards</h3>
						<%
							boolean hasPaymentMethods = false;
							if (paymentMethods != null) {
								for (Payment_methods paymentMethod : paymentMethods) {
									if (paymentMethod.isActive()) {
										hasPaymentMethods = true;
										String cardNumber = paymentMethod.getCard_number();
										String maskedCard = "Unknown";
										if (cardNumber != null && cardNumber.length() >= 4) {
											maskedCard = "•••• •••• •••• " + cardNumber.substring(cardNumber.length() - 4);
										}
										String expiryText = paymentMethod.getExpired_date() == null
												? "--/----"
												: expiryFormat.format(paymentMethod.getExpired_date());
						%>
										<div class="payment-card">
											<!-- Card picture -->
											<div class="visa-card">
												<div class="visa-card-top">
													<span class="visa-chip" aria-hidden="true"></span>
													<span class="visa-logo">VISA</span>
												</div>
												<div class="visa-card-number"><%= maskedCard %></div>
												<div class="visa-card-bottom">
													<span>Saved card</span>
													<span>Expires <%= expiryText %></span>
												</div>
											</div>

											<!-- Deactivate -->
											<div class="payment-card-action">
												<form action="${pageContext.request.contextPath}/pos-payment-method" method="post">
													<input type="hidden" name="action" value="deactivate">
													<input type="hidden" name="payment_method_id" value="<%= paymentMethod.getPayment_method_id() %>">
													<button type="submit" class="payment-delete-button">Deactivate</button>
												</form>
											</div>
										</div>
						<%
									}
								}
							}
							if (!hasPaymentMethods) {
						%>
								<div class="no-payment-method">
									<p>You haven't added any VISA cards for this theater yet.</p>
								</div>
						<%
							}
						%>
					</section>

					<!-- Add VISA Card -->
					<section class="payment-section">
						<h3 class="payment-section-title">Add VISA Card</h3>
						<div class="payment-form-card">
							<p class="payment-form-description">
								Add a VISA card to use for POS payments at this theater.
							</p>
							<form class="payment-form" action="${pageContext.request.contextPath}/pos-payment-method" method="post">
								<input type="hidden" name="action" value="add">

								<!-- Card Number -->
								<div class="input-group">
									<label for="card-number">Card Number</label>
									<input
										type="text"
										id="card-number"
										name="card_number"
										value="<%= esc(keptCardNumber) %>"
										placeholder="1234 5678 9012 3456"
										inputmode="numeric"
										autocomplete="cc-number"
										maxlength="19"
										required>
								</div>

								<!-- Expiry Date -->
								<div class="input-group">
									<label for="expired_date">Expiry Date</label>
									<input
										type="text"
										id="expired_date"
										name="expired_date"
										class="payment-form-input"
										value="<%= esc(keptExpiredDate) %>"
										placeholder="MM/YYYY"
										maxlength="7"
										pattern="(0[1-9]|1[0-2])/[0-9]{4}"
										autocomplete="cc-exp"
										required>
								</div>

								<!-- Simulation Notice -->
								<div class="payment-information">
									<p>
										<strong>Payment Information</strong>
									</p>
									<p>
										VISA payments are simulated for this website. No real payment will be processed.
									</p>
								</div>

								<!-- Submit -->
								<div class="payment-form-actions">
									<button type="submit" class="payment-submit-button">Add VISA</button>
								</div>
							</form>
						</div>
					</section>
				</div>
			</main>
		</section>
	</body>
</html>
