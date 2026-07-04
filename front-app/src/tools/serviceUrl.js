import { fetchUtils } from 'react-admin';


export const httpClientAuth = (url, options = {}) => {
    if (!options.headers) {
        options.headers = new Headers({ Accept: 'application/json' });
    }
    const auth = localStorage.getItem('auth');
    if (auth) {
        const { token } = JSON.parse(auth);
        options.headers.set('Authorization', `Bearer ${token}`);
    }

    // Special handling for FormData (file uploads)
    if (options.body && options.body instanceof FormData) {
        // Don't set Content-Type for FormData - browser will set it with proper boundary
        options.headers.delete('Content-Type');
        
        // Use native fetch for FormData instead of fetchUtils.fetchJson
        return fetch(url, options)
            .then(response => {
                if (response.status < 200 || response.status >= 300) {
                    return response.text().then(text => {
                        throw new Error(text || response.statusText);
                    });
                }
                return response.json().then(json => ({ json, headers: response.headers, status: response.status }));
            });
    } else {
        // For JSON requests, use the default behavior
        if (!options.headers.has('Content-Type') && !(options.body instanceof FormData)) {
            options.headers.set('Content-Type', 'application/json');
        }
        return fetchUtils.fetchJson(url, options);
    }
};

export const serviceUrl = process.env.REACT_APP_BACK_URL


export const googleKeyMy = process.env.REACT_APP_GOOGLE_KEY

export const googlePictureUrl = process.env.REACT_APP_GOOGLE_IMG_URL

export const fireBaseStorageBucket = process.env.REACT_APP_FIRE_BASE_STORAGE_BUCKET

export const fireBaseProjectId = process.env.REACT_APP_FIRE_BASE_PROJECT_ID

export const fireBaseAuthDomain = process.env.REACT_APP_FIRE_BASE_AUTH_DOMAIN

export const fireBaseDatabaseURL = process.env.REACT_APP_FIRE_BASE_DATA_BASE_URL

export const fireBaseMessagingSenderId = process.env.REACT_APP_FIRE_BASE_MESSAGING_SENDER_ID

export const fireBaseAppId = process.env.REACT_APP_FIRE_BASE_APP_IP

export const fireBaseMeasurementId = process.env.REACT_APP_FIRE_BASE_MEASUREMENT_ID
