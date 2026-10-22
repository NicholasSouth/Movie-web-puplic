<%
    String currentPage = (String) request.getAttribute("currentPage");
    if (currentPage == null) {
        currentPage = "";
    }
%>
<aside class="LeftSidebar">
    <nav class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/main.jsp"
           class="nav-item <%= "home".equals(currentPage) ? "active" : "" %>">
            Home
        </a>
        <a href="${pageContext.request.contextPath}/movies"
           class="nav-item <%= "movies".equals(currentPage) ? "active" : "" %>">
            Movies
        </a>
        <a href="${pageContext.request.contextPath}/theaters"
           class="nav-item <%= "theaters".equals(currentPage) ? "active" : "" %>">
            Theaters
        </a>
        <a href="${pageContext.request.contextPath}/user-profile#bookings"
           class="nav-item <%= "bookings".equals(currentPage) ? "active" : "" %>">
            My Bookings
        </a>
        <a href="${pageContext.request.contextPath}/user-profile#favourites"
           class="nav-item <%= "favorites".equals(currentPage) ? "active" : "" %>">
            Favorites
        </a>
    </nav>
</aside>