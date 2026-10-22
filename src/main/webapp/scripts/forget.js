function sendVerificationCode()
{
    const account = document.getElementById("account").value.trim();
    if (account === "")
    {
        alert("Please enter your username, email or phone number first.");
        document.getElementById("account").focus();
        return;
    }
    const button = document.querySelector(".send-button");
    button.disabled = true;
    button.textContent = "Sending...";
    fetch(
        "send-forgot-verification",
        {
            method: "POST",
            headers:
            {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: "account=" + encodeURIComponent(account)
        }
    )
    .then(response => response.text())
    .then(message =>
    {
        alert(message);
        button.disabled = false;
        button.textContent = "Send Code";
    })
    .catch(error =>
    {
        console.error(error);
        alert(
            "Unable to send verification code. " +
            "Please try again."
        );
        button.disabled = false;
        button.textContent = "Send Code";
    });
}

function validateForgotForm()
{
    const account = document.getElementById("account").value.trim();
    const password = document.getElementById("new-password").value;
    const confirmPassword = document.getElementById("confirm-password").value;
    const verificationCode = document.getElementById("verification-code").value.trim();
    if (account === "")
    {
        alert("Please enter your username, email or phone number.");
        return false;
    }
    if (password === "")
    {
        alert("Please enter a new password.");
        return false;
    }
    if (confirmPassword === "")
    {
        alert("Please confirm your new password.");
        return false;
    }
    if (password !== confirmPassword)
    {
        alert("Passwords do not match.");
        return false;
    }
    if (verificationCode === "")
    {
        alert("Please enter the verification code.");
        return false;
    }
    if (!/^\d{6}$/.test(verificationCode))
    {
        alert("Verification code must be 6 digits.");
        return false;
    }
    return true;
}