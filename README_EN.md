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

## License

```
Copyright 2024 Hezy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
