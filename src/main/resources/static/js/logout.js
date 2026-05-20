(() => {
    const link = document.getElementById('logout-link');
    if (!link) return;

    link.addEventListener('click', async (event) => {
        event.preventDefault();
        try {
            await fetch('/api/logout', {method: 'POST'}); // 서버가 인증 쿠키를 삭제한다.
        } catch (e) {
            // 네트워크 오류여도 로그인 페이지로 이동시킨다.
        }
        window.location.href = '/';
    });
})();
