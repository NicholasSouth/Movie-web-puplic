<%--
    Reusable Password Toggle Component
    Usage:
    <jsp:include page="/components/password_toggle.jsp">
        <jsp:param name="inputId" value="password" />
    </jsp:include>
--%>

<button
    type="button"
    class="password-toggle"
    onclick="togglePassword('<%= request.getParameter("inputId") %>', this)">
    Show
</button>
<script 
	src="${pageContext.request.contextPath}/scripts/password_toggle.js">
</script>