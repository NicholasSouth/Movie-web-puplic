function confirmDeleteAccount()
{
    const firstConfirmation = confirm("Are you sure you want to delete your PhnetPhlyx account?"
    );
    if (!firstConfirmation)
    {
        return;
    }
    const secondConfirmation = confirm(
        "Your account will be deactivated and marked for deletion. " +
        "After 30 days, its data may be overwritten. " +
        "Do you want to continue?"
    );
    if (!secondConfirmation)
    {
        return;
    }
    const form = document.createElement("form");
    form.method = "POST";
    form.action = "delete-account";
    document.body.appendChild(form);
    form.submit();
}