
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

const RelySecondAttachmentInputMy = ({ record = {}, showUploadButton }) => {
    const notify = useNotify();

    const bucketName = "messageAttachment"

    const [responseSecondAttachmentFileName, setResponseSecondAttachmentFileName] = useState(
        record == null || record.responseSecondAttachmentFileName == null ? '' : record.responseSecondAttachmentFileName);
    const [responseSecondAttachmentType, setResponseSecondAttachmentType] = useState(
        record == null || record.responseSecondAttachmentFileName == null ? '' : record.responseSecondAttachmentType);

    const [show, setShow] = useState(false);
    const [responseSecondFile, setResponseSecondFile] = useState(null);
    const [responseSecondDownloadUrl, setResponseSecondDownloadUrl] = useState(null);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);
    const onClick = () => handleShow();

    if (record != null && record.responseSecondAttachmentFileName != null) {
        const storageRef = ref(storage, `/${bucketName}/${record.responseSecondAttachmentFileName}`)
        getDownloadURL(storageRef).then((downloadURL) => {
            setResponseSecondDownloadUrl(downloadURL)
        });
    }

    const seeAttachment = () => {
        if (responseSecondDownloadUrl != null) {
            console.log(responseSecondDownloadUrl)
            window.open(responseSecondDownloadUrl, '_blank').focus()
        } else {
            notify("There is not responseSecondFile to download.")
        }
    }

    const deleteAttachment = () => {
        setResponseSecondAttachmentFileName('')
        setResponseSecondAttachmentType('')
    }

    const handleFile = (e) => {
        setResponseSecondFile(e.target.files[0]);
    }

    const handleSaveClick = () => {
        if (!responseSecondFile) {
            notify("There is no responseSecondFile");
            console.log(responseSecondFile)
            return;
        }
        console.log(responseSecondFile.type);
        const fileName = `${uuidv4()}`;

        setResponseSecondAttachmentFileName(fileName);
        setResponseSecondAttachmentType(responseSecondFile.type);

        const storageRef = ref(storage, `/${bucketName}/${fileName}`)

        const uploadTask = uploadBytes(storageRef, responseSecondFile)
        uploadTask
            .then(function(snapshot) {
                console.log('Uploaded responseSecondFile to storage! second');
                        getDownloadURL(snapshot.ref).then((downloadURL) => {
                            setResponseSecondDownloadUrl(downloadURL);
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
                                    label="Attachment file name 2"
                                    name="responseSecondAttachmentFileName"
                                    defaultValue={responseSecondAttachmentFileName}
                                    initialValue={responseSecondAttachmentFileName}
                        />
                        <TextInput  disabled
                                    hidden={true}
                                    label="Attachment type 2"
                                    name="responseSecondAttachmentType"
                                    defaultValue={responseSecondAttachmentType}
                                    initialValue={responseSecondAttachmentType}
                        />
                    </div>
                    : null }
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

export default RelySecondAttachmentInputMy;
