# ThanosEffect

一个Android自定义视图，实现了"消散"效果。当触发时，目标视图会分解成粒子并逐渐飘散消失，创造出戏剧性的消失效果。

[English Version](README_EN.md)

## 特性
- 流畅的粒子动画
- 不规则的粒子大小
- 可配置的动画参数
- 易于与任何视图集成
- 可自定义的粒子移动和淡出效果

## 使用方法

1. 在布局中添加 ThanosDisintegrationView：
```xml
<tech.hezy.thanoseffect.ThanosDisintegrationView
    android:id="@+id/thanosContainer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <!-- 在这里放置您的内容视图 -->
    <YourView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

</tech.hezy.thanoseffect.ThanosDisintegrationView>
```

2. 在 Activity 中控制效果：
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var thanosContainer: ThanosDisintegrationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        thanosContainer = findViewById(R.id.thanosContainer)
        
        // 开始效果
        thanosContainer.startDisintegration()
        
        // 重置视图
        thanosContainer.reset()
    }
}
```

## 参数说明
效果可以通过以下参数自定义：
- 网格大小：控制粒子数量（DEFAULT_GRID_ROWS = 10, DEFAULT_GRID_COLS = 20）
- 动画时长：控制动画持续时间（DEFAULT_DURATION = 3000L）
- 粒子速度：控制粒子移动速度（baseSpeedMin = 10f, baseSpeedMax = 50f）
- 移动角度：控制粒子移动方向（angleStart = -60.0, angleEnd = -30.0）
- 粒子大小：粒子的不规则程度（randomWidthFactor = 0.7f ~ 1.3f）
- 位置偏移：粒子的随机偏移量（offsetX最大40%, offsetY最大20%）

## 效果展示
![演示](demo.gif)
