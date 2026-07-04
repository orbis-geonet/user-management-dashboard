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

const StopEmailsCampaignButton = ({props, record}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const onSubmit = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.delete(`${serviceUrl}/emailCampaigns/${record.id}/stop`,
            {headers: {
                'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("Email campaign was stopped.")
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
            <Button {...props} className={classes.button} label="Stop" onClick={onSubmit}/>
        </Fragment>
    )
}

export default StopEmailsCampaignButton;
