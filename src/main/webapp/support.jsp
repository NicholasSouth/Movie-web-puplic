<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Support Center - PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<div style="max-width: 800px; margin: 0 auto; padding: 20px; line-height: 1.6;">
			<p>
				<a href="${pageContext.request.contextPath}/main.jsp" style="color: var(--accent);">&larr; Back to PhnetPhlyx</a>
			</p>
			<h1>Support Center</h1>
			<h2>Frequently Asked Questions (FAQ)</h2>
			<h3>Ticketing &amp; Seat Selection</h3>
			<p>
				<strong>Q: How long are my selected seats held while I complete my checkout?</strong><br>
				A: When you choose your seats and enter the payment phase, your reservation enters a temporary pending state. Seats are held exclusively for you for a maximum duration of 15 minutes. If the transaction is not finalized within this timeframe, the hold expires automatically, and the seats are released back into the public inventory.
			</p>
			<p>
				<strong>Q: How do promotion codes affect my final ticket price?</strong><br>
				A: Promotion codes can apply percentage-based reductions (calculated cumulatively alongside room, seat, ticket, weekday, and off-hour modifiers) or fixed monetary discounts. System rules ensure that final totals never drop below zero.
			</p>
			<h3>User Accounts &amp; Settings</h3>
			<p>
				<strong>Q: How do I switch between the Dark Theme and the Daylight Light Theme?</strong><br>
				A: You can easily change your visual preference by navigating to your account Settings page (or the Appearance panel within administrative profiles) and selecting your preferred mode. Your theme choice is saved locally to your current browser environment.
			</p>
			<p>
				<strong>Q: What happens if my user account is blocked by an administrator?</strong><br>
				A: Blocked accounts transition into a restricted, read-only state. While you may no longer execute active bookings or administrative actions, existing data remains viewable until session expiration.
			</p>
			<h3>Managerial &amp; Administrative Support</h3>
			<p>
				<strong>Q: How do theater managers request assignment to a specific theater?</strong><br>
				A: Registered managers can submit formal assignment requests through their manager dashboard. These requests are routed directly to system administrators, who can review, approve, or reject them in a single atomic transaction, accompanied by optional decision notification emails.
			</p>
			<p>
				<strong>Q: How are ticket refunds processed?</strong><br>
				A: Confirmed bookings with successful payments can have refund requests submitted by theater managers. Administrators review these requests within the admin refund panel. Upon approval, the booking status updates to cancelled and payment status reflects as refunded.
			</p>
		</div>
	</body>
</html>