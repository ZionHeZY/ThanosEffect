# ThanosEffect

An Android custom view that implements a particle disintegration effect, elegantly breaking down the target view into particles that gradually fade away.

[中文版](README.md)

## Features

- Progressive particle effect from left to right
- Smooth disintegration animation
- Customizable particle size and animation parameters
- Supports disintegration effect for any View
- High-performance rendering implementation
- Smooth particle motion trajectories

## Usage

1. Use ThanosDisintegrationView in your layout file:

```xml
<tech.hezy.thanoseffect.ThanosDisintegrationView
    android:id="@+id/thanosView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">
    
    <!-- Place the View you want to apply the effect to here -->
    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/your_image"/>
        
</tech.hezy.thanoseffect.ThanosDisintegrationView>
```

2. Control the animation in code:

```kotlin
// Start disintegration animation
thanosView.startDisintegration()

// Reset view
thanosView.reset()
```

## Custom Attributes

```xml
<declare-styleable name="ThanosDisintegrationView">
    <!-- Number of rows in particle grid -->
    <attr name="gridRows" format="integer"/>
    <!-- Number of columns in particle grid -->
    <attr name="gridCols" format="integer"/>
    <!-- Animation duration in milliseconds -->
    <attr name="durationMillis" format="integer"/>
    <!-- Gap between particles in pixels -->
    <attr name="gapPx" format="dimension"/>
</declare-styleable>
```

## Implementation Details

- Efficient drawing using Canvas and Bitmap
- Particle system for disintegration effect
- Gradient mask for smooth transitions
- Optimized memory usage and performance

## Performance Optimizations

- Off-screen caching to reduce drawing overhead
- Smart particle management system
- Optimized animation calculations
- Timely resource recycling

