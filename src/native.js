import { IceFishingAutoDetection } from 'ice-fishing-auto-detection';

window.startIceFishingDetection = async function () {
    try {
        return await IceFishingAutoDetection.requestScreenCapture();
    } catch (error) {
        console.error('Screen capture error:', error);
        throw error;
    }
};

window.onIceFishingScreenChange = async function (callback) {
    return await IceFishingAutoDetection.addListener(
        'screenChange',
        callback
    );
};
