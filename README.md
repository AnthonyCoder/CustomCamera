###  效果（gif有点卡）
<!-- ![预览](https://github.com/AnthonyCoder/CustomCamera/blob/master/gif/demo.gif) -->
<div align=center><img width="auto" height="480" src="https://github.com/AnthonyCoder/CustomCamera/blob/master/gif/demo.gif"/></div>

[点击下载预览apk](https://www.pgyer.com/CameraDemo)

### 基于原生Camera库cameralibrary支持的功能：

* 一键配置基本的拍照和视频录制
* 可配置拍摄质量
* 自定义照片/视频的保存路径，以及文件名
* 可设置预览图像
* 可自定义拍照/录制的分辨率
* 内置相机设置项（SettingFragment） 可通过该Fragment设置相机拍摄/录制参数

### 更新日志
* 1.0.1 :
    1. 优化已知问题，规范代码库。
    2. 添加是否从本地读取相机参数的功能
    3. 尽可能的让开发者少写代码，提高编码效率
* 1.0.0 :
    1. Builder模式初构，基本功能完善

### 使用方式
一、引用类库
```
 repositories {
        jcenter()
        mavenCentral();
    }

dependencies {
   compile 'cn.geek.anthony:cameralibrary:1.0.1'
}
```
二、所需权限
```

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
```
二、Java代码中的使用
1. 注意该自定义SurfaceView不能直接在layout直接使用，使用时候建议放入一个FrameLayout布局中，可以参考demo实现
2. 初始化CameraSurfaceView
![init.png](http://upload-images.jianshu.io/upload_images/2200042-a53da5e8b5bfd5ef.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/540)
这一步，我们可以配置拍摄的一些参数、保存地址、如果需要预览在某个ImageView上面，还可以设置预览控件等等。。
需要显示拍摄画面的时候请调用CameraSurfaceView实例对象的 startCamera() 方法。
3. SurfaceView绑定Activity/Fragment生命周期
![bind.png](http://upload-images.jianshu.io/upload_images/2200042-c7c03108941e1eaf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/540)
4. 拍照/录制的方法
在需要拍照或者录制的时候调用一下方法：
CustomCameraHelper.getInstance().startCamera();
录制这块还有一个停止录制的方法：
CustomCameraHelper.getInstance().stopRecording();
当然录制状态这个也需要有一个方法暴露给开发者：
CustomCameraHelper.getInstance().isRecording()
所以，使用录制功能，整体可以这样写：
![video.png](http://upload-images.jianshu.io/upload_images/2200042-70d0d7f989f2ec2a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/540)
 使用拍照功能时候直接使用 startCamera() 方法即可拍照
![pic.png](http://upload-images.jianshu.io/upload_images/2200042-ebb52a5280d0b475.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/540)

5. 可配置项
我们在使用该库的时候可以自己通过方法配置比如保存地址、拍摄分辨率等等的参数，这个配置属性在源码中的 [CameraController.java](https://github.com/AnthonyCoder/CustomCamera/blob/master/cameralibrary/src/main/java/anthony/cameralibrary/CameraController.java) 文件中
配置项如下
setCameraType(ECameraType cameraType) ------>设置拍摄类型（拍照/录制）
setJpegQuality(int quality)  ------> 设置拍摄照片质量
setOutPutFilePath(String path) -----> 设置输出路径(完整路径)
setOutPutDirName(String dirName) -----> 设置输出到指定文件夹内
setFileName(String fileName) -----> 设置输出文件名
setLoadSettingParams(boolean isload) ----> 设置是否加载本地参数(读取使用SP存储到本地的参数，SP存储对应的key具体的值定义在 [Constants.java](https://github.com/AnthonyCoder/CustomCamera/blob/master/cameralibrary/src/main/java/anthony/cameralibrary/constant/Constants.java) 中)
setPreviewImageView(ImageView ivPreview)
setPreviewImageView(int previewImgRes)
-----> 设置画面预览的ImageView（视频预览默认是显示第一帧的画面预览）




### 实现思路
[大表哥带你一步一步用Builder模式实现自定义相机（拍照+录制，附源码）](https://www.jianshu.com/p/1f7a2def4670)




