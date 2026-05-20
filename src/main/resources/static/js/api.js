(() => {
    // 응답이 실패면 서버의 공통 에러 본문(code/message)을 담은 Error를 throw 한다.
    async function apiFetch(url, options = {}) {
        // 브라우저는 same-origin 인증 쿠키가 자동 전송되므로 별도 헤더 부착이 필요 없다.
        const res = await fetch('/api' + url, options);
        if (res.ok) return res;

        let body = null;
        try {
            body = await res.json();
        } catch (e) {
            body = null;
        }

        const code = body && body.code;
        // 토큰 만료(INVALID_TOKEN)·누락(LOGIN_REQUIRED) → 로그인 페이지로.
        // 자격증명 오류(LOGIN_FAILED)는 제외하여 로그인 화면에서 토스트만 뜨게 한다.
        if (res.status === 401 && (code === 'INVALID_TOKEN' || code === 'LOGIN_REQUIRED')) {
            sessionStorage.setItem('authMessage', code === 'INVALID_TOKEN'
                ? '세션이 만료되었습니다. 다시 로그인해주세요.'
                : '로그인이 필요합니다.');
            window.location.href = '/';
            return new Promise(() => {}); // 페이지 전환 동안 호출부 정지(에러 토스트 방지)
        }

        const error = new Error((body && body.message) || '요청 처리에 실패했습니다.');
        error.code = code;
        error.status = res.status;
        throw error;
    }

    window.apiFetch = apiFetch;
})();
