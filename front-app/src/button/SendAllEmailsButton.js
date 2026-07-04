import {Button, useNotify, Labeled, TextField, useRefresh} from 'react-admin';
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

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
    div: {
        width: '100%'
    },
});

const SendAllEmailsButton = ({props, record}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const [show, setShow] = useState(false);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const onClick = () => {
        handleShow()
    }

    const buttonName = record.status === 'VERIFICATION_EMAIL_LIST' ? 'Start' : 'Create email list';
    const header = record.status === 'VERIFICATION_EMAIL_LIST' ? 'Start email campaign' : 'Creat email list';

    const line1 = record.status === 'VERIFICATION_EMAIL_LIST' ? 'Have you checked all emails?' : 'All duplicates will be deleted.';
    const line2 = record.status === 'VERIFICATION_EMAIL_LIST' ? 'You will not be able to change emails after stop' : 'If an user has a wrong email, the user will be deleted from campaign.';
    const line3 = record.status === 'VERIFICATION_EMAIL_LIST' ? '' : 'Warning: if you reuse campaign, all statistic will be deleted';

    const onSubmit = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.post(`${serviceUrl}/emailCampaigns/${record.id}/sendAllEmails`,
            null,
            {headers: {
                'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("Email campaign was started.")
                handleClose()
                refresh()
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

    return (
        <Fragment>
            <Button {...props} className={classes.button} label={buttonName} onClick={onClick}/>
            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label={`Send all emails`}
            >
                <DialogTitle>{header}</DialogTitle>
                <DialogContent>
                    <div className={classes.div}>
                        <p><b>{line1}</b></p>
                        <p><b>{line2}</b></p>
                        <p><b>{line3}</b></p>
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onSubmit} label="Submit">
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

export default SendAllEmailsButton;
