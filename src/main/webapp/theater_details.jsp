<%@ page import="java.util.List,java.util.Map,java.util.ArrayList,java.util.Collections,java.util.Comparator,java.util.HashSet,java.util.Set,java.time.LocalDate,java.time.format.DateTimeFormatter" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.Rooms" %>
<%@ page import="com.movieweb.model.Movies" %>
<%@ page import="com.movieweb.model.Showtimes" %>
<%
    Theaters theater = (Theaters) request.getAttribute("theater");
    List<Rooms> rooms = (List<Rooms>) request.getAttribute("rooms");
    List<Showtimes> scheduleShowtimes = (List<Showtimes>) request.getAttribute("scheduleShowtimes");
    Map<Integer, Movies> scheduleMovies = (Map<Integer, Movies>) request.getAttribute("scheduleMovies");
    LocalDate selectedDate = (LocalDate) request.getAttribute("selectedDate");
    List<LocalDate> weekDates = (List<LocalDate>) request.getAttribute("weekDates");
    String contextPath = request.getContextPath();
    DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd");  
    List<java.sql.Timestamp> timeSlots = new ArrayList<>();
    if (scheduleShowtimes != null) {
        for (Showtimes showtime : scheduleShowtimes) {
            if (showtime.getStart_at() != null) {
                boolean alreadyExists = false;
                for (java.sql.Timestamp existingTime : timeSlots) {
                    if (existingTime.toLocalDateTime().toLocalTime().equals(showtime.getStart_at().toLocalDateTime().toLocalTime())) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    timeSlots.add(showtime.getStart_at());
                }
            }
        }
    }
    Collections.sort(
        timeSlots,
        new Comparator<java.sql.Timestamp>() {
            @Override
            public int compare(java.sql.Timestamp first, java.sql.Timestamp second) {
                return first.compareTo(second);
            }
        }
    );
    
    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>
	        <%= theater != null ? theater.getTheater_name() + " - PhnetPhlyx" : "Theater Details - PhnetPhlyx" %>
	    </title>
	    <link rel="stylesheet" href="<%= contextPath %>/styles/main.css">
	    <link rel="stylesheet" href="<%= contextPath %>/styles/theater_details.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<section class="Layout1">
		
		    <!-- Header -->
		    <%@ include file="components/header.jsp" %>
		
		    <!-- Left Sidebar -->
		    <%
		        request.setAttribute("currentPage", "theaters");
		    %>
		    <%@ include file="components/left_sidebar.jsp" %>
		
		    <!-- Main Body -->
		    <main class="MainBody">
		        <%
		            if (theater != null) {
		        %>
				        <!-- Theater Information -->
				        <section class="theater-details">
				            <div class="theater-details-image">
				                <%
				                    if (theater.getTheater_image_path() != null && !theater.getTheater_image_path().isEmpty()) {
				                %>
				                    	<img 
					                    	src="<%= contextPath + theater.getTheater_image_path() %>" 
					                    	alt="<%= theater.getTheater_name() %>">
				                <%
				                    } 
				                    else {
				                %>
				                    	<div class="no-image">No Image Available</div>
				                <%
				                    }
				                %>
				            </div>
				            <div class="theater-details-information">
				                <h1><%= theater.getTheater_name() %></h1>
				                <p>
				                    <strong>Address:</strong>
				                    <%= theater.getTheater_address() != null ? theater.getTheater_address() : "Not available" %>
				                </p>
				                <p>
				                    <strong>Opening Hours:</strong>
				                    <%
				                        if (theater.getOpen_time() != null && theater.getClosing_time() != null) {
				                    %>
				                        	<%= theater.getOpen_time().toLocalTime() %> - <%= theater.getClosing_time().toLocalTime() %>
				                    <%
				                        } 
				                        else {
				                    %>
				                        	Not available
				                    <%
				                        }
				                    %>
				                </p>				                
				            </div>
				        </section>
		
				        <!-- About This Theater -->
				        <section class="theater-section">
				            <h2>About This Theater</h2>
				            <%
				                if (theater.getDescription() != null && !theater.getDescription().isEmpty()) {
				            %>
				                	<p>
				                		<%= theater.getDescription() %>
				                	</p>
				            <%
				                } 
				                else {
				            %>
				                	<p class="no-data">No description available.</p>
				            <%
				                }
				            %>
				        </section>
		
				        <!-- Available Rooms -->
				        <section class="theater-section">
				            <h2>Available Rooms</h2>
				            <div class="room-grid">
				                <%
				                    if (rooms != null && !rooms.isEmpty()) {
				                        for (Rooms room : rooms) {
				                %>
						                    <article class="room-card">
						                        <h3><%= room.getRoom_name() %></h3>
						                        <p>
						                            <%= room.getRoomType() != null ? room.getRoomType().getRoom_type_name() : "Standard" %>
						                        </p>
						                    </article>
				                <%
				                        }
				                    } 
				                    else {
				                %>
				                    	<p class="no-data">No available rooms.</p>
				                <%
				                    }
				                %>
				            </div>
				        </section>
		
				        <!-- Movie Schedule -->
				        <section class="theater-section theater-schedule" id="movie-schedule">
				            <h2>Movie Schedule</h2>
				            
				            <!-- Week Selector -->
				            <div class="schedule-week-selector">
				                <%
				                    if (weekDates != null && !weekDates.isEmpty()) {
				                        for (LocalDate date : weekDates) {
				                            boolean isSelected = selectedDate != null && selectedDate.equals(date);
				                %>
						                    <a 
						                    	href="<%= contextPath %>/theater-details?id=<%= theater.getTheater_id() %>&date=<%= date %>#movie-schedule"
   												class="schedule-day <%= isSelected ? "selected" : "" %>">
							                        <span class="schedule-day-name"><%= dayFormatter.format(date) %></span>
							                        <span class="schedule-day-date"><%= dateFormatter.format(date) %></span>
						                    </a>
				                <%
				                        }
				                    }
				                %>
				            </div>
		
				            <!-- Schedule Table -->
				            <div class="schedule-table-wrapper">
				                <table class="schedule-table">
				                    <thead>
				                        <tr>
				                            <th class="schedule-time-header">Time</th>
				                            <%
				                                if (rooms != null) {
				                                    for (Rooms room : rooms) {
				                            %>
						                            	<th><%= room.getRoom_name() %></th>
				                            <%
				                                    }
				                                }
				                            %>
				                        </tr>
				                    </thead>
				                    <tbody>
				                        <%
				                            if (timeSlots.isEmpty()) {
				                        %>
						                        <tr>
						                            <td colspan="<%= rooms != null ? rooms.size() + 1 : 1 %>" class="no-schedule">
						                                No movies are showing on this day.
						                            </td>
						                        </tr>
				                        <%
				                            } 
				                            else {
				                                for (java.sql.Timestamp timeSlot : timeSlots) {
				                        %>
						                        <tr>
						                            <td class="schedule-time">
						                                <%= timeFormat.format(timeSlot) %>
						                            </td>
						                            <%
						                                if (rooms != null) {
						                                    for (Rooms room : rooms) {
						                                        Showtimes matchingShowtime = null;
						                                        if (scheduleShowtimes != null) {
						                                            for (Showtimes showtime : scheduleShowtimes) {
						                                                if (showtime.getRoom_id() == room.getRoom_id() && 
						                                                    showtime.getStart_at() != null && 
						                                                    showtime.getStart_at().toLocalDateTime().toLocalTime().equals(timeSlot.toLocalDateTime().toLocalTime())) {
						                                                    matchingShowtime = showtime;
						                                                    break;
						                                                }
						                                            }
						                                        }
						                            %>
								                            <td class="schedule-cell">
								                                <%
								                                    if (matchingShowtime != null) {
								                                        Movies movie = scheduleMovies != null ? scheduleMovies.get(matchingShowtime.getMovie_id()) : null;
								                                        if (movie != null) {
								                                %>
										                                    <a href="<%= contextPath %>/movie-details?id=<%= movie.getMovie_id() %>" class="schedule-movie">
										                                        <div class="schedule-movie-poster">
										                                            <%
										                                                if (movie.getPoster_path() != null && !movie.getPoster_path().isEmpty()) {
										                                            %>
												                                        <img src="<%= contextPath %>/<%= movie.getPoster_path() %>" alt="<%= movie.getMovie_name() %>">
										                                            <%
										                                                } 
										                                                else {
										                                            %>
												                                        <div class="no-image">No Poster</div>
										                                            <%
										                                                }
										                                            %>
										                                        </div>
										                                        <div class="schedule-movie-information">
										                                            <h3><%= movie.getMovie_name() %></h3>
										                                            <p>
										                                                <%= timeFormat.format(matchingShowtime.getStart_at()) %> - <%= timeFormat.format(matchingShowtime.getEnd_at()) %>
										                                            </p>
										                                        </div>
										                                    </a>
								                                <%
								                                        } 
								                                        else {
								                                %>
										                                    <span class="schedule-missing">Movie unavailable</span>
								                                <%
								                                        }
								                                    }
								                                %>
								                            </td>
						                            <%
						                                    }
						                                }
						                            %>
						                        </tr>
				                        <%
				                                }
				                            }
				                        %>
				                    </tbody>
				                </table>
				            </div>
				        </section>
		        <%
		            } 
		            else {
		        %>
				        <!-- Theater Not Found -->
				        <section class="theater-section">
				            <h2>Theater Not Found</h2>
				            <p>The requested theater could not be found.</p>
				        </section>
		        <%
		            }
		        %>
		    </main>
		    <!-- Right Sidebar -->
		    <%@ include file="components/right_sidebar.jsp" %>
		
		    <!-- Footer -->
		    <%@ include file="components/footer.jsp" %>
		</section>
	</body>
</html>