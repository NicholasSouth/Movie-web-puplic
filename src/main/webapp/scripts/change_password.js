function sendVerificationCode()
{
    const button = document.querySelector(".send-button");
    button.disabled = true;
    button.textContent = "Sending...";
    fetch(
        "send-change-verification",
        {
            method: "POST",
            headers:
            {
                "Content-Type": "application/x-www-form-urlencoded"
            }
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

function validateChangePasswordForm()
{
    const currentPassword = document.getElementById("currentPassword").value;
    const password = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const verificationCode = document.getElementById("verification-code").value.trim();

    if (currentPassword === "")
    {
        alert("Please enter your current password.");
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