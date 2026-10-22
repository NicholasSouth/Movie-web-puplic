<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Terms &amp; Conditions - PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<div style="max-width: 800px; margin: 0 auto; padding: 20px; line-height: 1.6;">
			<p>
				<a href="${pageContext.request.contextPath}/main.jsp" style="color: var(--accent);">&larr; Back to PhnetPhlyx</a>
			</p>
			<h1>Terms &amp; Conditions</h1>
			<h2>Welcome to PhnetPhlyx</h2>
			<p>These Terms and Conditions govern your access to and use of the PhnetPhlyx movie ticketing platform, including all associated web pages, booking interfaces, user dashboards, manager operations, and administrative tools. By accessing, registering an account, or conducting transactions on our platform, you explicitly acknowledge that you have read, understood, and agreed to be legally bound by these terms. If you do not agree with any part of this document, you must discontinue your use of the platform immediately.</p>
			<h2>User Accounts, Registration, and Security</h2>
			<ul>
				<li>
					<strong>Account Creation:</strong> Users may register by providing valid credentials, which are verified through an emailed verification code process.
				</li>
				<li>
					<strong>Account Security:</strong> You are entirely responsible for maintaining the strict confidentiality of your account password, security tokens, and remember-me session cookies. PhnetPhlyx utilizes a 180-day remember-me cookie mechanism holding a secure hash, but users remain accountable for all activities that occur under their credentials.
				</li>
				<li>
					<strong>Account Restrictions:</strong> The platform administration reserves the absolute right to block or unblock user accounts, suspend privileges, or terminate access at any time for violations of platform policies, suspicious activity, or fraudulent behavior. Blocked accounts transition into a read-only state.
				</li>
			</ul>
			<h2>Movie Ticketing, Showtimes, and Booking Rules</h2>
			<ul>
				<li>
					<strong>Showtime Availability:</strong> Showtimes are scheduled from today up to six days in advance. Room ownership, movie availability, and room schedule overlaps are strictly validated.
				</li>
				<li>
					<strong>Hold Duration:</strong> When you select your seats and proceed through the booking flow, your selected seats are held in a temporary state for a maximum duration of 15 minutes. If payment is not completed within this 15-minute window, the booking automatically expires and the seats are released back into the available pool.
				</li>
				<li>
					<strong>Pricing and Modifiers:</strong> Ticket pricing is calculated dynamically via our pricing service rules. The base price corresponds to the normal ticket type of the selected showtime. Percentage modifiers accumulate based on room types, seat types, ticket types, weekday discounts (Monday to Friday reductions), off-hour incentives (showtimes starting at or before 07:00, or at or after 21:00), and active promotional discount percentages. Fixed promotional amounts (multiplied by a factor of 1,000) are subsequently subtracted, ensuring that the final total price never drops below zero.
				</li>
			</ul>
			<h2>Payment Processing and Refund Policies</h2>
			<ul>
				<li>
					<strong>Payment Simulation:</strong> Payments are processed via simulated credit card transactions utilizing saved customer payment methods or alternative counter-payment options.
				</li>
				<li>
					<strong>Refund Requests:</strong> Confirmed bookings backed by successful payments are eligible for refund requests. Managers may submit refund requests through the manager portal, which route directly to system administrators for final review and approval. Approving a refund transitions the booking status and triggers automated customer notification emails, though gross revenue metrics across dashboards continue to track overall financial turnover without automatic deductions for processed refunds.
				</li>
			</ul>
			<h2>Intellectual Property and Community Guidelines</h2>
			<ul>
				<li>
					<strong>User Interactions:</strong> Logged-in users may submit ratings ranging from 1 to 10 (which dynamically recalculates overall movie averages), leave comments, favorite movies with public or private visibility settings, and report inappropriate remarks (limited to one report per user per comment).
				</li>
				<li>
					<strong>Content Moderation:</strong> Administrators actively review reported comments through dedicated management consoles, retaining the discretion to dismiss invalid reports or completely remove offending comments and their associated report logs.
				</li>
			</ul>
		</div>
	</body>
</html>