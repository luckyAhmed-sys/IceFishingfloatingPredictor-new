import { IceFishingAutoDetection } from 'ice-fishing-auto-detection';

window.startIceFishingDetection = async function () {
    try {
        const result =
            await IceFishingAutoDetection.requestScreenCapture();

        return result;
    } catch (error) {
        console.error('Screen capture error:', error);
        throw error;
    }
};
