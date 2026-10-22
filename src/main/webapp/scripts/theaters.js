const btn = document.getElementById('find-near-btn');
const status = document.getElementById('find-near-status');
if (btn) {
    btn.addEventListener('click', () => {
        if (!navigator.geolocation) {
            alert('The browser does not support geolocation.');
            return;
        }
        status.style.display = 'inline';
        btn.disabled = true;
        navigator.geolocation.getCurrentPosition(
            pos => {
                const lat = pos.coords.latitude;
                const lng = pos.coords.longitude;

                /*Get the current search keyword, if there is one.*/
                const params = new URLSearchParams(window.location.search);
                const currentSearch = params.get('search');
                const contextPath = document.body.dataset.contextPath;

                /*Build the URL parameters.*/
                const urlParams = new URLSearchParams();
                urlParams.set('userLat', lat);
                urlParams.set('userLng', lng);

                /*Preserve the search keyword.*/
                if (
                    currentSearch
                    && currentSearch.trim() !== ''
                ) {
                    urlParams.set(
                        'search',
                        currentSearch
                    );
                }

                /*Go back to the theaters servlet.*/
                window.location.href =
                    contextPath
                    + '/theaters?'
                    + urlParams.toString();
            },
            error => {
                status.style.display = 'none';
                btn.disabled = false;
                alert('Unable to retrieve your location. Please allow location access.');
            }
        );
    });
}