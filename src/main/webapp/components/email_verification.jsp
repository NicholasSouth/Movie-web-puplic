<%
    String inputId = request.getParameter("inputId");
    String inputName = request.getParameter("inputName");
    String sendFunction = request.getParameter("sendFunction");
    if (inputId == null || inputId.trim().isEmpty()) {
        inputId = "verification-code";
    }
    if (inputName == null || inputName.trim().isEmpty()) {
        inputName = "verification_code";
    }
    if (sendFunction == null || sendFunction.trim().isEmpty()) {
        sendFunction = "sendVerificationCode";
    }
%>

<!-- Email Verification -->
<div class="verification-section">
    <div class="verification-title">
        Email Verification
    </div>
    <p class="verification-note">
        Enter the verification code sent to your email.
    </p>
    <div class="verification-row">
        <input
            type="text"
            id="<%= inputId %>"
            name="<%= inputName %>"
            placeholder="Enter verification code"
            maxlength="6"
            inputmode="numeric"
            autocomplete="one-time-code"
            required>
        <button
            type="button"
            class="send-button"
            onclick="<%= sendFunction %>()">
            Send Code
        </button>
    </div>
</div>