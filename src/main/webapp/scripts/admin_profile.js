function sendAdminVerificationCode() {
    const form = document.getElementById("adminPasswordForm");
    const button = document.getElementById("adminSendCodeButton");
    if (!form || !button) {
        return;
    }
    button.disabled = true;
    button.textContent = "Sending...";
    fetch(form.dataset.sendUrl, { method: "POST" })
        .then(function (response) { return response.text(); })
        .then(function (message) { alert(message); })
        .catch(function () { alert("Unable to send verification code. Please try again."); })
        .finally(function () {
            button.disabled = false;
            button.textContent = "Send Code";
        });
}

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("adminPasswordForm");
    if (form) {
        form.addEventListener("submit", function (event) {
            const next = document.getElementById("newPassword").value;
            const confirm = document.getElementById("confirmPassword").value;
            const code = document.getElementById("verification-code").value.trim();
            let message = null;
            if (next.length < 8) {
                message = "Password must contain at least 8 characters.";
            } else if (next !== confirm) {
                message = "Passwords do not match.";
            } else if (!/^\d{6}$/.test(code)) {
                message = "Verification code must be 6 digits.";
            }
            if (message) {
                event.preventDefault();
                alert(message);
            }
        });
    }
    // Show the chosen file name next to each upload button.
    document.querySelectorAll(".ap-file-input").forEach(function (input) {
        input.addEventListener("change", function () {
            const label = document.getElementById(input.id + "Name");
            if (label) {
                label.textContent = input.files.length ? input.files[0].name : "No file chosen";
            }
        });
    });
});
