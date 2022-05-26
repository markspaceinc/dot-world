Microsoft Pen Blog Post

Pen as multi-input device.

###	Concept
###	How we use pens in Argon

To this point, it has been difficult to find all the necessary information to use a pen as a multi- input device. 

DotWorld is a simple Android app thes draws and manages collection of yellow dots on a drawing surface, with a completely pen driven user interface. We will add code to use the pen to create, select, move, color and delete dots.

This example uses Jetpack Compose to present and manage the UI, so some experience with Kotlin and Compose will be helpful. (Plug Andy's blog post here).

			<DotWorld screenshot>

### Setup
A starter project can be downloaded from <URL>. This project contains the boilerplate Compse setup code, and the code that draws the dots, se we can concentrate on handling events from the pen.

The project is compatible with Android Studio Chipmunk, and was created using the "Empty Compose Activity" template. Note that is you choose to create you own project, the project's build gradle must be updated to use the latest Compose:
```	
compose_version = '1.2.0-beta01'
```
This version of compose adds support for chaging the stylus' hover icon.

You will also need to upgrade the Kotlin version to 1.6.21:

```
id 'org.jetbrains.kotlin.android' version '1.6.21 apply false
```


The app's drawing surface is a Compose canvas. Every time the UI refreshes, the drawing code iterates the list of dots. This is done in reverse order to keep the most recently created dots toward the front of the Z-order. It also checks to see if a dot is selected, and draws an outline around it, if so.

Now that we have a basic application set up, it's time to wire up events using the Canvas' Compose Modifier. We do this by overriding the pointerInteropFilter function to specify the behavion we want on various events.
	
### Tap

Taps from the pen are handled in the same way as touch events, with the advantage of pen taps being much more precise.	

Pen support is still evolving, so we are using the still experimental pointerInteropFilter API to enable better pen support. This API gives us access to the underlying Motion events from the UI, and gives access to additional stylus information such as whether it is hovering over the screen, and how hard it is being pressed on a tap.

All our code for this app is in the MainActivity.kt file. The DrawDots() function is where the pen (and other) event handling is implemented. The pointerInteropFilter in the sample code already captures the MotionEvent parameter, and has a skeleton when {} block with cases for the events we need to process. As you complete theis tutorial, you will be adding the handling code necessary to manage the objects on screen.

1. Tap

Add this code to the .pointerInteropFilter function override on the Canvas' modifier to add dots to the array, and select them.

Replacement ACTION_DOWN code block:

```
MotionEvent.ACTION_DOWN -> {
    var newSelectedDot = dotManager.findDotAt(dots, motionEvent.x, motionEvent.y)
    if (newSelectedDot != null) {
        if (!selectedDot.isEqual(newSelectedDot)) {
            selectedDot.selected = false
            selectedDot = newSelectedDot
            selectedDot.selected = true

            logText = "TAP SELECT at ${motionEvent.x}, ${motionEvent.y}"
        }
    } else {
            selectedDot.selected = false
            selectedDot = Dot(motionEvent.x, motionEvent.y, 50.0F)
            selectedDot.selected = true
            dots.add(selectedDot)

            logText = "TAP ADD at ${motionEvent.x}, ${motionEvent.y}"
    }
    penDown = true
    lastPenX = motionEvent.x
    lastPenY = motionEvent.y
}
```

Note that we're also setting a flag when the pan is touching the screen. We need to add matching code to the ACTION_UP case to clear the flag as well. This is important to handle dragging in the next step.

Replacement ACTION_UP code block:

```
MotionEvent.ACTION_UP -> {
    logText = "UP at ${motionEvent.x}, ${motionEvent.y}"
    penDown = false
}

```

Run the app now. At this point, you can tap into the background to create a dot, or on a dot to select it.

### Drag

While the pen is down, we track ACTION_MOVE events to implement dragging.

Replacement ACTION_MOVE code block:

```
MotionEvent.ACTION_MOVE -> {
    if (penDown && selectedDot != null) {
        var deltaX = motionEvent.x - lastPenX
        var deltaY = motionEvent.y - lastPenY
        selectedDot.x += deltaX
        selectedDot.y += deltaY
        lastPenX = motionEvent.x
        lastPenY = motionEvent.y

        logText = "MOVE to ${motionEvent.x}, ${motionEvent.y}"
    }
}
```

Now, tapping on a dot selects it, and it will move with the pen until an ACTION_UP is received.

### Hover

We want to set the cursor whenever the pen starts hovering over the drawing view, so we handle the ACTION_HOVER_ENTER event. We want the cursor to normally be crosshairs, but change to an eraser when the pen is inverted.

```
<ACTION_HOVER_ENTER code block here>

<ACTION_HOVER_MOVE block here>

<ACTION_HOVER_EXIT block here>
```

4. Pen button

To use the pen's side button, we check the MotionEvent's buttonState before processing the ACTION_DOWN event. If the button is pressed when a dot is tapped, it changes color.

Replacement ACTION_DOWN code block:
```
MotionEvent.ACTION_DOWN -> {
    var newSelectedDot = dotManager.findDotAt(dots, motionEvent.x, motionEvent.y)
    if (newSelectedDot != null) {
        if (!selectedDot.isEqual(newSelectedDot)) {
            selectedDot.selected = false
            selectedDot = newSelectedDot
            selectedDot.selected = true

            logText = "TAP SELECT at ${motionEvent.x}, ${motionEvent.y}"
        }
    } else {
            selectedDot.selected = false
            selectedDot = Dot(motionEvent.x, motionEvent.y, 50.0F)
            selectedDot.selected = true
            dots.add(selectedDot)

            logText = "TAP ADD at ${motionEvent.x}, ${motionEvent.y}"
    }

    if (selectedDot != null) {
        if (buttonState != 0) {
            selectedDot.color = abs(selectedDot.color - 1)
        }
    }

    penDown = true
    lastPenX = motionEvent.x
    lastPenY = motionEvent.y
}
```

### Eraser

So far, we have been treating any interaction source the same way - the code handles touches, pen taps and even mouse clicks the same way. In order to use the eraser properly, we need to call MotionEvent.getToolType() to get more information about the source. The sample project already includes this code.

We want the cursor to normally be invisible, but change to an eraser when the pen is inverted, so the three ACTION_HOVER handlers needs to be updated.


### Conclusion. 
    Source code download link.