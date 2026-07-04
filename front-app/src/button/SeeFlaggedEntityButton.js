import {
    BooleanField,
    Button,
    Datagrid,
    DateField,
    TextField,
    UrlField,
    Labeled,
    useNotify,
    useRefresh, ArrayField
} from 'react-admin';
import { Fragment, useState } from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import * as React from "react";
import UrlFieldMy from "../fields/UrlField";
import ImageField from "../fields/ImageField";
import DeleteFlaggedEntityButton from "./DeleteFlaggedEntityButton";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        // This is JSS syntax to target a deeper element using css selector, here the svg icon for this button
        '& svg': { color: 'orange' }
    },
});

const SeeFlaggedEntityButton = ({record}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const [show, setShow] = useState(false);

    const [entity, setEntity] = useState({
        items: [],
        loaded: false
    })

    const [imageBucket, setImageBucket] = useState("")

    const handleClose = () => {
        setShow(false);
        refresh();
    }

    const onClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(`${serviceUrl}/flagged/${record.type}/${record.id}`, {method: 'GET', headers:{'Authorization': `Bearer ${token}`}})
            .then(response => response.json())
            .then(json => {
                setEntity({
                    items: json,
                    loaded: true
                })
                if (json.type === "GROUP") {
                    setImageBucket("groupPictures")
                }
                if (json.type === "USER") {
                    setImageBucket("profilePictures")
                }
                if (json.type === "PLACE") {
                    setImageBucket("placePictures")
                }
                setShow(true);
            })
            .catch(console.error);
    }

    return (
        <Fragment>
            <Button className={classes.button} label="More details" onClick={onClick}/>
            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
            >
                <DialogTitle>Details</DialogTitle>
                <DialogContent>
                    <div>
                        <Labeled label="Id">
                            <TextField record={entity.items} source="id"/>
                        </Labeled>
                    </div>
                    <div>
                        <Labeled label="Type">
                            <TextField record={entity.items} source="type"/>
                        </Labeled>
                    </div>
                    <div>
                        <Labeled label="Reported Message">
                            <TextField record={entity.items} source="reportedMessage"/>
                        </Labeled>
                    </div>
                    <div>
                        <Labeled label="Reported Solved">
                            <BooleanField record={entity.items} source="reportedSolved"/>
                        </Labeled>
                    </div>
                    <div>
                        <Labeled label="Reported Time">
                            <DateField record={entity.items} showTime source="reportedTime"/>
                        </Labeled>
                    </div>
                    { entity.items.type === 'USER' ?
                        <div>
                            <Labeled label="E-mail">
                                <TextField record={entity.items} source="email"/>
                            </Labeled>
                        </div> : null
                    }
                    { entity.items.type !== 'USER' ?
                        <div>
                            <Labeled label="Name">
                                <TextField record={entity.items} source="name"/>
                            </Labeled>
                        </div> : null
                    }
                    { entity.items.type === 'USER' ?
                        <div>
                            <Labeled label="Display name">
                                <TextField record={entity.items} source="displayName"/>
                            </Labeled>
                        </div> : null
                    }
                    {entity.items.type !== 'USER' ?
                        <div>
                            <Labeled label="Details">
                                <TextField record={entity.items} source="details"/>
                            </Labeled>
                        </div> : null
                    }
                    { entity.items.type !== 'POST' ?
                        <div>
                            <Labeled label="Image">
                                <ImageField record={entity.items} imageBucket={imageBucket}/>
                            </Labeled>
                        </div> : null
                    }
                    { entity.items.type !== 'POST' ?
                        <div>
                            <Labeled label="Share Link">
                                <UrlField record={entity.items} source="shareLink"/>
                            </Labeled>
                        </div> : null
                    }
                    <div>
                        <Labeled label="Deleted">
                            <BooleanField record={entity.items} source="deleted"/>
                        </Labeled>
                    </div>
                    <div>
                        <Labeled label="Last updated time">
                            <DateField record={entity.items} showTime source="timestamp"/>
                        </Labeled>
                    </div>
                    <div>
                        <Labeled label="Created time">
                            <DateField record={entity.items} showTime source="createTimestamp"/>
                        </Labeled>
                    </div>
                    { entity.items.type === 'POST' ?
                        <div>
                            <Labeled label="Post type">
                                <TextField record={entity.items} source="postType"/>
                            </Labeled>
                        </div> : null
                    }
                    { entity.items.type === 'PLACE' ?
                        <div>
                            <Labeled label="Place type">
                                <TextField record={entity.items} source="placeType"/>
                            </Labeled>
                        </div> : null
                    }
                    { entity.items.type === 'POST' ?
                        <div>
                            <Labeled label="Media">
                                <ArrayField record={entity.items} source="mediaUrls">
                                    <Datagrid>
                                        <UrlFieldMy source="url"/>
                                    </Datagrid>
                                </ArrayField>
                            </Labeled>
                        </div> : null
                    }

                </DialogContent>
                <DialogActions>
                    <Button label="ra.action.cancel" onClick={handleClose} >
                        <IconCancel />
                    </Button>
                    <DeleteFlaggedEntityButton record={entity.items}/>
                </DialogActions>
            </Dialog>
        </Fragment>
        )

}

export default SeeFlaggedEntityButton;
