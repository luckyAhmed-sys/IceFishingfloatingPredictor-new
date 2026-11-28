import { registerPlugin } from '@capacitor/core';

import type { IceFishingAutoDetectionPlugin } from './definitions';

const IceFishingAutoDetection = registerPlugin<IceFishingAutoDetectionPlugin>('IceFishingAutoDetection', {
  web: () => import('./web').then((m) => new m.IceFishingAutoDetectionWeb()),
});

export * from './definitions';
export { IceFishingAutoDetection };
