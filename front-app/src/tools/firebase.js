import { initializeApp } from "firebase/app";
import { getStorage } from "firebase/storage";
import {
    googleKeyMy,
    fireBaseStorageBucket,
    fireBaseProjectId,
    fireBaseAuthDomain,
    fireBaseDatabaseURL,
    fireBaseMessagingSenderId,
    fireBaseAppId,
    fireBaseMeasurementId
} from "./serviceUrl";

const firebaseConfig = {
    apiKey: googleKeyMy,
    authDomain: fireBaseAuthDomain,
    databaseURL: fireBaseDatabaseURL,
    projectId: fireBaseProjectId,
    storageBucket: fireBaseStorageBucket,
    messagingSenderId: fireBaseMessagingSenderId,
    appId: fireBaseAppId,
    measurementId: fireBaseMeasurementId
};

const app = initializeApp(firebaseConfig);

const storage = getStorage(app);

export { storage, app };
