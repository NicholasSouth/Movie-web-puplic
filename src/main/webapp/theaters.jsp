<%@ page import="java.util.List" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%
    List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
    String searchParam = request.getParameter("search");
    boolean hasSearch = searchParam != null && !searchParam.trim().isEmpty();
    Theaters nearestTheater = (Theaters) request.getAttribute("nearestTheater");
    Double nearestDistance = (Double) request.getAttribute("nearestDistance");
    request.setAttribute("currentPage", "theaters");
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>PhnetPhlyx - Theaters</title>
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/theaters.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>
	<body data-context-path="<%= request.getContextPath() %>">
		<section class="Layout1">
		
		    <!-- Header -->
		    <%@ include file="components/header.jsp" %>
		
		    <%@ include file="components/left_sidebar.jsp" %>
		
		    <!-- Main Body -->
		    <main class="MainBody">
		        <div class="theater-header">
		            <h1>Theaters</h1>
		            <p>Find a cinema near you.</p>
		
		            <!-- Theater Search -->
		            <form class="theater-search" action="<%= request.getContextPath() %>/theaters" method="get">
		                <input 
		                    type="search" 
		                    name="search" 
		                    placeholder="Search by theater name or address..." 
		                    value="<%= searchParam != null ? searchParam : "" %>">
		                <button type="submit">Search</button>
		            </form>
		
		            <!-- Find Nearby Theater -->
		            <div class="theater-header-row">
		                <p>Find a theater near you</p>
		                <button type="button" id="find-near-btn" class="theater-button">
		                    Find theater near me
		                </button>
		            </div>
		
		            <span id="find-near-status" class="find-near-status" style="display: none;">
		                Finding the theater near you...
		            </span>
		        </div>
		
		        <!-- Nearest Theater Result -->
		        <%
		            if (nearestTheater != null) {
		        %>
				        <section id="nearest-theater-result" class="theater-section">
				            <h3 class="nearest-title">Nearest theater to you</h3>
				            <div class="theater-grid">
				                <article class="theater-card nearest-card">
				                    <div class="theater-image">
				                        <img 
				                            src="<%= request.getContextPath() %>/<%= nearestTheater.getTheater_image_path() %>" 
				                            alt="<%= nearestTheater.getTheater_name() %>">
				                    </div>
				                    <div class="theater-content">
				                        <h2><%= nearestTheater.getTheater_name() %></h2>
				                        <p class="theater-address"><%= nearestTheater.getTheater_address() %></p>
				                        <div class="theater-info">
				                            <%
				                                if (nearestTheater.getOpen_time() != null && nearestTheater.getClosing_time() != null) {
				                            %>
					                                <span>
					                                    Open: <%= nearestTheater.getOpen_time() %> - <%= nearestTheater.getClosing_time() %>
					                                </span>
				                            <%
				                                }
				                            %>
				                            <%
				                                if (nearestDistance != null) {
				                            %>
					                                <span class="theater-distance">
					                                    <%= String.format("%.2f", nearestDistance) %> km away
					                                </span>
				                            <%
				                                }
				                            %>
				                        </div>
				                        <a href="<%= request.getContextPath() %>/theater-details?id=<%= nearestTheater.getTheater_id() %>" class="theater-button">
				                            View Theater
				                        </a>
				                    </div>
				                </article>
				            </div>
				        </section>
		        <%
		            } 
		            else if (request.getParameter("userLat") != null && request.getParameter("userLng") != null) {
		        %>
				        <section id="nearest-theater-result" class="theater-section">
				            <p class="no-theaters">Unable to find a nearby theater.</p>
				        </section>
		        <%
		            }
		        %>
		
		        <!-- Theater List -->
		        <h3 class="all-theaters-title">
		            <%= hasSearch ? "Search Results" : "All Theaters" %>
		        </h3>
		
		        <!-- Theater browse -->
		        <section class="theater-grid">
		            <%
		                if (theaters != null && !theaters.isEmpty()) {
		                    for (Theaters theater : theaters) {
		            %>
				                <article class="theater-card">
				                    <div class="theater-image">
				                        <img 
				                            src="<%= request.getContextPath() %>/<%= theater.getTheater_image_path() %>" 
				                            alt="<%= theater.getTheater_name() %>">
				                    </div>
				                    <div class="theater-content">
				                        <h2><%= theater.getTheater_name() %></h2>
				                        <p class="theater-address"><%= theater.getTheater_address() %></p>
				                        <div class="theater-info">
				                            <%
				                                if (theater.getOpen_time() != null && theater.getClosing_time() != null) {
				                            %>
					                                <span>
					                                    Open: <%= theater.getOpen_time() %> - <%= theater.getClosing_time() %>
					                                </span>
				                            <%
				                                }
				                            %>
				                        </div>
				                        <a href="<%= request.getContextPath() %>/theater-details?id=<%= theater.getTheater_id() %>" class="theater-button">
				                            View Theater
				                        </a>
				                    </div>
				                </article>
		            <%
		                    }
		                } 
		                else {
		            %>
			                <p class="no-theaters">
			                    <%= hasSearch ? "No theaters found matching your search." : "No theaters found." %>
			                </p>
		            <%
		                }
		            %>
		        </section>
		    </main>
		
		    <!-- Right Sidebar -->
		    <%@ include file="components/right_sidebar.jsp" %>
		
		    <!-- Footer -->
		    <%@ include file="components/footer.jsp" %>
		</section>
		
		<script src="${pageContext.request.contextPath}/scripts/theaters.js"></script>
	</body>
</html>