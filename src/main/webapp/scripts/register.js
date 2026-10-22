function sendVerificationCode() {
    const email =
        document.getElementById("email").value.trim();
    if (email === "") {
        alert("Please enter your email address first.");
        document.getElementById("email").focus();
        return;
    }
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        alert("Please enter a valid email address.");
        document.getElementById("email").focus();
        return;
    }
    const button = document.querySelector(".send-button");
    button.disabled = true;
    button.textContent = "Sending...";
    fetch(
        "send-verification",
        {
            method: "POST",
            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded"
            },
            body:
                "email=" +
                encodeURIComponent(email) +
                "&purpose=register"
        }
    )
    .then(response => response.text())
    .then(message => {
        alert(message);
        button.disabled = false;
        button.textContent = "Send Code";
    })
    .catch(error => {
        console.error(error);
        alert(
            "Unable to send verification code. " +
            "Please try again."
        );
        button.disabled = false;
        button.textContent = "Send Code";
    });
}

function validateRegisterForm() {
    const username = document.getElementById("username").value.trim();
    const fullName = document.getElementById("full_name").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm_password").value;
    const verificationCode = document.getElementById("verification_code").value.trim();
		
    if (username === "") {
        alert("Please enter a username.");
        return false;
    }
    if (fullName === "") {
        alert("Please enter your full name.");
        return false;
    }
    if (email === "") {
        alert("Please enter your email address.");
        return false;
    }
    if (password === "") {
        alert("Please enter a password.");
        return false;
    }
    if (confirmPassword === "") {
        alert("Please confirm your password.");
        return false;
    }
    if (password !== confirmPassword) {
        alert("Passwords do not match.");
        return false;
    }
    if (verificationCode === "") {
        alert("Please enter the verification code.");
        return false;
    }
    return true;
}