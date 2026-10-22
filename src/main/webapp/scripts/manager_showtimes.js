document.addEventListener("DOMContentLoaded", function () {
    const startAtInput = document.getElementById("startAt");
    const endAtInput = document.getElementById("endAt");
    const movieSelect = document.getElementById("movieId");
    if (!startAtInput || !endAtInput || !movieSelect) {
        return;
    }

    // Movie durations are supplied by the JSP through data-duration.
    function updateEndTime() {
        const startValue = startAtInput.value;
        const selectedOption = movieSelect.options[movieSelect.selectedIndex];
        const duration = Number(selectedOption?.dataset.duration);
        if (!startValue || !Number.isFinite(duration) || duration <= 0) {
            endAtInput.value = "";
            return;
        }
        const startDate = new Date(startValue);
        if (Number.isNaN(startDate.getTime())) {
            endAtInput.value = "";
            return;
        }
        const endDate = new Date(startDate.getTime() + duration * 60 * 1000);
        const pad = number => String(number).padStart(2, "0");
        endAtInput.value =
            `${endDate.getFullYear()}-${pad(endDate.getMonth() + 1)}-${pad(endDate.getDate())}` +
            `T${pad(endDate.getHours())}:${pad(endDate.getMinutes())}`;
    }
    startAtInput.addEventListener("input", updateEndTime);
    movieSelect.addEventListener("change", updateEndTime);
    updateEndTime();
});
