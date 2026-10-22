document.addEventListener("DOMContentLoaded", function () {
    /*Get the menu button and dropdown menu*/
    const menuButton = document.getElementById("menuButton");
    const menuDropdown = document.getElementById("menuDropdown");
    
    if (!menuButton || !menuDropdown) {
        return;
    }

	menuDropdown.classList.remove("show");
	menuButton.setAttribute(
	    "aria-expanded",
	    "false"
	);
	
    /*Open / Close Menu*/
    menuButton.addEventListener("click", function (event) {
        /*Prevent this click from reaching the document click listener below.*/
        event.stopPropagation();

        /*Check whether the menu is currently open.*/
        const isOpen =
            menuDropdown.classList.contains("show");
        /*If it is open, close it.*/
        if (isOpen) {
            menuDropdown.classList.remove("show");
            menuButton.setAttribute(
                "aria-expanded",
                "false"
            );
        }
        /*Else open it.*/
        else {
            menuDropdown.classList.add("show");
            menuButton.setAttribute(
                "aria-expanded",
                "true"
            );
        }
    });

    /*Don't let a click inside the dropdown immediately close it.*/
    menuDropdown.addEventListener("click", function (event) {
        event.stopPropagation();
    });

    /*Clicking anywhere else on the page closes the dropdown.*/
    document.addEventListener("click", function () {
        menuDropdown.classList.remove("show");
        menuButton.setAttribute(
            "aria-expanded",
            "false"
        );
    });

    /*Pressing the Escape key closes the menu.*/
    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            menuDropdown.classList.remove("show");
            menuButton.setAttribute(
                "aria-expanded",
                "false"
            );
        }
    });
});