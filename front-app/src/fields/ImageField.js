import * as React from "react";
import {googlePictureUrl} from "../tools/serviceUrl";
import BaseImg from "../image/defaulImg.png"
import {makeStyles} from "@material-ui/core/styles";
import {ref} from "firebase/storage";
import {storage} from "../tools/firebase";
import {getDownloadURL} from "@firebase/storage";
import {useRef, useState} from "react";

const useStyles = makeStyles({
    img: {
        width: 140,
        borderRadius: 1000
    },
});

const ImageField = ({ record = {}, source, imageBucket}) => {
    const classes = useStyles();

    const [imageSource, setImageSource] = useState(null);
    const [showImg, setShowImg] = useState(false)

    if (record != null && record.imageName != null) {
        const storageRef = ref(storage, `/${imageBucket}/${record.imageName}`)
        getDownloadURL(storageRef).then((downloadURL) => {
            setImageSource(downloadURL)
            setShowImg(true)
        });
    }

    return (
        <div>
            {
                !showImg &&
                <div>
                    <img className={classes.img} alt="image" width={'20%'} src={BaseImg}/>
                </div>
            }
            {
                showImg && imageSource &&
                <div>
                    <img className={classes.img} alt="image" width={'20%'} src={imageSource}/>
                </div>
            }
        </div>

    )
};

export default ImageField;
