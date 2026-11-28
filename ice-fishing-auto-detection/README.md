# ice-fishing-auto-detection

Automatically detects Ice Fishing game results using Android screen capture and OCR, provides prediction signals, and supports a movable floating overlay with result history.

## Install

To use npm

```bash
npm install ice-fishing-auto-detection
````

To use yarn

```bash
yarn add ice-fishing-auto-detection
```

Sync native files

```bash
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`requestScreenCapture()`](#requestscreencapture)
* [`stopScreenCapture()`](#stopscreencapture)
* [`addListener('detectionResult', ...)`](#addlistenerdetectionresult-)
* [`addListener('screenChange', ...)`](#addlistenerscreenchange-)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### requestScreenCapture()

```typescript
requestScreenCapture() => Promise<{ success: boolean; message: string; }>
```

**Returns:** <code>Promise&lt;{ success: boolean; message: string; }&gt;</code>

--------------------


### stopScreenCapture()

```typescript
stopScreenCapture() => Promise<{ success: boolean; message: string; }>
```

**Returns:** <code>Promise&lt;{ success: boolean; message: string; }&gt;</code>

--------------------


### addListener('detectionResult', ...)

```typescript
addListener(eventName: 'detectionResult', listenerFunc: (result: DetectionResult) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                             |
| ------------------ | -------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'detectionResult'</code>                                                   |
| **`listenerFunc`** | <code>(result: <a href="#detectionresult">DetectionResult</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('screenChange', ...)

```typescript
addListener(eventName: 'screenChange', listenerFunc: (event: ScreenChangeEvent) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                                |
| ------------------ | ----------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'screenChange'</code>                                                         |
| **`listenerFunc`** | <code>(event: <a href="#screenchangeevent">ScreenChangeEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### DetectionResult

| Prop             | Type                                                            |
| ---------------- | --------------------------------------------------------------- |
| **`category`**   | <code><a href="#detectioncategory">DetectionCategory</a></code> |
| **`confidence`** | <code>number</code>                                             |
| **`timestamp`**  | <code>number</code>                                             |


#### ScreenChangeEvent

| Prop             | Type                |
| ---------------- | ------------------- |
| **`timestamp`**  | <code>number</code> |
| **`difference`** | <code>number</code> |


### Type Aliases


#### DetectionCategory

<code>'WHITE_LEAF' | 'GRAY_LEAF' | 'LIL_BLUES' | 'BIG_ORANGES' | 'HUGE_REDS'</code>

</docgen-api>
