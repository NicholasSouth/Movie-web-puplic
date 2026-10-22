(function () {
    var seatsEl = document.querySelector('.seats');
    var confirmBtn = document.querySelector('.confirm-btn');
    var seatNameEl = document.querySelector('.summary-seat');
    var totalEl = document.querySelector('.summary-total');
    var bookingForm = document.querySelector('#bookingForm');
    var promotionCodeEl = document.querySelector('#promotionCode');
    var applyPromotionBtn = document.querySelector('#applyPromotionBtn');
    var promotionMessage = document.querySelector('#promotionMessage');
    function formatPrice(value) {
        return value.toLocaleString('vi-VN') + 'đ';
    }

    function getNormalTotal() {
        var selectedSeats = seatsEl.querySelectorAll( '.seat.selected');
        var total = 0;
        selectedSeats.forEach(function (seat) {
            total += parseInt(seat.dataset.price, 10) || 0;
        });
        return total;
    }

    function updateSummary() {
        var selectedSeats = seatsEl.querySelectorAll('.seat.selected');
        var seatNames = [];
        var total = 0;
        selectedSeats.forEach(function (seat) {
            seatNames.push(seat.dataset.name);
            total += parseInt(seat.dataset.price, 10) || 0;
        });
        if (selectedSeats.length > 0) {
            seatNameEl.textContent = seatNames.join(', ');
            totalEl.textContent = 'Total: ' + formatPrice(total);
            confirmBtn.disabled = false;
        }
        else {
            seatNameEl.textContent = '-';
            totalEl.textContent = 'Total: 0đ';
            confirmBtn.disabled = true;
        }
    }

    /* Seat selection */
    seatsEl.addEventListener('click', function (e) {
            var seat = e.target.closest('.seat');
            if (!seat
                || seat.classList.contains('booked')
                || seat.classList.contains('unavailable')) {
                return;
            }
            seat.classList.toggle('selected');
            updateSummary();
        }
    );

    /* Apply promotion */
    applyPromotionBtn.addEventListener('click', function () {
            console.log( 'Apply button clicked.');
            var promotionCode = promotionCodeEl.value.trim();
            var selectedSeats = seatsEl.querySelectorAll('.seat.selected');

            /* Check promotion code */
            if (promotionCode === '') {
                promotionMessage.textContent = 'Please enter a promotion code.';
                return;
            }

            /* Check seats */
            if (selectedSeats.length === 0) {
                promotionMessage.textContent = 'Please select at least one seat.';
                return;
            }

            /* Build request data */
            var showtimeInput = bookingForm.querySelector('input[name="showtime_id"]');
            var params = new URLSearchParams();
            params.append('promotion_code', promotionCode);
            params.append('showtime_id', showtimeInput.value);
            selectedSeats.forEach(
                function (seat) {
                    params.append('seat_id', seat.dataset.seatId);
                }
            );

            /* Send request to servlet */
            applyPromotionBtn.disabled = true;
            applyPromotionBtn.textContent = 'Applying...';
            var formAction = bookingForm.action;
            var contextPath = formAction.substring(0, formAction.indexOf('/manager-create-booking'));
            fetch(
                contextPath + '/validate-promotion',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    },
                    body: params.toString()
                }
            )
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.success) {
                    totalEl.textContent = 'Total: ' + formatPrice(parseInt(data.total, 10));
                    promotionMessage.textContent = 'Promotion applied successfully.';
                }
                else {
                    totalEl.textContent = 'Total: ' + formatPrice(getNormalTotal());
                    promotionMessage.textContent = data.message;
                }
            })
            .catch(function (error) {
                totalEl.textContent = 'Total: ' + formatPrice(getNormalTotal());
                promotionMessage.textContent = 'Could not validate the promotion code.';
            })
            .finally(function () {
                applyPromotionBtn.disabled = false;
                applyPromotionBtn.textContent = 'Apply';
            });
        }
    );

    /* Booking submission */
    bookingForm.addEventListener('submit', function (e) {
            var selectedSeats = seatsEl.querySelectorAll('.seat.selected');
            if (selectedSeats.length === 0) {
                e.preventDefault();
                return;
            }
            bookingForm.querySelectorAll('input[name="seat_id"]').forEach(function (input) {
                    input.remove();
                });
            selectedSeats.forEach(
                function (seat) {
                    var input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'seat_id';
                    input.value = seat.dataset.seatId;
                    bookingForm.appendChild(input);
                }
            );
            confirmBtn.disabled = true;
            confirmBtn.textContent = 'Processing...';
        }
    );
    updateSummary();
})();