const inMemoryJWTManager = () => {
    let inMemoryJWT = null;

    window.addEventListener('storage', (event) => {
        if (event.key === 'ra-logout') {
            inMemoryJWT = null;
        }
    });

    const getToken = () => inMemoryJWT;

    const setToken = (token) => {
        inMemoryJWT = token;
        return true;
    };

    const ereaseToken = () => {
        inMemoryJWT = null;
        window.localStorage.setItem('ra-logout', Date.now());
        return true;
    }

    return {
        ereaseToken,
        getToken,
        setToken,
    }
};

export default inMemoryJWTManager();
