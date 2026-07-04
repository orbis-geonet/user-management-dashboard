import * as React from 'react';
import { useState } from 'react';
import { useDataProvider, useNotify } from 'react-admin';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Stepper, Step, StepLabel, StepContent } from '@material-ui/core';
import CloudUploadIcon from '@material-ui/icons/CloudUpload';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { serviceUrl } from '../tools/serviceUrl';

const useStyles = makeStyles((theme) => ({
    button: {
        marginLeft: '5px',
    },
    input: {
        display: 'none',
    },
    label: {
        display: 'flex',
        alignItems: 'center',
        cursor: 'pointer',
        marginTop: '20px',
    },
    fileInfo: {
        marginTop: '10px',
        marginBottom: '20px',
    },
    instructions: {
        marginBottom: '20px',
    },
    resultBox: {
        padding: '8px',
        marginTop: '16px',
        backgroundColor: '#f5f5f5',
        borderRadius: '4px',
    },
    actionsContainer: {
        marginBottom: theme.spacing(2),
    },
    stepButtons: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(1),
    },
    stepContent: {
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
        padding: theme.spacing(2),
        border: '1px solid #eaeaea',
        borderRadius: '4px',
    }
}));

const BulkUploadEmailCampaignsButton = () => {
    const classes = useStyles();
    const dataProvider = useDataProvider();
    const notify = useNotify();
    
    const [open, setOpen] = useState(false);
    const [activeStep, setActiveStep] = useState(0);
    const [emailsFile, setEmailsFile] = useState(null);
    const [campaignsFile, setCampaignsFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [emailsResult, setEmailsResult] = useState(null);
    const [campaignsResult, setCampaignsResult] = useState(null);
    const [legacyMode, setLegacyMode] = useState(false);
    const [legacyFile, setLegacyFile] = useState(null);
    const [legacyResult, setLegacyResult] = useState(null);

    const handleOpen = () => {
        setOpen(true);
        setActiveStep(0);
        setEmailsFile(null);
        setEmailsResult(null);
        setCampaignsFile(null);
        setCampaignsResult(null);
        setLegacyMode(false);
        setLegacyFile(null);
        setLegacyResult(null);
        setUploading(false);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const handleEmailsFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            setEmailsFile(e.target.files[0]);
            setEmailsResult(null);
        }
    };

    const handleCampaignsFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            setCampaignsFile(e.target.files[0]);
            setCampaignsResult(null);
        }
    };

    const handleLegacyFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            setLegacyFile(e.target.files[0]);
            setLegacyResult(null);
        }
    };

    const handleNext = () => {
        setActiveStep(prevActiveStep => prevActiveStep + 1);
    };

    const handleBack = () => {
        setActiveStep(prevActiveStep => prevActiveStep - 1);
    };

    const handleSwitchToLegacy = () => {
        setLegacyMode(true);
    };

    const handleSwitchToTwoStep = () => {
        setLegacyMode(false);
    };

    const uploadEmails = async () => {
        if (!emailsFile) {
            notify('Please select a file first', 'warning');
            return;
        }

        // Check if file is CSV
        if (!emailsFile.name.toLowerCase().endsWith('.csv')) {
            notify('Only CSV files are supported', 'warning');
            return;
        }

        setUploading(true);
        setEmailsResult(null);

        const formData = new FormData();
        formData.append('file', emailsFile);
        formData.append('step', '1'); // Specify step 1 for emails upload

        try {
            // Get auth token
            const auth = localStorage.getItem('auth');
            const { token } = JSON.parse(auth);
            
            // Use fetch for step 1 - upload emails with tags
            const response = await fetch(`${serviceUrl}/emailCampaigns/bulk-upload`, {
                method: 'POST',
                headers: new Headers({
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }),
                body: formData,
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }
            
            const data = await response.json();
            setEmailsResult(data);
            notify('Email upload completed successfully', 'success');
            
            // Automatically proceed to the next step
            handleNext();
        } catch (error) {
            console.error('Error uploading emails:', error);
            notify(`Email upload failed: ${error.message || 'Unknown error'}`, 'error');
        } finally {
            setUploading(false);
        }
    };

    const uploadCampaigns = async () => {
        if (!campaignsFile) {
            notify('Please select a file first', 'warning');
            return;
        }

        // Check if file is CSV
        if (!campaignsFile.name.toLowerCase().endsWith('.csv')) {
            notify('Only CSV files are supported', 'warning');
            return;
        }

        setUploading(true);
        setCampaignsResult(null);

        const formData = new FormData();
        formData.append('file', campaignsFile);
        formData.append('step', '2'); // Specify step 2 for campaigns upload

        try {
            // Get auth token
            const auth = localStorage.getItem('auth');
            const { token } = JSON.parse(auth);
            
            // Use fetch for step 2 - upload campaigns with tag references
            const response = await fetch(`${serviceUrl}/emailCampaigns/bulk-upload`, {
                method: 'POST',
                headers: new Headers({
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }),
                body: formData,
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }
            
            const data = await response.json();
            setCampaignsResult(data);
            notify('Campaign upload completed successfully', 'success');
        } catch (error) {
            console.error('Error uploading campaigns:', error);
            notify(`Campaign upload failed: ${error.message || 'Unknown error'}`, 'error');
        } finally {
            setUploading(false);
        }
    };

    const legacyUpload = async () => {
        if (!legacyFile) {
            notify('Please select a file first', 'warning');
            return;
        }

        // Check if file is CSV
        if (!legacyFile.name.toLowerCase().endsWith('.csv')) {
            notify('Only CSV files are supported', 'warning');
            return;
        }

        setUploading(true);
        setLegacyResult(null);

        const formData = new FormData();
        formData.append('file', legacyFile);

        try {
            // Get auth token
            const auth = localStorage.getItem('auth');
            const { token } = JSON.parse(auth);
            
            // Use fetch for legacy one-step upload
            const response = await fetch(`${serviceUrl}/emailCampaigns/bulk-upload`, {
                method: 'POST',
                headers: new Headers({
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }),
                body: formData,
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }
            
            const data = await response.json();
            setLegacyResult(data);
            notify('Bulk upload completed successfully', 'success');
        } catch (error) {
            console.error('Error uploading file:', error);
            notify(`Upload failed: ${error.message || 'Unknown error'}`, 'error');
        } finally {
            setUploading(false);
        }
    };

    return (
        <>
            <Button
                onClick={handleOpen}
                className={classes.button}
                color="primary"
                startIcon={<CloudUploadIcon />}
            >
                Bulk Upload
            </Button>
            
            <Dialog
                open={open}
                onClose={handleClose}
                aria-labelledby="bulk-upload-dialog-title"
                maxWidth="md"
                fullWidth
            >
                <DialogTitle id="bulk-upload-dialog-title">
                    Bulk Upload Email Campaigns
                    {!legacyMode ? 
                        <Button 
                            color="secondary" 
                            style={{ float: 'right' }} 
                            size="small" 
                            onClick={handleSwitchToLegacy}
                        >
                            Switch to Legacy Mode
                        </Button> 
                        : 
                        <Button 
                            color="primary" 
                            style={{ float: 'right' }} 
                            size="small" 
                            onClick={handleSwitchToTwoStep}
                        >
                            Switch to Two-Step Mode
                        </Button>
                    }
                </DialogTitle>
                
                <DialogContent>
                    {!legacyMode ? (
                        // Two-step upload process
                        <Stepper activeStep={activeStep} orientation="vertical">
                            <Step>
                                <StepLabel>Upload Emails with Tags</StepLabel>
                                <StepContent>
                                    <div className={classes.stepContent}>
                                        <Typography variant="subtitle1">
                                            Step 1: Upload a CSV file with emails and tags.
                                        </Typography>
                                        <Typography variant="body2">
                                            The CSV must contain these required headers:
                                        </Typography>
                                        <ul>
                                            <li><strong>name</strong> - Person's name</li>
                                            <li><strong>email</strong> - Email address</li>
                                            <li><strong>companyName</strong> - Company name</li>
                                            <li><strong>tags</strong> - Comma-separated list of tags (will be created if they don't exist)</li>
                                        </ul>
                                        <Typography variant="body2">
                                            Optional headers include:
                                        </Typography>
                                        <ul>
                                            <li><strong>phoneNumber</strong> - Phone number</li>
                                            <li><strong>webSite</strong> - Website</li>
                                            <li><strong>comment</strong> - Additional comments</li>
                                        </ul>
                                        
                                        <input
                                            accept=".csv"
                                            className={classes.input}
                                            id="upload-emails-csv-file"
                                            type="file"
                                            onChange={handleEmailsFileChange}
                                        />
                                        <label htmlFor="upload-emails-csv-file" className={classes.label}>
                                            <Button
                                                variant="contained"
                                                color="primary"
                                                component="span"
                                            >
                                                Select Emails CSV File
                                            </Button>
                                            <span style={{ marginLeft: '10px' }}>
                                                {emailsFile ? emailsFile.name : 'No file selected'}
                                            </span>
                                        </label>

                                        {emailsResult && (
                                            <div className={classes.resultBox}>
                                                <Typography variant="subtitle1">Upload Results:</Typography>
                                                <Typography variant="body2">Emails created: {emailsResult.totalEmails}</Typography>
                                                <Typography variant="body2">Tags created: {emailsResult.totalTags}</Typography>
                                                {emailsResult.createdTags && emailsResult.createdTags.length > 0 && (
                                                    <Typography variant="body2">
                                                        New tags: {emailsResult.createdTags.join(', ')}
                                                    </Typography>
                                                )}
                                            </div>
                                        )}
                                        
                                        <div className={classes.stepButtons}>
                                            <Button
                                                onClick={uploadEmails}
                                                variant="contained"
                                                color="primary"
                                                disabled={!emailsFile || uploading}
                                            >
                                                {uploading ? 'Uploading...' : 'Upload Emails & Continue'}
                                            </Button>
                                        </div>
                                    </div>
                                </StepContent>
                            </Step>
                            
                            <Step>
                                <StepLabel>Upload Campaigns with Tag References</StepLabel>
                                <StepContent>
                                    <div className={classes.stepContent}>
                                        <Typography variant="subtitle1">
                                            Step 2: Upload a CSV file with campaigns that reference the tags from Step 1.
                                        </Typography>
                                        <Typography variant="body2">
                                            The CSV must contain these required headers:
                                        </Typography>
                                        <ul>
                                            <li><strong>campaignName</strong> - Name of the campaign</li>
                                            <li><strong>subject</strong> - Email subject line</li>
                                            <li><strong>content</strong> - Email body content (used if no template specified)</li>
                                            <li><strong>emailTag</strong> - Tag name to target (must match a tag from Step 1)</li>
                                            <li><strong>scheduleDate</strong> - Start time (format: yyyy-MM-dd HH:mm:ss) - REQUIRED</li>
                                        </ul>
                                        <Typography variant="body2">
                                            Optional headers include:
                                        </Typography>
                                        <ul>
                                            <li><strong>remindDate</strong> - Remind time (format: yyyy-MM-dd HH:mm:ss)</li>
                                            <li><strong>mailSubjectSecond</strong> - Subject for follow-up email</li>
                                            <li><strong>mailBodySecond</strong> - Content for follow-up email</li>
                                            <li><strong>sendSecondEmail</strong> - Enable follow-up (true/false)</li>
                                            <li><strong>sendOpenEmailIn</strong> - Days to wait before follow-up (number)</li>
                                            <li><strong>useAllEmails</strong> - Use all emails in database (true/false)</li>
                                            <li><strong>useOpen</strong> - Target users who opened email (true/false)</li>
                                            <li><strong>useNotOpen</strong> - Target users who didn't open email (true/false)</li>
                                            <li><strong>autoSend</strong> - Automatically create email list and start the campaign (true/false)</li>
                                        </ul>
                                        
                                        <input
                                            accept=".csv"
                                            className={classes.input}
                                            id="upload-campaigns-csv-file"
                                            type="file"
                                            onChange={handleCampaignsFileChange}
                                        />
                                        <label htmlFor="upload-campaigns-csv-file" className={classes.label}>
                                            <Button
                                                variant="contained"
                                                color="primary"
                                                component="span"
                                            >
                                                Select Campaigns CSV File
                                            </Button>
                                            <span style={{ marginLeft: '10px' }}>
                                                {campaignsFile ? campaignsFile.name : 'No file selected'}
                                            </span>
                                        </label>

                                        {campaignsResult && (
                                            <div className={classes.resultBox}>
                                                <Typography variant="subtitle1">Upload Results:</Typography>
                                                <Typography variant="body2">Campaigns created: {campaignsResult.totalCampaigns}</Typography>
                                            </div>
                                        )}
                                        
                                        <div className={classes.stepButtons}>
                                            <Button
                                                onClick={handleBack}
                                                style={{ marginRight: '8px' }}
                                            >
                                                Back
                                            </Button>
                                            <Button
                                                onClick={uploadCampaigns}
                                                variant="contained"
                                                color="primary"
                                                disabled={!campaignsFile || uploading}
                                            >
                                                {uploading ? 'Uploading...' : 'Upload Campaigns'}
                                            </Button>
                                        </div>
                                    </div>
                                </StepContent>
                            </Step>
                        </Stepper>
                    ) : (
                        // Legacy one-step process
                        <div>
                            <div className={classes.instructions}>
                                <Typography variant="subtitle1">
                                    Legacy Mode: Upload a CSV file with both email campaigns and recipients.
                                </Typography>
                                <Typography variant="body2">
                                    The CSV must contain these required headers:
                                </Typography>
                                <ul>
                                    <li><strong>campaignName</strong> - Name of the campaign</li>
                                    <li><strong>subject</strong> - Email subject line</li>
                                    <li><strong>content</strong> - Email body content (used if no template specified)</li>
                                    <li><strong>name</strong> - Recipient's name</li>
                                    <li><strong>email</strong> - Recipient's email address</li>
                                    <li><strong>companyName</strong> - Recipient's company name</li>
                                    <li><strong>scheduleDate</strong> - Start time (format: yyyy-MM-dd HH:mm:ss) - REQUIRED</li>
                                </ul>
                                <Typography variant="body2">
                                    Optional headers include:
                                </Typography>
                                <ul>
                                    <li><strong>remindDate</strong> - Remind time (format: yyyy-MM-dd HH:mm:ss)</li>
                                    <li><strong>tags</strong> - Comma-separated list of tag names</li>
                                    <li><strong>sendSecondEmail</strong> - Enable follow-up (true/false)</li>
                                    <li><strong>mailSubjectSecond</strong> - Subject for follow-up email</li>
                                    <li><strong>mailBodySecond</strong> - Content for follow-up email</li>
                                    <li><strong>autoSend</strong> - Automatically create email list and start the campaign (true/false)</li>
                                </ul>
                            </div>
                            
                            <input
                                accept=".csv"
                                className={classes.input}
                                id="upload-legacy-csv-file"
                                type="file"
                                onChange={handleLegacyFileChange}
                            />
                            <label htmlFor="upload-legacy-csv-file" className={classes.label}>
                                <Button
                                    variant="contained"
                                    color="primary"
                                    component="span"
                                >
                                    Select CSV File
                                </Button>
                                <span style={{ marginLeft: '10px' }}>
                                    {legacyFile ? legacyFile.name : 'No file selected'}
                                </span>
                            </label>

                            {legacyResult && (
                                <div className={classes.resultBox}>
                                    <Typography variant="subtitle1">Upload Results:</Typography>
                                    <Typography variant="body2">Campaigns created: {legacyResult.totalCampaigns}</Typography>
                                    <Typography variant="body2">Total recipients: {legacyResult.totalRecipients}</Typography>
                                </div>
                            )}
                            
                            <div style={{ marginTop: '20px' }}>
                                <Button
                                    onClick={legacyUpload}
                                    color="primary"
                                    variant="contained"
                                    disabled={!legacyFile || uploading}
                                >
                                    {uploading ? 'Uploading...' : 'Upload'}
                                </Button>
                            </div>
                        </div>
                    )}
                </DialogContent>
                
                <DialogActions>
                    <Button onClick={handleClose} color="primary">
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default BulkUploadEmailCampaignsButton; 