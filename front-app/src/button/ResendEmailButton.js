import {Button, useNotify, TextInput, required, SimpleForm } from 'react-admin';
import React, {Fragment, useState} from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import axios from "axios";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

const ResendEmailButton = ({props, campaignId, recipientId}) => {
    const classes = useStyles();
    const notify = useNotify();

    const onSubmit = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        axios.post(`${serviceUrl}/emailCampaigns/${campaignId}/resend?recipientId=${recipientId}`,
            null,
            {headers: {
                'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("Mail was resend.")
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
            <Button {...props} className={classes.button} label="resend" onClick={onSubmit}/>
        </Fragment>
    )
}

export default ResendEmailButton;
