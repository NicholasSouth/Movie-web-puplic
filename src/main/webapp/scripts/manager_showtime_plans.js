document.addEventListener("DOMContentLoaded", function () {
    const dialog = document.getElementById("deleteDialog");
    const openBtn = document.getElementById("btnOpenDelete");
    if (!dialog || !openBtn) {
        return;
    }
    const form = document.getElementById("deleteForm");
    const deletable = Number(dialog.dataset.deletable);
    const checks = Array.from(form.querySelectorAll(".pick-check:not(:disabled)"));
    const countEl = document.getElementById("pickedCount");
    const note = document.getElementById("deleteNote");

    function refresh() {
        const n = checks.filter(c => c.checked).length;
        countEl.textContent = n;
        // the status badge of each row follows its checkbox
        checks.forEach(c => {
            const badge = c.closest("tr").querySelector(".showtimes-status");
            badge.textContent = c.checked ? "WILL BE DELETED" : "KEPT";
            badge.classList.toggle("status-scheduled", c.checked);
            badge.classList.toggle("status-completed", !c.checked);
        });
        if (deletable === 0) {
            note.textContent = "No showtime can be deleted. Only the plan record will be deleted.";
            note.classList.remove("is-error");
        } else if (n === 0) {
            note.textContent = "No showtime is selected: only the plan is deleted and all its showtimes stay.";
            note.classList.remove("is-error");
        } else if (n === deletable) {
            note.textContent = "The plan and every deletable showtime will be deleted.";
            note.classList.add("is-error");
        } else {
            note.textContent = n + " showtime(s) will be deleted, " + (deletable - n) + " will be kept.";
            note.classList.remove("is-error");
        }
    }

    openBtn.addEventListener("click", () => {
        checks.forEach(c => { c.checked = true; });   // always start from "delete everything deletable"
        refresh();
        dialog.showModal();
    });
    document.getElementById("btnCancelDelete").addEventListener("click", () => dialog.close());
    dialog.addEventListener("click", e => { if (e.target === dialog) dialog.close(); });   // click on the backdrop
    checks.forEach(c => c.addEventListener("change", refresh));
    document.getElementById("btnPickAll").addEventListener("click", () => { checks.forEach(c => c.checked = true); refresh(); });
    document.getElementById("btnPickNone").addEventListener("click", () => { checks.forEach(c => c.checked = false); refresh(); });

    form.addEventListener("submit", function (e) {
        const n = checks.filter(c => c.checked).length;
        if (!confirm("Delete this plan and " + n + " showtime(s)? This cannot be undone.")) {
            e.preventDefault();
        }
    });
});
