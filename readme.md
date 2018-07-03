# 小盒精灵（TVRemoteIME） 
电视盒子的管理精灵，可跨屏远程输入、跨屏远程控制盒子、远程文件管理、HTTP/RTMP/MMS网络视频直播、ED2K/种子文件的视频文件边下边播、视频投屏（DLNA）

# 应用的诞生
自从家里有电视盒子以来，电视收看、电影播放、娱乐小游戏什么的都是直接在盒子里运行，因为电视屏幕比起手机屏幕大，玩起来那效果是手机没法比的，但是在娱乐的过程中也总是有一些不便，比如玩游戏过程中想聊天什么的，在电视盒子里输入文字用遥控器按？只有用过才知道痛苦！外挂物理键盘，可惜很多输入法都不支持物理键盘的同时输入，远远达不到电脑的效果！于是找了很多遥控与跨屏输入的软件，但可惜没有一款是比较理想的，特别家里的一个创维Q+二代盒子，只要一进游戏的聊天界面，不管外面设置了什么跨屏输入法，都会自动切换为厂家自带的百度输入法，非常的可恶！于是就有了自己做一款远程跨屏的输入法，于是这小盒精灵（TVRemoteIME）就这样诞生了…………  

# 它能做什么  
它能帮助盒子实现跨屏输入，以后在盒子里聊天打字要多快就有多快；它能代替盒子遥控器，用手机，电脑，IPAD摇控盒子都不是问题；它能管理盒子的应用与文件，一键快速启动/卸载应用；它能跨屏安装应用与传送文件到盒子；还能实现HTTP/RTMP/MMS网络视频直播、ED2K/种子文件的视频文件边下边播；还支持DLNA方式的视频投屏，手机、IPAD或者电脑的视频可直接投屏到电视上播放。

# 安装方法
下载最新版本的APK包：https://github.com/kingthy/TVRemoteIME/raw/master/released/IMEService-release.apk  

## 一、通过adb命令安装应用  
1、电视盒子开启adb调试 

2、电脑通过adb命令连接电视盒子（假如电视盒子的内网ip为：192.168.1.100）  
`adb connect 192.168.1.100:5555`  
注意,手机要与盒子在同一个WIFI网络(内网网络)  执行`adb devices`命令显示有device列表，则表示已连接上盒子，可继续下一步

3、通过以下命令安装输入法apk包  
`adb install IMEService-release.apk`  

4、设置为系统默认输入法  
`adb shell ime set com.android.tvremoteime/.IMEService`    

注：如果无法设置为系统的默认输入法，则先启动TVRemoteIME应用后再手动启动服务，如需使用远程输入及远程遥控功能还需要开启盒子的ADB模式。  

5、电脑或者手机浏览器访问远程输入法的控制页面
`http://192.168.1.100:9978/`  

## 二、通过U盘或者其它方式安装  
1、安装后在盒子应用列表里找到TVRemoteIME的图标点击运行  

2、根据应用的提示进行设置即可。  

## 视频播放功能说明  
1、本地视频文件： 通过传送功能将手机、电脑等控制端的视频文件传送到盒子后会自动播放  

2、种子文件：通过传送功能将种子文件传送到盒子后会自动播放种子里的第一个视频文件  

3、网络视频（http/rtmp/mms协议的直播或者thunder/ed2k协议的视频）： 直接在网络视频地址框输入视频URL，点击“远程播放”按钮盒子会自动开始播放  

注：对于种子文件及非直播的网络视频，本应用采用的是边下边播方式，所以会占用盒子的大量空间（根据视频大小而定），如果盒子的可用空间不够，视频会播放失败。正常播放结束时应用会自动删除边下边播放的缓存文件，或者你可以点击控制界面里的“清除缓存”删除。  

## 视频播放控制说明  

在视频播放时，点击控制器进行控制，左右键用于控制快进或者快退（非直播情况下可用）、上下键用于选择需要播放的视频文件（播放种子文件且里面包含多个视频时可用），在播放单视频（非种子视频）时长按下键1秒左右会重头开始播放视频、确定键用于暂停或者恢复播放功能。

## 电视直播源列表  
需要更改直接源列表有两种方式：  
1、直接在控制端界面修改  
2、通过文件管理界面进入“/Android/data/com.android.tvremoteime/files/” 目录，对里面的tv.txt文件进行修改（通过下载与上传）即可。如tv.txt文件不存在，新建即可覆盖默认的直播源，此文件格式为ini文件格式，格式如下：  
<pre><code>[电视台名称]  
源名称1 = 源地址  
源名称2 = 源地址 

[电视台名称]  
源名称 = 源地址  
</pre></code>

## 视频投屏说明  
只要支持DLNA协议的播放器（如手机迅雷、爱奇艺等）都可投屏到小盒精灵在电视上播放  

## 控制界面示例截图  
注： 输入控制端不需要安装任何APK应用，直接浏览器操作  

![示例截图](https://raw.githubusercontent.com/kingthy/TVRemoteIME/master/released/screenshot.png "控制界面示例截图")    

![示例截图](https://raw.githubusercontent.com/kingthy/TVRemoteIME/master/released/screenshot_2.png "控制界面示例截图")    

![示例截图](https://raw.githubusercontent.com/kingthy/TVRemoteIME/master/released/screenshot_3.png "控制界面示例截图")    

![示例截图](https://raw.githubusercontent.com/kingthy/TVRemoteIME/master/released/screenshot_4.png "控制界面示例截图")    


# 版本历史 
1、 V1.0.0版本  
于2018-1-11发布，本应用的第一个版本，实现盒子跨屏输入、远程遥控、APP包管理、文件传送等功能  

2、 V1.1.0版本  
于2018-2-4发布，增加视频的播放功能，支持播放多种不同网络协议的网络视频及网络直播源，采用边下载边播方式实现，  

3、 V1.2.0版本  （完全开源版本）  
于2018-3-3发布，增加盒子的文件管理功能、优化了控制端的UI设计、优化了播放器的功能、增加电视频道数据，    

4、 V1.2.1版本    
于2018-3-5发布，增加手动启动服务功能。   

5、 V1.3.0版本    
于2018-3-8发布，增加视频播放时可选择调用系统播放器、播放种子文件视频时可在控制端界面选择要播放的视频文件、无法设置为系统默认输入法时通过ADB模式实现遥控及简单输入功能、修复BUG等。  

6、 V1.3.1版本    
于2018-3-25发布，增加快进调速功能、在文件管理界面增加本地视频文件的播放功能、修复文件传送功能无法播放视频的BUG、修复其它已知BUG。   

7、 V1.4.0版本    
于2018-4-11发布，APP正式更名为“小盒精灵”，增加视频投屏功能、增加自更新功能、修复其它已知BUG。   

8、 V1.4.1版本    
于2018-4-30发布，支持更改投屏名称、TV端键盘添加隐藏键、修复已知BUG。

**小盒精灵因抄袭事件，以后不会再维护更新开源代码，也就是说V1.4.1版本属于最后的开源版本**  
[关于小盒精灵被抄袭的事件申明] (https://www.v2ex.com/t/452320)  


# 引用第三方包/资源说明
1、[NanoHttpd](https://github.com/NanoHttpd/nanohttpd "NanoHttpd")  用于实现HTTP WEB服务  

2、[ZXing](https://github.com/zxing/zxing/ "QRCode") 用于实现二维码的输出
 
3、[悟空遥控](http://www.wukongtv.com/views/input.html "悟空遥控")  非常棒的一款遥控软件。  
注：本软件远程控制端的遥控导航面板设计图和小部分CSS代码参考于它。

4、[AFAP Player](https://github.com/AFAP/Player "AFAP Player") 用于实现视频播放，采用[ijkplayer](https://github.com/Bilibili/ijkplayer "ijkplayer")播放器核心

5、[MiniThunder](https://github.com/oceanzhang01/MiniThunder "MiniThunder") 用于实现视频文件下载功能  

6、[AdbLib](https://github.com/cgutman/AdbLib "AdbLib") 用于连接adb服务，在非输入法状态下的实现遥控功能  

7、[DroidDLNA](https://github.com/offbye/DroidDLNA "DroidDLNA") 用于实现DLNA视频投屏服务，内部核心采用[Cling](https://github.com/4thline/cling "cling") DLNA库   

# 衍生项目  
由小盒精灵衍生出来的APP项目  
1、[云播投屏](https://github.com/yunbotouping/- "云播投屏") 

# 交流QQ群  
QQ群号：7149348， 加入QQ群可以分享直播源、反馈问题及建议。  

# 赞赏一下  
如果您觉得本应用项目对你有帮助，可以赞赏一下作者：）。万水千山总是情，一块十块都是情！  

![赞赏二维码](https://raw.githubusercontent.com/kingthy/TVRemoteIME/master/released/reward.png "赞赏二维码")    
