
import 'react-image-crop/dist/ReactCrop.css';
import React, {Fragment, useState} from "react";
import {Button, Labeled, TextInput, useNotify} from "react-admin";
import IconContentAdd from "@material-ui/icons/Add";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import {storage} from "../tools/firebase";
import { ref, getDownloadURL, uploadBytes } from "firebase/storage";
import { v4 as uuidv4 } from 'uuid';

const FileInputMy = ({ record = {}, showUploadButton, second }) => {
    const notify = useNotify();

    const bucketName = "emailDraft"

    const [mailBodyFileNameFirst, setMailBodyFileNameFirst] = useState(
        record == null ? '' : record.mailBodyFileNameFirst);

    const [mailBodyFileNameSecond, setMailBodyFileNameSecond] = useState(
        record == null ? '' : record.mailBodyFileNameSecond);

    const [show, setShow] = useState(false);
    const [file, setFile] = useState(null);
    const [downloadUrl, setDownloadUrl] = useState(null);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);
    const onClick = () => handleShow();

    if (second) {
        if (record != null && record.mailBodyFileNameSecond != null) {
            const storageRef = ref(storage, `/${bucketName}/${record.mailBodyFileNameSecond}`)
            getDownloadURL(storageRef).then((downloadURL) => {
                setDownloadUrl(downloadURL)
            });
        }
    } else {
        if (record != null && record.mailBodyFileNameFirst != null) {
            const storageRef = ref(storage, `/${bucketName}/${record.mailBodyFileNameFirst}`)
            getDownloadURL(storageRef).then((downloadURL) => {
                setDownloadUrl(downloadURL)
            });
        }
    }


    const seeDraft = () => {
        if (downloadUrl === null) {
            notify("There is not file to download.")
        } else {
            console.log(downloadUrl)
            window.open(downloadUrl, '_blank').focus()
        }
    }

    const handleFile = (e) => {
        const newFile = e.target.files[0];
        if (!newFile.type.startsWith("text/html")) {
            notify("Please load html file");
            return;
        }
        setFile(e.target.files[0])
    }

    const handleSaveClick = () => {
        if (!file) {
            notify("There is no file");
            console.log(file)
            return;
        }
        const fileName = `${uuidv4()}.html`;
        if (second) {
            setMailBodyFileNameSecond(fileName);
        } else {
            setMailBodyFileNameFirst(fileName);
        }

        const storageRef = ref(storage, `/${bucketName}/${fileName}`)

        const uploadTask = uploadBytes(storageRef, file)
        uploadTask
            .then(function(snapshot) {
                console.log('Uploaded file to storage!');
                        getDownloadURL(snapshot.ref).then((downloadURL) => {
                            setDownloadUrl(downloadURL);
                            handleClose()
                        });
            })
    }

    return (
        <Fragment>
            <Labeled label="File body first">
                { showUploadButton ?
                    (
                        second ?
                            <TextInput  disabled
                                        hidden={true}
                                        label="Mail file name"
                                        name="mailBodyFileNameSecond"
                                        defaultValue={mailBodyFileNameSecond}
                                        initialValue={mailBodyFileNameSecond}/> :
                            <TextInput  disabled
                                        hidden={true}
                                        label="Mail file name"
                                        name="mailBodyFileNameFirst"
                                        defaultValue={mailBodyFileNameFirst}
                                        initialValue={mailBodyFileNameFirst}/>
                    ) : null }
            </Labeled>
            { showUploadButton ? <br/> : null }
            { showUploadButton ?
                <Button label="Upload a new draft" onClick={onClick}>
                    <IconContentAdd />
                </Button>
                : null }
            <br/>
            <Button label="See a draft" onClick={seeDraft}/>

            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label="Upload email draft"
            >
                <DialogTitle>Upload email draft</DialogTitle>
                <DialogContent>
                    <div>
                        <input onChange={handleFile} type="file"/>
                    </div>
                 </DialogContent>
                 <DialogActions>
                     <Button label="ra.action.save" onClick={handleSaveClick}>
                         <IconContentAdd />
                     </Button>
                     <Button label="ra.action.cancel" onClick={handleClose}>
                         <IconCancel />
                     </Button>
                 </DialogActions>
             </Dialog>
         </Fragment>
    )
};

export default FileInputMy;
