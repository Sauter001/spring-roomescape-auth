(() => {
    const link = document.getElementById('logout-link');
    if (!link) return;

    link.addEventListener('click', (event) => {
        event.preventDefault();
        localStorage.removeItem('accessToken');
        window.location.href = '/';
    });
})();
