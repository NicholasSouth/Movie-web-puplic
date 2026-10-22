document.addEventListener("DOMContentLoaded", function () {
    const visibilityButton = document.getElementById("favouriteVisibilityButton");
    const visibilityIcon = document.getElementById("visibilityIcon");
    const visibilityText = document.getElementById("visibilityText");
		
    if (!visibilityButton) {
        return;
    }
    let isPublic = visibilityButton.dataset.public === "true";

    function updateVisibilityButton() {
        if (isPublic) {
            visibilityIcon.src = visibilityIcon.src.substring(
					0,
                    visibilityIcon.src.lastIndexOf("/") + 1) + "public.png";
            visibilityIcon.alt = "Public";
            visibilityText.textContent = "Public";
        }
        else {
            visibilityIcon.src = visibilityIcon.src.substring(
                    0,
                    visibilityIcon.src.lastIndexOf("/") + 1) + "private.png";
            visibilityIcon.alt = "Private";
            visibilityText.textContent = "Private";
        }
    }
    visibilityButton.addEventListener(
        "click",
        function () {
            const newVisibility = !isPublic;

            fetch(
                visibilityButton.dataset.url,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: "isPublic=" + encodeURIComponent(newVisibility)
                }
            )
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Failed to update visibility.");
                }
                isPublic = newVisibility;
                updateVisibilityButton();
            })
            .catch(function (error) {
                console.error(error);
                alert("Could not update favourite movie visibility.");
            });
        }
    );
    updateVisibilityButton();
});