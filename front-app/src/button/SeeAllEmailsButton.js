import {
    BooleanInput,
    Button,
    Datagrid, DateField,
    FunctionField,
    List, TextField, TextInput,
    useNotify, useRedirect,
    useRefresh, SimpleForm, BooleanField
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
import ResendEmailButton from "./ResendEmailButton";
import IconContentAdd from "@material-ui/icons/Add";
import axios from "axios";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

export const filters = [
    <TextInput label="id" source="id"/>,
    <TextInput label="Name" source="name"/>,
    <TextInput label="Company name" source="companyName"/>,
    <TextInput label="Email" source="mail"/>,
    <BooleanInput label="Mail was open" source="open"/>
];

const SeeAllEmailsButton = ({campaignId}) => {
    const classes = useStyles();
    const refresh = useRefresh();
    const notify = useNotify();
    const redirect = useRedirect();

    const [show, setShow] = useState(false);

    const handleClose = () => {
        setShow(false);
        refresh();
    }
    const handleShow = () => setShow(true);

    const onClick = () => {
        handleShow()
    }

    const onClickDelete = (emailId) => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(`${serviceUrl}/emailCampaigns/emailCampaignInfo/${emailId}`,
            {method: 'DELETE', headers: {'Authorization': `Bearer ${token}`}})
            .then(response => {
                notify(`Email was deleted from EmailCampaign`)
                setShow(false)

                setShow(true)
            })
            .catch(console.error);
    }

    console.log(campaignId)

    return (
        <Fragment>
            <Button className={classes.button} label="See list of emails" onClick={onClick}/>
            <Dialog
                fullScreen
                open={show}
                onClose={handleClose}
            >
                <DialogTitle>Entity List</DialogTitle>
                <DialogContent>
                    <List resource="emailCampaigns/emailCampaignInfo" basePath="/emailCampaigns/emailCampaignInfo"
                          sort={{field: 'timestamp', order: 'DESC'}}
                          hasCreate={false} exporter={false}
                          filterDefaultValues={{campaignId: campaignId}}
                          filters={filters}
                          bulkActionButtons={false}
                    >
                        <Datagrid>
                            <TextField source="emailKey"/>
                            <TextField source="mail"/>
                            <TextField source="name"/>
                            <TextField source="companyName"/>
                            <TextField source="phoneNumber"/>
                            <TextField source="status"/>
                            <DateField showTime={true} source="openTime"/>
                            <DateField showTime={true} source="sendOpenEmailTime"/>
                            <FunctionField render={recordIn => <ResendEmailButton campaignId={campaignId} recipientId={recordIn.id}/> }/>
                            {/*<EditButton label="" resource="emailCampaigns/emailCampaignInfo" basePath="/emailCampaigns/emailCampaignInfo"/>*/}
                            <FunctionField render={recordIn => <EmailInfoEdit emailId={recordIn.id}/>}/>
                            <FunctionField render={recordIn => <Button className={classes.button} label="Delete" onClick={() => onClickDelete(recordIn.id)}/>}/>
                        </Datagrid>
                    </List>
                </DialogContent>
                <DialogActions>
                    <Button label="ra.action.cancel" onClick={handleClose}>
                        <IconCancel/>
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>
    )
}

const DelEmailInfoCampaign = ({emailId}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const onClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(`${serviceUrl}/emailCampaigns/emailCampaignInfo/${emailId}`,
            {method: 'DELETE', headers: {'Authorization': `Bearer ${token}`}})
            .then(response => {
                notify(`Email was deleted from EmailCampaign`)
                refresh()
            })
            .catch(console.error);
    }

    return <Button className={classes.button} label="Delete" onClick={onClick}/>
}

export const EmailInfoEdit = ({emailId}) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();
    const classes = useStyles();

    const onSubmit = () => {
        const name = document.getElementById("name").value
        const companyName = document.getElementById("companyName").value
        const mail = document.getElementById("mail").value
        const phoneNumber = document.getElementById("phoneNumber").value
        const emailKey = document.getElementById("emailKey").value

        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.put(`${serviceUrl}/emailCampaigns/emailCampaignInfo/${emailId}`,
            JSON.stringify({'id':emailId, 'emailKey': emailKey, 'name': name, 'companyName': companyName, 'mail': mail, 'phoneNumber': phoneNumber}),
            {headers: {
                    'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify(`A email was edited`);
                setShowEdit(false)
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

    const [showEdit, setShowEdit] = useState(false);

    const handleClose = () => {
        setShowEdit(false);
        refresh();
    }
    const handleShow = () => setShowEdit(true);

    const onClick = () => {
        handleShow()
    }

    return (
        <Fragment>
            <Button className={classes.button} label="Edit" onClick={onClick}/>
            <Dialog
                open={showEdit}
                onClose={handleClose}
            >
                <DialogTitle>Import</DialogTitle>
                <DialogContent>
                    <SimpleForm onSubmit={onSubmit} toolbar={null}>
                        <TextInput source="emailKey" disabled={true}/>
                        <TextInput source="name"/>
                        <TextInput source="companyName"/>
                        <TextInput source="mail"/>
                        <TextInput source="phoneNumber"/>
                    </SimpleForm>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onSubmit} label="Save">
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

export default SeeAllEmailsButton;
