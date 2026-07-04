import {
    BooleanInput,
    Button, CreateButton,
    Datagrid, FilterButton,
    FunctionField,
    List, required, SimpleForm, TextInput, TopToolbar,
    useNotify, useRedirect,
    useRefresh
} from 'react-admin';
import * as React from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import {Fragment, useState} from "react";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import {campaignTagsFilters} from "../sections/emailCampaignTag";
import axios from "axios";
import IconContentAdd from "@material-ui/icons/Add";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});


export const CloneEmailCampaign = ({emailCampaignId}) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const classes = useStyles();

    const [copyOpened, setCopyOpened] = useState(false);
    const [copyNotOpened, setCopyNotOpened] = useState(false);

    const onSubmit = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.post(`${serviceUrl}/emailCampaigns/copy`,
            JSON.stringify({'id': emailCampaignId, 'copyOpened': copyOpened, 'copyNotOpened': copyNotOpened}),
            {headers: {
                    'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify(`Email campaign was cloned`);
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
    };

    const [showCreate, setShowCreate] = useState(false);

    const handleClose = () => {
        setShowCreate(false);
        refresh();
    }
    const handleShow = () => setShowCreate(true);

    const onClick = () => {
        handleShow()
    }

    return (
        <Fragment>
            <Button className={classes.button} label="Clone" onClick={onClick}/>
            <Dialog
                open={showCreate}
                onClose={handleClose}
            >
                <DialogTitle>Import</DialogTitle>
                <DialogContent>
                    <SimpleForm onSubmit={onSubmit} toolbar={null}>
                        <TextInput source="id" defaultValue={emailCampaignId} disabled={true}/>
                        <BooleanInput source="copyOpened" defaultValue={false} validate={required()} onChange={v => {setCopyOpened(v)}}/>
                        <BooleanInput source="copyNotOpened" defaultValue={false} validate={required()} onChange={v => {setCopyNotOpened(v)}}/>
                    </SimpleForm>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onSubmit} label="Clone">
                        <IconContentAdd />
                    </Button>
                    <Button label="ra.action.cancel" onClick={handleClose} >
                        <IconCancel />
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>

    );
}

export default CloneEmailCampaign;