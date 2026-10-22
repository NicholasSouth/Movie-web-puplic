document.addEventListener("DOMContentLoaded", function () {
    /* Get the menu button and dropdown menu */
    const menuButton = document.getElementById("menuButton");
    const menuDropdown = document.getElementById("menuDropdown");

    if (!menuButton || !menuDropdown) {
        return;
    }

    /* Ensure the menu starts closed */
    menuDropdown.classList.remove("show");
    menuButton.setAttribute("aria-expanded", "false");

    /* Open / Close Menu */
    menuButton.addEventListener("click", function (event) {
        event.stopPropagation();

        const isOpen = menuDropdown.classList.contains("show");

        if (isOpen) {
            menuDropdown.classList.remove("show");
            menuButton.setAttribute("aria-expanded", "false");
        } else {
            menuDropdown.classList.add("show");
            menuButton.setAttribute("aria-expanded", "true");
        }
    });

    /* Prevent clicks inside the dropdown from closing it */
    menuDropdown.addEventListener("click", function (event) {
        event.stopPropagation();
    });

    /* Close the dropdown when clicking outside */
    document.addEventListener("click", function () {
        menuDropdown.classList.remove("show");
        menuButton.setAttribute("aria-expanded", "false");
    });

    /* Close the dropdown when pressing Escape */
    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            menuDropdown.classList.remove("show");
            menuButton.setAttribute("aria-expanded", "false");
        }
    });
});
