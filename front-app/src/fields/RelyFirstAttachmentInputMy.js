
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

const RelyFirstAttachmentInputMy = ({ record = {}, showUploadButton }) => {
    const notify = useNotify();

    const bucketName = "messageAttachment"

    const [responseFirstAttachmentFileName, setResponseFirstAttachmentFileName] = useState(
        record == null || record.responseFirstAttachmentFileName == null ? '' : record.responseFirstAttachmentFileName);
    const [responseFirstAttachmentType, setResponseFirstAttachmentType] = useState(
        record == null || record.responseFirstAttachmentFileName == null ? '' : record.responseFirstAttachmentType);

    const [show, setShow] = useState(false);
    const [responseFirstFile, setResponseFirstFile] = useState(null);
    const [responseFirstDownloadUrl, setResponseFirstDownloadUrl] = useState(null);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);
    const onClick = () => handleShow();

    if (record != null && record.responseFirstAttachmentFileName != null) {
        const storageRef = ref(storage, `/${bucketName}/${record.responseFirstAttachmentFileName}`)
        getDownloadURL(storageRef).then((downloadURL) => {
            setResponseFirstDownloadUrl(downloadURL)
        });
    }

    const seeAttachment = () => {
        if (responseFirstDownloadUrl != null) {
            console.log(responseFirstDownloadUrl)
            window.open(responseFirstDownloadUrl, '_blank').focus()
        } else {
            notify("There is not responseFirstFile to download.")
        }
    }

    const deleteAttachment = () => {
        setResponseFirstAttachmentFileName('')
        setResponseFirstAttachmentType('')
    }

    const handleFile = (e) => {
        setResponseFirstFile(e.target.files[0]);
    }

    const handleSaveClick = () => {
        if (!responseFirstFile) {
            notify("There is no responseFirstFile");
            console.log(responseFirstFile)
            return;
        }
        console.log(responseFirstFile.type);
        const fileName = `${uuidv4()}`;

        setResponseFirstAttachmentFileName(fileName);
        setResponseFirstAttachmentType(responseFirstFile.type);

        const storageRef = ref(storage, `/${bucketName}/${fileName}`)

        const uploadTask = uploadBytes(storageRef, responseFirstFile)
        uploadTask
            .then(function(snapshot) {
                console.log('Uploaded responseFirstFile to storage!');
                        getDownloadURL(snapshot.ref).then((downloadURL) => {
                            setResponseFirstDownloadUrl(downloadURL);
                            handleClose()
                        });
            })
    }

    return (
        <Fragment>
            <Labeled label="Message attachment">
                { showUploadButton ?
                    <div>
                        <TextInput  disabled
                                    hidden={true}
                                    label="Attachment file name 1"
                                    name="responseFirstAttachmentFileName"
                                    defaultValue={responseFirstAttachmentFileName}
                                    initialValue={responseFirstAttachmentFileName}
                        />
                        <TextInput  disabled
                                    hidden={true}
                                    label="Attachment type 1"
                                    name="responseFirstAttachmentType"
                                    defaultValue={responseFirstAttachmentType}
                                    initialValue={responseFirstAttachmentType}
                        />
                    </div> : null }
            </Labeled>
            { showUploadButton ? <br/> : null }
            { showUploadButton ?
                <Button label="Upload a new message attachment" onClick={onClick}>
                    <IconContentAdd />
                </Button>
                : null }
            <br/>
            <Button label="See a message attachment" onClick={seeAttachment}/>
            <br/>
            { showUploadButton ? <Button label="Delete a message attachment" onClick={deleteAttachment}/> : null }

            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label="Upload message attachment"
            >
                <DialogTitle>Upload message attachment</DialogTitle>
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

export default RelyFirstAttachmentInputMy;
