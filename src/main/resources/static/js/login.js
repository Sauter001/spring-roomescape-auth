(() => {
    const form = document.getElementById('login-form');
    if (!form) return;

    // 토큰 만료 등으로 튕겨온 경우 안내 메시지를 보여준다.
    const authMessage = sessionStorage.getItem('authMessage');
    if (authMessage) {
        sessionStorage.removeItem('authMessage');
        showToast(authMessage, 'error');
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const uid = form.elements['uid'].value.trim();
        const password = form.elements['password'].value;

        if (!uid || !password) {
            showToast('아이디와 비밀번호를 모두 입력하세요.', 'error');
            return;
        }

        try {
            await apiFetch('/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({uid, password})
            });
            // 브라우저 인증은 서버가 내려준 쿠키로 유지된다.
            window.location.href = '/home';
        } catch (error) {
            toastError(error);
        }
    });
})();
