# ijkplayer 

已整合mini_thunder项目，实现边下边播放功能，支持直播流（http/ftp/rtmp/mms），支持种子文件（.torrent)，支持本地视频文件或者网络视频文件(thunder/ed2k），目前不支持磁力链(magnet），但可先将磁力链转为种子文件后再实现播放。

# xllib  

此包主要是实现与mini_thunder项目的整合，并供给ijkplayer播放器调用

# XLVideoPlayActivity  

播放器界面，支持上面提到的任意视频资源地址，对于种子视频资源提供了播放清单列表。播放器已实现了TV盒子的遥控器按键操作方式。  

## 外部调用方法  

```XLVideoPlayActivity.intentTo(context, url, title); ```  
