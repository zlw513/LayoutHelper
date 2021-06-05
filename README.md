# LayoutHelper
基于安卓accessibilityservice设计的查看控件布局的工具

基本功能：
1.查看控件信息
2.抖音音量键刷视频
3.抖音视频播放完自动切下一个视频
4.查看屏幕控件大小信息（请先监听你需要查看的应用）

1 , 4 功能是面向开发者的，可能需要稍微调试代码才能达到预期效果。

下面是1.功能的效果图：

![功能1图片](https://github.com/zlw513/LayoutHelper/blob/master/screenshot/example4.jpg)

下面是4.功能的效果图:

![功能4图片1](https://github.com/zlw513/LayoutHelper/blob/master/screenshot/example2.jpg)

![功能4图片2](https://github.com/zlw513/LayoutHelper/blob/master/screenshot/example3.jpg)

![功能4图片3](https://github.com/zlw513/LayoutHelper/blob/master/screenshot/example1.jpg)

其中功能4的显示效果会根据目标控件大小来做调节，也有可能不显示控件大小信息（控件太小）,此功能适合开发者使用

1,4功能的使用说明

  点击箭头即可打开相应功能
  （1） 功能1会在你点击时展示控件相应的信息(前提是监听了那个应用包名,可以在xml下的configurate文件中android:packageNames属性中追加你要监听的应用包名)。 
  （2） 功能4会在你点击时默认关闭所有其他功能。 在此悬浮窗内输入控件id即可显示控件信息。 然后在悬浮窗可见的情况下再次点击该功能箭头，即可关闭当前功能


