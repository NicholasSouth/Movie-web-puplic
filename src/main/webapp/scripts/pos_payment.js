(function () {
    var paymentForm = document.getElementById("POSPaymentForm");
    var paymentOptions = document.querySelectorAll('input[name="payment_method"]');
    var visaSection = document.getElementById("VisaCardSection");
    var qrSection = document.getElementById("QRPaymentSection");
    var visaCards = document.querySelectorAll('input[name="payment_method_id"]');
    var payButton = document.getElementById("PayButton");

    function updatePaymentMethod() {
        var selectedMethod = document.querySelector('input[name="payment_method"]:checked');

        if (!selectedMethod) {
            visaSection.style.display = "none";
            if (qrSection) {
                qrSection.style.display = "none";
            }
            payButton.disabled = true;
            return;
        }

        if (selectedMethod.value === "VISA") {
            visaSection.style.display = "block";
            if (qrSection) {
                qrSection.style.display = "none";
            }

            var selectedVisa = document.querySelector('input[name="payment_method_id"]:checked');

            if (selectedVisa) {
                payButton.disabled = false;
            } else {
                payButton.disabled = true;
            }
        } 
		else if (selectedMethod.value === "QR") {
            visaSection.style.display = "none";
            if (qrSection) {
                qrSection.style.display = "block";
            }
            visaCards.forEach(function (card) {
                card.checked = false;
            });
            payButton.disabled = false;
        } 
		else if (selectedMethod.value === "CASH") {
            visaSection.style.display = "none";
            if (qrSection) {
                qrSection.style.display = "none";
            }
            visaCards.forEach(function (card) {
                card.checked = false;
            });
            payButton.disabled = false;
        }
    }
    paymentOptions.forEach(function (option) {
        option.addEventListener("change", updatePaymentMethod);
    });
    visaCards.forEach(function (card) {
        card.addEventListener("change", updatePaymentMethod);
    });
    if (paymentForm) {
        paymentForm.addEventListener("submit", function () {
            payButton.disabled = true;
            payButton.classList.add("PayButtonLoading");
            payButton.innerHTML = "<span>Processing payment...</span>";
        });
    }
    updatePaymentMethod();
})();