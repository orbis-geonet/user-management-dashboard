import {Button, useNotify, TextInput, required, SimpleForm } from 'react-admin';
import React, {Fragment, useState} from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import axios from "axios";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import IconContentAdd from "@material-ui/icons/Add";
import DraftTypeInput from "../fields/enum/DraftTypeInput";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

const SendDraftEmailButton = ({props, record}) => {
    const classes = useStyles();
    const notify = useNotify();

    const [show, setShow] = useState(false);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const onClick = () => {
        handleShow()
    }

    const onSubmit = () => {
        const recipient = document.getElementById("recipient").value
        const emailNumber = document.getElementById("emailNumber").value

        if (recipient === '') {
            notify("Email cannot be empty.")
        } else {
            const { token } = JSON.parse(localStorage.getItem('auth'));

            axios.post(`${serviceUrl}/emailCampaigns/${record.id}/sendDraft`,
                JSON.stringify({'recipient': recipient, 'emailNumber': emailNumber}),
                {headers: {
                        'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
                .then((response) => {
                    notify("Draft was send.")
                    handleClose()
                })
                .catch((error) => {
                    console.log(error)
                    if (error.response) {
                        notify("Something is going wrong. Try again." + error.response.data.message)
                    } else {
                        notify("Something is going wrong. Try again." + error)
                    }
                })
        }
     }

    return (
        <Fragment>
            <Button {...props} className={classes.button} label="Send draft" onClick={onClick}/>
            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label={`Send draft`}
            >
                <DialogTitle>Import</DialogTitle>
                <DialogContent>
                    <SimpleForm onSubmit={onSubmit} toolbar={null}>
                        <TextInput source="recipient" id="recipient" validate={required()}/>
                        <DraftTypeInput validate={required()}/>
                    </SimpleForm>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onSubmit} label="Send">
                        <IconContentAdd />
                    </Button>
                    <Button label="ra.action.cancel" onClick={handleClose} >
                        <IconCancel />
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>
    )
}

export default SendDraftEmailButton;
