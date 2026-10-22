<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Privacy Policy - PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<div style="max-width: 800px; margin: 0 auto; padding: 20px; line-height: 1.6;">
			<p>
				<a href="${pageContext.request.contextPath}/main.jsp" style="color: var(--accent);">&larr; Back to PhnetPhlyx</a>
			</p>
			<h1>Privacy Policy</h1>
			<h2>Commitment to Your Privacy</h2>
			<p>At PhnetPhlyx, we recognize the importance of protecting your personal information. This Privacy Policy outlines our transparent practices regarding the collection, utilization, storage, and safeguarding of data gathered when you interact with our movie ticket booking portal, administrative dashboards, and theater management tools.</p>
			<h2>Categories of Information We Collect</h2>
			<ul>
				<li>
					<strong>Account and Profile Data:</strong> When registering or maintaining your profile, we record your legal name, email address, account role assignments (Guest, User, Manager, or Admin), uploaded avatar and banner graphics, and account status indicators.
				</li>
				<li>
					<strong>Transaction and Booking Records:</strong> We maintain detailed logs of your movie reservations, seat selections, showtime history, promotion code usages, favorite movie lists, and user ratings or comments.
				</li>
				<li>
					<strong>Payment and Financial Data:</strong> We store structured references to your simulated payment methods and saved credit card profiles to facilitate seamless checkouts.
				</li>
				<li>
					<strong>Technical and Preference Data:</strong> We record session identifiers, remember-me authentication tokens, and user interface preferences, such as your customized theme choice (Dark Mode or Daylight Light Theme). Note that theme choices are saved locally per browser environment rather than permanently bound to database columns across multiple separate devices.
				</li>
			</ul>
			<h2>How We Utilize Your Collected Information</h2>
			<ul>
				<li>
					<strong>Core Service Delivery:</strong> To process ticket bookings, validate seat availability, calculate dynamic pricing, execute 15-minute reservation holds, and handle simulated payment completions.
				</li>
				<li>
					<strong>Communication and Verification:</strong> To dispatch automated verification codes during account registration and password reset workflows, as well as status updates regarding managerial theater assignments, request approvals, and refund determinations.
				</li>
				<li>
					<strong>Platform Personalization and Security:</strong> To maintain secure session states, apply personalized theme settings instantly upon page load, filter content based on active user roles, and monitor system health via comprehensive administrative statistics and activity counts.
				</li>
			</ul>
			<h2>Data Security and Storage Practices</h2>
			<p>We implement standard industry-standard relational database structures using plain JDBC and SQL Server backends to store and query platform information. Administrative controls and role-based filters (such as automatic request routing and role checks) ensure that sensitive management interfaces remain restricted to authorized personnel. However, users should remain aware that continuous security hardening is an ongoing operational commitment, and baseline security measures (such as credential handling and session management) are regularly evaluated for future enhancement.</p>
		</div>
	</body>
</html>