function openRefundModal(bookingId) {
	const modal = document.getElementById("refundModal");
	const bookingIdInput = document.getElementById("refundBookingId");
	if (!modal || !bookingIdInput) {
	    return;
	}
	
	bookingIdInput.value = bookingId;
	modal.classList.add("Show");
}

function closeRefundModal() {
	const modal = document.getElementById("refundModal");
	if (!modal) {
	    return;
	}
	modal.classList.remove("Show");
}
	
document.addEventListener("DOMContentLoaded", function () {
const modal = document.getElementById("refundModal");
if (!modal) {
    return;
}

modal.addEventListener("click", function (event) {
    if (event.target === modal) {
        closeRefundModal();
    }
});

document.addEventListener("keydown", function (event) {
    if (event.key === "Escape"
            && modal.classList.contains("Show")) {
        closeRefundModal();
    }
});
});