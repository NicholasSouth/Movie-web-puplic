document.addEventListener("DOMContentLoaded", function () {
    const fmt = n => Number(n).toLocaleString("en-US");

    // ---- Budget hint under the input (works on the form even without a preview) ----
    const budgetInput = document.getElementById("budget");
    const budgetHint = document.getElementById("budgetHint");
    if (budgetInput && budgetHint) {
        const updateHint = () => {
            const digits = budgetInput.value.replace(/[^0-9]/g, "");
            budgetHint.textContent = digits ? fmt(digits) + " VND" : "";
        };
        budgetInput.addEventListener("input", updateHint);
        budgetInput.addEventListener("blur", () => {
            const digits = budgetInput.value.replace(/[^0-9]/g, "");
            if (digits) {
                budgetInput.value = fmt(digits);
            }
            updateHint();
        });
        updateHint();
    }

    // ---- Preview: live count / cost / profit ----
    const preview = document.getElementById("plannerPreview");
    if (!preview) {
        return;
    }
    const budget = Number(preview.dataset.budget);
    const checks = Array.from(preview.querySelectorAll(".slot-check"));
    const byPriority = checks.slice().sort((a, b) => Number(a.dataset.order) - Number(b.dataset.order));
    const countInput = document.getElementById("countInput");
    const countRange = document.getElementById("countRange");
    const statCount = document.getElementById("statCount");
    const statCost = document.getElementById("statCost");
    const statProfit = document.getElementById("statProfit");
    const profitBox = document.getElementById("statProfitBox");
    const confirmBtn = document.getElementById("btnConfirm");
    const confirmNote = document.getElementById("confirmNote");

    function refresh() {
        let count = 0;
        let cost = 0;
        checks.forEach(c => {
            if (c.checked) {
                count++;
                cost += Number(c.dataset.cost);
            }
        });
        const leftover = budget - cost;
        statCount.textContent = fmt(count);
        statCost.textContent = fmt(cost);
        statProfit.textContent = fmt(leftover);
        countInput.value = count;
        countRange.value = count;

        const over = leftover < 0;
        profitBox.classList.toggle("is-negative", over);
        confirmBtn.disabled = over || count === 0;
        if (over) {
            confirmNote.textContent = "Over budget by " + fmt(-leftover) + " VND. Remove some showtimes.";
        } else if (count === 0) {
            confirmNote.textContent = "Select at least one showtime.";
        } else {
            confirmNote.textContent = "You will keep " + fmt(leftover) + " VND as profit.";
        }
        confirmNote.classList.toggle("is-error", over || count === 0);

        preview.querySelectorAll(".planner-day").forEach(day => {
            const n = day.querySelectorAll(".slot-check:checked").length;
            day.querySelector(".day-selected").textContent = n;
        });
    }

    // Keep the first N slots of the priority list, drop the rest.
    function selectFirst(n) {
        n = Math.max(0, Math.min(byPriority.length, Math.floor(Number(n) || 0)));
        byPriority.forEach((c, i) => { c.checked = i < n; });
        refresh();
    }

    checks.forEach(c => c.addEventListener("change", refresh));
    countInput.addEventListener("input", () => selectFirst(countInput.value));
    countRange.addEventListener("input", () => selectFirst(countRange.value));
    document.getElementById("btnAuto").addEventListener("click", function () {
        selectFirst(this.dataset.auto);
    });
    document.getElementById("btnClear").addEventListener("click", () => selectFirst(0));

    const expandBtn = document.getElementById("btnExpand");
    let expanded = false;
    expandBtn.addEventListener("click", () => {
        expanded = !expanded;
        preview.querySelectorAll(".planner-day").forEach(d => { d.open = expanded; });
        expandBtn.textContent = expanded ? "Collapse all days" : "Expand all days";
    });

    document.getElementById("confirmForm").addEventListener("submit", function (e) {
        if (confirmBtn.disabled) {
            e.preventDefault();
            return;
        }
        const count = checks.filter(c => c.checked).length;
        if (!confirm("Create " + count + " showtime(s)? This cannot be undone in one step.")) {
            e.preventDefault();
        }
    });

    refresh();
});
