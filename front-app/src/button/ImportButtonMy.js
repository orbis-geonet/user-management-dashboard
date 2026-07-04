import {Button, useNotify, useRefresh} from 'react-admin';
import * as React from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import {Fragment, useRef, useState} from "react";
import axios from 'axios';
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        '& svg': { color: 'orange' }
    },
});

const ImportButtonMy = ({props, importUrl, fileType}) => {
    const classes = useStyles();
    const notify = useNotify();
    const fileRef = useRef();
    const refresh = useRefresh();

    const [show, setShow] = useState(false);
    const [showTip, setShowTip] = useState(true);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const handleChange = (e) => {
        const [file] = e.target.files;

        setShowTip(false);

        const formData = new FormData();
        formData.append('file', file, file.name);

        // console.log(serviceUrl + props.basePath + importUrl)

        const {token} = JSON.parse(localStorage.getItem('auth'));
        // axios.post(serviceUrl + props.basePath + '/import',
        axios.post(`${serviceUrl}${props.basePath}${importUrl}?fileType=${fileType}`,
            formData,
            {headers: {
                'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("Import is ready.")
                setShowTip(true);
                handleClose()
                refresh()
            })
            .catch((error) => {
                if (error.response) {
                    notify("Something is going wrong. Try again." + error.response.data.message)
                } else {
                    notify("Something is going wrong. Try again." + error)
                }
                setShowTip(true);
                handleClose()
            })
    }

    const onClick = () => {
        handleShow()
    }

    return (
        <Fragment>
            <Button {...props} className={classes.button} label={`Import ${fileType}`} onClick={onClick}/>
            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label={`Import`}
            >
                <DialogTitle>Import</DialogTitle>
                <DialogContent>
                    <div>After choose file please wait until file loads.</div>
                    <div hidden={showTip}>Import is going.... please wait....</div>
                    <input {...props} ref={fileRef} onChange={handleChange} multiple={false} type="file"/>
                </DialogContent>
                <DialogActions>
                    <Button label="ra.action.cancel" onClick={handleClose} >
                        <IconCancel />
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>
        )
}

export default ImportButtonMy;
