<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.movieweb.model.Payment_methods" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    request.setAttribute("currentPage", "settings");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
    List<Payment_methods> paymentMethods = (List<Payment_methods>) request.getAttribute("paymentMethods");
%>
<%
    SimpleDateFormat expiryFormat = new SimpleDateFormat("MM/yyyy");
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>Payment Methods - PhnetPhlyx</title>
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/payment_methods.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<section class="Layout1">
		
		    <!-- Header -->
		    <jsp:include page="/components/header.jsp" />
		
		    <!-- Left Sidebar -->
		    <%@ include file="components/left_sidebar.jsp" %>
		
		    <!-- Main Content -->
		    <main class="MainBody">
		        <div class="payment-method-container">
		
		            <!-- Page Header -->
		            <div class="payment-method-header">
		                <h2>Payment Methods</h2>
		                <p>Manage your saved VISA cards.</p>
		            </div>
		
		            <!-- Error Message -->
		            <%
		                if (error != null) {
		            %>
				            <div class="error-message">
				                <%= error %>
				            </div>
		            <%
		                }
		            %>
		
					<!-- In payment method we actually need success message 
						cuz we don't get redirect elsewhere after successfully 
						added a payment method -->
		            <!-- Success Message -->
		            <%
		                if (success != null) {
		            %>
				            <div class="success-message">
				                <%= success %>
				            </div>
		            <%
		                }
		            %>
		
		            <!-- Saved VISA Cards -->
		            <section class="payment-section">
		                <h3 class="payment-section-title">Your VISA Cards</h3>
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
						                            <span>Expires <%= expiryFormat.format(paymentMethod.getExpired_date()) %></span>
						                        </div>
						                    </div>
						
						                    <!-- Deactivate -->
						                    <div class="payment-card-action">
						                        <form action="${pageContext.request.contextPath}/payment-method" method="post">
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
				                    <p>You haven't added any VISA cards yet.</p>
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
		                        Add a VISA card to use for your movie bookings.
		                    </p>
		                    <form class="payment-form" action="${pageContext.request.contextPath}/payment-method" method="post">
		                        <input type="hidden" name="action" value="add">
		
		                        <!-- Card Number -->
		                        <div class="input-group">
		                            <label for="card-number">Card Number</label>
		                            <input 
		                                type="text" 
		                                id="card-number" 
		                                name="card_number" 
		                                placeholder="1234 5678 9012 3456" 
		                                inputmode="numeric" 
		                                autocomplete="cc-number" 
		                                maxlength="19" 
		                                required>
		                        </div>
		
		                        <!-- Expiry Date -->
		                        <div class="input-group">
		                            <label for="expired_date">Expiry Date</label>
		                            <input type="text"
								           id="expired_date"
								           name="expired_date"
								           class="payment-form-input"
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
		
		            <!-- Back to Settings -->
		            <div class="payment-method-footer">
		                <a href="${pageContext.request.contextPath}/settings.jsp" class="cancel-button">
		                    Back to Settings
		                </a>
		            </div>
		        </div>
		    </main>
		
		    <!-- Right Sidebar -->
		    <%@ include file="components/right_sidebar.jsp" %>
		
		    <!-- Footer -->
		    <%@ include file="components/footer.jsp" %>
		</section>
	</body>
</html>
