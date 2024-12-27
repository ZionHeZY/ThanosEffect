    # ThanosEffect

An Android custom view that implement the "disintegration" effect. When triggered, it breaks the target view into particles that float away and fade out, creating a dramatic disappearing effect.

## Features
- Smooth particle animation
- Irregular particle sizes
- Configurable animation parameters
- Easy to integrate with any view
- Customizable particle movement and fade-out effects

## Usage

1. Add ThanosDisintegrationView to your layout:
```xml
<tech.hezy.thanoseffect.ThanosDisintegrationView
    android:id="@+id/thanosContainer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <YourView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

</tech.hezy.thanoseffect.ThanosDisintegrationView>
```

2. Control the effect in your Activity:
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var thanosContainer: ThanosDisintegrationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        thanosContainer = findViewById(R.id.thanosContainer)
        
        // Start the effect
        thanosContainer.startDisintegration()
        
        // Reset the view
        thanosContainer.reset()
    }
}
```

## Parameters
The effect can be customized through several parameters:
- Grid size: Controls the number of particles (DEFAULT_GRID_ROWS = 10, DEFAULT_GRID_COLS = 20)
- Animation duration: Length of the animation (DEFAULT_DURATION = 3000L)
- Particle speed: Controls how fast particles move (baseSpeedMin = 10f, baseSpeedMax = 50f)
- Movement angle: Direction of particle movement (angleStart = -60.0, angleEnd = -30.0)
- Particle size: Irregularity of particles (randomWidthFactor = 0.7f ~ 1.3f)
- Position offset: Random offset of particles (offsetX up to 40%, offsetY up to 20%)

## Demo
![Demo](demo.gif)
