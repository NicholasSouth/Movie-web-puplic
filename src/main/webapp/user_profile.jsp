<%@ page import="com.movieweb.model.Users" %>
<%@ page import="java.util.List,java.util.Map,java.text.SimpleDateFormat" %>
<%
    Users loggedInUser = (Users) request.getAttribute("loggedInUser");
    Users profileUser = (Users) request.getAttribute("profileUser");
    Boolean ownProfileAttribute = (Boolean) request.getAttribute("isOwnProfile");
    boolean isOwnProfile = ownProfileAttribute != null && ownProfileAttribute;

    /*Fallback in case the JSP is accessed without the servlet.*/
    if (loggedInUser == null) {
        loggedInUser = (Users) session.getAttribute("user");
    }
    if (profileUser == null) {
        profileUser = loggedInUser;
    }
    if (loggedInUser == null || profileUser == null) {
        response.sendRedirect(request.getContextPath() + "/log_in.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>
	        <%= profileUser.getFullName() %> - Profile
	    </title>
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/user_profile.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<div class="Layout1">
		
		    <!-- Header -->
		    <jsp:include page="/components/header.jsp" />
		
		    <!-- Left Sidebar -->
		    <%@ include file="components/left_sidebar.jsp" %>
		
		    <!-- Main body -->
		    <main class="MainBody profile-page">
		
		        <!-- Profile header -->
		        <section class="profile-header">
		
		            <!-- Banner -->
		            <div class="profile-banner">
		                <%
		                    if (profileUser.getBannerPath() != null && !profileUser.getBannerPath().isEmpty()) {
		                %>
		                    	<img src="${pageContext.request.contextPath}/<%= profileUser.getBannerPath() %>" alt="Profile banner">
		                <%
		                    } 
		                    else {
		                %>
		                    	<div class="default-banner"></div>
		                <%
		                    }
		                %>
		            </div>
		
		            <!-- Profile Information -->
		            <div class="profile-header-info">
		
		                <!-- Avatar -->
		                <div class="profile-avatar">
		                    <%
		                        if (profileUser.getAvtPath() != null && !profileUser.getAvtPath().isEmpty()) {
		                    %>
		                        	<img src="${pageContext.request.contextPath}/<%= profileUser.getAvtPath() %>" alt="<%= profileUser.getFullName() %>">
		                    <%
		                        } 
		                        else {
		                    %>
		                        	<div class="profile-default-avatar">
		                            	<%
		                            	    String fullName = profileUser.getFullName();
		                            	    if (fullName != null && !fullName.trim().isEmpty()) {
		                            	%>
		                                		<%= fullName.trim().substring(0, 1).toUpperCase() %>
		                            	<%
		                            	    } 
		                            	    else {
		                            	%>
		                                		?
		                            	<%
		                            	    }
		                            	%>
		                        	</div>
		                    <%
		                        }
		                    %>
		                </div>
		
		                <!-- User Information -->
		                <div class="profile-identity">
		                    <h1><%= profileUser.getFullName() %></h1>
		                    <p class="profile-username">@<%= profileUser.getUsername() %></p>
		                    <span class="profile-role"><%= profileUser.getRole() %></span>
		                </div>
		
		                <!-- Settings -->
		                <%
		                    if (isOwnProfile) {
		                %>
				                <div class="profile-actions">
				                    <a href="${pageContext.request.contextPath}/information_change.jsp" class="profile-settings-button">
				                        <img src="${pageContext.request.contextPath}/pictures/assessments/setting.png" alt="Settings">
				                        <span>Settings</span>
				                    </a>
				                </div>
		                <%
		                    }
		                %>
		
		            </div>
		
		        </section>
		
		        <!-- Favourite movies -->
		        <section id="favourites" class="profile-section favourite-section">
		
		            <!-- Section Header -->
		            <div class="section-header">
		                <div>
		                    <h2>Favourite Movies</h2>
		                    <p class="section-description">
		                        <%
		                            if (isOwnProfile) {
		                        %>
		                            	Movies you have added to your favourites.
		                        <%
		                            } 
		                            else {
		                        %>
		                            	Movies this user has added to their favourites.
		                        <%
		                            }
		                        %>
		                    </p>
		                </div>
		
		                <!-- Public / Private -->
		                <%
		                    if (isOwnProfile) {
		                %>
				                <button 
				                    type="button" 
				                    class="visibility-button" 
				                    id="favouriteVisibilityButton" 
				                    data-public="<%= profileUser.getFavouriteMoviesVisibility() %>" 
				                    data-url="${pageContext.request.contextPath}/favourite-visibility">
				                    <img 
				                        id="visibilityIcon" 
				                        src="${pageContext.request.contextPath}/pictures/assessments/public.png" 
				                        alt="Public" 
				                        class="visibility-icon">
				                    <span id="visibilityText">Public</span>
				                </button>
		                <%
		                    } 
		                    else if (profileUser.getFavouriteMoviesVisibility()) {
		                %>
				                <span class="public-label">Public</span>
		                <%
		                    } 
		                    else {
		                %>
				                <span class="private-label">Private</span>
		                <%
		                    }
		                %>
		            </div>
		
		            <!-- Favourite movie grid -->
		            <jsp:include page="/components/movie_grid.jsp" />
		        </section>
		
		        <!-- Booking History -->
				<section id="bookings" class="profile-section booking-section">
				    <div class="section-header">
				        <div>
				            <h2>Bookings</h2>
				            <p class="section-description">
		                        <%
		                            if (isOwnProfile) {
		                        %>
		                            	Your booking history.
		                        <%
		                            } 
		                            else {
		                        %>
		                            	This user's booking history.
		                        <%
		                            }
		                        %>
		                    </p>
				        </div>
				        <span class="private-label">Private</span>
				    </div>
				
				    <%
				        if (isOwnProfile) {
				            List<Map<String, Object>> bookingHistory = (List<Map<String, Object>>) request.getAttribute("bookingHistory");				
				            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm");
				    %>				
				            <%
				                if (bookingHistory == null || bookingHistory.isEmpty()) {
				            %>
				                    <div class="booking-placeholder">
				                        <h3>No bookings yet</h3>
				                        <p>Your bookings will appear here after you book a movie.</p>
				                    </div>
				            <%
				                } 
				                else {
				            %>
				                    <div class="booking-history-list">
				                        <%
				                            for (Map<String, Object> booking : bookingHistory) {
				                                String status = String.valueOf(booking.get("booking_status"));
				                                String statusClass = status.toLowerCase();				
				                                Object bookAt = booking.get("book_at");
				                                Object startAt = booking.get("start_at");
				                                Object endAt = booking.get("end_at");
				                                String posterPath = (String) booking.get("poster_path");
				                        %>
				                                <article class="booking-card">
				                                    <div class="booking-card-main">
				                                        <div class="booking-poster">
				                                            <%
				                                                if (posterPath != null && !posterPath.trim().isEmpty()) {
				                                            %>
				                                                    <img src="<%= request.getContextPath() %>/<%= posterPath %>" alt="Movie poster">
				                                            <%
				                                                } 
				                                                else {
				                                            %>
				                                                    <div class="booking-no-poster">No poster</div>
				                                            <%
				                                                }
				                                            %>
				                                        </div>
				
				                                        <div class="booking-card-info">
				                                            <h3><%= booking.get("movie_name") %></h3>								                                           			
				                                            <p>
				                                                Booked:
				                                                <%= bookAt == null ? "-" : dateFormat.format((java.util.Date) bookAt) %>
				                                            </p>
				
				                                            <div class="booking-card-bottom">
				                                                <span class="booking-status <%= statusClass %>">
				                                                    <%= status %>
				                                                </span>
				
				                                                <strong>
				                                                    <%= String.format("%,d", booking.get("price")) %>VND
				                                                </strong>
				                                            </div>
				                                        </div>
				                                    </div>
				
				                                    <details class="booking-details">
				                                        <summary>View Details</summary>
				
				                                        <div class="booking-details-content">
				                                            <p>
				                                                <strong>Theater:</strong>
				                                                <%= booking.get("theater_name") %>
				                                            </p>
				
				                                            <p>
				                                                <strong>Room:</strong>
				                                                <%= booking.get("room_name") %>
				                                            </p>
				
				                                            <p>
				                                                <strong>Showtime:</strong>
				                                                <%= startAt == null ? "-" : dateFormat.format((java.util.Date) startAt) %>
				                                            </p>
				
				                                            <p>
				                                                <strong>End time:</strong>
				                                                <%= endAt == null ? "-" : dateFormat.format((java.util.Date) endAt) %>
				                                            </p>
				
				                                            <p>
				                                                <strong>Seats and ticket types:</strong>
				                                                <%= booking.get("seat_summary") == null ? "No seat information" : booking.get("seat_summary") %>
				                                            </p>
				
				                                            <p>
				                                                <strong>Payment status:</strong>
				                                                <%= booking.get("payment_status") %>
				                                            </p>
				
				                                            <p>
				                                                <strong>Payment method:</strong>
				                                                <%= booking.get("payment_method") == null ? "-" : booking.get("payment_method") %>
				                                            </p>
				
				                                            <p>
				                                                <strong>Payment amount:</strong>
				                                                <%= booking.get("payment_amount") == null ? "-" : String.format("%,d", ((Number) booking.get("payment_amount")).longValue()) + "VND" %>
				                                            </p>
				                                        </div>
				                                    </details>
				                                </article>
				                        <%
				                            }
				                        %>
				                    </div>
				            <%
				                }
				            %>
				
				    <%
				        } 
				        else {
				    %>
				            <div class="booking-placeholder">
				                <div class="booking-lock">Private</div>
				                <h3>Booking History</h3>
				                <p>This user's bookings are private and can only be viewed by them.</p>
				            </div>
				    <%
				        }
				    %>
				</section>
		    </main>
		
		    <!-- Right Sidebar -->
		    <%@ include file="components/right_sidebar.jsp" %>
		
		    <!-- Footer -->
		    <%@ include file="components/footer.jsp" %>
		</div>
		<%
		    /*Only load the favourite visibility JavaScript on the owner's profile.*/
		    if (isOwnProfile) {
		%>
				<script src="${pageContext.request.contextPath}/scripts/favourite_movies_visibility.js"></script>
		<%
		    }
		%>
	</body>
</html>