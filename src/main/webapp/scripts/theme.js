/* Theme switch for the Settings pages (user, manager) and the admin profile.
   Saves the choice for this account in this browser and applies it right away.
   The first paint is handled by components/theme.jsp. */
(function () {
    var box = document.querySelector("[data-theme-user]");
    if (!box) {
        return;
    }
    var key = "phnetphlyx_theme_" + box.getAttribute("data-theme-user");
    var radios = box.querySelectorAll('input[name="theme"]');

    function apply(theme) {
        if (theme === "light") {
            document.documentElement.setAttribute("data-theme", "light");
        } else {
            document.documentElement.removeAttribute("data-theme");
        }
        try {
            localStorage.setItem(key, theme);
        } catch (e) {}
    }

    var saved = "dark";
    try {
        if (localStorage.getItem(key) === "light") {
            saved = "light";
        }
    } catch (e) {}

    for (var i = 0; i < radios.length; i++) {
        radios[i].checked = (radios[i].value === saved);
        radios[i].addEventListener("change", function () {
            if (this.checked) {
                apply(this.value);
            }
        });
    }
})();
