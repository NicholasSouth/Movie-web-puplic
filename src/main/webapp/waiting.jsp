<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Waiting - PhnetPhlyx</title>
		<link 
			rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/main.css">
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/waiting.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<main class="waiting-page">
		    <div class="waiting-container">
		        <!--Waiting Message-->
		        <h1>
		            Please Wait
		        </h1>
		        <p class="waiting-message">
		            Your manager account is waiting for administrator approval.
		            Please wait for an email before logging in again.
		        </p>
		
		        <!--Back to Main-->
		        <a href="${pageContext.request.contextPath}/main.jsp" class="main-button">
		            Back to Main
		        </a>
		    </div>
		</main>
	</body>
</html>