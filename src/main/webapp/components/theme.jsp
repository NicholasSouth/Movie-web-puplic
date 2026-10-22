<%
	com.movieweb.model.Users themeUser = (com.movieweb.model.Users) session.getAttribute("user");
	if (themeUser != null) {
%>
		<link rel="stylesheet" href="<%= request.getContextPath() %>/styles/theme_light.css">
		<script>
			(function () {
				try {
					if (localStorage.getItem("phnetphlyx_theme_<%= themeUser.getUserId() %>") === "light") {
						document.documentElement.setAttribute("data-theme", "light");
					}
				} catch (e) {}
			})();
		</script>
<%
	}
%>
