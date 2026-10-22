function changeMovie(movieId) {
    const dateSelect = document.getElementById("dateSelect");
    const date = dateSelect ? dateSelect.value : "";
    if (!movieId) {
        window.location.href = contextPath + "/manager-create-booking";
        return;
    }
    let url = contextPath
        + "/manager-create-booking"
        + "?movie_id="
        + encodeURIComponent(movieId);
    if (date) {
        url += "&date=" + encodeURIComponent(date);
    }
    window.location.href = url;
}

function changeDate(date) {
    const movieSelect = document.getElementById("movieSelect");
    if (!movieSelect || !movieSelect.value) {
        return;
    }
    let url = contextPath
        + "/manager-create-booking"
        + "?movie_id="
        + encodeURIComponent(movieSelect.value);
    if (date) {
        url += "&date=" + encodeURIComponent(date);
    }
    window.location.href = url;
}

function continueBooking() {
    const movieSelect = document.getElementById("movieSelect");
    const dateSelect = document.getElementById("dateSelect");
    const selectedShowtime = document.querySelector('input[name="showtime_id"]:checked');
    const selectedTicketType = document.querySelector('input[name="ticket_type_id"]:checked');
    
	if (!movieSelect || !movieSelect.value) {
        alert("Please select a movie.");
        return;
    }
    if (!dateSelect || !dateSelect.value) {
        alert("Please select a date.");
        return;
    }
    if (!selectedShowtime) {
        alert("Please select a showtime.");
        return;
    }
    if (!selectedTicketType) {
        alert("Please select a ticket type.");
        return;
    }
    window.location.href = contextPath
        + "/manager-seat-selection"
        + "?showtime_id="
        + encodeURIComponent(selectedShowtime.value)
        + "&ticket_type_id="
        + encodeURIComponent(selectedTicketType.value);
}

document.addEventListener("DOMContentLoaded", function () {
        const movieSelect = document.getElementById("movieSelect");
        const dateSelect = document.getElementById("dateSelect");
        const continueButton = document.getElementById("continueBookingButton");
        if (movieSelect) {
            movieSelect.addEventListener("change", function () {
                    changeMovie(this.value);
                }
            );
        }
        if (dateSelect) {
            dateSelect.addEventListener("change", function () {
                    changeDate(this.value);
                }
            );
        }
        if (continueButton) {
            continueButton.addEventListener(
                "click",
                continueBooking
            );
        }
    }
);