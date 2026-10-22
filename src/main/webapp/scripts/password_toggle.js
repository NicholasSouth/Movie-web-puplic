function togglePassword(inputId, button) {
    const input = document.getElementById(inputId);
    if (input.type === "password") {
        input.type = "text";
        button.textContent = "Hide";
		button.classList.add("password-visible");
    } 
	else {
        input.type = "password";
        button.textContent = "Show";
		button.classList.remove("password-visible");
    }
}