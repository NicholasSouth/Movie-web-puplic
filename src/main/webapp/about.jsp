<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>About Us - PhnetPhlyx</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<div style="max-width: 800px; margin: 0 auto; padding: 20px; line-height: 1.6;">
			<p>
				<a href="${pageContext.request.contextPath}/main.jsp" style="color: var(--accent);">&larr; Back to PhnetPhlyx</a>
			</p>
			<h1>About Us</h1>
			<h2>The PhnetPhlyx Vision</h2>
			<p>PhnetPhlyx was established with a singular, clear mission: to make movie ticket booking simple, efficient, and accessible for everyone. What began as a streamlined ticketing concept has evolved into a comprehensive cinema management ecosystem that effortlessly bridges the gap between everyday moviegoers, regional theater managers, and platform administrators.</p>
			<h2>A Robust Ecosystem for Every Role</h2>
			<ul>
				<li>
					<strong>For Moviegoers (Users):</strong> PhnetPhlyx offers an immersive browsing experience where customers can explore upcoming films, filter showtimes by genre, tags, actors, directors, or authors, view theater details, and read top-rated movie lists. Our streamlined booking engine allows users to lock in preferred seating charts, apply promotional discount codes, and execute swift simulated transactions backed by a reliable 15-minute reservation hold window. Furthermore, community features enable users to rate films, share comments, manage public or private favorites, and customize their viewing atmosphere with our integrated Daylight light theme.
				</li>
				<li>
					<strong>For Theater Managers:</strong> Managers are empowered with dedicated control panels scoped precisely to their assigned theaters. From managing room layouts, generating precise row-and-column seating configurations, and scheduling future showtimes without room overlaps, to overseeing counter sales via point-of-sale (POS) systems and submitting formal refund requests, managers have all the tools necessary for smooth day-to-day operations.
				</li>
				<li>
					<strong>For System Administrators:</strong> Behind the scenes, administrators command a robust suite of eleven specialized management modules. From reviewing manager theater assignment requests and auditing comprehensive gross revenue statistics to moderating user accounts, blocking disruptive profiles, handling reported comments, and managing site-wide promotions, refunds, and banner advertisements, the administration hub ensures total platform stability.
				</li>
			</ul>
			<h2>Built with Modern Engineering Standards</h2>
			<p>Engineered using modern Java servlets, JavaServer Pages (JSP), plain JDBC database connectivity, and structured cascading style sheets utilizing modern CSS root variables, PhnetPhlyx represents a harmonious blend of performance, modular architecture, and clean user-interface design. We are dedicated to continuous innovation, ensuring that every trip to the cinema starts with a flawless digital experience.</p>
		</div>
	</body>
</html>