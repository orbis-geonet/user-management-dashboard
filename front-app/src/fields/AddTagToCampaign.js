import {
    Button, CreateButton,
    Datagrid, FilterButton,
    FunctionField,
    List, SimpleForm, TextInput, TopToolbar,
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

const CampaignTagActions = ({props, goalPath}) => {
    return (
        <TopToolbar>
            <FilterButton/>
            <CreateTagButton goalPath={goalPath}/>
        </TopToolbar>
    )
};

const AddTagToCampaign = ({props, goalPath, goalId, goalName, source}) => {
    const classes = useStyles();
    const refresh = useRefresh();

    const [show, setShow] = useState(false);

    const handleClose = () => {
        setShow(false);
        refresh();
    }
    const handleShow = () => setShow(true);

    const onClick = () => {
        handleShow()
    }

    return (
        <Fragment>
            <Button className={classes.button} label="Add" onClick={onClick}/>
            <Dialog
                fullScreen
                open={show}
                onClose={handleClose}
            >
                <DialogTitle>Tags</DialogTitle>
                <DialogContent>
                    <List resource={goalPath} basePath={`/${goalPath}`}
                          sort={{ field: 'tagName', order: 'DESC' }} exporter={false}
                          filters={campaignTagsFilters}
                          bulkActionButtons={false}
                          actions={<CampaignTagActions goalPath={goalPath}/>}
                    >
                        <Datagrid>
                            <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                            <FunctionField source="tagName" render={record => record.tagName ? `${record.tagName}` : '-'}/>
                            <AddButton label="" goalId={goalId} source={source} goalName={goalName}/>
                            <FunctionField render={recordIn => <TagEdit tagId={recordIn.id} goalPath = {goalPath}/>}/>
                            <FunctionField render={recordIn => <TagDelete tagId={recordIn.id} goalPath={goalPath}/>}/>
                        </Datagrid>
                    </List>
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

const AddButton = ({record, source, goalName, goalId}) => {
    const notify = useNotify();

    const addOnClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(`${serviceUrl}/${goalName}/${goalId}/add-tag?tagId=${record[source]}`, {
            method: 'POST',
            headers: {'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json'}
        })
            .then(response => { notify(`Tag was added to ${goalName}`)})
            .catch(error => {
                if (error.response) {
                    notify("Something is going wrong. Try again." + error.response.data.message)
                } else {
                    notify("Something is going wrong. Try again." + error)
                }
            });
    }

    return (
        <Button label="Add" onClick={addOnClick}/>
    )
}

export const TagDelete = ({tagId, goalPath}) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const classes = useStyles();

    const onClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.delete(`${serviceUrl}/${goalPath}/${tagId}`,
            {headers: {
                    'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify(`A tag was deleted`);
                // refresh()
                // setShowEdit(false)
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

    return (
        <Fragment>
            <Button className={classes.button} label="Delete tag" onClick={onClick}/>
        </Fragment>
    );
}

export const TagEdit = ({tagId, goalPath}) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();
    const classes = useStyles();

    const onSubmit = () => {
        const tagName = document.getElementById("tagName").value

        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.put(`${serviceUrl}/${goalPath}/${tagId}`,
            JSON.stringify({'id':tagId, 'tagName': tagName}),
            {headers: {
                    'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify(`A tag was edited`);
                refresh()
                // setShowEdit(false)
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
                <DialogTitle>Edit</DialogTitle>
                <DialogContent>
                    <SimpleForm onSubmit={onSubmit} toolbar={null}>
                        <TextInput source="id" disabled={true}/>
                        <TextInput source="tagName"/>
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

export const CreateTagButton = ({goalPath}) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();
    const classes = useStyles();

    const onSubmit = () => {
        const tagName = document.getElementById("tagName").value

        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.post(`${serviceUrl}/${goalPath}`,
            JSON.stringify({'tagName': tagName}),
            {headers: {
                    'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify(`Tag was created`);
                refresh()
                // setShowEdit(false)
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
            <Button className={classes.button} label="Create" onClick={onClick}/>
            <Dialog
                open={showCreate}
                onClose={handleClose}
            >
                <DialogTitle>Import</DialogTitle>
                <DialogContent>
                    <SimpleForm onSubmit={onSubmit} toolbar={null}>
                        <TextInput source="tagName"/>
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

export default AddTagToCampaign;