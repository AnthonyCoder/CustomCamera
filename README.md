# CustomCamera
一个灵活配置的自定义相机库（拍照+录制视频）
####一、前言    
* ######初衷    
在我们做很多项目的过程中，经常会遇到很多需要自定义的相机的需求，这个时候，很多人第一步都是网上查资料，包括我也是这样，但是我没有发现有比较靠谱没有Bug的开源项目，虽然这个需求也不是很难，但是由于android市场的碎片化，各机型的适配也是很头疼，一步一步去写难免会踩到不少的坑，所以，我打算长期维护这个项目，有问题的欢迎提交Issues，以便于我完善这个开源项目。


* 简单介绍    
该开源项目整体由Builder模式编写，方便后期扩展，支持链式调用。

    目前可支持的自定义扩展项：    
1、相机拍摄方式：拍照or录制       
2、拍摄质量      
3、拍摄保存路径、文件名     
4、可预览的imageView    
5、拍照分辨率    
6、录制分辨率   

####二、准备
* 了解SurfaceView
[Google官方对SurfaceView的解释]（https://developer.android.com/reference/android/view/SurfaceView.html）





##1.前言
* ####1.1 初衷
在我们做很多项目的过程中，经常会遇到很多需要自定义的相机的需求，这个时候，很多人第一步都是网上查资料，包括我也是这样，但是我没有发现有比较靠谱没有Bug的开源项目，虽然这个需求也不是很难，但是由于android市场的碎片化，各机型的适配也是很头疼，一步一步去写难免会踩到不少的坑，所以，我打算长期维护这个项目，有问题的欢迎提交Issues，以便于我完善这个开源项目。

* ####1.2 简单介绍
该开源项目整体由Builder模式编写，方便后期扩展，支持链式调用。

目前可支持的自定义扩展项：
1、相机拍摄方式：拍照or录制
2、拍摄质量
3、拍摄保存路径、文件名
4、可预览的imageView
5、拍照分辨率
6、录制分辨率
* ####1.3 开源地址：
  **Github地址：[一个灵活配置的自定义相机库（拍照+录制视频）](https://github.com/AnthonyCoder/CustomCamera)**

  **欢迎大家指正错误，和提出问题，如觉得帮助到了你希望能够star和fork。。感激不尽，希望能够帮到你。。**
##2.准备
* ####2.1 了解SurfaceView
[Google官方对SurfaceView的解释](https://developer.android.com/reference/android/view/SurfaceView.html)
###### 2.1.1 简介
通常情况程序的View和用户响应都是在同一个线程中处理的，这也是为什么处理长时间事件（例如访问网络）需要放到另外的线程中去（防止阻塞当前UI线程的操作和绘制）。但是在其他线程中却不能修改UI元素，例如用后台线程更新自定义View（调用View的在自定义View中的onDraw函数）是不允许的。

如果需要在另外的线程绘制界面、需要迅速的更新界面或则渲染UI界面需要较长的时间，这种情况就要使用SurfaceView了。SurfaceView中包含一个Surface对象，而Surface是可以在后台线程中绘制的。Surface属于OPhone底层显示系统,SurfaceView的性质决定了其比较适合一些场景：需要界面迅速更新、对帧率要求较高的情况。

SurfaceView的核心提供了两个线程：UI线程和渲染线程。应该注意的是：
   a.所有的SurfaceView和SurfaceHolder.Callback的方法都应该在UI线程里调用，一般来说就是应用程序的主线程。渲染线程所要访问的各种变量应该做同步处理。
   b.由于surface可能被销毁，它只在SurfaceHolder.Callback.surfaceCreated()和SurfaceHoledr.Callback.surfaceDestroyed()之间有效，所以要确保渲染线程访问的是合法有效地surface.


######2.1.2 SurfaceView类 和View类的区别:
SurfaceView 和View的最本质的区别在于，surfaceView是在一个在新起的单独线程中可以重新绘制画面，而View必须在UI的主线程中更新画面。那么在UI的主线程中更新画面，可能会引发问题，比如你更新画面的时间过长，那么你的主UI线程会被你正在画的函数阻塞，那么将无法响应按键，触摸等消息。当使用surfaceView由于是在新的线程中更新画面所以不会阻塞你的UI主线程，但是这也会有另外一个问题，就是事件同步。比如你触屏了一下，你需要surfaceView中thread处理，一般就需要有一个event queue的设计来保存touch event，这样就会有点复杂了。
View：必须在UI的主线程中更新画面，用于被动更新画面。
surfaceView：UI线程和子线程中都可以。在一个新启动的线程中重新绘制画面，主动更新画面。
所以在游戏的应用上，根据游戏的特点，一般分为两类：
   a. 被动更新画面的。比如棋类，这种用view就好。因为画面的跟新依赖于onTouch来更新，可以直接使用invalidate.因为这种情况下，这一次Touch和下一次Touch需要的时间比较长些，不会产生
影响。
   b.主动更新：比如一个人在一直跑动。这就需要一个单独的thread不停地重绘人的转台，避免阻塞mian UI Thread 。所以显然view 不适合，需要surfaceView来控制。

* ####2.2理解Builder模式
其实在我们在编写程序的很多时候，都会使用Builder模式，如v7包自带的AlertDialog的实现、OkHttpClient的参数配置等等。

Builder模式也就是建造者模式，主要用于将一个复杂的对象分离，通过不同的构造（不同的参数值）去构建不同的对象，在使用的时候隐藏构造过程和细节，用户不需要知道内部实现过程，方便用户创建复杂的对象

###### 2.2.1 优缺点：
* 优点
1.易于解耦 
将产品本身与产品创建过程进行解耦，可以使用相同的创建过程来得到不同的产品。也就说细节依赖抽象。
易于精确控制对象的创建 
将复杂产品的创建步骤分解在不同的方法中，使得创建过程更加清晰
2.易于拓展 
增加新的具体建造者无需修改原有类库的代码，易于拓展，符合“开闭原则“。 
每一个具体建造者都相对独立，而与其他的具体建造者无关，因此可以很方便地替换具体建造者或增加新的具体建造者，用户使用不同的具体建造者即可得到不同的产品对象。
* 缺点
建造者模式所创建的产品一般具有较多的共同点，其组成部分相似；如果产品之间的差异性很大，则不适合使用建造者模式，因此其使用范围受到一定的限制。
如果产品的内部变化复杂，可能会导致需要定义很多具体建造者类来实现这种变化，导致系统变得很庞大。

##3.编码
* ####3.1  自定义SurfaceView的实现和设计
   ######3.1.1 自定义SurfaceView实现
  编写一个类CameraSurfaceView继承surfaceView并实现SurfaceHolder.Callback接口，实现方法中包含了surfaceView生命周期的几个方法：创建时候调用（surfaceCreated）、页面更新时候调用（surfaceChanged）、surfaceView销毁时候调用（surfaceDestroyed）。

  由于我的需求事在直接使用cameraSufaceView时候就需要链式配置所有的参数，所以我们将要在cameraSufaceView中写一个Builder的静态内部类。
然后考虑到解耦性，我们单独写一个零件类（需要灵活配置的参数），包含了我们一开始提到的拍摄方式、质量、保存地址等等参数
在Builder内部类中，把所有的零件配置和获取暴露提供给外部，最后调用某个方法（项目中是startCamera（））开始组装零件变成一个完整的产品。

* ####3.2  辅助类的编写思路
    ######3.2.1 编写前的思考
    我们编写这个类的主要目的是区别与SurfaceView内部代码，把所有逻辑代码都分离出来，各司其职，这样以后逻辑有改动不会过多的牵扯到view层，这和mvp的优点类似。
    那么，这个辅助类应该具备什么样的功能呢？
    当然首先会有一个方法去绑定SurfaceView用于初始化数据，然后会有方法去一一对应SurfaceView生命周期的几个方法来创建camera、适配屏幕预览、销毁camera等。其中camera的创建应该是一个单例，因为系统同时只能同时存在一个可操作的camera，除了这些，还有一系列的其他辅助方法，如矫正拍照角度、自适应预览画面、录制配置、拍照配置等等
    这个辅助类应该有些什么特征呢？
    首先，由于我们会在SurfaceView内部的生命周期方法里面和Activity/Fragment中使用到这个操作逻辑的辅助类，所以，我打算把它写成单例，在任何位置操作都可以控制。
   其次，辅助类中应有对应SurfaceView生命周期逻辑的方法去控制camera对象实例，如在SurfaceView创建（surfaceCreated）的时候去创建Camera对象，绑定SurfaceView的Holder并开始预览，在SurfaceView更新的时候（surfaceChanged）去适应相机方向和预览方位防止变形和方向错乱，在SurfaceView销毁（surfaceDestroyed）的时候去释放Camera实例。

  ######3.2.2 处理异常
  在我们编写过程中，某些地方可能会出现不可预知的错误，这个时候我们要通知到View层去提示用户异常信息，所以这里我们要用到一个接口去供View层去实现，通过这个接口去通知View层出现了某些异常。
* ####3.3   运行时权限的处理
  在Android6.0（API级别 23）中，在使用到部分危险权限时候，会向系统申请权限才能正常使用某些功能，所以我们要针对权限进行适配，这里我推荐PermissionsDispatcher，该库还针对xiaomi做了专门的适配。
  ######3.3.1 PermissionsDispatcher介绍
   PermissionsDispatcher是一个基于注解、帮助开发者简单处理Android 6.0系统中的运行时权限的开源库。避免了开发者编写大量繁琐的样板代码。
**开源地址：[https://github.com/hotchemi/PermissionsDispatcher](https://link.jianshu.com/?t=https://github.com/hotchemi/PermissionsDispatcher)**
**文档介绍：[http://hotchemi.github.io/PermissionsDispatcher/](https://link.jianshu.com/?t=http://hotchemi.github.io/PermissionsDispatcher/)**

  **使用方法可以参见github地址给出的步骤。**
##4.遇到的问题

##5.结语
* ####5.1 该自定义库优点
  a.可以大大减少Activity/Fragment的逻辑代码，增强可读性
  b.完全发挥了Builder设计模式的优点，可配置性和灵活性得到了提升
  c.解决了大部分开发者遇到的问题（图像变形、方向错乱、位置异常等），若有其他问题欢迎大家提Issues，我看到了会尽快修复~

* ####5.2 参考
  * [Android相机开发(四): 旋转与纵横比](http://www.polarxiong.com/archives/Android%E7%9B%B8%E6%9C%BA%E5%BC%80%E5%8F%91-%E5%9B%9B-%E6%97%8B%E8%BD%AC%E4%B8%8E%E7%BA%B5%E6%A8%AA%E6%AF%94.html) 
  * [PermissionsDispatcher使用详解](http://www.jianshu.com/p/dd5d2e4cb353)
  * [Android相机开发那些坑 - QQ空间开发团队](https://www.qcloud.com/community/article/164816001481011880)






