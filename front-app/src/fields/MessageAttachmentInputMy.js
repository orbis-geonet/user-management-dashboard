
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

const MessageAttachmentInputMy = ({ record = {}, showUploadButton }) => {
    const notify = useNotify();

    const bucketName = "messageAttachment"

    const [messageAttachmentFileName, setMessageAttachmentFileName] = useState(
        record == null || record.messageAttachmentFileName == null ? '' : record.messageAttachmentFileName);
    const [messageAttachmentType, setMessageAttachmentType] = useState(
        record == null || record.messageAttachmentFileName == null ? '' : record.messageAttachmentType);

    const [show, setShow] = useState(false);
    const [messageAttachmentFile, setMessageAttachmentFile] = useState(null);
    const [messageAttachmentDownloadUrl, setMessageAttachmentDownloadUrl] = useState(null);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);
    const onClick = () => handleShow();

    if (record != null && record.messageAttachmentFileName != null) {
        const storageRef = ref(storage, `/${bucketName}/${record.messageAttachmentFileName}`)
        getDownloadURL(storageRef).then((downloadURL) => {
            setMessageAttachmentDownloadUrl(downloadURL)
        });
    }

    const seeAttachment = () => {
        if (messageAttachmentDownloadUrl != null) {
            console.log(messageAttachmentDownloadUrl)
            window.open(messageAttachmentDownloadUrl, '_blank').focus()
        } else {
            notify("There is not messageAttachmentFile to download.")
        }
    }

    const deleteAttachment = () => {
        setMessageAttachmentFileName('')
        setMessageAttachmentType('')
    }

    const handleFile = (e) => {
        setMessageAttachmentFile(e.target.files[0]);
    }

    const handleSaveClick = () => {
        if (!messageAttachmentFile) {
            notify("There is no messageAttachmentFile");
            console.log(messageAttachmentFile)
            return;
        }
        console.log(messageAttachmentFile.type);
        const fileName = `${uuidv4()}`;

        setMessageAttachmentFileName(fileName);
        setMessageAttachmentType(messageAttachmentFile.type);

        const storageRef = ref(storage, `/${bucketName}/${fileName}`)

        const uploadTask = uploadBytes(storageRef, messageAttachmentFile)
        uploadTask
            .then(function(snapshot) {
                console.log('Uploaded messageAttachmentFile to storage! message');
                        getDownloadURL(snapshot.ref).then((downloadURL) => {
                            setMessageAttachmentDownloadUrl(downloadURL);
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
                                            label="Attachment file name"
                                            name="messageAttachmentFileName"
                                            defaultValue={messageAttachmentFileName}
                                            initialValue={messageAttachmentFileName}
                                />
                                <TextInput  disabled
                                            hidden={true}
                                            label="Attachment type"
                                            name="messageAttachmentType"
                                            defaultValue={messageAttachmentType}
                                            initialValue={messageAttachmentType}
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

export default MessageAttachmentInputMy;
