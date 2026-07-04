import {
    Button,
    useNotify,
    useRefresh,
    SimpleForm,
    DateInput,
    BooleanInput,
    fetchStart,
    fetchEnd,
    required, NumberInput
} from 'react-admin';
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
import IconContentAdd from '@material-ui/icons/Add';
import PostTypeInput from "../fields/enum/PostTypeInput";

import PeriodStatisticTypeInput from "../fields/enum/PeriodStatisticTypeInput";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        '& svg': { color: 'orange' }
    },
});

const CustomPeriodSettingButton = ({props, record, source}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    // const type = record[source]

    const [showSetting, setShowSetting] = useState(false);
    const [showGraph, setShowGraph] = useState(false);

    const handleCloseSetting = () => setShowSetting(false);
    const handleShowSetting = () => setShowSetting(true);

    const submit = () => {
        const from = document.getElementById("from").value
        const till = document.getElementById("till").value
        const periodType = document.getElementById("periodType") ? document.getElementById("periodType").value : null;
        const postType = document.getElementById("type") ? document.getElementById("type").value : null;
        const limit = document.getElementById("limit") ? document.getElementById("limit").value : null;

        const {token} = JSON.parse(localStorage.getItem('auth'));

        axios.put(`${serviceUrl}${props.basePath}/${record["id"]}/setting`,
            {"from":from, "till":till, "postType":postType, "periodType":periodType, "limit":limit},
            {headers: {
                'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("See result on custom setting page")
                handleCloseSetting()
                refresh()
            })
            .catch((error) => {
                if (error.response) {
                    notify("Something is going wrong. Try again." + error.response.data.message)
                } else {
                    notify("Something is going wrong. Try again." + error)
                }
                handleCloseSetting()
            })
    }

    const onClick = () => {
        handleShowSetting()
    }

    return (
        <Fragment>
            <Button {...props} className={classes.button} label="Set period" onClick={onClick}/>
            <Dialog
                fullWidth
                open={showSetting}
                onClose={handleCloseSetting}
                aria-label={`Activity`}
            >
                <DialogTitle>Activity</DialogTitle>
                <DialogContent>
                    <SimpleForm onSubmit={submit} toolbar={null}>
                        <DateInput source="from" id="from" validate={required()}/>
                        <DateInput source="till" id="till" validate={required()}/>
                        {record[source] !== 'MOST_ACTIVE_GROUP' && record[source] !== 'MOST_POPULAR_GROUP' && record[source] !== 'MOST_POPULAR_PLACE' ?
                            <PeriodStatisticTypeInput id="periodType"/>  : null}
                        {record[source] === 'POST_ACTIVITY' ?
                            <PostTypeInput id="type"/> : null}
                        {record[source] === 'MOST_ACTIVE_GROUP' || record[source] === 'MOST_POPULAR_GROUP' || record[source] === 'MOST_POPULAR_PLACE' ?
                            <NumberInput source="limit" id="limit"/>  : null}
                    </SimpleForm>
                </DialogContent>
                <DialogActions>
                    <Button onClick={submit} label="Set">
                        <IconContentAdd />
                    </Button>
                    <Button label="ra.action.cancel" onClick={handleCloseSetting} >
                        <IconCancel />
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>
        )
}

export default CustomPeriodSettingButton;
