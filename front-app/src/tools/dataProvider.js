import simpleRestProvider from 'ra-data-simple-rest';
import { fetchUtils } from 'react-admin';
import { serviceUrl, httpClientAuth } from './serviceUrl';

/**
 * Custom data provider that extends the simple rest provider with FormData support
 */
const baseDataProvider = simpleRestProvider(serviceUrl, httpClientAuth);

const dataProvider = {
    ...baseDataProvider,
    
    /**
     * Send form data (files) to the API
     * @param {string} type The request type
     * @param {string} resource The resource name
     * @param {FormData} formData The form data to send
     * @returns {Promise} The response
     */
    sendFormData: (type, resource, formData) => {
        // Map method based on type
        let method = 'POST';
        if (type === 'PUT') method = 'PUT';
        if (type === 'DELETE') method = 'DELETE';
        
        const url = `${serviceUrl}/${resource}`;
        
        return httpClientAuth(url, {
            method,
            body: formData,
        });
    }
};

export default dataProvider; 