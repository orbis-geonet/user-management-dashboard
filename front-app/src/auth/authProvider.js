import {serviceUrl} from "../tools/serviceUrl";
const url = serviceUrl + '/auth/login'

const authProvider = {
    login: ({ username, password }) =>  {
        const request = new Request(url, {
            method: 'POST',
            body: JSON.stringify({ username, password }),
            headers: new Headers({ 'Content-Type': 'application/json' }),
        });
        return fetch(request)
            .then(response => {
                if (response.status < 200 || response.status >= 300) {
                    throw new Error(response.statusText);
                }
                return response.json();
            })
            .then(auth => {
                localStorage.setItem('auth', JSON.stringify(auth));
            })
            .catch(() => {
                throw new Error('Wrong user/password')
            });
    },

    logout: () => {
        localStorage.removeItem('auth')
        return Promise.resolve();
    },

    checkAuth: () => {
        return localStorage.getItem('auth') ? Promise.resolve() : Promise.reject();
    },

    checkError: (error) => {
        const status = error.status;
        if (status === 401 || status === 403) {
            localStorage.removeItem('auth');
            return Promise.reject({ 'Wrong': false });
        }
        // other error code (404, 500, etc): no need to log out
        return Promise.resolve();
    },

    getPermissions: () => {
        return localStorage.getItem('auth') ? Promise.resolve() : Promise.reject();
    },

    getIdentity: () => {
        try {
            const { fullName } = JSON.parse(localStorage.getItem('auth'));
            return Promise.resolve({ fullName });
        } catch (error) {
            return Promise.reject(error);
        }
    }
};

export default authProvider;
