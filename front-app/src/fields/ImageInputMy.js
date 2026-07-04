import ReactCrop from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import React, {Fragment, useRef, useState} from "react";
import {googlePictureUrl, serviceUrl} from "../tools/serviceUrl";
import {Button, TextInput} from "react-admin";
import IconContentAdd from "@material-ui/icons/Add";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import BaseImg from "../image/defaulImg.png"
import {storage} from "../tools/firebase";
import { ref, getDownloadURL, uploadString } from "firebase/storage";
import { v4 as uuidv4 } from 'uuid';
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles({
    img: {
        width: 140,
        borderRadius: 1000
    },
});

const ImageInputMy = ({ record = {}, source , props, imageBucket}) => {
    const classes = useStyles();

    const imgRef = useRef(null);

    const [imageSource, setImageSource] = useState(null);
    const [showImg, setShowImg] = useState(false)

    if (record != null && record.imageName != null) {
        const storageRef = ref(storage, `/${imageBucket}/${record.imageName}`)
        getDownloadURL(storageRef).then((downloadURL) => {
            setImageSource(downloadURL)
            setShowImg(true)
        });
    }

    const [imageName, setImageName] = useState(record == null ? '' : record.imageName);

    const [show, setShow] = useState(false);
    const [src, setSrc] = useState(null);

    const [img, setImg] = useState(null);
    const [result, setResult] = useState(imageSource);

    const [crop, setCrop] = useState({
        aspect: 1/1,
        width: 200,
        height: 200,
    });

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const onClick = () => {
        handleShow()
    }

    const handleFile = (e) => {
        const newFile = e.target.files[0];
        if (!newFile.type.startsWith("image/")) return;
        setSrc(URL.createObjectURL(newFile))
    }

    const handleSaveClick = () => {
        const canvas = document.createElement("canvas");
        const scaleX = img.naturalWidth / img.width;
        const scaleY = img.naturalHeight / img.height;
        canvas.width = crop.width;
        canvas.height = crop.height;
        const ctx = canvas.getContext("2d");

        // New lines to be added
        const pixelRatio = window.devicePixelRatio;
        canvas.width = crop.width * pixelRatio;
        canvas.height = crop.height * pixelRatio;
        ctx.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
        ctx.imageSmoothingQuality = "high";

        ctx.drawImage(
            img,
            crop.x * scaleX,
            crop.y * scaleY,
            crop.width * scaleX,
            crop.height * scaleY,
            0,
            0,
            crop.width,
            crop.height
        );
        const base64Image = canvas.toDataURL("image/jpeg");
        // setResult(base64Image)
        setImageSource(base64Image)
        setShowImg(true)
        imgRef.current.src = base64Image
        const imageNameNew = `${uuidv4()}.jpg`
        setImageName(imageNameNew)
        uploadToBucket(base64Image, imageNameNew)
    }

    const uploadToBucket = (file, imageNameNew) => {
        if (!file) return;
        const data = `data:image/jpeg;base64,${file.split(/,(.+)/)[1]}`

        const storageRef = ref(storage, `/${imageBucket}/${imageNameNew}`)
        const uploadTask = uploadString(storageRef, data, 'data_url')
        uploadTask
            .then(function(snapshot) {
                console.log('Uploaded a base64 string!');
                        getDownloadURL(snapshot.ref).then((downloadURL) => {
                            console.log("File available at", downloadURL);
                            handleClose()
                        });
            })
    }

    return (
        <Fragment>
            <TextInput label="Image Name" name="imageName" defaultValue={imageName} initialValue={imageName}/>
            <div>
                { showImg && imageSource &&
                <div>
                    <img className={classes.img} ref={imgRef} src={imageSource} alt="result"/>
                </div>
                }
                <Button label="Upload a new image" onClick={onClick}>
                    <IconContentAdd />
                </Button>
            </div>

            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label="Create post"
            >
                <DialogTitle>Upload image</DialogTitle>
                <DialogContent>
                         <div>
                             <div>
                                 {src &&
                                 (<ReactCrop circularCrop={true} src={src} onImageLoaded={setImg} crop={crop} onChange={setCrop}/>)}
                             </div>
                             {/*<TextInput source="imageName"/>*/}
                             <div>
                                 <input onChange={handleFile} type="file" accept="image/*"/>
                             </div>
                         </div>
                 </DialogContent>
                 <DialogActions>
                     <Button label="ra.action.save" onClick={handleSaveClick}>
                         <IconContentAdd />
                     </Button>
                     <Button label="ra.action.cancel" onClick={handleClose}>
                         <IconCancel />
                     </Button>
                 </DialogActions>
             </Dialog>
         </Fragment>
    )
};

export default ImageInputMy;
