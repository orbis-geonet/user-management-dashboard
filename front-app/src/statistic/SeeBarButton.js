import {
    Button,
    useNotify,
    useRefresh,
} from 'react-admin';
import * as React from "react";

import { makeStyles } from '@material-ui/core/styles';
import {Fragment, useRef, useState} from "react";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";

import {Bar} from 'react-chartjs-2';
import { Chart as ChartJS } from 'chart.js/auto'

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        '& svg': { color: 'orange' }
    },
});

const SeeBarButton = ({record, source}) => {
    const classes = useStyles();

    const [showGraph, setShowGraph] = useState(false);

    const handleCloseGraph = () => setShowGraph(false);
    const handleShowGraph = () => setShowGraph(true);

    const [charDate, setCharDate] = useState(
        {
            labels: "Draft",
            datasets: [{
                label: ["draft"],
                data: [10],
                backgroundColor: 'rgba(75,192,192,1)',
                borderColor: 'rgba(0,0,0,1)',
                borderWidth: 2,
            }]
        }
    )

    const onClick = () => {
        const inputData = record[source]
        console.log(inputData)
        setCharDate({
            labels: inputData.name,
            datasets: [{
                label: "Statistic",
                data: inputData.data,
                backgroundColor: inputData.colour,
                borderColor: 'rgba(0,0,0,1)',
                borderWidth: 2,
            }]
        })
        console.log(charDate)
        handleShowGraph()
    }

    return (
        <Fragment>
            <Button className={classes.button} label="See bar graph" onClick={onClick}/>
            <Dialog
                fullWidth
                open={showGraph}
                onClose={handleCloseGraph}
                aria-label={`Graph`}
            >
                <DialogTitle>Graph</DialogTitle>
                <DialogContent>
                    <Fragment>
                        <div>
                            <Bar
                                data={charDate}
                                options={{
                                    title:{
                                        display:true,
                                        text:'Average Rainfall per month',
                                        fontSize:20
                                    },
                                    legend:{
                                        display:true,
                                        position:'right'
                                    }
                                }}
                             type={"bar"}/>
                        </div>
                    </Fragment>
                </DialogContent>
                <DialogActions>
                    <Button label="ra.action.cancel" onClick={handleCloseGraph} >
                        <IconCancel />
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>
        )
}

export default SeeBarButton;
