//index.js
//获取应用实例
const app = getApp()

Page({
  data: {
    motto: 'Hello World',
    userInfo: {},
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo'),
    
  },
  //事件处理函数
  bindViewTap: function () {

  },
  onLoad: function () {
   
  },
  getUserInfo: function (e) {
    // if (e.detail.userInfo) {
    //   app.globalData.userInfo = e.detail.userInfo
    //   this.setData({
    //     userInfo: e.detail.userInfo,
    //     hasUserInfo: true
    //   })
    // }
    wx.requestSubscribeMessage({
      tmplIds: app.globalData.tmplIds,
      complete(res) {
        wx.navigateTo({
          url: '../main/main'
        })
      }
    })
  }
})